package com.hbm.main;

import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The atlas registrar for custom particle textures.
 *
 * After plan C particles go through the shared buffer (ParticleManager does one begin/draw per layer),
 * so their textures must squeeze into the same block atlas as blocks, or that batch of quads can't be drawn in one pass —
 * which is exactly why sprites get registered here ahead of time.
 *
 * Client-only (@EventBusSubscriber pins CLIENT); the server never loads this class.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class HbmTextureStitcher {

	/**
	 * Custom particle textures that need the block atlas take **pure alpha** only (the ones actually on the shared buffer).
	 *
	 * Additive glow-type particles never go on the list: they draw on their own channel with bindTexture, and atlas space would be wasted
	 * （rbmk_mush / shockwave / flare / rbmk_fire / rbmk_jet_steam）。
	 * The one exception is hadron — ParticleHadron draws and binds directly while ParticleRift uses the atlas,
	 * the same texture serving both sides without conflict, so it registers as usual.
	 *
	 * Naming: the registry name is "hbm:particle/<name>" — no textures/ prefix, no .png suffix.
	 */
	public static final String[] PARTICLE_SPRITES = {
			"particle/contrail",
			"particle/debug_fluid",
			"particle/debug_power",
			"particle/explosion",
			"particle/explosion_bf",
			"particle/fog",
			"particle/hadron",
			"particle/haze",
			"particle/meat",
			"particle/metal",
			"particle/slime",
			// 1.7.10's block atlas stitched icon names under textures/blocks/, and these particle files came from that batch,
			// so registration names must match ParticleSprites.legacySprite's strings word for word, or layer-1 draws nothing but missing textures
			"blocks/particle/dead_leaf",
			"blocks/particle/particle_base",
			"blocks/particle/particlenote1",
			"blocks/particle/particlenote2",
			"blocks/particle/particle_splash",
			"blocks/particle/yelflare",
	};

	private HbmTextureStitcher() { }

	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {

		// Only the block atlas matters here; the item atlas takes no particles
		TextureMap map = event.getMap();
		if(map != Minecraft.getMinecraft().getTextureMapBlocks()) return;

		for(String sprite : PARTICLE_SPRITES) {
			map.registerSprite(new ResourceLocation(RefStrings.MODID, sprite));
		}
	}
}
