package com.hbm.hazard.transformer;

import java.util.List;

import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.util.Compat;

import net.minecraft.item.ItemStack;

/**
 * AE2 storage cell shielding: treat the whole cell as one container and spread out whatever is inside it.
 * <br><br>
 * Matching on class names instead of importing appeng is because this mod does not depend on AE2: no foreign class may appear at compile time,
 * and with AE2 absent at runtime no stack carries those two names anyway, so the whole block skips itself.
 * Neutron dose is counted separately: it is a different axis from radiation, the old version emitted a separate entry, and folding it into radiation would swallow the activated state.
 */
public class HazardTransformerRadiationME extends HazardTransformerBase {

	@Override
	public void transformPre(ItemStack stack, List<HazardEntry> entries) { }

	@Override
	public void transformPost(ItemStack stack, List<HazardEntry> entries) {

		String name = stack.getItem().getClass().getName();
		if(!name.equals("appeng.items.storage.ItemBasicStorageCell") && !name.equals("appeng.items.tools.powered.ToolPortableCell"))
			return;

		List<ItemStack> stacks = Compat.scrapeItemFromME(stack);
		float radiation = 0;
		float neutrons = 0;

		for(ItemStack held : stacks) {
			radiation += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.RADIATION) * held.getCount();
			neutrons += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.NEUTRON) * held.getCount();
		}

		if(radiation > 0) {
			entries.add(new HazardEntry(HazardRegistry.RADIATION, radiation));
		}

		if(neutrons > 0) {
			entries.add(new HazardEntry(HazardRegistry.NEUTRON, neutrons));
		}
	}
}
