package com.hbm.hazard.transformer;

import java.util.List;

import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.type.HazardTypeNeutron;

import net.minecraft.item.ItemStack;

/**
 * Reads the dose straight out of the item's NBT.
 * <br><br>
 * Neither key is registered by this mod: {@code hfrHazRadiation} is a reading a foreign mod (the HFR set) writes onto items,
 * and {@code ntmNeutron} is what neutron hazard activation produced. In other words this one only takes what the thing reports about itself,
 * without asking who wrote it.
 */
public class HazardTransformerRadiationNBT extends HazardTransformerBase {

	/** The key a foreign mod uses for its radiation reading; the name is theirs, we only read */
	public static final String RAD_KEY = "hfrHazRadiation";

	@Override
	public void transformPre(ItemStack stack, List<HazardEntry> entries) { }

	@Override
	public void transformPost(ItemStack stack, List<HazardEntry> entries) {

		float level = 0;

		if(stack.hasTagCompound() && stack.getTagCompound().hasKey(RAD_KEY)) {
			level += stack.getTagCompound().getFloat(RAD_KEY);
		}
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey(HazardTypeNeutron.NEUTRON_KEY)) {
			level += stack.getTagCompound().getFloat(HazardTypeNeutron.NEUTRON_KEY);
		}

		entries.add(new HazardEntry(HazardRegistry.RADIATION, level));
	}

}
