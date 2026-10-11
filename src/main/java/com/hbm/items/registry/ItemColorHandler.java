package com.hbm.items.registry;

import com.hbm.items.IColoredItem;
import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item color registration.
 *
 * Why not do it inside the registrar's `registerModels()`? Because the model step runs in mod preInit,
 * while `Minecraft.itemColors` is constructed **after** preInit — fetching it there returns null.
 * The `ItemColors` carried by `ColorHandlerEvent.Item` is the ready one.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class ItemColorHandler {

	private ItemColorHandler() { }

	@SubscribeEvent
	public static void onItemColors(ColorHandlerEvent.Item event) {

		for(final Item item : ItemModels.colored()) {
			final IColoredItem colored = (IColoredItem) item;

			event.getItemColors().registerItemColorHandler(new IItemColor() {
				@Override
				public int colorMultiplier(ItemStack stack, int tintIndex) {
					return colored.getColor(stack, tintIndex);
				}
			}, item);
		}
	}
}
