package com.hbm.render;

import com.hbm.lib.RefStrings;
import com.hbm.util.LoggingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Where the renderer-side custom textures get stitched into the atlas.
 *
 * 1.12.2's block atlas only stitches what gets referenced: a png that no blockstate/model JSON points at,
 * and that nobody registers here, comes out as the purple-black checker. The old version relied on each block's registerBlockIcons stuffing icons into the atlas;
 * that door is gone, so the batch used only by the renderer (the switch signs and the like) has to be named here.
 *
 * Only the block atlas; the item atlas crowd is the JSON models' business, not taken here.
 * Unrelated to the particle list (custom particle textures); the two sides' names won't collide.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class HbmRenderSprites {

	/** Names relative to the textures/ directory, no .png; when fetching, they must match the renderer's string verbatim */
	private static final String[] BLOCK_SPRITES = {
			"blocks/rail_switch_sign",
			"blocks/rail_switch_sign_flipped",
	};

	private HbmRenderSprites() { }

	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {

		TextureMap map = event.getMap();
		if(map != Minecraft.getMinecraft().getTextureMapBlocks()) return;

		int registered = 0;

		for(String sprite : BLOCK_SPRITES) {
			if(map.registerSprite(new ResourceLocation(RefStrings.MODID, sprite)) != null) registered++;
		}

		// One self-check line: this batch is used only by the renderer and no JSON vouches for it, so whether registration actually happened can only be seen here
		LoggingUtil.info("[hbm] renderer-side custom textures stitched into the atlas: {}", registered);
	}
}
