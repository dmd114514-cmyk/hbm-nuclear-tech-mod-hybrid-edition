package com.hbm.entity.projectile;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.hbm.util.TrackerUtil;

/**
 * The throwable base: logic nearly identical to vanilla's EntityThrowable, with the unrelated bits stripped,
 * and "what it hits / how it flies / when it despawns" all left to subclasses to re-tune.
 *
 * Four deltas from the old version, all flagged below:
 * - collision ray: world.func_147447_a → rayTraceBlocks, same parameter order (stop on liquid / ignore non-colliding blocks /
 *   return the last non-collidable miss), return type RayTraceResult; the block hit's three ints blockX/Y/Z
 *   became getBlockPos().
 * - box shaping: the old addCoord translated a copy — this version's synonym is offset; the old expand(x,y,z) grew both sides
 *   by x/y/z — this version's synonym is grow. Mixing them up doesn't fail compilation, it just makes "which entities get scanned" wrong forever, hence the flag.
 * - portal entry: the old setInPortal() is gone; setPortal(BlockPos) replaces it and sets inPortal and cooldown itself.
 * - IProjectile in this version has only shoot(double,double,double,float,float); the old setThrowableHeading
 *   is no longer an interface method. The implementation stays on setThrowableHeading (a dozen-plus call sites in the tree know that name,
 *   and later ballistic subclasses port against it), with shoot forwarding once.
 *
 * Also, the old `this.yOffset = 0.0F` has no counterpart field (render lift is getEyeHeight/shadow radius business now);
 * dropped outright, flight unaffected.
 *
 * One more recurring naming trap: the old MathHelper.sqrt_double actually **returned float** (the name lies),
 * while this version's sqrt(double) honestly returns double — so every old line that "sqrt'd then assigned to float" needs an explicit narrowing,
 * the four spots above included.
 *
 * The old entity-side getShadowSize() override is gone too — shadow radius belongs to the renderer (Render.shadowSize),
 * with no entity-side door anymore; this family's RenderCog doesn't draw shadows anyway, so those two lines are deleted.
 */
public abstract class EntityThrowableNT extends Entity implements IProjectile {

	private static final DataParameter<Byte> DW_STUCK_SIDE = EntityDataManager.createKey(EntityThrowableNT.class, DataSerializers.BYTE);

	private int stuckBlockX = -1;
	private int stuckBlockY = -1;
	private int stuckBlockZ = -1;
	private Block stuckBlock;
	protected boolean inGround;
	public int throwableShake;
	protected EntityLivingBase thrower;
	private String throwerName;
	public int ticksInGround;
	public int ticksInAir;

	public EntityThrowableNT(World world) {
		super(world);
		this.setSize(0.25F, 0.25F);
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(DW_STUCK_SIDE, Byte.valueOf((byte) 0));
	}

	/** Which face it bit into when sticking, stored in the watcher so clients know the pose */
	public void setStuckIn(int side) {
		this.getDataManager().set(DW_STUCK_SIDE, (byte) side);
	}

	public int getStuckIn() {
		return this.getDataManager().get(DW_STUCK_SIDE).byteValue();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double dist) {

		double perimeter = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
		perimeter *= 64.0D;
		return dist < perimeter * perimeter;
	}

	public EntityThrowableNT(World world, EntityLivingBase thrower) {
		super(world);
		this.thrower = thrower;
		this.setSize(0.25F, 0.25F);
		this.setLocationAndAngles(thrower.posX, thrower.posY + (double) thrower.getEyeHeight(), thrower.posZ, thrower.rotationYaw, thrower.rotationPitch);
		this.posX -= (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
		this.posY -= 0.1D;
		this.posZ -= (double) (MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
		this.setPosition(this.posX, this.posY, this.posZ);
		float velocity = 0.4F;
		this.motionX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * velocity);
		this.motionZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * velocity);
		this.motionY = (double) (-MathHelper.sin((this.rotationPitch + this.throwAngle()) / 180.0F * (float) Math.PI) * velocity);
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, this.throwForce(), 1.0F);
	}

	public EntityThrowableNT(World world, double x, double y, double z) {
		super(world);
		this.ticksInGround = 0;
		this.setSize(0.25F, 0.25F);
		this.setPosition(x, y, z);
	}

	/* ---------- the doors subclasses re-tune; defaults are the old throwable feel ---------- */

	protected float throwForce() {
		return 1.5F;
	}

	protected double headingForceMult() {
		return 0.0075D;
	}

	protected float throwAngle() {
		return 0.0F;
	}

	/** Per-tick displacement multiplier: slow-motion trajectories live on it, and the ray/collision scan scales to the same length */
	protected double motionMult() {
		return 1.0D;
	}

	public double getGravityVelocity() {
		return 0.03D;
	}

	protected float getAirDrag() {
		return 0.99F;
	}

	protected float getWaterDrag() {
		return 0.8F;
	}

	/** Ticks stuck in the ground before auto-despawn; 0 or negative means never */
	protected int groundDespawn() {
		return 1200;
	}

	/** Whether to run full block-collision movement; off means raw displacement — cheaper through grass-thin stuff */
	public boolean fullBlockCollisions() {
		return false;
	}

	public boolean doesImpactEntities() {
		return true;
	}

	/** When on, one hit settles every entity along the path, not just the nearest */
	public boolean doesPenetrate() {
		return false;
	}

	/** Spectral trajectory: no block ray, entity scanning only */
	public boolean isSpectral() {
		return false;
	}

	/** Ticks after launch before friendly fire becomes possible, so a fresh throw can't hit the thrower */
	public int selfDamageDelay() {
		return 5;
	}

	protected abstract void onImpact(RayTraceResult mop);

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		this.setThrowableHeading(x, y, z, velocity, inaccuracy);
	}

	public void setThrowableHeading(double motionX, double motionY, double motionZ, float velocity, float inaccuracy) {
		float throwLen = (float) MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		motionX /= throwLen;
		motionY /= throwLen;
		motionZ /= throwLen;
		motionX += this.rand.nextGaussian() * headingForceMult() * (double) inaccuracy;
		motionY += this.rand.nextGaussian() * headingForceMult() * (double) inaccuracy;
		motionZ += this.rand.nextGaussian() * headingForceMult() * (double) inaccuracy;
		motionX *= velocity;
		motionY *= velocity;
		motionZ *= velocity;
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		float hyp = (float) MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(motionY, (double) hyp) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		if(this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float hyp = (float) MathHelper.sqrt(x * x + z * z);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(y, (double) hyp) * 180.0D / Math.PI);
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.throwableShake > 0) {
			--this.throwableShake;
		}

		if(this.inGround) {
			if(this.world.getBlockState(new BlockPos(this.stuckBlockX, this.stuckBlockY, this.stuckBlockZ)).getBlock() == this.stuckBlock) {
				++this.ticksInGround;

				if(this.groundDespawn() > 0 && this.ticksInGround == this.groundDespawn()) {
					this.setDead();
				}

				return;

			} else {

				this.inGround = false;
				this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
				this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
				this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}

		} else {
			++this.ticksInAir;

			Vec3d pos = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d nextPos = new Vec3d(this.posX + this.motionX * motionMult(), this.posY + this.motionY * motionMult(), this.posZ + this.motionZ * motionMult());
			RayTraceResult mop = null;
			if(!this.isSpectral()) mop = this.world.rayTraceBlocks(pos, nextPos, false, true, false);
			pos = new Vec3d(this.posX, this.posY, this.posZ);
			nextPos = new Vec3d(this.posX + this.motionX * motionMult(), this.posY + this.motionY * motionMult(), this.posZ + this.motionZ * motionMult());

			if(mop != null) {
				nextPos = new Vec3d(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
			}

			if(!this.world.isRemote && this.doesImpactEntities()) {

				Entity hitEntity = null;
				// offset = old addCoord (translated copy), grow = old expand (grown both ways); reversed it won't error, just scan nothing
				AxisAlignedBB sweep = this.getEntityBoundingBox().offset(this.motionX * motionMult(), this.motionY * motionMult(), this.motionZ * motionMult()).grow(1.0D, 1.0D, 1.0D);
				List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, sweep);
				double nearest = 0.0D;
				EntityLivingBase thrower = this.getThrower();
				RayTraceResult nonPenImpact = null;

				for(int j = 0; j < list.size(); ++j) {
					Entity entity = list.get(j);

					if(entity.canBeCollidedWith() && (entity != thrower || this.ticksInAir >= this.selfDamageDelay()) && entity.isEntityAlive()) {
						double hitbox = 0.3F;
						AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(hitbox, hitbox, hitbox);
						RayTraceResult hitMop = aabb.calculateIntercept(pos, nextPos);

						if(hitMop != null) {

							// with piercing on, every entity along the way settles once
							if(this.doesPenetrate()) {
								this.onImpact(new RayTraceResult(entity, hitMop.hitVec));
							} else {

								double dist = pos.distanceTo(hitMop.hitVec);

								if(dist < nearest || nearest == 0.0D) {
									hitEntity = entity;
									nearest = dist;
									nonPenImpact = hitMop;
								}
							}
						}
					}
				}

				// without piercing, only the nearest hit settles
				if(!this.doesPenetrate() && hitEntity != null) {
					mop = new RayTraceResult(hitEntity, nonPenImpact.hitVec);
				}
			}

			if(mop != null) {
				if(mop.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(mop.getBlockPos()).getBlock() == Blocks.PORTAL) {
					this.setPortal(mop.getBlockPos());
				} else {
					this.onImpact(mop);
				}
			}

			if(!this.onGround) {
				float hyp = (float) MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
				this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

				for(this.rotationPitch = (float) (Math.atan2(this.motionY, (double) hyp) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);

				while(this.rotationPitch - this.prevRotationPitch >= 180.0F) this.prevRotationPitch += 360.0F;
				while(this.rotationYaw - this.prevRotationYaw < -180.0F) this.prevRotationYaw -= 360.0F;
				while(this.rotationYaw - this.prevRotationYaw >= 180.0F) this.prevRotationYaw += 360.0F;

				this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
				this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			}

			float drag = this.getAirDrag();
			double gravity = this.getGravityVelocity();

			if(fullBlockCollisions()) {
				this.move(MoverType.SELF, this.motionX * motionMult(), this.motionY * motionMult(), this.motionZ * motionMult());
			} else {
				this.posX += this.motionX * motionMult();
				this.posY += this.motionY * motionMult();
				this.posZ += this.motionZ * motionMult();
			}

			if(this.isInWater()) {
				for(int i = 0; i < 4; ++i) {
					float f = 0.25F;
					this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double) f, this.posY - this.motionY * (double) f, this.posZ - this.motionZ * (double) f, this.motionX, this.motionY, this.motionZ);
				}

				drag = this.getWaterDrag();
			}

			this.motionX *= (double) drag;
			this.motionY *= (double) drag;
			this.motionZ *= (double) drag;
			this.motionY -= gravity;
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	/** Sticks into a block face, syncing position to clients (packets in the old version, TrackerUtil here) */
	public void getStuck(int x, int y, int z, int side) {
		this.stuckBlockX = x;
		this.stuckBlockY = y;
		this.stuckBlockZ = z;
		this.stuckBlock = this.world.getBlockState(new BlockPos(x, y, z)).getBlock();
		this.inGround = true;
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		this.setStuckIn(side);
		TrackerUtil.sendTeleport(this.world, this);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setShort("xTile", (short) this.stuckBlockX);
		nbt.setShort("yTile", (short) this.stuckBlockY);
		nbt.setShort("zTile", (short) this.stuckBlockZ);
		nbt.setByte("inTile", (byte) Block.getIdFromBlock(this.stuckBlock));
		nbt.setByte("shake", (byte) this.throwableShake);
		nbt.setByte("inGround", (byte) (this.inGround ? 1 : 0));
		nbt.setInteger("ticksInGround", this.ticksInGround);
		nbt.setInteger("ticksInAir", this.ticksInAir);

		if((this.throwerName == null || this.throwerName.length() == 0) && this.thrower != null && this.thrower instanceof EntityPlayer) {
			this.throwerName = this.thrower.getName();
		}

		nbt.setString("ownerName", this.throwerName == null ? "" : this.throwerName);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		this.stuckBlockX = nbt.getShort("xTile");
		this.stuckBlockY = nbt.getShort("yTile");
		this.stuckBlockZ = nbt.getShort("zTile");
		this.stuckBlock = Block.getBlockById(nbt.getByte("inTile") & 255);
		this.throwableShake = nbt.getByte("shake") & 255;
		this.inGround = nbt.getByte("inGround") == 1;
		this.throwerName = nbt.getString("ownerName");
		this.ticksInGround = nbt.getInteger("ticksInGround");
		this.ticksInAir = nbt.getInteger("ticksInAir");

		if(this.throwerName != null && this.throwerName.length() == 0) {
			this.throwerName = null;
		}
	}

	public void setThrower(EntityLivingBase thrower) {
		this.thrower = thrower;
	}

	public EntityLivingBase getThrower() {
		if(this.thrower == null && this.throwerName != null && this.throwerName.length() > 0) {
			this.thrower = this.world.getPlayerEntityByName(this.throwerName);
		}
		return this.thrower;
	}
}
