package com.hbm.items.food;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.items.armor.ArmorFSB;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The moon alloy pancake: eating it fills every battery armor at once, if you can bite it —
 * only a full euphemium set (or that BJ hood) lets it pass your lips; otherwise right-click just says so.
 */
public class ItemPancake extends ItemFoodBase {

	public ItemPancake(int hunger, float saturation, boolean isDogFood) {
		super(hunger, saturation, isDogFood);
		this.setAlwaysEdible();
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {

		for(ItemStack armor : player.inventory.armorInventory) {

			if(armor.isEmpty() || !(armor.getItem() instanceof IBatteryItem))
				continue;

			IBatteryItem battery = (IBatteryItem) armor.getItem();
			battery.setCharge(armor, battery.getMaxCharge(armor));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(ArmorFSB.hasFSBArmorIgnoreCharge(player)
				&& player.getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.HEAD).getItem() == ModItems.bj_helmet)
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);

		if(!world.isRemote) {
			player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Your teeth are too soft to eat this."));
		}

		return new ActionResult<>(EnumActionResult.FAIL, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add("Can be eaten to recharge lunar cybernetic armor");
		list.add("Not for people with weak molars");
		list.add("");
		list.add("Half burnt and smells horrible");
	}
}
