package com.hbm.hazard;

import java.util.List;

import com.hbm.hazard.type.HazardTypeNeutron;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Writes hazard readings into the item tooltip: each hazard's own line is dispatched by {@link HazardSystem#addFullTooltip},
 * and this only adds the leftover neutron activation line.
 * <br><br>
 * The leftover is written separately because it is not the same source as normal radiation: a thing can read zero right now (it does not radiate itself)
 * while the activated dose neutrons left in its NBT keeps leaking out. This line only shows when the stack's own reading is zero,
 * otherwise the two numbers pile up and the player cannot tell which one they are looking at.
 * <br><br>
 * The callback is static and registration goes through {@code ClientProxy.init}: this class is only named on the client,
 * and the server neither loads it nor shoves dose text into tooltips.
 */
public class HazardTooltipHandler {

	@SubscribeEvent
	public static void drawTooltip(ItemTooltipEvent event) {

		ItemStack stack = event.getItemStack();
		List<String> list = event.getToolTip();

		HazardSystem.addFullTooltip(stack, event.getEntityPlayer(), list);

		float activation = 0;

		if(HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.RADIATION) == 0 && stack.hasTagCompound()) {
			if(stack.getTagCompound().hasKey(HazardTypeNeutron.NEUTRON_KEY)) {
				activation += stack.getTagCompound().getFloat(HazardTypeNeutron.NEUTRON_KEY);
			}
		}

		if(activation < 1e-5)
			return;

		/* three decimals is plenty for a reading; finer just makes the tooltip longer */
		String rads = "" + (Math.floor(activation * 1000) / 1000);
		list.add(TextFormatting.GREEN + "[" + I18nUtil.resolveKey("trait.radioactive") + "]");
		list.add(TextFormatting.YELLOW + rads + "RAD/s");

		if(stack.getCount() > 1) {
			String total = "" + (Math.floor(activation * 1000 * stack.getCount()) / 1000);
			list.add(TextFormatting.YELLOW + "Stack: " + total + "RAD/s");
		}
	}
}
