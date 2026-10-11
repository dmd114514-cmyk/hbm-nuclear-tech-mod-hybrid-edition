package com.hbm.items;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 * The item-sound resolution door.
 *
 * In 1.12.2 every key in `sounds.json` registers as a `SoundEvent`, but before registration finishes (say, item init
 * already stashing events as static fields) they're unreachable. So everything goes through here: query the registry first, and build a temporary one on miss —
 * the built one is a placeholder only; after registration the same-named event resolves naturally.
 *
 * Key spelling follows one rule: **namespaced strings pass through; only bare names get `hbm:`**.
 * Blindly prefixing would turn an already-namespaced `hbm:item.syringe` into `hbm:hbm:item.syringe` —
 * no exception, no error, just permanently unresolvable: the whole sound silently dead.
 */
public final class ItemSounds {

	private ItemSounds() { }

	public static SoundEvent resolve(String name) {

		if(name.indexOf(':') < 0)
			name = "hbm:" + name;

		ResourceLocation location = new ResourceLocation(name);
		SoundEvent event = SoundEvent.REGISTRY.getObject(location);
		return event != null ? event : new SoundEvent(location);
	}

	/**
	 * Takes **vanilla** sound names.
	 *
	 * The `VANILLA_*` batch in `NTMSounds` are bare names (like `item.flintandsteel.use`) with no prefix;
	 * {@link #resolve} would pad them to `hbm:item.flintandsteel.use` and miss — they register under `minecraft`.
	 */
	public static SoundEvent resolveVanilla(String name) {
		ResourceLocation location = new ResourceLocation(name);
		SoundEvent event = SoundEvent.REGISTRY.getObject(location);
		return event != null ? event : new SoundEvent(location);
	}
}
