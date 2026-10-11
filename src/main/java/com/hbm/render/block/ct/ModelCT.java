package com.hbm.render.block.ct;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.hbm.lib.RefStrings;
import com.hbm.util.LoggingUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;

/**
 * Where connected textures land in this version: a cube's six faces, each cut 2×2 into four quads,
 * each quad wearing its own fragment, so neighboring blocks piece together continuous borders/corners.
 *
 * The old version's layer here was the ISBRH renderer (RenderBlocksCT) drawing vertices itself, averaging the face-corner light and brightness
 * RenderBlocks computed onto the four sub-vertices. That whole apparatus is gone; baked models emit the quads now:
 * more accurate than the old "average of four corners". The VertInfo interpolation therefore doesn't come over.
 * lighting is no longer computed by hand — once isAmbientOcclusion() concedes, Forge's block renderer computes smooth light and AO from vertex positions,
 *
 * World coordinates arrive through Block#getExtendedState (see CTContext), so the connect judgment and the quad drawing
 * use the same cell and the same direction table.
 */
public final class ModelCT {

	private ModelCT() { }

	/**
	 * Writing a tier's model in blockstate JSON as hbm:ct/&lt;texture path&gt; enables the connective look; the base and _ct textures are both derived from it.
	 * The prefix consumes only "ct/", the whole rest being the texture path: block textures already live under blocks/ (assets/hbm/textures/blocks/),
	 * and a prefix that eats the directory too would send the base image to a nonexistent hbm:deco_steel, painting the whole family purple-black.
	 */
	public static final String PATH_PREFIX = "ct/";

	/** The four corners of one face, matching CTContext.Layout's corner order */
	public static final int TL = 0;
	public static final int TR = 1;
	public static final int BL = 2;
	public static final int BR = 3;

	/** Fragment rects are floored onto the sprite's atlas UV, and the quad maps exactly that piece, so the old 16-cell interpolation is no longer needed */
	private static final float HALF = 0.5F;

	/**
	 * Extracting &lt;texture path&gt;: when the model name in blockstate lacks "models/", vanilla completes it against the block model directory as
	 * "block/…" (verified: baking hbm:ct/blocks/deco_steel arrives here as hbm:block/ct/blocks/deco_steel),
	 * so both "models/" and "block/" prefixes must be stripped before deciding whether this loader handles it.
	 * @return a texture path like "blocks/deco_steel"; null when this loader doesn't handle it
	 */
	private static String texturePath(ResourceLocation modelLocation) {

		if(!RefStrings.MODID.equals(modelLocation.getResourceDomain()))
			return null;

		String path = modelLocation.getResourcePath();

		if(path.startsWith("models/"))
			path = path.substring("models/".length());

		if(path.startsWith("block/"))
			path = path.substring("block/".length());

		if(!path.startsWith(PATH_PREFIX))
			return null;

		return path.substring(PATH_PREFIX.length());
	}

	/**
	 * Feeds this loader into ModelLoaderRegistry. The registration lives only here, not in the registration unit itself:
	 * that constructor line needs an assignment check for the `Loader`-to-`ICustomModelLoader` conversion, and bytecode verification walks every method the moment the server
	 * loads the registration unit — which would go load that client class. A dedicated server doesn't have it, and the server start dies right there.
	 * Placed here, the registration unit keeps a single call the server never reaches, and that class is never loaded.
	 */
	public static void registerLoader() {
		ModelLoaderRegistry.registerLoader(new Loader());
		LoggingUtil.info("[hbm] connected-texture model loader registered, claiming model locations hbm:{}<texture path> (takes that texture plus the same-named _ct sheet)", PATH_PREFIX);
	}

	/**
	 * Claims the hbm:ct/* model locations. Custom loaders don't fight the vanilla loader: the vanilla path only runs when
	 * no custom loader claims the name (a hardwired order in ModelLoaderRegistry), so what's claimed here belongs here.
	 */
	public static final class Loader implements ICustomModelLoader {

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
		}

		@Override
		public boolean accepts(ResourceLocation modelLocation) {
			return texturePath(modelLocation) != null;
		}

		@Override
		public IModel loadModel(ResourceLocation modelLocation) throws Exception {

			String texture = texturePath(modelLocation);

			if(texture == null)
				throw new IllegalStateException("CT model location should never reach here: " + modelLocation);

			ResourceLocation base = new ResourceLocation(RefStrings.MODID, texture);
			ResourceLocation connected = new ResourceLocation(RefStrings.MODID, texture + "_ct");

			// Self-check: whether connected textures actually get claimed can only be said here — with no blockstate pointing over, the game shows no difference at all
			LoggingUtil.info("[hbm] connected-texture model claimed: {} → base {} + connective sheet {}", modelLocation, base, connected);

			return new Model(base, connected);
		}
	}

	/**
	 * The unbaked model. Two sprites named here. The old CTStitchReceiver waited in postStitch for the stitching result before grabbing textures;
	 * this version is the reverse: declare the two you want here first, and only then will the baker stitch them into the atlas and hand the sprites back to bake().
	 */
	public static final class Model implements IModel {

		private final ResourceLocation baseTexture;
		private final ResourceLocation connectedTexture;

		Model(ResourceLocation baseTexture, ResourceLocation connectedTexture) {
			this.baseTexture = baseTexture;
			this.connectedTexture = connectedTexture;
		}

		@Override
		public Collection<ResourceLocation> getTextures() {
			return ImmutableList.of(baseTexture, connectedTexture);
		}

		@Override
		public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
			return new Baked(format, bakedTextureGetter.apply(baseTexture), bakedTextureGetter.apply(connectedTexture), connectedTexture);
		}
	}

	/**
	 * The baked model: quads computed per layout and kept as a whole group, so one cell's seven consecutive asks (six faces + one general pass) compute once.
	 *
	 * The cache swaps "layout + that quad group" as one unit, not two fields: per-face asks may come from different threads,
	 * and separate fields would let a thread read "new layout with old quads" — that draws wrong pictures.
	 */
	public static final class Baked implements IBakedModel {

		private final VertexFormat format;
		private final TextureAtlasSprite base;
		private final CTFragment[] fragments;

		private volatile FaceCache cache;

		Baked(VertexFormat format, TextureAtlasSprite base, TextureAtlasSprite connected, ResourceLocation connectedTexture) {
			this.format = format;
			this.base = base;
			this.fragments = CTFragment.build(base, connected);

			// When the connective sheet missed the atlas you get the purple-black one, and connected parts paint purple-black. A name mismatch is the most common cause; naming beats staring at the screen
			if(connected == Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
				LoggingUtil.warn("[hbm] connected texture {} didn't make it into the atlas; the connected parts will draw purple-black", connectedTexture);
			}
		}

		private static final class FaceCache {

			final CTContext.Layout layout;
			final EnumMap<EnumFacing, List<BakedQuad>> quads;

			FaceCache(CTContext.Layout layout, EnumMap<EnumFacing, List<BakedQuad>> quads) {
				this.layout = layout;
				this.quads = quads;
			}
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {

			// All six faces carry their own orientation; there is no "face-agnostic" group. The item form walks the same road, just without world coordinates
			if(side == null)
				return ImmutableList.of();

			CTContext.Layout layout = layout(state);
			FaceCache cache = this.cache;

			if(cache == null || !cache.layout.equals(layout)) {
				cache = new FaceCache(layout, bakeFaces(layout));
				this.cache = cache;
			}

			return cache.quads.get(side);
		}

		/** With world coordinates, compute connections live; without (item form, or the block skipped the CTContext steps), the whole cell stays disconnected */
		private CTContext.Layout layout(IBlockState state) {

			CTContext.Source source = state == null ? null : CTContext.source(state);

			if(source == null)
				return CTContext.DISCONNECTED;

			return CTContext.layout(source.world, source.pos);
		}

		private EnumMap<EnumFacing, List<BakedQuad>> bakeFaces(CTContext.Layout layout) {

			EnumMap<EnumFacing, List<BakedQuad>> faces = new EnumMap<>(EnumFacing.class);

			for(EnumFacing side : EnumFacing.values()) {

				ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

				for(int corner = 0; corner < 4; corner++) {
					quads.add(quad(side, corner, layout.fragment(side, corner)));
				}

				faces.put(side, quads.build());
			}

			return faces;
		}

		/**
		 * The quarter quad at one corner.
		 *
		 * Corner rows/columns: TL=(0,0) TR=(1,0) BL=(0,1) BR=(1,1), the same "up/left" the CTContext sampling table uses.
		 * Vertex order = TR,TL,BL,BR — an order that points (v1-v0)×(v2-v1) outward from the face, the winding this version's quads want;
		 * the old drawSubFace used the same order, and both are copied over together.
		 */
		private BakedQuad quad(EnumFacing side, int corner, int fragmentIndex) {

			int col = (corner == TR || corner == BR) ? 1 : 0;
			int row = (corner == BL || corner == BR) ? 1 : 0;

			float[] origin = cornerPoint(side);
			EnumFacing right = CTContext.left(side).getOpposite();
			EnumFacing down = CTContext.up(side).getOpposite();

			float[] tl = gridPoint(origin, right, down, col, row);
			float[] tr = gridPoint(origin, right, down, col + 1, row);
			float[] bl = gridPoint(origin, right, down, col, row + 1);
			float[] br = gridPoint(origin, right, down, col + 1, row + 1);

			CTFragment fragment = fragments[fragmentIndex];

			UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
			builder.setQuadOrientation(side);
			builder.setTexture(fragment.sprite);

			putVertex(builder, side, tr[0], tr[1], tr[2], fragment.maxU, fragment.minV);
			putVertex(builder, side, tl[0], tl[1], tl[2], fragment.minU, fragment.minV);
			putVertex(builder, side, bl[0], bl[1], bl[2], fragment.minU, fragment.maxV);
			putVertex(builder, side, br[0], br[1], br[2], fragment.maxU, fragment.maxV);

			return builder.build();
		}

		/** This face's top-left vertex: face, up and left each push one axis to 1 */
		private static float[] cornerPoint(EnumFacing side) {

			float[] point = new float[] { 0F, 0F, 0F };

			pushToCorner(point, side);
			pushToCorner(point, CTContext.up(side));
			pushToCorner(point, CTContext.left(side));

			return point;
		}

		private static void pushToCorner(float[] point, EnumFacing dir) {

			if(dir.getFrontOffsetX() > 0) point[0] = 1F;
			if(dir.getFrontOffsetY() > 0) point[1] = 1F;
			if(dir.getFrontOffsetZ() > 0) point[2] = 1F;
		}

		/** In-face grid points, one cell being half a face */
		private static float[] gridPoint(float[] corner, EnumFacing right, EnumFacing down, int col, int row) {

			float[] point = new float[] { corner[0], corner[1], corner[2] };

			step(point, right, col * HALF);
			step(point, down, row * HALF);

			return point;
		}

		private static void step(float[] point, EnumFacing dir, float amount) {
			point[0] += dir.getFrontOffsetX() * amount;
			point[1] += dir.getFrontOffsetY() * amount;
			point[2] += dir.getFrontOffsetZ() * amount;
		}

		/** Fills element by element against the vertex format the model received; extra elements (TANGENT and friends) keep their defaults */
		private void putVertex(UnpackedBakedQuad.Builder builder, EnumFacing side, float x, float y, float z, float u, float v) {

			for(int e = 0; e < format.getElementCount(); e++) {

				switch(format.getElement(e).getUsage()) {

				case POSITION:
					builder.put(e, x, y, z, 1F);
					break;
				case COLOR:
					// Tint it: leaving this cell default gives alpha 0, and the whole block turns invisible
					builder.put(e, 1F, 1F, 1F, 1F);
					break;
				case NORMAL:
					builder.put(e, side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ(), 0F);
					break;
				case UV:
					if(format.getElement(e).getIndex() == 0) {
						builder.put(e, u, v, 0F, 1F);
						break;
					}
					builder.put(e);
					break;
				default:
					builder.put(e);
					break;
				}
			}
		}

		@Override
		public boolean isAmbientOcclusion() {
			return true;
		}

		@Override
		public boolean isGui3d() {
			return true;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return base;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides() {
			return ItemOverrideList.NONE;
		}
	}
}
