package com.hbm.items.special;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.lib.RefStrings;
import com.hbm.render.icon.RGBMutatorInterpolatedComponentRemap;
import com.hbm.render.icon.TextureAtlasSpriteMutatable;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Builds sprites for the {@link ItemAutogen} family: one per material, color baked into the texture pixel by pixel at load.
 *
 * Must land before atlas stitching, so it subscribes to `TextureStitchEvent.Pre`;
 * items and blocks share one atlas, so only `getTextureMapBlocks()` counts.
 *
 * The base image is a grayscale gradient, hence a remap instead of `ItemColors` flat tinting — the latter would flatten the shading away.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class ItemAutogenSprites {

	private ItemAutogenSprites() { }

	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {

		if(event.getMap() != Minecraft.getMinecraft().getTextureMapBlocks()) return;

		for(Item item : Item.REGISTRY) {

			if(!(item instanceof ItemAutogen)) continue;
			if(item.getRegistryName() == null) continue;

			ItemAutogen autogen = (ItemAutogen) item;

			for(NTMMaterial mat : Mats.orderedList) {

				if(!autogen.autogens(mat)) continue;

				String override = autogen.getOverrideTexture(mat);

				if(override != null) {
					// the override texture is a real file; register it as usual
					event.getMap().registerSprite(new ResourceLocation(override.contains(":") ? override : "hbm:items/" + override));
					continue;
				}

				String name = ItemAutogen.spriteName(autogen, mat);

				event.getMap().setTextureEntry(new TextureAtlasSpriteMutatable(name,
						new RGBMutatorInterpolatedComponentRemap(0xFFFFFF, 0x505050, mat.solidColorLight, mat.solidColorDark)));
			}
		}
	}
}
