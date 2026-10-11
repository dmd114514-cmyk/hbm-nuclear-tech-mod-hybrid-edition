package com.hbm.registry;

import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

/**
 * The single entry point for registration events.
 *
 * All content is merely queued by HbmRegistry first; the actual registration happens here — Forge
 * pours the queue into the official registries in one go inside each RegistryEvent.Register.
 *
 * Why centralized instead of letting each subdomain subscribe on its own: registry names, casing, chunk-companion items —
 * spread across owners, those details get written wrong sooner or later, and centralizing also guarantees Block registers before Item.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class RegistryHandler {

	private RegistryHandler() { }

	@SubscribeEvent
	public static void onBlocks(RegistryEvent.Register<Block> event) {
		HbmRegistry.flushBlocks(event.getRegistry());
	}

	@SubscribeEvent
	public static void onItems(RegistryEvent.Register<Item> event) {
		HbmRegistry.flushItems(event.getRegistry());
	}

	@SubscribeEvent
	public static void onSounds(RegistryEvent.Register<SoundEvent> event) {
		HbmRegistry.flushSounds(event.getRegistry());
	}

	@SubscribeEvent
	public static void onEntities(RegistryEvent.Register<EntityEntry> event) {
		HbmRegistry.flushEntities(event.getRegistry());
	}

	@SubscribeEvent
	public static void onPotions(RegistryEvent.Register<Potion> event) {
		HbmRegistry.flushPotions(event.getRegistry());
	}
}
