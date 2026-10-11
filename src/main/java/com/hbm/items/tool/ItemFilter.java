package com.hbm.items.tool;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import com.hbm.main.NTMSounds;
import com.hbm.util.ArmorUtil;

import api.hbm.item.IGasMask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

/**
 * The gas mask filter: click it onto a helmet to mount it, and the swapped-out old filter goes back to your hand.
 *
 * The target helmet may be the mask itself, or "a mask slotted into a helmet's insert slot" — both have to be recognized.
 * Durability 20000 is the filter's lifespan: the real consumption is deducted as durability on the IGasMask side.
 */
public class ItemFilter extends Item {

	public ItemFilter() {
		this.setMaxDamage(20000);
		this.setCreativeTab(ModItems.consumableTab);
		this.setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);
		ItemStack helmet = player.inventory.armorInventory.get(3);

		if(helmet.isEmpty()) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

		if(!(helmet.getItem() instanceof IGasMask) && ArmorModHandler.hasMods(helmet)) {

			ItemStack[] mods = ArmorModHandler.pryMods(helmet);
			ItemStack mask = mods[ArmorModHandler.helmet_only];

			if(!mask.isEmpty()) {
				ItemStack ret = installFilterOn(mask, stack, world, player);
				ArmorModHandler.applyMod(helmet, mask);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, ret);
			}
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, installFilterOn(helmet, stack, world, player));
	}

	private ItemStack installFilterOn(ItemStack helmet, ItemStack filter, World world, EntityPlayer player) {

		if(!(helmet.getItem() instanceof IGasMask)) return filter;

		IGasMask mask = (IGasMask) helmet.getItem();

		if(!mask.isFilterApplicable(helmet, player, filter)) return filter;

		ItemStack copy = filter.copy();
		ItemStack current = ArmorUtil.getGasMaskFilter(helmet);

		if(current.isEmpty()) {
			filter.setCount(0);
		} else {
			filter = current;
		}

		ArmorUtil.installGasMaskFilter(helmet, copy);

		world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.FILTER_SCREW), SoundCategory.PLAYERS, 1.0F, 1.0F);

		return filter;
	}
}
