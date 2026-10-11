package com.hbm.entity.mob;

import api.hbm.entity.ISuffocationImmune;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * The parasite maggot: crawls the ground, bites players, doesn't drown.
 *
 * The old version only overrode findPlayerToAttack to report "the nearest non-creative player within 16 blocks"; target acquisition
 * was driven by EntityCreature's built-in targeting task calling it; no such hook here, so targeting needs explicit tasks —
 * hence adding "melee" and "find player". Range no longer rides call parameters either:
 * the search range computes from the FOLLOW_RANGE attribute, so the old 16 becomes the attribute base.
 *
 * Drops are an empty table (this bug never dropped anything), but the table still hangs: pack authors add drops by editing JSON,
 * never by touching this class.
 */
public class EntityParasiteMaggot extends EntityMob implements ISuffocationImmune {

	// Drops via table (§15): assets/hbm/loot_tables/entities/entity_parasite_maggot.json
	private static final ResourceLocation LOOT_TABLE = new ResourceLocation("hbm", "entities/entity_parasite_maggot");

	public EntityParasiteMaggot(World world) {
		super(world);
		this.setSize(0.3F, 0.7F);
		this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
	}

	/*The old version turned the body with travel through this, rather than turning with the head; renderYawOffset is the same field here */
	@Override
	public void onUpdate() {
		this.renderYawOffset = this.rotationYaw;
		super.onUpdate();
	}

	// bugs crawl, they shouldn't thump footsteps
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	// The old line let it spawn in light; isValidLightLevel is still EntityMob's protected method here
	@Override
	protected boolean isValidLightLevel() {
		return true;
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}
}
