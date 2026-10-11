package com.hbm.inventory.recipes.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import com.hbm.config.GeneralConfig;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutputMulti;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

/**
 * What a generic recipe looks like: solid in/out via AStack and IOutput, fluids via FluidStack,
 * plus duration, power, icon, blueprint pool and auto-grouping bolted on.
 *
 * Fields are all public and nullable — "this machine has no fluid input" is simply inputFluid being null,
 * and both read and write skip on null, so subclasses never fake values for unused slots.
 */
public class GenericRecipe {

	protected final String name;
	public String nameWrapper;
	public AStack[] inputItem;
	public FluidStack[] inputFluid;
	public IOutput[] outputItem;
	public FluidStack[] outputFluid;
	public int duration;
	public long power;
	protected ItemStack icon;
	public boolean writeIcon = false;
	public boolean customLocalization = false;
	protected String[] blueprintPools = null;
	public String autoSwitchGroup = null;

	public GenericRecipe(String name) {
		this.name = name;
	}

	public boolean isPooled() { return blueprintPools != null; }
	public String[] getPools() { return this.blueprintPools; }

	public boolean isPartOfPool(String lookingFor) {
		if(!isPooled()) return false;
		for(String pool : blueprintPools) if(pool.equals(lookingFor)) return true;
		return false;
	}

	public GenericRecipe setDuration(int duration) { this.duration = duration; return this; }
	public GenericRecipe setPower(long power) { this.power = power; return this; }
	public GenericRecipe setup(int duration, long power) { return this.setDuration(duration).setPower(power); }
	public GenericRecipe setupNamed(int duration, long power) { return this.setDuration(duration).setPower(power).setNamed(); }
	public GenericRecipe setNameWrapper(String wrapper) { this.nameWrapper = wrapper; return this; }
	public GenericRecipe setIcon(ItemStack icon) { this.icon = icon; this.writeIcon = true; return this; }
	public GenericRecipe setIcon(Item item, int meta) { return this.setIcon(new ItemStack(item, 1, meta)); }
	public GenericRecipe setIcon(Item item) { return this.setIcon(new ItemStack(item)); }
	public GenericRecipe setIcon(Block block) { return this.setIcon(new ItemStack(block)); }
	public GenericRecipe setNamed() { this.customLocalization = true; return this; }

	/** Hard-coded default recipes may not reference 528 blueprint pools; only the config file may bring those in */
	public GenericRecipe setPools(String... pools) {
		this.blueprintPools = pools;
		for(String pool : pools) {
			if(!GeneralConfig.enable528 && pool.startsWith(GenericRecipes.POOL_PREFIX_528))
				throw new IllegalArgumentException("Tried initializing a recipe's default blueprint pool with a 528 blueprint - this is not allowed.");
			GenericRecipes.addToPool(pool, this);
		}
		return this;
	}
	/** For recipe-side convenience: same as above, but 528 isn't blocked */
	public GenericRecipe setPoolsAllow528(String... pools) { this.blueprintPools = pools; for(String pool : pools) GenericRecipes.addToPool(pool, this); return this; }
	public GenericRecipe setPools528(String... pools) { if(GeneralConfig.enable528) { this.blueprintPools = pools; for(String pool : pools) GenericRecipes.addToPool(pool, this); } return this; }
	public GenericRecipe setGroup(String autoSwitch, GenericRecipes<?> set) { this.autoSwitchGroup = autoSwitch; set.addToGroup(autoSwitch, this); return this; }

	public GenericRecipe inputItems(AStack... input) { this.inputItem = input; for(AStack stack : this.inputItem) checkStackLimit(stack); return this; }
	public GenericRecipe inputItemsEx(AStack... input) { if(!GeneralConfig.enableExpensiveMode) return this; this.inputItem = input; for(AStack stack : this.inputItem) checkStackLimit(stack); return this; }
	public GenericRecipe inputFluids(FluidStack... input) { this.inputFluid = input; return this; }
	public GenericRecipe inputFluidsEx(FluidStack... input) { if(!GeneralConfig.enableExpensiveMode) return this; this.inputFluid = input; return this; }
	public GenericRecipe outputItems(IOutput... output) { this.outputItem = output; return this; }
	public GenericRecipe outputFluids(FluidStack... output) { this.outputFluid = output; return this; }

	/** A recipe whose count exceeds the item's own stack limit can never be crafted as displayed, so it's blocked at registration */
	private void checkStackLimit(AStack stack) {
		int max = 64;
		if(stack instanceof ComparableStack) {
			ComparableStack comp = (ComparableStack) stack;
			max = comp.item.getItemStackLimit(comp.toStack());
		}
		if(stack.stacksize > max) throw new IllegalArgumentException("AStack " + stack + " in " + this.name + " exceeds stack limit of " + max + "!");
	}

	public GenericRecipe outputItems(ItemStack... output) {
		this.outputItem = new IOutput[output.length];
		for(int i = 0; i < outputItem.length; i++) this.outputItem[i] = new ChanceOutput(output[i]);
		return this;
	}

	public GenericRecipe setIconToFirstIngredient() {
		if(this.inputItem != null) {
			List<ItemStack> stacks = this.inputItem[0].extractStacks();
			if(!stacks.isEmpty()) this.icon = stacks.get(0);
		}
		return this;
	}

	/** Without an explicit icon, the first output stands in; only when everything is empty does it fall to the "nothing" block */
	public ItemStack getIcon() {

		if(icon == null) {
			if(outputItem != null) {
				if(outputItem[0] instanceof ChanceOutput) icon = ((ChanceOutput) outputItem[0]).stack.copy();
				if(outputItem[0] instanceof ChanceOutputMulti) icon = ((ChanceOutputMulti) outputItem[0]).pool.get(0).stack.copy();
				return icon;
			}
			if(outputFluid != null) {
				icon = ItemFluidIcon.make(outputFluid[0]);
			}
		}

		if(icon == null) icon = new ItemStack(ModItems.nothing);
		return icon;
	}

	public String getInternalName() {
		return this.name;
	}

	public String getLocalizedName() {
		String name = null;
		if(customLocalization) name = I18nUtil.resolveKey(this.name);
		if(name == null) name = this.getIcon().getDisplayName();
		if(this.nameWrapper != null) name = I18nUtil.resolveKey(this.nameWrapper, name);
		return name;
	}

	public List<String> print() {
		List<String> list = new ArrayList<String>();

		header(list);
		autoSwitch(list);
		duration(list);
		power(list);
		input(list);
		output(list);

		return list;
	}

	protected void header(List<String> list) {
		list.add(TextFormatting.YELLOW + this.getLocalizedName());
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) list.add(TextFormatting.DARK_GRAY + "Internal: " + this.getInternalName());
	}

	protected void autoSwitch(List<String> list) {
		if(this.autoSwitchGroup != null) {
			String[] lines = I18nUtil.resolveKeyArray("autoswitch", I18nUtil.resolveKey(this.autoSwitchGroup));
			for(String line : lines) list.add(TextFormatting.GOLD + line);
		}
	}

	protected void duration(List<String> list) {
		if(duration > 0) {
			double seconds = this.duration / 20D;
			list.add(TextFormatting.RED + I18nUtil.resolveKey("gui.recipe.duration") + ": " + seconds + "s");
		}
	}

	protected void power(List<String> list) {
		if(power > 0) {
			list.add(TextFormatting.RED + I18nUtil.resolveKey("gui.recipe.consumption") + ": " + BobMathUtil.getShortNumber(power) + "HE/t");
		}
	}

	protected void input(List<String> list) {
		list.add(TextFormatting.BOLD + I18nUtil.resolveKey("gui.recipe.input") + ":");
		if(inputItem != null) for(AStack stack : inputItem) {
			ItemStack display = stack.extractForCyclingDisplay(20);
			list.add("  " + TextFormatting.GRAY + display.getCount() + "x " + display.getDisplayName());
		}
		if(inputFluid != null) for(FluidStack fluid : inputFluid)
			list.add("  " + TextFormatting.BLUE + fluid.fill + "mB " + fluid.type.getLocalizedName() + pressurePart(fluid));
	}

	protected void output(List<String> list) {
		list.add(TextFormatting.BOLD + I18nUtil.resolveKey("gui.recipe.output") + ":");
		if(outputItem != null) for(IOutput output : outputItem)
			for(String line : output.getLabel()) list.add("  " + line);
		if(outputFluid != null) for(FluidStack fluid : outputFluid)
			list.add("  " + TextFormatting.BLUE + fluid.fill + "mB " + fluid.type.getLocalizedName() + pressurePart(fluid));
	}

	private String pressurePart(FluidStack fluid) {
		if(fluid.pressure == 0) return "";
		return " " + I18nUtil.resolveKey("gui.recipe.atPressure") + " " + TextFormatting.RED + fluid.pressure + " PU";
	}

	/** Searches localized names for substrings by default; subclasses needing ingredient search extend it themselves */
	public boolean matchesSearch(String substring) {
		return getLocalizedName().toLowerCase(Locale.US).contains(substring.toLowerCase(Locale.US));
	}
}
