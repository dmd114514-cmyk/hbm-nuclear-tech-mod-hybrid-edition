package com.hbm.compat.jei.category;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

/**
 * A machine's display category: one input row, one output row, slot counts set by the machine's declared maximum.
 * <br><br>
 * Programmed against {@link IRecipeWrapper} rather than any concrete wrapper because the category layer only does "arrange slots" —
 * recipe content is flattened by wrappers in {@code getIngredients}. One wrapper per machine family,
 * categories reused (each family news one up with its own uid/title/slot counts), not one category class per machine.
 * <br><br>
 * The background is a blank plate, no texture: slot positions computed from counts and spacing; no assets pulled yet, none needed for function.
 */
public class MachineRecipeCategory implements IRecipeCategory<IRecipeWrapper> {

	private static final int SLOT = 18;
	private static final int PADDING = 2;
	private static final int INFO_HEIGHT = 12;

	private final String uid;
	private final String title;
	private final int inputSlots;
	private final int outputSlots;
	private final IDrawable background;
	private final IDrawable icon;

	public MachineRecipeCategory(IGuiHelper helper, String uid, String title, int inputSlots, int outputSlots, ItemStack iconStack) {

		this.uid = uid;
		this.title = title;
		this.inputSlots = Math.max(1, inputSlots);
		this.outputSlots = Math.max(1, outputSlots);

		int columns = Math.max(this.inputSlots, this.outputSlots);
		int width = PADDING * 2 + columns * SLOT;
		int height = PADDING * 3 + SLOT * 2 + INFO_HEIGHT;

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

		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

		for(int i = 0; i < this.inputSlots; i++)
			stacks.init(i, true, PADDING + i * SLOT, PADDING);

		for(int i = 0; i < this.outputSlots; i++)
			stacks.init(this.inputSlots + i, false, PADDING + i * SLOT, PADDING * 2 + SLOT);

		/* Empty slots simply get nothing fed; JEI draws them blank; this only puts the filled ones in */
		stacks.set(ingredients);
	}
}
