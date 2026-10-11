package com.hbm.entity.mob;

import com.hbm.entity.mob.ai.EntityMoonWalkHelper;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import api.hbm.entity.IRadiationImmune;
import api.hbm.entity.ISuffocationImmune;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * The moon cow: a masked cow, the head glass drawn by the model, walking swapped for the "head down, lumber forward" helper.
 *
 * Four deltas:
 * - {@code moveHelper} is still EntityLiving's protected field, overridden directly as before.
 * - {@code createChild}: {@code EntityCow} overrides it itself here (producing normal cows), kept so cows birth moon cows.
 * - drop counts scale with weapon looting (both old chains were "random 0~2 plus 0~looting"), which this version's tables can't do:
 *   table random counts come only in uniform and binomial shapes, neither reading looting, and the only entity condition is "is it on fire"
 *   (decompile-verified). So both chains stay in code, registered as hard-coded-drop exceptions — same reasoning as the RAD beast.
 * - but the table still points at this mod's **empty** one: this cow inherits vanilla's (leather + beef), and without suppressing it the code chains would double-drop.
 *   Pack authors change this cow's drops by editing {@code loot_tables/entities/entity_moon_cow.json} and adding pools,
 *   at which point the two code chains come off too.
 *
 * The old {@code getDropItem()} cheese line didn't come over: that hook is consulted only inside {@code EntityLiving.dropFewItems}'s default
 * here, and the whole method below overrides it without calling super — no effect point.
 */
public class EntityMoonCow extends EntityCow implements IRadiationImmune, ISuffocationImmune {

	// Empty table: only to suppress vanilla's cow one (leather + beef); the real drops are the section below
	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(RefStrings.MODID, "entities/entity_moon_cow");

	public EntityMoonCow(World world) {
		super(world);
		this.moveHelper = new EntityMoonWalkHelper(this);
	}

	@Override
	public EntityMoonCow createChild(EntityAgeable parent) {
		return new EntityMoonCow(this.world);
	}

	/** The moo comes out muffled through the mask */
	@Override
	protected float getSoundVolume() {
		return 0.1F;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	@Override
	protected void dropFewItems(boolean hitByPlayer, int looting) {

		int j = this.rand.nextInt(3) + this.rand.nextInt(1 + looting);

		for(int k = 0; k < j; ++k) {
			this.dropItem(Items.LEATHER, 1);
		}

		j = this.rand.nextInt(3) + 1 + this.rand.nextInt(1 + looting);

		for(int k = 0; k < j; ++k) {
			this.dropItem(ModItems.cheese, 1);
		}
	}
}
