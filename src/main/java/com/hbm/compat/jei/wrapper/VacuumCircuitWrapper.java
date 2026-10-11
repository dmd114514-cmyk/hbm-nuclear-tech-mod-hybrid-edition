package com.hbm.compat.jei.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.VacuumCircuitRecipes.VacuumCircuitRecipe;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/**
 * The vacuum circuit writer's recipe display: several wafer-side passes + several baseboard-side passes → one circuit.
 * <br><br>
 * The machine validates the two input groups **separately** (the wafer slot only takes {@code wafer}, the baseboard slot only {@code pcb}),
 * but display still flattens in "wafers first, then baseboard" order — the slot order matches the GUI, so position itself carries the grouping,
 * no second notion of "group" needed.
 * <br><br>
 * Every input is an {@code AStack} (a set of matching items), so the whole candidate list goes up; any one of them counts.
 * Duration and power don't live in slots; they draw on the line below.
 */
public class VacuumCircuitWrapper implements IRecipeWrapper {

	/** The duration/power line draws below the slots */
	private static final int INFO_COLOR = 0x808080;

	private final VacuumCircuitRecipe recipe;

	public VacuumCircuitWrapper(VacuumCircuitRecipe recipe) {
		this.recipe = recipe;
	}

	public VacuumCircuitRecipe getRecipe() {
		return this.recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {

		List<List<ItemStack>> inputs = new ArrayList<List<ItemStack>>();

		addStacks(inputs, this.recipe.wafer);
		addStacks(inputs, this.recipe.pcb);

		ingredients.setInputLists(VanillaTypes.ITEM, inputs);

		List<ItemStack> outputs = new ArrayList<ItemStack>();

		if(this.recipe.output != null && !this.recipe.output.isEmpty())
			outputs.add(this.recipe.output);

		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}

	/** Slot count set by the widest row in the table; narrower recipes leave those slots empty */
	public static int maxInputSlots(List<VacuumCircuitRecipe> recipes) {

		int max = 0;

		for(VacuumCircuitRecipe recipe : recipes) {
			if(recipe == null)
				continue;

			max = Math.max(max, slotCount(recipe.wafer) + slotCount(recipe.pcb));
		}

		return Math.max(1, max);
	}

	private static int slotCount(AStack[] stacks) {
		return stacks == null ? 0 : stacks.length;
	}

	private static void addStacks(List<List<ItemStack>> inputs, AStack[] stacks) {

		if(stacks == null)
			return;

		for(AStack stack : stacks) {

			if(stack == null)
				continue;

			List<ItemStack> list = stack.extractStacks();

			if(list == null)
				continue;

			List<ItemStack> usable = new ArrayList<ItemStack>();

			for(ItemStack item : list)
				if(item != null && !item.isEmpty())
					usable.add(item);

			if(!usable.isEmpty())
				inputs.add(usable);
		}
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

		String line = this.info();

		if(line != null)
			minecraft.fontRenderer.drawString(line, 2, recipeHeight - 10, INFO_COLOR);
	}

	private String info() {

		boolean hasTime = this.recipe.duration > 0;
		boolean hasPower = this.recipe.consumption > 0;

		if(!hasTime && !hasPower)
			return null;

		String text = "";

		if(hasTime)
			text += (this.recipe.duration / 20) + "s";

		if(hasPower)
			text += (text.isEmpty() ? "" : "  ") + this.recipe.consumption + "HE/t";

		return text;
	}
}
