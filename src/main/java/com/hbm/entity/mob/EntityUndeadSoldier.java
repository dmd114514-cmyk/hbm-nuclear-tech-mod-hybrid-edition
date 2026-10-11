package com.hbm.entity.mob;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/**
 * The wasteland's undead soldier: randomly grows into a zombie or skeleton look, wearing a taurun set and carrying a random gun.
 *
 * The form is a synced field, the renderer swapping model and texture by it, hence the key is public.
 */
public class EntityUndeadSoldier extends EntityMob {

	public static final DataParameter<Byte> DW_TYPE = EntityDataManager.<Byte>createKey(EntityUndeadSoldier.class, DataSerializers.BYTE);
	public static final byte TYPE_ZOMBIE = 0;
	public static final byte TYPE_SKELETON = 1;

	// Drops via table (§15): pack authors edit the one in assets/hbm/loot_tables/entities/ to change them
	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(RefStrings.MODID, "entities/entity_ntm_undead_soldier");

	public EntityUndeadSoldier(World world) {
		super(world);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		// The old (class, 0, true) four-arg constructor has no equivalent; what remains expressible is "can it be seen", so true
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, true));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_TYPE, Byte.valueOf((byte) 0));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
	}

	// onSpawnWithEgg became onInitialSpawn (carrying a difficulty instance); the old isAIEnabled is always true here and the method is gone
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data) {
		this.addRandomArmor();
		this.getDataManager().set(DW_TYPE, this.rand.nextBoolean() ? TYPE_ZOMBIE : TYPE_SKELETON);
		return super.onInitialSpawn(difficulty, data);
	}

	/*The old one was an EntityMob hook; gone in this version, now this class's own assembly step */
	protected void addRandomArmor() {

		this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.taurun_helmet));
		this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.taurun_plate));
		this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.taurun_legs));
		this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.taurun_boots));

		int gun = this.rand.nextInt(5);
		if(gun == 0) this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_heavy_revolver));
		if(gun == 1) this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_light_revolver));
		if(gun == 2) this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_carbine));
		if(gun == 3) this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_maresleg));
		if(gun == 4) this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_greasegun));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		byte type = this.getDataManager().get(DW_TYPE);
		if(type == TYPE_ZOMBIE) return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
		if(type == TYPE_SKELETON) return SoundEvents.ENTITY_SKELETON_AMBIENT;
		return super.getAmbientSound();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		byte type = this.getDataManager().get(DW_TYPE);
		if(type == TYPE_ZOMBIE) return SoundEvents.ENTITY_ZOMBIE_HURT;
		if(type == TYPE_SKELETON) return SoundEvents.ENTITY_SKELETON_HURT;
		return super.getHurtSound(source);
	}

	@Override
	protected SoundEvent getDeathSound() {
		byte type = this.getDataManager().get(DW_TYPE);
		if(type == TYPE_ZOMBIE) return SoundEvents.ENTITY_ZOMBIE_DEATH;
		if(type == TYPE_SKELETON) return SoundEvents.ENTITY_SKELETON_DEATH;
		return super.getDeathSound();
	}

	// The old func_145780_a(x,y,z,block) is playStepSound, taking a BlockPos directly here
	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		byte type = this.getDataManager().get(DW_TYPE);
		if(type == TYPE_ZOMBIE) this.playSound(SoundEvents.ENTITY_ZOMBIE_STEP, 0.15F, 1.0F);
		if(type == TYPE_SKELETON) this.playSound(SoundEvents.ENTITY_SKELETON_STEP, 0.15F, 1.0F);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEAD;
	}

	// Not on peaceful; collision/liquid checks are already EntityLiving.getCanSpawnHere's job, no need to re-copy
	@Override
	public boolean getCanSpawnHere() {
		return super.getCanSpawnHere() && this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	/** Equipment doesn't drop (the taurun set and gun are its kit); with no table entries nothing drops, but this guard stays */
	@Override
	protected void dropEquipment(boolean player, int loot) { }
}
