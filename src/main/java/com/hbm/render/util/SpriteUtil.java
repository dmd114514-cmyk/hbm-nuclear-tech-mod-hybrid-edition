package com.hbm.render.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * The unified door for pulling sprites from the block atlas.
 *
 * The old IIcon registration regime is gone: icons no longer register through registerBlockIcons, and there are exactly two ways into the atlas —
 * being referenced by some blockstate/model JSON, or registerSprite in TextureStitchEvent.Pre (A2's
 * HbmTextureStitcher owns the custom-particle batch). Fetching always goes by the "hbm:blocks/xxx" icon-name form.
 */
public final class SpriteUtil {

	private SpriteUtil() { }

	/** Names look like "hbm:blocks/steel"; missing from the atlas you get the purple-black checker sprite, no crash */
	public static TextureAtlasSprite getSprite(String iconName) {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(iconName);
	}
}
