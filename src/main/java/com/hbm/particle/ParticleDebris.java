package com.hbm.particle;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.wiaj.WorldInAJar;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A chunk blasted out by an explosion: it carries a miniature world (jar) inside and tumbles away at an angle.
 *
 * Self-drawn channel (layer 3): what it draws is **the block's own model**, not a quad from the atlas.
 * The old `RenderBlocks.renderBlockByRenderType` went away with `ISBRH`; this version's equivalent bridge is
 * `BlockModelRenderer.renderModel(IBlockAccess, IBakedModel, IBlockState, BlockPos, BufferBuilder, boolean)`
 * — its first parameter takes any IBlockAccess, so {@link WorldInAJar} plugs in directly, no new interface needed.
 *
 * Two relocations noted so a diff against the old version doesn't read as omissions:
 *   · the old field was literally named `world`, shadowing the parent's; the parent's field is World-typed here and a same-named one collides, so it's `jar`,
 *     still injected by the creator (the constructor deliberately leaves it null; `jar == null` means simply not drawn).
 *   · the old used `getEntityId()` as the per-frame tumble seed; Particle isn't an Entity and has no id, so it carries its own seed.
 */
@SideOnly(Side.CLIENT)
public class ParticleDebris extends Particle {

	private static final Random rng = new Random();

	public WorldInAJar jar;

	private BlockModelRenderer blockModelRenderer;
	private final long seed;

	private float rotationPitch, rotationYaw;
	private float prevRotationPitch, prevRotationYaw;

	public ParticleDebris(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.seed = this.rand.nextLong();
		HbmParticleEngine.add(this);
	}

	public ParticleDebris(World world, double x, double y, double z, double mx, double my, double mz) {
		super(world, x, y, z);
		double mult = 3;
		this.motionX = mx * mult;
		this.motionY = my * mult;
		this.motionZ = mz * mult;
		this.particleMaxAge = 100;
		this.particleGravity = 0.15F;
		this.canCollide = false; // the old noClip = true
		this.seed = this.rand.nextLong();
		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge > 5) this.canCollide = true;

		// the seed is fixed per piece, so each tumbles on its own without per-frame jitter (the old version managed the same via entity id)
		rng.setSeed(this.seed);
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		this.rotationPitch += rng.nextFloat() * 10;
		this.rotationYaw += rng.nextFloat() * 10;

		if(this.seed % 3 == 0 && this.jar != null) {
			ParticleRocketFlame fx = new ParticleRocketFlame(Minecraft.getMinecraft().getTextureManager(), this.world, this.posX, this.posY, this.posZ)
					.setScale(1F * Math.max(this.jar.sizeY, 6) / 16F);
			// This version's Particle constructor already initializes prevPos to the spawn point; the old three-line manual reset doesn't come over
			fx.setMaxAge(50);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		this.motionY -= this.particleGravity;

		this.move(this.motionX, this.motionY, this.motionZ);

		this.particleAge++;
		if(this.onGround) this.setExpired();
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float interp, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		if(this.jar == null) return;

		Minecraft mc = Minecraft.getMinecraft();

		if(this.blockModelRenderer == null) {
			this.blockModelRenderer = mc.getBlockRendererDispatcher().getBlockModelRenderer();
		}

		// the jar is a fake world; light must be copied from the real world at this position
		this.jar.lightlevel = this.world.getCombinedLight(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ)), 0);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableBlend();

		GlStateManager.pushMatrix();
		// The drawing channel is HbmParticleEngine (RenderWorldLastEvent), whose whole stack is translated by -viewerPos, so absolute world coordinates here
		GlStateManager.translate(this.prevPosX + (this.posX - this.prevPosX) * (double) interp,
				this.prevPosY + (this.posY - this.prevPosY) * (double) interp,
				this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp);
		GlStateManager.rotate(prevRotationPitch + (rotationPitch - prevRotationPitch) * interp, 0F, 1F, 0F);
		GlStateManager.rotate(prevRotationYaw + (rotationYaw - prevRotationYaw) * interp, 0F, 0F, 1F);
		GlStateManager.translate(-this.jar.sizeX / 2D, -this.jar.sizeY / 2D, -this.jar.sizeZ / 2D);

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		// The old try/catch ring sheltered the ISBRH era's "every block may throw its own"; one renderModel now — no need to keep
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		// vertices use the mini-world's local coordinates (0..size); all offsetting is the matrix's job above
		buf.setTranslation(0.0D, 0.0D, 0.0D);
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		for(int ix = 0; ix < this.jar.sizeX; ix++) {
			for(int iy = 0; iy < this.jar.sizeY; iy++) {
				for(int iz = 0; iz < this.jar.sizeZ; iz++) {

					IBlockState state = this.jar.getState(ix, iy, iz);
					if(state.getBlock() == Blocks.AIR) continue;

					IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
					blockModelRenderer.renderModel(this.jar, model, state, new BlockPos(ix, iy, iz), buf, true);
				}
			}
		}

		Tessellator.getInstance().draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}
}
