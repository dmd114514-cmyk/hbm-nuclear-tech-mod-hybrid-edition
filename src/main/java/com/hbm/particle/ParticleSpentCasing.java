package com.hbm.particle;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;
import com.hbm.sound.SoundRefs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The spent casing: tumbles, bounces off the ground, and trails a fading smoke.
 *
 * Self-drawn channel (layer 3): the shell is a 3D OBJ wanting culling and standard item lighting, while the trail is per-vertex-alpha
 * textureless quads — the shared buffer can't serve both state sets.
 *
 * 1.12.2's Particle isn't an Entity: no rotationYaw/Pitch, and the Entity-bred yOffset/ySize, inWater, isInWeb are all gone,
 * so tumble angles ride along locally and the landing-pose correction uses rotationPitch only.
 * Position math simplifies too: this version's setPosition builds the box with minY == posY, and vanilla's resetPositionToBB fills back
 * exactly that value, so the old yOffset/ySize compensations have no counterpart and are simply omitted.
 * The old getBrightnessForRender override didn't come over: it only served vanilla's three shared-buffer layers' lighting,
 * while this layer's brightness is set below via setLightmapTextureCoords — an override nobody would call.
 */
@SideOnly(Side.CLIENT)
public class ParticleSpentCasing extends Particle {

	private static final ResourceLocation CASINGS_TEX = new ResourceLocation("hbm", "textures/particle/casings.png");
	private static final String CASINGS_MODEL = "models/effect/casings.obj";

	private static final float dScale = 0.05F, smokeJitter = 0.001F;

	/** One trail node: position relative to the shell plus remaining density. The old Pair<Vec3,Double> can't exist (Vec3d is immutable), so a small holder */
	private static final class SmokeNode {
		double x;
		double y;
		double z;
		double alpha;

		SmokeNode(double x, double y, double z, double alpha) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.alpha = alpha;
		}
	}

	private int maxSmokeGen = 120;
	private double smokeLift = 0.5D;
	private int nodeLife = 30;

	private final List<SmokeNode> smokeNodes = new ArrayList<SmokeNode>();

	private final TextureManager textureManager;

	private final SpentCasing config;
	private boolean isSmoking;

	private float momentumPitch, momentumYaw;
	private float rotationPitch, rotationYaw;
	private float prevRotationPitch, prevRotationYaw;

	public ParticleSpentCasing(TextureManager textureManager, World world, double x, double y, double z, double mx, double my, double mz, float momentumPitch, float momentumYaw, SpentCasing config, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
		super(world, x, y, z, 0, 0, 0);
		this.textureManager = textureManager;
		this.momentumPitch = momentumPitch;
		this.momentumYaw = momentumYaw;
		this.config = config;

		this.particleMaxAge = config.getMaxAge();
		this.setSize(2 * dScale * Math.max(config.getScaleX(), config.getScaleZ()), dScale * config.getScaleY());

		this.isSmoking = smoking;
		this.maxSmokeGen = smokeLife;
		this.smokeLift = smokeLift;
		this.nodeLife = nodeLife;

		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;

		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;

		this.particleGravity = 1F;

		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	/** The shell's initial pose at ejection. The old version wrote the Entity fields rotationYaw/rotationPitch from outside; the fields are this class's own now */
	public void setRotation(float yaw, float pitch) {
		this.rotationYaw = yaw;
		this.prevRotationYaw = yaw;
		this.rotationPitch = pitch;
		this.prevRotationPitch = pitch;
	}

	/*The ejector's offset: the old version read posX/posY/posZ then setPosition'd again; those fields are protected now, so the step moves inside */
	public void offsetPosition(double dx, double dy, double dz) {
		this.setPosition(this.posX + dx, this.posY + dy, this.posZ + dz);
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}

		this.motionY -= 0.04D * (double) this.particleGravity;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;

		if(this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;

			// lying flat: pitch snaps to the nearest 180 degrees, so the shell rests steadily instead of wobbling in place
			this.rotationPitch = (float) (Math.floor(this.rotationPitch / 180F + 0.5F)) * 180F;
			this.momentumYaw *= 0.7F;
			this.onGround = false;
		}

		if(this.particleAge > this.maxSmokeGen && !this.smokeNodes.isEmpty()) {
			this.smokeNodes.clear();
		}

		if(this.isSmoking && this.particleAge <= this.maxSmokeGen) {

			for(SmokeNode node : this.smokeNodes) {
				node.x += this.rand.nextGaussian() * smokeJitter;
				node.z += this.rand.nextGaussian() * smokeJitter;
				node.y += this.smokeLift * dScale;
				node.alpha = Math.max(0, node.alpha - (1D / (double) this.nodeLife));
			}

			// The old version also had an || inWater branch here, but the outer guard already pins particleAge <= maxSmokeGen,
			// reachable only on the equal frame, and 1.12.2's Particle has no inWater to query — treated as dry, plainly.
			this.smokeNodes.add(new SmokeNode(0, 0, 0, this.smokeNodes.isEmpty() ? 0D : 1D));
		}

		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;

		this.rotationPitch += this.momentumPitch;
		this.rotationYaw += this.momentumYaw;

		if(Math.abs(this.prevRotationPitch - this.rotationPitch) > 180) {
			if(this.prevRotationPitch < this.rotationPitch) this.prevRotationPitch += 360;
			if(this.prevRotationPitch > this.rotationPitch) this.prevRotationPitch -= 360;
		}

		if(Math.abs(this.prevRotationYaw - this.rotationYaw) > 180) {
			if(this.prevRotationYaw < this.rotationYaw) this.prevRotationYaw += 360;
			if(this.prevRotationYaw > this.rotationYaw) this.prevRotationYaw -= 360;
		}
	}

	/**
	 * Hand-rolled collision, not vanilla's move: vanilla zeroes horizontal velocity on contact,
	 * while what's wanted here is "hit and bounce", keeping the original speed and scaling it.
	 */
	public void moveEntity(double mx, double my, double mz) {

		this.world.profiler.startSection("move");

		double initMoX = mx;
		double initMoY = my;
		double initMoZ = mz;

		List<AxisAlignedBB> list = this.world.getCollisionBoxes(null, this.getBoundingBox().offset(mx, my, mz));

		for(AxisAlignedBB box : list) {
			my = box.calculateYOffset(this.getBoundingBox(), my);
		}

		this.setBoundingBox(this.getBoundingBox().offset(0.0D, my, 0.0D));

		for(AxisAlignedBB box : list) {
			mx = box.calculateXOffset(this.getBoundingBox(), mx);
		}

		this.setBoundingBox(this.getBoundingBox().offset(mx, 0.0D, 0.0D));

		for(AxisAlignedBB box : list) {
			mz = box.calculateZOffset(this.getBoundingBox(), mz);
		}

		this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, mz));

		this.world.profiler.endSection();
		this.world.profiler.startSection("rest");
		this.resetPositionToBB();
		boolean collidedVertically = initMoY != my;
		this.onGround = initMoY != my && initMoY < 0.0D;

		// wall hit: horizontal velocity pulled back to a quarter, tumble decaying in reverse — or a random spin if it wasn't spinning
		if(initMoX != mx) {
			this.motionX *= -0.25D;

			if(Math.abs(this.momentumYaw) > 1e-7)
				this.momentumYaw *= -0.75F;
			else
				this.momentumYaw = (float) this.rand.nextGaussian() * 10F * this.config.getBounceYaw();
		}

		if(initMoY != my) {
			this.motionY *= -0.5D;

			boolean rotFromSpeed = Math.abs(this.motionY) > 0.04;
			if(rotFromSpeed || Math.abs(this.momentumPitch) > 1e-7) {
				this.momentumPitch *= -0.75F;
				if(rotFromSpeed) {
					float mult = (float) Math.max(-1F, Math.min(1F, initMoY / 0.2F));
					this.momentumPitch += (float) this.rand.nextGaussian() * 10F * this.config.getBouncePitch() * mult;
					this.momentumYaw += (float) this.rand.nextGaussian() * 10F * this.config.getBounceYaw() * mult;
				}
			}
		}

		if(initMoZ != mz) {
			this.motionZ *= -0.25D;

			if(Math.abs(this.momentumYaw) > 1e-7)
				this.momentumYaw *= -0.75F;
			else
				this.momentumYaw = (float) this.rand.nextGaussian() * 10F * this.config.getBounceYaw();
		}

		if(this.config.getSound() != null && collidedVertically && Math.abs(initMoY) >= 0.2) {
			float volume = SpentCasing.PLINK_LARGE.equals(this.config.getSound()) ? 1F : 0.5F;
			float pitch = 1F + this.rand.nextFloat() * 0.2F;
			// The old line went through MainRegistry.proxy.playSoundClient, a door nobody has filled here, so SoundHandler gets fed directly;
			// the category follows what sounds.json declares for these entries
			Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(
					SoundRefs.resolve(new ResourceLocation(this.config.getSound())), SoundCategory.PLAYERS,
					volume, pitch, (float) this.posX, (float) this.posY, (float) this.posZ));
		}

		this.world.profiler.endSection();
	}

	/** Used for frame-perfect translation of smoke */
	private boolean setupDeltas = false;
	private double prevRenderX;
	private double prevRenderY;
	private double prevRenderZ;

	@Override
	public void renderParticle(BufferBuilder buffer, Entity viewer, float interp, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableRescaleNormal();
		GlStateManager.depthMask(true);

		double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) interp;
		double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) interp;
		double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp;

		if(!this.setupDeltas) {
			this.prevRenderX = pX;
			this.prevRenderY = pY;
			this.prevRenderZ = pZ;
			this.setupDeltas = true;
		}

		int brightness = this.world.getCombinedLight(new BlockPos(MathHelper.floor(pX), MathHelper.floor(pY), MathHelper.floor(pZ)), 0);
		int lX = brightness % 65536;
		int lY = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lX, (float) lY);

		this.textureManager.bindTexture(CASINGS_TEX);

		// the engine stack is already translated to the camera origin; absolute world coordinates throughout, no extra camera subtraction
		GlStateManager.translate(pX, pY - this.height / 4 + this.config.getScaleY() * 0.01, pZ);

		GlStateManager.scale(dScale, dScale, dScale);

		GlStateManager.rotate(180 - (float) interp(prevRotationYaw, rotationYaw, interp), 0, 1, 0);
		GlStateManager.rotate((float) -interp(prevRotationPitch, rotationPitch, interp), 1, 0, 0);

		GlStateManager.scale(this.config.getScaleX(), this.config.getScaleY(), this.config.getScaleZ());

		IModelCustomNamed casings = ResourceManager.getModel(CASINGS_MODEL);

		int index = 0;
		for(String name : this.config.getType().partNames) {
			int col = this.config.getColors()[index]; //unsafe on purpose, set your colors properly or else...!
			GlStateManager.color(((col >> 16) & 0xFF) / 255F, ((col >> 8) & 0xFF) / 255F, (col & 0xFF) / 255F);
			casings.renderPart(name);
			index++;
		}

		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(pX, pY - this.height / 4, pZ);

		if(!this.smokeNodes.isEmpty()) {

			// shell drawn; the trail smoke switches state sets: no lighting, no texture, no culling, per-vertex alpha
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
			GlStateManager.enableBlend();
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.disableTexture2D();
			GlStateManager.disableCull();

			float scale = this.config.getScaleX() * 0.5F * dScale;
			float yaw = viewer.prevRotationYaw + (viewer.rotationYaw - viewer.prevRotationYaw) * interp;
			Vec3d vec = new Vec3d(scale, 0, 0).rotateYaw(-yaw * (float) Math.PI / 180F);

			double deltaX = this.prevRenderX - pX;
			double deltaY = this.prevRenderY - pY;
			double deltaZ = this.prevRenderZ - pZ;

			// however far the shell moved this frame is added back in reverse to the node, so the smoke stays put instead of chasing the shell
			for(SmokeNode node : this.smokeNodes) {
				node.x += deltaX;
				node.y += deltaY;
				node.z += deltaZ;
			}

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

			for(int i = 0; i < this.smokeNodes.size() - 1; i++) {
				final SmokeNode node = this.smokeNodes.get(i), past = this.smokeNodes.get(i + 1);
				float nodeAlpha = (float) node.alpha;
				float pastAlpha = (float) past.alpha;

				double timeAlpha = 1D - (double) this.particleAge / (double) this.maxSmokeGen;
				nodeAlpha *= (float) timeAlpha;
				pastAlpha *= (float) timeAlpha;

				// The old version also had a setNormal(0,1,0), a fixed-pipeline leftover for unlit drawing;
				// POSITION_COLOR has no normal element at all and the lights are off — nothing to restore
				vertex(buffer, node.x, node.y, node.z, nodeAlpha);
				vertex(buffer, node.x + vec.x, node.y, node.z + vec.z, 0F);
				vertex(buffer, past.x + vec.x, past.y, past.z + vec.z, 0F);
				vertex(buffer, past.x, past.y, past.z, pastAlpha);

				vertex(buffer, node.x, node.y, node.z, nodeAlpha);
				vertex(buffer, node.x - vec.x, node.y, node.z - vec.z, 0F);
				vertex(buffer, past.x - vec.x, past.y, past.z - vec.z, 0F);
				vertex(buffer, past.x, past.y, past.z, pastAlpha);
			}

			Tessellator.getInstance().draw();

			GlStateManager.enableCull();
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableLighting();
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();

		this.prevRenderX = pX;
		this.prevRenderY = pY;
		this.prevRenderZ = pZ;
	}

	private static double interp(double prev, double current, float partial) {
		return prev + (current - prev) * (double) partial;
	}

	private void vertex(BufferBuilder buffer, double x, double y, double z, float alpha) {
		buffer.pos(x, y, z).color(1F, 1F, 1F, alpha).endVertex();
	}
}
