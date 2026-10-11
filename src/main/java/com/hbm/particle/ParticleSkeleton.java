package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Skeletonization: splits a mob into head/torso/limb bone models that tumble to the ground with a thud.
 *
 * Self-drawn (layer 3): drawing 3D OBJ parts, with standard lighting, culling, and SRC_ALPHA/ONE_MINUS_SRC_ALPHA blending —
 * beyond what the shared buffer's quads can express. The parts live in one obj, picked by renderPart(group name).
 *
 * Three honest deviations:
 *   · tumble angles: the old version borrowed Entity's rotationYaw/rotationPitch; Particle no longer extends Entity, so it carries its own
 *     (these four fields are genuinely used, unlike ParticleGiblet's dead pair);
 *   · landing sound: the old one went through MainRegistry.proxy.playSoundClient, a door nobody has filled here, so this hands SoundHandler
 *     a PositionedSoundRecord directly; category MASTER, equivalent to the old category-less call (master volume only);
 *   · one extra standard-lighting shutdown at the end: the old enableStandardItemLighting was never undone, leaking GL_LIGHT1 to whatever drew next.
 * The light source reads getBrightnessForRender (internally world.getCombinedLight), because
 * World.getLightBrightnessForSkyBlocks no longer exists in this version.
 */
@SideOnly(Side.CLIENT)
public class ParticleSkeleton extends Particle {

	/** Part textures and the bone model */
	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/skeleton.png");
	public static final ResourceLocation texture_ext = new ResourceLocation(RefStrings.MODID + ":textures/particle/skoilet.png");
	public static final ResourceLocation texture_blood = new ResourceLocation(RefStrings.MODID + ":textures/particle/skeleton_blood.png");
	public static final ResourceLocation texture_blood_ext = new ResourceLocation(RefStrings.MODID + ":textures/particle/skoilet_blood.png");

	private static final IModelCustomNamed skeleton = ResourceManager.getModel("models/effect/skeleton.obj");

	/*The old enum lived in SkeletonCreator; only these two files ever used it, so it parks here — the creator can import when it lands */
	public static enum EnumSkeletonType {
		SKULL, TORSO, LIMB, SKULL_VILLAGER
	}

	protected EnumSkeletonType type;

	public ResourceLocation useTexture;
	public ResourceLocation useTextureExt;

	// Particle isn't an Entity in this version, so tumble angles have to be carried locally
	public float rotationYaw;
	public float prevRotationYaw;
	public float rotationPitch;
	public float prevRotationPitch;

	private float momentumYaw;
	private float momentumPitch;
	private int initialDelay;

	private final TextureManager textureManager;

	public ParticleSkeleton(TextureManager textureManager, World world, double x, double y, double z, float r, float g, float b, EnumSkeletonType type) {
		super(world, x, y, z);
		this.textureManager = textureManager;
		this.type = type;

		this.particleMaxAge = 1200 + this.rand.nextInt(20);

		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.particleGravity = 0.02F;
		this.initialDelay = 20;

		this.momentumPitch = this.rand.nextFloat() * 5 * (this.rand.nextBoolean() ? 1 : -1);
		this.momentumYaw = this.rand.nextFloat() * 5 * (this.rand.nextBoolean() ? 1 : -1);

		this.useTexture = texture;
		this.useTextureExt = texture_ext;

		HbmParticleEngine.add(this);
	}

	public ParticleSkeleton makeGib() {
		this.initialDelay = -2; // skips the random drift phase after landing
		this.useTexture = texture_blood;
		this.useTextureExt = texture_blood_ext;
		this.particleGravity = 0.04F;
		this.particleMaxAge = 600 + this.rand.nextInt(20);
		return this;
	}

	/*The creator needs to give bones an initial velocity on shatter; Particle.motionX is protected and unwritable from outside, so a same-style door */
	public ParticleSkeleton setMotion(double mx, double my, double mz) {
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		return this;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;

		if(this.initialDelay-- > 0) return;

		if(this.initialDelay == -1) {
			this.motionX = this.rand.nextGaussian() * 0.025;
			this.motionZ = this.rand.nextGaussian() * 0.025;
		}

		if(this.particleAge++ >= this.particleMaxAge) this.setExpired();

		boolean wasOnGround = this.onGround;

		this.motionY -= this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;

		if(!this.onGround) {
			this.rotationPitch += this.momentumPitch;
			this.rotationYaw += this.momentumYaw;
		} else {
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;

			if(!wasOnGround) {
				float pitch = 0.8F + this.rand.nextFloat() * 0.4F;
				Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(SoundEvents.ENTITY_SKELETON_HURT,
						SoundCategory.MASTER, 0.25F, pitch, (float) this.posX, (float) this.posY, (float) this.posZ));
			}
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		GlStateManager.pushMatrix();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		RenderHelper.enableStandardItemLighting();

		// the engine stack is already translated by -viewerPos, so absolute world coordinates here
		double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) partial;
		double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) partial;
		double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial;

		GlStateManager.translate(pX, pY, pZ);

		GlStateManager.rotate(this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partial, 0, 1, 0);
		GlStateManager.rotate(this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partial, 1, 0, 0);

		float timeLeft = this.particleMaxAge - (this.particleAge + partial);
		if(timeLeft < 40) {
			this.particleAlpha = timeLeft / 40F;
		} else {
			this.particleAlpha = 1F;
		}

		int brightness = this.getBrightnessForRender(partial);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) (brightness & 65535), (float) (brightness >> 16));

		GlStateManager.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
		GlStateManager.rotate(-90, 0, 1, 0);

		switch(this.type) {
		case SKULL:
			this.textureManager.bindTexture(this.useTexture);
			skeleton.renderPart("Skull"); break;
		case TORSO:
			this.textureManager.bindTexture(this.useTexture);
			skeleton.renderPart("Torso"); break;
		case LIMB:
			this.textureManager.bindTexture(this.useTexture);
			skeleton.renderPart("Limb"); break;
		case SKULL_VILLAGER:
			this.textureManager.bindTexture(this.useTextureExt);
			skeleton.renderPart("SkullVillager"); break;
		}

		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableCull();
		RenderHelper.disableStandardItemLighting();

		GlStateManager.popMatrix();
	}
}
