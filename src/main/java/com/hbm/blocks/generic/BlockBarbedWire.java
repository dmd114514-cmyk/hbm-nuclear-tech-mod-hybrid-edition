package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorUtil;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Barbed wire: step in and you're snagged, losing a sliver of health; the six brothers differ only in what comes bundled with the snagging.
 *
 * The old version strung six `if(this == ModBlocks.barbed_wire_xxx)` through the collision callback; here it's one tier enum,
 * since all six share the same code — reading is just the switch. The geometry in the old version was an OBJ rotated by meta;
 * this version's block-level render dispatch chain is deleted ⇒ facing and body geometry move together to the render side's custom baked-geometry batch,
 * and this batch only owns in-world behavior, the block state keeping the existing placeholder tier.
 */
public class BlockBarbedWire extends BlockBase {

	/** The effect layered on top of snagging, tier order as the old six */
	public enum Effect {
		PLAIN, FIRE, POISON, ACID, WITHER, ULTRADEATH
	}

	private static final int DAMAGE_TICK = 20;

	private final Effect effect;

	public BlockBarbedWire(Effect effect) {
		super(Material.IRON);
		this.effect = effect;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	/** Wire doesn't block, it snags: contact stops you, but you can pass through the block itself */
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		entity.setInWeb();

		entity.motionX *= 0.15D;
		entity.motionY *= 0.1D;
		entity.motionZ *= 0.15D;

		//the old version ran this on both sides; damage and potions are server business, and a client pass just flickers health bars and bottles
		if(world.isRemote) return;

		if(this.effect == Effect.ULTRADEATH) {
			entity.attackEntityFrom(ModDamageSource.pc, 5.0F);
			if(entity instanceof EntityLivingBase)
				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(HbmPotion.radiation, 5 * DAMAGE_TICK, 9));
			return;
		}

		entity.attackEntityFrom(DamageSource.CACTUS, 2.0F);

		if(this.effect == Effect.FIRE) {
			entity.setFire(1);
		} else if(this.effect == Effect.POISON) {
			if(entity instanceof EntityLivingBase)
				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.POISON, 5 * DAMAGE_TICK, 2));
		} else if(this.effect == Effect.ACID) {
			//the acid wire eats armor durability, not health: the old version looped the player's four pieces; ArmorUtil takes a living being here,
			if(entity instanceof EntityLivingBase) {
				for(int slot = 0; slot < 4; slot++)
					ArmorUtil.damageSuit((EntityLivingBase) entity, slot, 1);
			}
		} else if(this.effect == Effect.WITHER) {
			if(entity instanceof EntityLivingBase)
				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.WITHER, 5 * DAMAGE_TICK, 4));
		}
	}
}
