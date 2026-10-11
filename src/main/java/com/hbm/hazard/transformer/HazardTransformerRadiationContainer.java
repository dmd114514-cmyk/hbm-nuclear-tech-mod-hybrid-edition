package com.hbm.hazard.transformer;

import java.util.List;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ItemStackUtil;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Container shielding: a pile of radioactive things in a box does not equal the readings added up.
 * <br><br>
 * every container damps differently, so each branch does its own maths - a crate adds its contents up as-is (iron shells the strictest),
 * a storage box goes through {@link BobMathUtil#squirt}, and a plastic bag actually doubles it (it leaks). Any of the four slots with no reading is skipped,
 * 1.12.2 returns EMPTY rather than null for an empty slot, so the guard has to check isEmpty too.
 */
public class HazardTransformerRadiationContainer extends HazardTransformerBase {

	/** The old version used the biggest crate capacity as the bound; the extra slots read EMPTY and are skipped */
	private static final int CRATE_SLOTS = 104;

	@Override
	public void transformPre(ItemStack stack, List<HazardEntry> entries) { }

	@Override
	public void transformPost(ItemStack stack, List<HazardEntry> entries) {

		boolean isCrate = Block.getBlockFromItem(stack.getItem()) instanceof BlockStorageCrate;
		boolean isBox = stack.getItem() == ModItems.containment_box;
		boolean isBag = stack.getItem() == ModItems.plastic_bag;
		// containers going through the standard ItemInventory path; only the toolbox so far
		boolean isContainer = stack.getItem() == ModItems.toolbox;

		if(!isCrate && !isBox && !isBag && !isContainer)
			return;
		if(!stack.hasTagCompound())
			return;

		float radiation = 0;

		if(isContainer) {
			ItemStack[] items = ItemStackUtil.readStacksFromNBT(stack, 24);	// the biggest of them: the 24-slot toolbox

			if(items != null)
				for(ItemStack held : items)
					if(held != null && !held.isEmpty())
						radiation += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.RADIATION) * held.getCount();
		}

		if(isCrate) {

			for(int i = 0; i < CRATE_SLOTS; i++) {
				ItemStack held = readSlot(stack, i);

				if(held != null && !held.isEmpty()) {
					radiation += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.RADIATION) * held.getCount();
				}
			}
		}

		if(isBox) {

			ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(stack, 20);
			if(fromNBT == null)
				return;

			for(ItemStack held : fromNBT) {
				if(held != null && !held.isEmpty()) {
					radiation += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.RADIATION) * held.getCount();
				}
			}

			radiation = (float) BobMathUtil.squirt(radiation);
		}

		if(isBag) {

			ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(stack, 1);
			if(fromNBT == null)
				return;

			for(ItemStack held : fromNBT) {
				if(held != null && !held.isEmpty()) {
					radiation += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.RADIATION) * held.getCount();
				}
			}

			radiation *= 2F;
		}

		if(radiation > 0) {
			entries.add(new HazardEntry(HazardRegistry.RADIATION, radiation));
		}
	}

	/** A crate writes each of its slots as slotN; this line reads them back */
	private static ItemStack readSlot(ItemStack stack, int slot) {

		if(!stack.hasTagCompound())
			return null;

		NBTTagCompound compound = stack.getTagCompound().getCompoundTag("slot" + slot);
		if(!compound.hasKey("id"))
			return null;

		return new ItemStack(compound);
	}

}
