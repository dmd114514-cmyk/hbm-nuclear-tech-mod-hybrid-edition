package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.main.NTMSounds;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/** The fusion core: tops off every battery of a whole powered armor suit in one go; does nothing unless you wear the full set. */
public class ItemFusionCore extends ItemBase {

	private final int charge;

	public ItemFusionCore(int charge) {
		this.charge = charge;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		// The old version checked armorInventory[2] (the chest slot); this version's NonNullList keeps the same order, so the index doesn't move
		if(!ArmorFSB.hasFSBArmorIgnoreCharge(player) || !(player.inventory.armorInventory.get(2).getItem() instanceof ArmorFSBPowered))
			return new ActionResult<>(EnumActionResult.PASS, stack);

		for(ItemStack st : player.inventory.armorInventory) {

			if(st.isEmpty())
				continue;

			if(st.getItem() instanceof IBatteryItem) {

				IBatteryItem battery = (IBatteryItem) st.getItem();
				long newcharge = Math.min(battery.getCharge(st) + this.charge, battery.getMaxCharge(st));
				battery.setCharge(st, newcharge);
			}
		}

		if(!player.capabilities.isCreativeMode)
			stack.shrink(1);

		world.playSound(player, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.SUIT_BATTERY), SoundCategory.PLAYERS, 1.0F, 1.0F);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, net.minecraft.client.util.ITooltipFlag flag) {
		list.add(TextFormatting.YELLOW + "Charges all worn armor pieces by " + BobMathUtil.getShortNumber(charge) + "HE");
		list.add("[Requires full electric set to be worn]");
	}
}
