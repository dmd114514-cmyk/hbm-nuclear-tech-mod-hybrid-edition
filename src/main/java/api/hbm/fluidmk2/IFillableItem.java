package api.hbm.fluidmk2;

import api.hbm.fluid.IFluidType;
import net.minecraft.item.ItemStack;

/** Fillable/pourable items (buckets, cans, syringes...) */
public interface IFillableItem {

	/** Whether this thing can hold that fluid; the normal flow does not need it, the UI does */
	public boolean acceptsFluid(IFluidType type, ItemStack stack);

	/** Try to fill, returns what did not fit */
	public int tryFill(IFluidType type, int amount, ItemStack stack);

	/** Whether this thing can hand that fluid out */
	public boolean providesFluid(IFluidType type, ItemStack stack);

	/** Try to pour out, capped at the requested amount */
	public int tryEmpty(IFluidType type, int amount, ItemStack stack);

	/** The currently held (first) fluid, may be null. Only used for bedrock ore sampling right now */
	public IFluidType getFirstFluidType(ItemStack stack);

	/** Current amount. Also only used for bedrock ore sampling */
	public int getFill(ItemStack stack);
}
