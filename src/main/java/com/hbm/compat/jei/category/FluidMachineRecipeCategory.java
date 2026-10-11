package com.hbm.compat.jei.category;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

/**
 * The pure-fluid machine display category: one input row, one output row, cells slightly taller than item cells (fluids need room for the fill bar).
 * <br><br>
 * Kept separate from {@link MachineRecipeCategory} rather than merged into one four-count class: item and fluid sides differ in
 * layout API and renderer ({@code getItemStacks} vs {@code getFluidStacks}),
 * and forcing them together just piles constructor args into a chain. If a true item+fluid mixed machine ever appears, add a mixed class then.
 */
public class FluidMachineRecipeCategory implements IRecipeCategory<IRecipeWrapper> {

	private static final int SLOT = 18;
	private static final int FLUID_HEIGHT = 30;
	private static final int PADDING = 2;
	private static final int INFO_HEIGHT = 12;

	private final String uid;
	private final String title;
	private final int inputSlots;
	private final int outputSlots;
	private final IDrawable background;
	private final IDrawable icon;

	public FluidMachineRecipeCategory(IGuiHelper helper, String uid, String title, int inputSlots, int outputSlots, ItemStack iconStack) {

		this.uid = uid;
		this.title = title;
		this.inputSlots = Math.max(1, inputSlots);
		this.outputSlots = Math.max(1, outputSlots);

		int columns = Math.max(this.inputSlots, this.outputSlots);
		int width = PADDING * 2 + columns * SLOT;
		int height = PADDING * 3 + FLUID_HEIGHT * 2 + INFO_HEIGHT;

		this.background = helper.createBlankDrawable(width, height);
		this.icon = iconStack == null || iconStack.isEmpty() ? helper.getSlotDrawable() : helper.createDrawableIngredient(iconStack);
	}

	@Override
	public String getUid() {
		return this.uid;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public String getModName() {
		return com.hbm.lib.RefStrings.NAME;
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper wrapper, IIngredients ingredients) {

		IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();

		for(int i = 0; i < this.inputSlots; i++)
			fluids.init(i, true, PADDING + i * SLOT, PADDING, SLOT - 2, FLUID_HEIGHT, 1, false, null);

		for(int i = 0; i < this.outputSlots; i++)
			fluids.init(this.inputSlots + i, false, PADDING + i * SLOT, PADDING * 2 + FLUID_HEIGHT, SLOT - 2, FLUID_HEIGHT, 1, false, null);

		fluids.set(ingredients);
	}
}
