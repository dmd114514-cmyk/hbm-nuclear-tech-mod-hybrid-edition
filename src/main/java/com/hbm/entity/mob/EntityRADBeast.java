package com.hbm.entity.mob;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.NTMSounds;

import api.hbm.entity.IRadiationImmune;
import api.hbm.entity.ISuffocationImmune;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * The RAD beast: a blaze-skeleton mob that attacks by sprinkling radiation at your feet, not with claws.
 *
 * The deltas are all on the "this version's base gives less" side:
 * - this version's {@code EntityMob} constructor only sets XP (decompile-verified); the old bundled melee and targeting tasks are gone,
 *   so they're hung here: the melee task ({@code EntityAIAttackMelee}) is the call site for the attack body below,
 *   calling {@code attackEntityAsMob} when in reach; the old {@code attackEntity(target, dist)} hook doesn't exist here,
 *   so its body moves verbatim into the {@code attackEntityAsMob} override, with distance computed between entities like the old caller did.
 *   Targeting replaces the old {@code EntityMob}-bundled findPlayerToAttack, reporting players.
 * - the melee task's third argument follows the old default true (grudges last longer), speed 1.0 likewise.
 * - {@code attackTime} is {@code idleTime} here; {@code hasAttacked} has no counterpart
 *   (the wind-up animation moved to {@code swingArm}/{@code isSwingInProgress}), so dropping that line doesn't change the feel.
 * - the old dataWatcher slot 16 held "the entity it drags"; this version uses a {@code DataParameter}+VARINT,
 *   slots assigned by registration order — the number 16 has no equivalent and needs none.
 * - the two glow methods lost their partialTicks parameter here, overridden no-arg; {@code @SideOnly} dropped too —
 *   the server calls both now, and they return constants either way.
 *
 * Two things not ported, both noted here:
 * - {@code getDropItem()}: the old {@code dropFewItems} overrode to call its own drop chain and never called super, so that override had no effect even then.
 * - the death-time achievement broadcast: that achievement has no landing surface in this version (0 hits repo-wide); wire it when achievements arrive.
 *   {@code makeLeader()} stays — that's the door the producer ({@code BossSpawnHandler}, still on the world side) calls when summoning the leader.
 *
 * Drops stay in code, not tables: this version's loot tables expose only "is the entity on fire" as an entity property (decompile-verified),
 * and the wet/dry split (nuclear waste in water, spent fuel rods on land) can't be expressed in a table, nor can "drop looting many".
 */
public class EntityRADBeast extends EntityMob implements IRadiationImmune, ISuffocationImmune {

	/** The id of the entity it drags with radiation; the renderer draws the green beam after it */
	private static final DataParameter<Integer> DW_TARGET_ID = EntityDataManager.<Integer>createKey(EntityRADBeast.class, DataSerializers.VARINT);

	/** The old death-sound chain, keyed as such in the sound table ({@code ItemSounds} namespaced verbatim) */
	private static final String DEATH_SOUND = "hbm:step.iron";

	private float heightOffset = 0.5F;
	private int heightOffsetUpdateTime;

	public EntityRADBeast(World world) {
		super(world);

		this.isImmuneToFire = true;
		this.experienceValue = 30;

		this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, true));
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_TARGET_ID, Integer.valueOf(0));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(120.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(16.0D);
	}

	/** The leader: tripled health, a radiation coin on its head, and that coin always drops */
	public EntityRADBeast makeLeader() {
		this.setDropChance(EntityEquipmentSlot.MAINHAND, 1.0F);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.coin_radiation));
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(360.0D);
		this.heal(this.getMaxHealth());
		return this;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ItemSounds.resolve(NTMSounds.GEIGER_PREFIX + (1 + this.rand.nextInt(6)));
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_BLAZE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ItemSounds.resolve(DEATH_SOUND);
	}

	@Override
	public int getBrightnessForRender() {
		return 15728880;
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@Override
	public int getTotalArmorValue() {
		return 8;
	}

	@Override
	public void onLivingUpdate() {

		if(!this.world.isRemote) {

			if(this.isWet()) {
				this.attackEntityFrom(DamageSource.DROWN, 1.0F);
			}

			--this.heightOffsetUpdateTime;

			if(this.heightOffsetUpdateTime <= 0) {
				this.heightOffsetUpdateTime = 100;
				this.heightOffset = 0.5F + (float) this.rand.nextGaussian() * 3.0F;
			}

			EntityLivingBase target = this.getAttackTarget();

			if(target != null && target.posY + (double) target.getEyeHeight() > this.posY + (double) this.getEyeHeight() + (double) this.heightOffset) {
				this.motionY += (0.30000001192092896D - this.motionY) * 0.30000001192092896D;
			}

			if(target != null && this.idleTime < 10) {

				if(this.getDataManager().get(DW_TARGET_ID) != Integer.valueOf(target.getEntityId()))
					this.getDataManager().set(DW_TARGET_ID, Integer.valueOf(target.getEntityId()));
			} else {
				this.getDataManager().set(DW_TARGET_ID, Integer.valueOf(0));
			}
		}

		if(!this.onGround && this.motionY < 0.0D) {
			this.motionY *= 0.6D;
		}

		if(this.getMaxHealth() <= 150) {

			for(int i = 0; i < 6; i++) {
				this.world.spawnParticle(EnumParticleTypes.TOWN_AURA,
						this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width * 1.5,
						this.posY + this.rand.nextDouble() * (double) this.height,
						this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width * 1.5,
						0.0D, 0.0D, 0.0D);
			}

			if(this.rand.nextInt(6) == 0) {
				this.world.spawnParticle(EnumParticleTypes.FLAME,
						this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width,
						this.posY + this.rand.nextDouble() * (double) this.height * 0.75,
						this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width,
						0.0D, 0.0D, 0.0D);
			}

		} else {
			this.world.spawnParticle(EnumParticleTypes.LAVA,
					this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width,
					this.posY + this.rand.nextDouble() * (double) this.height * 0.75,
					this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width,
					0.0D, 0.0D, 0.0D);
		}

		super.onLivingUpdate();
	}

	/**
	 * The old {@code attackEntity(target, dist)} body, moved in verbatim: a claw swipe when in reach and close enough,
	 * otherwise sprinkle radiation at its feet within 30 blocks, wind up, and shriek.
	 */
	@Override
	public boolean attackEntityAsMob(Entity target) {

		float dist = this.getDistance(target);

		if(this.idleTime <= 0 && dist < 2.0F && target.getEntityBoundingBox().maxY > this.getEntityBoundingBox().minY
				&& target.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
			this.idleTime = 20;
			return super.attackEntityAsMob(target);
		} else if(dist < 30.0F) {

			double deltaX = target.posX - this.posX;
			double deltaZ = target.posZ - this.posZ;

			if(this.idleTime == 0 && this.getAttackTarget() != null) {

				ChunkRadiationManager.proxy.incrementRad(this.world, (int) Math.floor(this.posX), (int) Math.floor(this.posY), (int) Math.floor(this.posZ), 100);
				target.attackEntityFrom(ModDamageSource.radiation, 16.0F);
				this.swingArm(EnumHand.MAIN_HAND);
				this.playLivingSound();
				this.idleTime = 20;
			}

			this.rotationYaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
		}

		return false;
	}

	/** The renderer reads this to draw the link line; {@code getEntityByID(0)} returns null when the slot is empty */
	public Entity getUnfortunateSoul() {
		return this.world.getEntityByID(this.getDataManager().get(DW_TARGET_ID));
	}

	/** It floats; the old version left this cell empty */
	@Override
	public void fall(float distance, float damageMultiplier) { }

	@Override
	protected void dropFewItems(boolean beenHit, int looting) {

		if(!beenHit) {
			return;
		}

		if(looting > 0) {
			this.dropItem(ModItems.nugget_polonium, looting);
		}

		int count = this.rand.nextInt(3) + 1;

		for(int i = 0; i < count; i++) {

			int r = this.rand.nextInt(3);

			if(r == 0) {
				this.dropItem(this.isWet() ? ModItems.waste_uranium : ModItems.rod_zirnox_uranium_fuel_depleted, this.isWet() ? 2 : 1);

			} else if(r == 1) {
				this.dropItem(this.isWet() ? ModItems.waste_mox : ModItems.rod_zirnox_mox_fuel_depleted, this.isWet() ? 2 : 1);

			} else if(r == 2) {
				this.dropItem(this.isWet() ? ModItems.waste_plutonium : ModItems.rod_zirnox_plutonium_fuel_depleted, this.isWet() ? 2 : 1);

			}
		}
	}
}
