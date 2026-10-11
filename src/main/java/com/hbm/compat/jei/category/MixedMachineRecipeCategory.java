package com.hbm.compat.jei.category;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

/**
 * The mixed item/fluid machine category: two item rows on top (in, out), two fluid rows below (in, out).
 * <br><br>
 * Written separately from the pure item/pure fluid categories because the three classes' layout APIs and renderers differ; merging would just pile
 * constructor args into a long chain. All three share the "slot counts declared by the machine, empty slots left blank" convention, so wiring a new machine stays fill-in-the-params.
 * Pass 0 for a side with no slots; no empty slots get drawn.
 */
public class MixedMachineRecipeCategory implements IRecipeCategory<IRecipeWrapper> {

	private static final int SLOT = 18;
	private static final int FLUID_HEIGHT = 30;
	private static final int PADDING = 2;
	private static final int INFO_HEIGHT = 12;

	private final String uid;
	private final String title;
	private final int itemInSlots;
	private final int itemOutSlots;
	private final int fluidInSlots;
	private final int fluidOutSlots;
	private final IDrawable background;
	private final IDrawable icon;
	private final int itemTop = PADDING;
	private final int fluidTop = PADDING * 4 + SLOT * 2;

	public MixedMachineRecipeCategory(IGuiHelper helper, String uid, String title,
			int itemInSlots, int itemOutSlots, int fluidInSlots, int fluidOutSlots, ItemStack iconStack) {

		this.uid = uid;
		this.title = title;
		this.itemInSlots = Math.max(0, itemInSlots);
		this.itemOutSlots = Math.max(0, itemOutSlots);
		this.fluidInSlots = Math.max(0, fluidInSlots);
		this.fluidOutSlots = Math.max(0, fluidOutSlots);

		int columns = Math.max(Math.max(this.itemInSlots, this.itemOutSlots), Math.max(this.fluidInSlots, this.fluidOutSlots));
		columns = Math.max(1, columns);

		int width = PADDING * 2 + columns * SLOT;
		int height = this.fluidTop + FLUID_HEIGHT * 2 + PADDING * 2 + INFO_HEIGHT;

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

		IGuiItemStackGroup items = recipeLayout.getItemStacks();

		for(int i = 0; i < this.itemInSlots; i++)
			items.init(i, true, PADDING + i * SLOT, this.itemTop);

		for(int i = 0; i < this.itemOutSlots; i++)
			items.init(this.itemInSlots + i, false, PADDING + i * SLOT, this.itemTop + PADDING + SLOT);

		IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();

		for(int i = 0; i < this.fluidInSlots; i++)
			fluids.init(i, true, PADDING + i * SLOT, this.fluidTop, SLOT - 2, FLUID_HEIGHT, 1, false, null);

		for(int i = 0; i < this.fluidOutSlots; i++)
			fluids.init(this.fluidInSlots + i, false, PADDING + i * SLOT, this.fluidTop + FLUID_HEIGHT + PADDING, SLOT - 2, FLUID_HEIGHT, 1, false, null);

		items.set(ingredients);
		fluids.set(ingredients);
	}
}
