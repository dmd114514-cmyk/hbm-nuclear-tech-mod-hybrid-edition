package com.hbm.entity.mob;

import com.hbm.blocks.generic.ModBlocksGeneric;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockMutatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.particle.helper.GibletsCreator;

import net.minecraft.world.World;

/**
 * The flesh creeper: detonating scatters itself into ground meat first, then rings the crater's rim with tumors.
 *
 * Spawned by the Laythe chunk provider in the old version (the dimension side, not this family); name follows the old table
 * (entity_mob_flesh_creeper); fuse and detonation live in {@link EntityCreeperBase},
 * only the old detonation body remains in this file.
 *
 * The meat-scatter step hand-built NBT and sent packets in the old version; both now fold into {@code GibletsCreator.composeEffect}'s
 * packing door: packet type, the 150-block view, and height-as-origin are its internals — callers pass only "giblet type" and "trim divisor".
 * The old packet never wrote the giblet-type field, reading back as default 0; passing 0 here isn't an omission.
 *
 * Drops untouched: the old version only overrode the explosion; the gunpowder share still rides vanilla's table.
 */
public class EntityCreeperFlesh extends EntityCreeperBase {

	public EntityCreeperFlesh(World world) {
		super(world);
	}

	@Override
	protected void doExplode() {

		GibletsCreator.composeEffect(this.world, this, 0, 2);

		this.setDead();

		ExplosionVNT vnt = new ExplosionVNT(this.world, this.posX, this.posY, this.posZ, this.getPowered() ? 7.0F : 4.0F, this);
		vnt.setBlockAllocator(new BlockAllocatorBulkie(30, this.getPowered() ? 16 : 8));
		vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBulkie(ModBlocksGeneric.tumor)));
		vnt.setEntityProcessor(new EntityProcessorStandard().withRangeMod(0.25F));
		vnt.setPlayerProcessor(new PlayerProcessorStandard());
		vnt.setSFX(new ExplosionEffectStandard());
		vnt.explode();
	}
}
