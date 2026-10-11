package com.hbm.entity.mob;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockMutatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * The gold creeper: what it blows open isn't a crater but a mine — the rim gets a ring of gold ore and it drops a string of gold nuggets.
 *
 * The old table also gave it a spawn egg, produced on the world side; both ends live on the item and world sides, so this wires only the entity and renderer;
 * name follows the old table (entity_mob_gold_creeper) — that's what saves recognize.
 *
 * Why detonation is no longer an override of a vanilla hook and why the fuse is counted locally: all in {@link EntityCreeperBase};
 * only the old detonation body remains in this file.
 */
public class EntityCreeperGold extends EntityCreeperBase {

	// Drops stay in code rather than tables, reasons in the dropFewItems note below; this empty table only suppresses vanilla's creeper one (gunpowder + head)
	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(RefStrings.MODID, "entities/entity_mob_gold_creeper");

	public EntityCreeperGold(World world) {
		super(world);
	}

	@Override
	protected void doExplode() {

		this.setDead();

		ExplosionVNT vnt = new ExplosionVNT(this.world, this.posX, this.posY, this.posZ, this.getPowered() ? 14.0F : 7.0F, this);
		vnt.setBlockAllocator(new BlockAllocatorBulkie(60, this.getPowered() ? 32 : 16));
		vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBulkie(Blocks.GOLD_ORE)));
		vnt.setEntityProcessor(new EntityProcessorStandard().withRangeMod(0.5F));
		vnt.setPlayerProcessor(new PlayerProcessorStandard());
		vnt.setSFX(new ExplosionEffectStandard());
		vnt.explode();
	}

	// spawns only above the nether and only in this world (the old criteria, copied)
	@Override
	public boolean getCanSpawnHere() {
		return super.getCanSpawnHere() && this.posY <= 40 && this.dimension == 0;
	}

	/**
	 * Drops stay in code, not tables: this version's table conditions are only KilledByPlayer / RandomChance / RandomChanceWithLooting
	 * and friends, with EntityOnFire as the sole entity property (decompile-verified); "killed by a player" sets the base and looting multiplies the count —
	 * none of which one table can express.
	 */
	@Override
	protected void dropFewItems(boolean hitByPlayer, int looting) {
		int amount = hitByPlayer ? 5 + this.rand.nextInt(6 + looting * 2) : 3;

		for(int i = 0; i < amount; ++i) {
			this.entityDropItem(new ItemStack(ModItems.crystal_gold), 0F);
		}
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}
}
