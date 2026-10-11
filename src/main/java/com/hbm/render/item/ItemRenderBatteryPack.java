package com.hbm.render.item;

import com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * The battery/capacitor look: a socket model, texture swapped by tier, parts swapped by kind (batteries draw Battery, capacitors Capacitor).
 *
 * The old version stacked another translate(0,-3,0)+scale(5) on top of {@code ItemRenderBase}'s INVENTORY branch for this one;
 * this version's caller doesn't say "GUI or in hand", so it comes out in the generic pose first — tune in applyPose if it looks wrong in-game,
 * rather than growing another if inside the renderer (that only hides the "context unknown" problem under the rug).
 */
public class ItemRenderBatteryPack extends ItemRenderBase {

	private static final String MODEL = "models/machines/battery.obj";

	@Override
	protected String modelFile() {
		return MODEL;
	}

	@Override
	protected ResourceLocation texture(ItemStack stack) {
		return pack(stack).texture;
	}

	@Override
	protected void renderModel(IModelCustomNamed model, ItemStack stack) {
		model.renderPart(pack(stack).isCapacitor() ? "Capacitor" : "Battery");
	}

	/** The old version went through EnumUtil.grabEnumSafely, still unported under A2; here two lines catch the overflow themselves (a higher meta in a save won't crash) */
	private static EnumBatteryPack pack(ItemStack stack) {

		EnumBatteryPack[] all = EnumBatteryPack.values();
		int index = stack.getItemDamage();

		if(index < 0) index = 0;
		if(index >= all.length) index = all.length - 1;

		return all[index];
	}
}
