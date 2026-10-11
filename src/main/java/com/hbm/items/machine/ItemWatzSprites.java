package com.hbm.items.machine;

import com.hbm.lib.RefStrings;
import com.hbm.render.icon.RGBMutatorInterpolatedComponentRemap;
import com.hbm.render.icon.TextureAtlasSpriteMutatable;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Builds sprites for the Watz fuel pellets: one grayscale base growing 21 colorways × 2 (normal/depleted).
 *
 * Must land before atlas stitching, so it subscribes to `TextureStitchEvent.Pre`; items and blocks share one atlas,
 * so only `getTextureMapBlocks()` counts. The base carries its own gradient (the 0xD2D2D2→0x333333 stretch),
 * hence a per-pixel remap rather than an ItemColors flat tint — the latter would flatten the shading away.
 * The depleted piece runs the same color through an extra {@link ItemWatzPellet#desaturate}.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class ItemWatzSprites {

	private ItemWatzSprites() { }

	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {

		if(event.getMap() != Minecraft.getMinecraft().getTextureMapBlocks()) return;

		for(Item item : Item.REGISTRY) {

			if(!(item instanceof ItemWatzPellet)) continue;
			if(item.getRegistryName() == null) continue;

			boolean depleted = item == com.hbm.items.ModItems.watz_pellet_depleted;

			for(int i = 0; i < ItemWatzPellet.EnumWatzType.values().length; i++) {

				ItemWatzPellet.EnumWatzType type = ItemWatzPellet.EnumWatzType.values()[i];

				int light = depleted ? ItemWatzPellet.desaturate(type.colorLight) : type.colorLight;
				int dark = depleted ? ItemWatzPellet.desaturate(type.colorDark) : type.colorDark;

				event.getMap().setTextureEntry(new TextureAtlasSpriteMutatable(ItemWatzPellet.spriteName(item, i),
						new RGBMutatorInterpolatedComponentRemap(0xD2D2D2, 0x333333, light, dark)));
			}
		}
	}
}
