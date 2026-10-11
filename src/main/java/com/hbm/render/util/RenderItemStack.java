package com.hbm.render.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

/**
 * Draws an item stack in a GUI. 1.12.2's RenderItem is a singleton (Minecraft.getRenderItem()),
 * and renderItemIntoGUI no longer wants fontRenderer/renderEngine args; the item "use cooldown" zoom
 * is handled inside RenderItem; the tween here just preserves the old look.
 */
public class RenderItemStack {

	public static RenderItem getRenderItem() {
		return Minecraft.getMinecraft().getRenderItem();
	}

	public static void renderItemStack(int x, int y, float f0, ItemStack stack) {

		if(stack == null || stack.isEmpty()) return;

		RenderItem renderItem = getRenderItem();
		float f1 = stack.getAnimationsToGo() - f0;

		if(f1 > 0.0F) {
			GlStateManager.pushMatrix();
			float f2 = 1.0F + f1 / 5.0F;
			GlStateManager.translate(x + 8, y + 12, 0.0F);
			GlStateManager.scale(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
			GlStateManager.translate(-(x + 8), -(y + 12), 0.0F);
		}

		renderItem.renderItemAndEffectIntoGUI(stack, x, y);

		if(f1 > 0.0F) GlStateManager.popMatrix();

		renderItem.renderItemAndEffectIntoGUI(stack, x, y);
	}

	public static void renderItemStackNoEffect(int x, int y, float f0, ItemStack stack) {

		if(stack == null || stack.isEmpty()) return;

		RenderItem renderItem = getRenderItem();
		float f1 = stack.getAnimationsToGo() - f0;

		if(f1 > 0.0F) {
			GlStateManager.pushMatrix();
			float f2 = 1.0F + f1 / 5.0F;
			GlStateManager.translate(x + 8, y + 12, 0.0F);
			GlStateManager.scale(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
			GlStateManager.translate(-(x + 8), -(y + 12), 0.0F);
		}

		renderItem.renderItemIntoGUI(stack, x, y);

		if(f1 > 0.0F) GlStateManager.popMatrix();

		renderItem.renderItemIntoGUI(stack, x, y);
	}
}
