package api.hbm.item;

import java.util.ArrayList;

import api.hbm.hazard.HazardClass;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/** Gas mask. The filter decides what it blocks; the mask itself declares a blacklist of what it cannot. */
public interface IGasMask {

	/** Hazards this mask does not block (a half-mask cannot hold chlorine, say); an empty list when there is no blacklist */
	public ArrayList<HazardClass> getBlacklist(ItemStack stack, EntityLivingBase entity);

	/** The filter currently screwed on, null when there is none */
	public ItemStack getFilter(ItemStack stack, EntityLivingBase entity);

	/** Whether this filter can go on (installed or not; an installed one just gets pushed out) */
	public boolean isFilterApplicable(ItemStack stack, EntityLivingBase entity, ItemStack filter);

	/** Write the filter, does not deal with whatever was there */
	public void installFilter(ItemStack stack, EntityLivingBase entity, ItemStack filter);

	/** Use up filter durability */
	public void damageFilter(ItemStack stack, EntityLivingBase entity, int damage);
}
