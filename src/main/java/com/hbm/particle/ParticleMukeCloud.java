package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The muke cloud: a 5x5, 25-frame explosion strip paged by age, with rising/level/falling initial speeds on different friction and lifetime tiers.
 *
 * Triple-checked: pure alpha (770/771) + the old depthMask(false) + the explosion texture inside the atlas table
 * → layer 1 (binds the block atlas) + shouldDisableDepth() true (the no-depth-write page); no accumulated matrix, so no offset baking.
 *
 * Three deliberate deviations:
 * · the old `noClip = age <= 2` (wall-phasing for the first two frames). 1.12.2's Particle has no noClip; by its physics switch
 *   it becomes `canCollide = age > 2` — same semantics.
 * · the old renderParticle wrote particleScale = 3 and alpha = 1 every time — a render function mutating particle state is a side effect.
 *   Moved to the constructor, given once; the look is unchanged.
 * · the vertical radius was the literal 1 rather than the camera's y basis vector (the fog's top/bottom edges don't squash with pitch),
 *   kept as the same effect here: 1F passed into emitQuad's rotZ slot.
 */
@SideOnly(Side.CLIENT)
public class ParticleMukeCloud extends Particle {

	// the strip is 5x5
	private static final int SHEET = 5;
	private static final float FRAME = 1F / SHEET;

	// the old render wrote this half-width/height as a literal
	private static final float CLOUD_SCALE = 3F;

	private final float friction;

	public ParticleMukeCloud(TextureManager texman, World world, double x, double y, double z, double mx, double my, double mz) {
		super(world, x, y, z, mx, my, mz);

		this.particleScale = CLOUD_SCALE;
		this.particleAlpha = 1F;
		this.setParticleTexture(getSprite());

		if(my > 0) {
			this.friction = 0.9F;
			// the harder it surges, the longer it lives
			this.particleMaxAge = my > 0.1 ? 92 + this.rand.nextInt(11) + (int) (my * 20) : 72 + this.rand.nextInt(11);
		} else if(my == 0) {
			this.friction = 0.95F;
			this.particleMaxAge = 52 + this.rand.nextInt(11);
		} else {
			this.friction = 0.85F;
			this.particleMaxAge = 122 + this.rand.nextInt(31);
			// the falling tier starts from the strip's midpoint, or it reads as freshly detonated
			this.particleAge = 80;
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	// the BF tier swaps the strip by overriding this one door, sitting where the old getTexture() sat
	protected TextureAtlasSprite getSprite() {
		return ParticleSprites.sprite("explosion");
	}

	@Override
	public boolean shouldDisableDepth() {
		// the old version drew this with depth writes off; depth doesn't read the layer number now — this door picks the page
		return true;
	}

	@Override
	public void onUpdate() {

		// wall-phasing for the first two frames, colliding after that
		this.canCollide = this.particleAge > 2;

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge - 2) this.setExpired();

		this.motionY -= 0.04D * this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);

		this.motionX *= this.friction;
		this.motionY *= this.friction;
		this.motionZ *= this.friction;

		if(this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		if(this.particleAge > this.particleMaxAge) this.particleAge = this.particleMaxAge;

		int texIndex = this.particleAge * SHEET * SHEET / this.particleMaxAge;

		float uMin = (texIndex % SHEET) * FRAME;
		float uMax = uMin + FRAME;
		float vMin = (texIndex / SHEET) * FRAME;
		float vMax = vMin + FRAME;

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ);

		ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
				rotX, 1F, rotYZ, rotXY, rotXZ, this.particleScale,
				uMin, vMin, uMax, vMax, 1F, 1F, 1F, this.particleAlpha);
	}
}
