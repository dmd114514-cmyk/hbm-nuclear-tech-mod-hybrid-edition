package com.hbm.items.tool;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * The greatsword. Right-click does nothing — it once had a "charge up and fire a plasma beam" trick, commented out ages ago,
 * and what's left is an empty shell. Behavior stays as-is; don't get creative and add features.
 */
public class BigSword extends ItemSword {

	public BigSword(ToolMaterial mat) {
		super(mat);
	}

	@Override
	public boolean isFull3D() {
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
	}
}
