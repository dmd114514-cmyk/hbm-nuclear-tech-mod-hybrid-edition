package com.hbm.items.special;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The rag. Gets wet falling into water; wring it in hand — high radiation makes it a blood rag, otherwise a urine rag.
 * One cloth, three uses, all judged on the spot.
 */
public class ItemRag extends ItemBase {

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {

		if(entityItem != null && !entityItem.world.isRemote) {

			BlockPos pos = new BlockPos(Math.floor(entityItem.posX), Math.floor(entityItem.posY), Math.floor(entityItem.posZ));

			if(entityItem.world.getBlockState(pos).getMaterial() == Material.WATER) {
				ItemStack stack = entityItem.getItem();
				entityItem.setItem(new ItemStack(ModItems.rag_damp, stack.getCount()));
				return true;
			}
		}

		return false;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);
		stack.shrink(1);

		boolean irradiated = HbmLivingProps.getRadiation(player) > 500;
		player.inventory.addItemStackToInventory(new ItemStack(irradiated ? ModItems.rag_blood : ModItems.rag_piss));

		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		for(String line : I18nUtil.resolveKeyArray("item.rag.desc")) {
			list.add(line);
		}
	}
}
