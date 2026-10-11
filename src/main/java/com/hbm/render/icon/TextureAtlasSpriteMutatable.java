package com.hbm.render.icon;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import com.google.common.io.ByteStreams;
import com.hbm.main.MainRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Atlas icons whose pixels get recolored at load: one base image grows N colorways, each an independent sprite.
 *
 * For the ore-dict/plate family of "same shape, different metal" icons. The base carries its own shading, so the gradient interval must be remapped to the target color per pixel
 * (RGBMutatorInterpolatedComponentRemap); a flat ItemColors tint would wipe the shading out entirely.
 *
 * The loading contract differs from 1.7.10 here; all three points are bytecode-verified (javap -c TextureMap / TextureAtlasSprite):
 *   · the base class load(IResourceManager, ResourceLocation, Function) is an **empty shell**, always returning true;
 *   · TextureMap.loadTexture: load() is only called when hasCustomLoader()==true, and **false = fully loaded**,
 *     returning true falls back to vanilla's loadSprite;
 *   · more crucially, frame decoding isn't in loadTexture but in TextureMap.generateMipmaps, and that method
 *     returns outright the moment it sees hasCustomLoader()==true — so a custom sprite must run
 *     loadSprite + loadSpriteFrames + generateMipmaps itself, in that order, or no mip levels will exist.
 * The old basePath / setBlockAtlas() served 1.7.10's "auto-prepend textures/blocks/ by atlas type" scheme;
 * sprite names here already carry the items/ or blocks/ segment, so both members are deleted; the injection point also moved from
 * TextureMap.setTextureEntry(name, sprite) to the public single-arg setTextureEntry(sprite) (keyed by iconName).
 *
 * Never fail silently here: getting this wrong shows up as "compiles, doesn't crash, the colorways just quietly vanish" — no exception to look for,
 * so a load failure must always leave a log line; one client run confirms whether it took effect.
 *
 * Fourth: the IResource a resource pack hands out **caches the stream for reuse** — read the same stream twice and the second read comes back empty —
 * so dimensions and pixels can't each read that one stream; drain it once, then hand out a fresh stream every time. Getting bitten doesn't error, it goes silent:
 * vanilla receives a null BufferedImage and NPEs inside its own loadSpriteFrames (all 254 observed cases were this).
 */
@SideOnly(Side.CLIENT)
public class TextureAtlasSpriteMutatable extends TextureAtlasSprite {

	private final RGBMutator mutator;

	public TextureAtlasSpriteMutatable(String iconName, RGBMutator mutator) {
		super(iconName);
		this.mutator = mutator;
	}

	@Override
	public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
		return true;
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {

		ResourceLocation real = withoutPlaceholderSuffix(location);

		try {
			IResource resource = manager.getResource(real);

			// The resource handed out by the pack caches its stream (the observed stack breaks right here: read dimensions, then read pixels, second read comes back empty,
			// and on vanilla's side the BufferedImage is null, NPE landing in TextureAtlasSprite.loadSpriteFrames).
			// So the bytes get drained once here, and every later stream request gets a fresh one.
			byte[] png;
			try(InputStream in = resource.getInputStream()) {
				png = ByteStreams.toByteArray(in);
			}
			IResource reopenable = new ReopenableResource(resource, png);

			this.loadSprite(PngSizeInfo.makeFromResource(reopenable), reopenable.getMetadata("animation") != null);

			int mipmap = Minecraft.getMinecraft().gameSettings.mipmapLevels;
			// What's wanted here is the "level count", not the "layer count": vanilla itself passes mipmapLevels + 1 (iconst_1 + this.mipmapLevels in the bytecode),
			// miss that one level and generateMipmaps writing level 4 throws ArrayIndexOutOfBoundsException
			this.loadSpriteFrames(reopenable, mipmap + 1);

			mutateFrames();

			// Mip levels must be generated after the recolor, or downscaling fades back to the original colorway
			if(mipmap > 0) this.generateMipmaps(mipmap);

			return false;
		} catch(Exception ex) {
			// Returning true lets TextureMap run its own missing/broken handling; don't fabricate an empty sprite here
			// Always print the full stack here: ex.toString() leaves just a class name, and an NPE gives no clue which line broke
			MainRegistry.logger.error("hbm: icon " + real + " load failed, handed back to vanilla", ex);
			return true;
		}
	}

	/** Only getInputStream is swapped: a fresh stream each time, everything else copied from the original resource */
	private static final class ReopenableResource implements IResource {

		private final IResource delegate;
		private final byte[] png;

		ReopenableResource(IResource delegate, byte[] png) {
			this.delegate = delegate;
			this.png = png;
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(png);
		}

		@Override
		public ResourceLocation getResourceLocation() {
			return delegate.getResourceLocation();
		}

		@Override
		public boolean hasMetadata() {
			return delegate.hasMetadata();
		}

		@Override
		public <T extends IMetadataSection> T getMetadata(String sectionName) {
			return delegate.getMetadata(sectionName);
		}

		@Override
		public String getResourcePackName() {
			return delegate.getResourcePackName();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}

	// The segment after the dash is just a placeholder-unique suffix (one base image holds many colorways); the real texture path strips it off
	private ResourceLocation withoutPlaceholderSuffix(ResourceLocation location) {

		String path = location.getResourcePath();

		// This reads the disk by path directly, so the extension must survive: peel it off, cut the dash segment, then stitch it back
		int slash = path.lastIndexOf('/');
		int dot = path.lastIndexOf('.');
		String ext = dot > slash ? path.substring(dot) : "";
		if(!ext.isEmpty()) path = path.substring(0, dot);

		int dash = path.indexOf('-');

		if(dash < 0) return location;

		return new ResourceLocation(location.getResourceDomain(), path.substring(0, dash) + ext);
	}

	private void mutateFrames() {

		if(this.mutator == null) return;

		int frames = this.getFrameCount();

		for(int frame = 0; frame < frames; frame++) {

			int[][] levels = this.getFrameTextureData(frame);

			if(levels == null) continue;

			for(int mipmap = 0; mipmap < levels.length; mipmap++) {

				int[] pixels = levels[mipmap];

				if(pixels == null) continue;

				int w = Math.max(1, this.width >> mipmap);
				int h = Math.max(1, this.height >> mipmap);

				BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				image.setRGB(0, 0, w, h, pixels, 0, w);

				this.mutator.mutate(image, frame, frames);

				image.getRGB(0, 0, w, h, pixels, 0, w);
			}
		}
	}
}
