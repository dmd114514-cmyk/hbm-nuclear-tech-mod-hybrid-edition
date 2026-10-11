package com.hbm.items.tool;

import com.hbm.items.ModItems;

import api.hbm.block.IToolable;
import api.hbm.block.IToolable.ToolType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The screwdriver/drill/wrench family: the block says "which tool I want", and this family only reports which one it is.
 *
 * Each tool registers into its ToolType's display table at construction, so a machine GUI's "needs screwdriver" cell
 * can list every screwdriver directly — no name matching, no material checks.
 *
 * Durability 0 entries (the desh one) mean "never wears", so the damageItem step skips too,
 * or a 0-durability stack is instantly judged broken.
 */
public class ItemTooling extends ItemCraftingDegradation {

	protected ToolType type;

	public ItemTooling(ToolType type, int durability) {
		super(durability);
		this.type = type;
		this.setFull3D();
		this.setCreativeTab(ModItems.controlTab);

		type.register(new ItemStack(this));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float fX, float fY, float fZ) {

		ItemStack stack = player.getHeldItem(hand);
		Block block = world.getBlockState(pos).getBlock();

		if(block instanceof IToolable && ((IToolable) block).onScrew(world, player, pos, side, fX, fY, fZ, this.type)) {

			if(this.getMaxDamage() > 0)
				stack.damageItem(1, player);

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
