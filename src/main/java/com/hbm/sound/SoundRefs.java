package com.hbm.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

// In 1.12.2 every key in sounds.json gets registered as a SoundEvent; if registration hasn't finished yet, a temporary one is put together to plug the gap.
// The particle side (shell casing sounds) also resolves by string on the spot, so this facade is shared — not just for the sound package
public final class SoundRefs {

	private SoundRefs() { }

	public static SoundEvent resolve(ResourceLocation location) {
		SoundEvent event = SoundEvent.REGISTRY.getObject(location);
		return event != null ? event : new SoundEvent(location);
	}
}
