package com.hbm.entity.mob;

import java.util.function.Predicate;

import com.hbm.entity.mob.ai.EntityAIEatBread;
import com.hbm.entity.mob.ai.EntityAIStartFlying;
import com.hbm.entity.mob.ai.EntityAIStopFlying;
import com.hbm.entity.mob.ai.EntityAISwimmingConditional;
import com.hbm.entity.mob.ai.EntityAIWanderConditional;
import com.hbm.items.tool.ItemFertilizer;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

/**
 * The pigeon: a flying creature on the "walk/fly" two-state pair; sheds fat only while flying, fattens only on bread.
 *
 * Production isn't here: the old table hung it on plains biomes ({@code addSpawn(EntityPigeon.class, 1, 5, 10, creature, PLAINS)})
 * with a spawn egg; both ends live on the world and item sides, so this wires only the entity and its renderer.
 *
 * The deltas are all on this side:
 * - state bits: the old version stuffed two bytes into dataWatcher slots 12/13; both became {@code DataParameter}s
 *   (BYTE serializers) with registration-ordered slots; those two numbers have no equivalent and need none.
 * - {@code isAIEnabled()} is entirely gone (AI on by default); dropping that line changes nothing.
 * - the landing and pressure-plate hooks changed signatures: {@code fall} gained a float,
 *   {@code updateFallState} became four-arg (block state and position added); the three empty overrides' semantics unchanged —
 *   the pigeon flies, takes no fall damage, makes no footsteps, presses no plates.
 * - the fertilizer line called {@code ItemFertilizer.fertilize(world, x, y, z, player, force)}; that door now takes
 *   {@code BlockPos}+hand+stack: the pigeon holds no fertilizer, so {@code ItemStack.EMPTY} with force=true
 *   taking the "no consumption, just grow" path (verified against this version's implementation: only the caller-side instance shrinks the stack).
 *   {@code playAuxSFX(2005, ...)} is {@code playEvent(2005, pos, 0)} here.
 * - the sweat particle packet rode {@code PacketThreading} in the old version; this version's facade is {@code AsyncPacketSender},
 *   and per this tree's convention it's sent server-side only (a client sending a to-client packet points the wrong way); the four NBT keys unchanged word for word,
 *   consumed by {@code effectNT}'s sweat branch.
 *
 * One thing not ported: {@code getDropItem()} returning a feather. The method (still on {@code EntityLiving} here)
 * is consulted only by vanilla's own few dropFewItems implementations, and this pigeon's dropFewItems is fully self-written, never calling super,
 * with no other callers — it had no effect even in the old version, so no empty shell.
 *
 * Drops stay in code, not tables: this version's loot tables expose only "is the entity on fire" as an entity property (decompile-verified),
 * "three feathers only after eating bread" can't be expressed in a table ({@code isFat} is our own state bit, not a vanilla property),
 * and the {@code dropFewItems} trick of feeding looting into the random ceiling has no table function either.
 */
public class EntityPigeon extends EntityCreature implements IFlyingCreature, IAnimals {

	/** The walk/fly pair; old dataWatcher slot 12 */
	private static final DataParameter<Byte> DW_FLYING = EntityDataManager.<Byte>createKey(EntityPigeon.class, DataSerializers.BYTE);

	/** Whether it's eaten its fill; old dataWatcher slot 13 — model and drops both read it */
	private static final DataParameter<Byte> DW_FAT = EntityDataManager.<Byte>createKey(EntityPigeon.class, DataSerializers.BYTE);

	/** These four are for the renderer's interpolation ({@code RenderPigeon.handleRotationFloat}), hence not private */
	public float fallTime;
	public float dest;
	public float prevDest;
	public float prevFallTime;
	public float offGroundTimer = 1.0F;

	public EntityPigeon(World world) {
		super(world);

		Predicate<EntityLivingBase> noFlyCondition = entity -> ((EntityPigeon) entity).getFlyingState() == IFlyingCreature.STATE_WALKING;

		this.tasks.addTask(0, new EntityAIStartFlying(this, this));
		this.tasks.addTask(0, new EntityAIStopFlying(this, this));
		this.tasks.addTask(1, new EntityAISwimmingConditional(this, noFlyCondition));
		this.tasks.addTask(2, new EntityAIEatBread(this, 0.4D));
		this.tasks.addTask(5, new EntityAIWanderConditional(this, 0.2D, noFlyCondition));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.setSize(0.5F, 1.0F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_FLYING, Byte.valueOf((byte) 0));
		this.getDataManager().register(DW_FAT, Byte.valueOf((byte) 0));
	}

	/**
	 * One hit past half health shatters it on the spot: the old line was "damage {@code >= getMaxHealth() * 2}", copied.
	 * Feathers use its own scattering routine (ten of them, flung random directions), not {@code dropFewItems} — so fortune never applies.
	 */
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if(amount >= this.getMaxHealth() * 2 && !this.world.isRemote) {
			this.setDead();

			for(int i = 0; i < 10; i++) {
				Vec3d vec = new Vec3d(this.rand.nextGaussian(), this.rand.nextGaussian(), this.rand.nextGaussian()).normalize();

				EntityItem feather = new EntityItem(this.world,
						this.posX + vec.x, this.posY + this.height / 2.0D + vec.y, this.posZ + vec.z, new ItemStack(Items.FEATHER));

				feather.motionX = vec.x * 0.5;
				feather.motionY = vec.y * 0.5;
				feather.motionZ = vec.z * 0.5;

				this.world.spawnEntity(feather);
			}

			return true;
		}

		return super.attackEntityFrom(source, amount);
	}

	@Override
	public int getFlyingState() {
		return ((Byte) this.getDataManager().get(DW_FLYING)).intValue();
	}

	@Override
	public void setFlyingState(int state) {
		this.getDataManager().set(DW_FLYING, Byte.valueOf((byte) state));
	}

	public boolean isFat() {
		return ((Byte) this.getDataManager().get(DW_FAT)).byteValue() == 1;
	}

	public void setFat(boolean fat) {
		this.getDataManager().set(DW_FAT, Byte.valueOf((byte) (fat ? 1 : 0)));
	}

	@Override
	protected void dropFewItems(boolean byPlayer, int looting) {
		int j = this.rand.nextInt(3) + this.rand.nextInt(1 + looting);

		for(int k = 0; k < j; ++k) {
			this.dropItem(Items.FEATHER, 1);
		}

		if(this.isBurning()) {
			this.dropItem(Items.COOKED_CHICKEN, this.isFat() ? 3 : 1);
		} else {
			this.dropItem(Items.CHICKEN, this.isFat() ? 3 : 1);
		}
	}

	@Override
	protected void playStepSound(BlockPos pos, Block blockIn) {
		this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
	}

	/** This pigeon is silent: all three old getters returned null ({@code getHurtSound} now takes the damage source) */
	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}

	/**
	 * The flying leg: lift hand-written (cut at altitude ceiling, extra push in water), facing-forward 1.5 with the occasional head tilt.
	 * When fat it sheds while flying, scanning ten blocks down a layer for somewhere to plump up, stopping at the first find; one chance in ten sheds the fat.
	 *
	 * This version's {@code updateAITasks()} still exists and is still called by {@code updateEntityActionState()} (decompile-verified),
	 * so this block hangs in the same place as before; only the particle/sound half gained a server gate — reasons in the class note.
	 */
	@Override
	protected void updateAITasks() {
		super.updateAITasks();

		if(this.getFlyingState() == IFlyingCreature.STATE_FLYING) {

			int height = this.world.getHeight((int) Math.floor(this.posX), (int) Math.floor(this.posZ));

			boolean ceil = this.posY - height > 10;

			this.motionY = this.getRNG().nextGaussian() * 0.05 + (ceil ? 0 : 0.04) + (this.isInWater() ? 0.2 : 0);

			if(this.onGround) {
				this.motionY = Math.abs(this.motionY) + 0.1D;
			}

			this.moveForward = 1.5F;

			if(this.getRNG().nextInt(20) == 0) {
				this.rotationYaw += this.getRNG().nextGaussian() * 30;
			}

			if(this.isFat() && this.getRNG().nextInt(50) == 0) {

				if(!this.world.isRemote) {
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("type", "sweat");
					nbt.setInteger("count", 3);
					nbt.setInteger("block", Block.getIdFromBlock(Blocks.WOOL));
					nbt.setInteger("entity", this.getEntityId());
					AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),
							new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 50));

					int x = (int) Math.floor(this.posX);
					int y = (int) Math.floor(this.posY) - 1;
					int z = (int) Math.floor(this.posZ);
					EntityPlayer player = FakePlayerFactory.getMinecraft((WorldServer) this.world);

					for(int i = 0; i < 25; i++) {

						if(ItemFertilizer.fertilize(this.world, new BlockPos(x, y - i, z), player, EnumHand.MAIN_HAND, ItemStack.EMPTY, true)) {
							this.world.playEvent(2005, new BlockPos(x, y - i, z), 0);
							break;
						}
					}
				}

				if(this.getRNG().nextInt(10) == 0) {
					this.setFat(false);
				}
			}

		} else if(!this.onGround && this.motionY < 0.0D) {
			this.motionY *= 0.8D;
		}
	}

	/** The landing-wobble interpolation pair: the renderer smooths frames off these prev→current fields, algorithm as before */
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		this.prevFallTime = this.fallTime;
		this.prevDest = this.dest;
		this.dest = (float) ((double) this.dest + (double) (this.onGround ? -1 : 4) * 0.3D);

		if(this.dest < 0.0F) {
			this.dest = 0.0F;
		}

		if(this.dest > 1.0F) {
			this.dest = 1.0F;
		}

		if(!this.onGround && this.offGroundTimer < 1.0F) {
			this.offGroundTimer = 1.0F;
		}

		this.offGroundTimer = (float) ((double) this.offGroundTimer * 0.9D);

		if(!this.onGround && this.motionY < 0.0D) {
			this.motionY *= 0.6D;
		}

		this.fallTime += this.offGroundTimer * 2.0F;
	}

	@Override
	public boolean doesEntityNotTriggerPressurePlate() {
		return true;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public void fall(float distance, float damageMultiplier) { }

	@Override
	protected void updateFallState(double distanceFallen, boolean onGroundIn, IBlockState state, BlockPos pos) { }
}
