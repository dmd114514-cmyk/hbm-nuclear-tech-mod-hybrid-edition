package com.hbm.entity.mob;

import com.hbm.lib.RefStrings;
import com.hbm.sound.SoundRefs;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * A duck: walking, laying, wolf-snatching all borrowed from the chicken; only the calls and drops changed.
 */
public class EntityDuck extends EntityChicken {

	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(RefStrings.MODID, "entities/entity_fucc_a_ducc");
	private static final ResourceLocation DUCC = new ResourceLocation(RefStrings.MODID + ":entity.ducc");

	public EntityDuck(World world) {
		super(world);
	}

	// The sounds are sounds.json's two entity/ducc entries; registration order doesn't guarantee they're early, so they resolve each time
	private SoundEvent quack() {
		return SoundRefs.resolve(DUCC);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return quack();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return quack();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return quack();
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	@Override
	public EntityChicken createChild(EntityAgeable parent) {
		return new EntityDuck(this.world);
	}

	/**
	 * A duck's death announces the killer's name to the whole server — the old version did exactly this; it looks like debug leftovers but is a kept wart.
	 * The old line read the combat log's killing source; this version's same entry is getCombatTracker().getDeathMessage().
	 */
	@Override
	public void onDeath(DamageSource source) {

		if(!this.world.isRemote) {
			this.world.getMinecraftServer().getPlayerList().sendMessage(this.getCombatTracker().getDeathMessage());
		}

		super.onDeath(source);
	}
}
