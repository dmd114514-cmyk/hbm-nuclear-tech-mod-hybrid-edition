package com.hbm.compat.jei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.hbm.compat.jei.category.FluidMachineRecipeCategory;
import com.hbm.compat.jei.category.MachineRecipeCategory;
import com.hbm.compat.jei.category.MixedMachineRecipeCategory;
import com.hbm.compat.jei.wrapper.AnnihilatorWrapper;
import com.hbm.compat.jei.wrapper.ExposureChamberWrapper;
import com.hbm.compat.jei.wrapper.FluidRecipeWrapper;
import com.hbm.compat.jei.wrapper.FuelPoolWrapper;
import com.hbm.compat.jei.wrapper.GenericRecipeWrapper;
import com.hbm.compat.jei.wrapper.MixedRecipeWrapper;
import com.hbm.compat.jei.wrapper.VacuumCircuitWrapper;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.AlkylationRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.AtmosphereRecipes;
import com.hbm.inventory.recipes.BlastFurnaceRecipe;
import com.hbm.inventory.recipes.BlastFurnaceRecipesNT;
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.inventory.recipes.CombinationRecipes;
import com.hbm.inventory.recipes.CryoRecipes;
import com.hbm.inventory.recipes.CrackingRecipes;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes;
import com.hbm.inventory.recipes.ElectrolyserMetalRecipes;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.inventory.recipes.FluidBreederRecipes;
import com.hbm.inventory.recipes.FuelPoolRecipes;
import com.hbm.inventory.recipes.HydrotreatingRecipes;
import com.hbm.inventory.recipes.LiquefactionRecipes;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.OutgasserRecipes;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes;
import com.hbm.inventory.recipes.PyroOvenRecipes;
import com.hbm.inventory.recipes.ReformingRecipes;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.VacuumCircuitRecipes;
import com.hbm.inventory.recipes.VacuumCircuitRecipes.VacuumCircuitRecipe;
import com.hbm.inventory.recipes.VacuumRefineryRecipes;
import com.hbm.inventory.recipes.SolidificationRecipes;
import com.hbm.inventory.recipes.ExposureChamberRecipes;
import com.hbm.inventory.recipes.ExposureChamberRecipes.ExposureChamberRecipe;
import com.hbm.inventory.recipes.PrecAssRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.util.i18n.I18nUtil;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

/**
 * The JEI plugin entry. JEI scans and instantiates this class itself via {@code @JEIPlugin}, so it never appears in core code,
 * and environments without JEI never reach it.
 * <br><br>
 * Each new family = one small method here (build the category) + one {@code addRecipes} line; the display layer needs no rewriting:
 * categories are generic ({@link MachineRecipeCategory}), recipe content flattened by each family's wrapper.
 * Which families are wired is just the uid constant list below; no second copy in comments (that one goes stale).
 */
@mezz.jei.api.JEIPlugin
public class HbmJeiPlugin implements IModPlugin {

	/** The generic recipe family: any machine extending GenericRecipes in this version enters this one category */
	public static final String UID_GENERIC = "hbm:generic";
	public static final String UID_EXPOSURE = "hbm:exposure_chamber";
	/** The precision assembler: also GenericRecipes, but its 9×9 grid is too far from the blast furnace's — its own panel */
	public static final String UID_PRECASS = "hbm:precass";
	public static final String UID_ANNIHILATOR = "hbm:annihilator";
	/** Pure-fluid machines: the splitter and cracker share one table shape (one oil → two oils), one wrapper class, separate uids */
	public static final String UID_FRACTIONING = "hbm:fractioning";
	public static final String UID_CRACKING = "hbm:cracking";
	public static final String UID_HYDROTREATING = "hbm:hydrotreating";
	public static final String UID_REFORMING = "hbm:reforming";
	public static final String UID_VACUUM_REFINERY = "hbm:vacuum_refinery";
	public static final String UID_ELECTROLYSER = "hbm:electrolyser";
	public static final String UID_COKER = "hbm:coker";
	public static final String UID_VACUUM_CIRCUIT = "hbm:vacuum_circuit";
	/** The alkylation unit and cryogenic fractionator: those tables are private in {@code inventory.recipes}, so this reads only their own public accessors */
	public static final String UID_ALKYLATION = "hbm:alkylation";
	public static final String UID_CRYO = "hbm:cryo_distillator";
	/** The refinery: fluid in + four fractions + one solid byproduct, hence the mixed category */
	public static final String UID_REFINERY = "hbm:refinery";
	public static final String UID_FUSION_BREEDER = "hbm:fusion_breeder";
	/** The spent fuel pool: items in, items out, hence the generic machine category */
	public static final String UID_FUEL_POOL = "hbm:fuel_pool";
	/** The solidifier: fluid in, item out, mixed category (that side has no fluid output; pass 0 slots and nothing draws) */
	public static final String UID_SOLIDIFIER = "hbm:solidifier";
	/** The pyrolysis oven and mixer: these families take solids as inputs too, using the wrapper's fourth table (items in) */
	public static final String UID_PYRO_OVEN = "hbm:pyro_oven";
	public static final String UID_MIXER = "hbm:mixer";
	/** The liquefier: items in, fluid out, mixed category again (that side has no fluid input; pass 0 slots and nothing draws) */
	public static final String UID_LIQUEFACTION = "hbm:liquefaction";
	/** The arc furnace: items in, items out; melts aren't a fluid type in this version (counted by material), so both output tiers draw on the item row */
	public static final String UID_ARC_FURNACE = "hbm:arc_furnace";
	/** The irradiation channel and dual coker: both are "solid in → solid out + fluid out", same category parameters, only the table shapes differ (one keys on AStack, the other Object) */
	public static final String UID_OUTGASSER = "hbm:outgasser";
	public static final String UID_COMBINATION = "hbm:combination";
	/**
	 * Metal electrolysis: the old version already had it as a separate page (key {@code ntmElectrolysisMetal}), sharing the electrolyzer block's name with fluid electrolysis,
	 * so a suffix distinguishes them here, or the two pages would collide in JEI's list.
	 */
	public static final String UID_ELECTROLYSER_METAL = "hbm:electrolyser_metal";
	/** The rotary furnace: multi-slot solid in + one optional fluid in → a melt counted by material */
	public static final String UID_ROTARY_FURNACE = "hbm:rotary_furnace";
	/** The welding bench: components + baseboard + solder, three solid groups in + one optional fluid in → one product */
	public static final String UID_SOLDERING = "hbm:soldering";
	/** Atmospheric chemistry: two fluids in → one fluid out (the table keys on output, reversed from the other fluid families) */
	public static final String UID_ATMOSPHERE = "hbm:atmosphere";
	/** The cyclotron: particle piece + target, two solid slots in → one product, no fluid slots on either side */
	public static final String UID_CYCLOTRON = "hbm:cyclotron";
	/** The particle accelerator: two particle slots in (order swappable) → one or two products; the old page hung under the particle detector block */
	public static final String UID_PARTICLE_ACCELERATOR = "hbm:particle_accelerator";
	/**
	 * Crucible alloys: several melts in → several melts out. The old family had three pages (smelting / alloying / casting),
	 * and this version's table keeps only the alloying tier's data, so one page; slot count computed from the widest row in the table, not hand-counted.
	 */
	public static final String UID_CRUCIBLE = "hbm:crucible";

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {

		IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();

		registration.addRecipeCategories(
				new MachineRecipeCategory(helper, UID_GENERIC, I18nUtil.resolveKey("jei.hbm.generic"),
						BlastFurnaceRecipesNT.INSTANCE.inputItemLimit(),
						BlastFurnaceRecipesNT.INSTANCE.outputItemLimit(),
						iconOf(BlastFurnaceRecipesNT.INSTANCE)),
				new MachineRecipeCategory(helper, UID_EXPOSURE, I18nUtil.resolveKey("jei.hbm.exposure_chamber"),
						2, 1,
						exposureIcon()),
				new MachineRecipeCategory(helper, UID_PRECASS, I18nUtil.resolveKey("jei.hbm.precass"),
						PrecAssRecipes.INSTANCE.inputItemLimit(),
						PrecAssRecipes.INSTANCE.outputItemLimit(),
						iconOf(PrecAssRecipes.INSTANCE)),
				new MachineRecipeCategory(helper, UID_ANNIHILATOR, I18nUtil.resolveKey("jei.hbm.annihilator"),
						1, 1,
						annihilatorIcon()),
				new FluidMachineRecipeCategory(helper, UID_FRACTIONING, I18nUtil.resolveKey("jei.hbm.fractioning"),
						1, 2,
						ItemStack.EMPTY),
				new FluidMachineRecipeCategory(helper, UID_CRACKING, I18nUtil.resolveKey("jei.hbm.cracking"),
						1, 2,
						ItemStack.EMPTY),
				new FluidMachineRecipeCategory(helper, UID_HYDROTREATING, I18nUtil.resolveKey("jei.hbm.hydrotreating"),
						2, 2,
						ItemStack.EMPTY),
				new FluidMachineRecipeCategory(helper, UID_REFORMING, I18nUtil.resolveKey("jei.hbm.reforming"),
						1, 3,
						ItemStack.EMPTY),
				new FluidMachineRecipeCategory(helper, UID_VACUUM_REFINERY, I18nUtil.resolveKey("jei.hbm.vacuum_refinery"),
						1, 4,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_ELECTROLYSER, I18nUtil.resolveKey("jei.hbm.electrolyser"),
						0, MixedRecipeWrapper.maxItemOutputs(ElectrolyserFluidRecipes.recipes), 1, 2,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_COKER, I18nUtil.resolveKey("jei.hbm.coker"),
						0, 1, 1, 1,
						ItemStack.EMPTY),
				new MachineRecipeCategory(helper, UID_VACUUM_CIRCUIT, I18nUtil.resolveKey("jei.hbm.vacuum_circuit"),
						VacuumCircuitWrapper.maxInputSlots(VacuumCircuitRecipes.recipes), 1,
						vacuumCircuitIcon()),
				new FluidMachineRecipeCategory(helper, UID_ALKYLATION, I18nUtil.resolveKey("jei.hbm.alkylation"),
						2, 2,
						ItemStack.EMPTY),
				new FluidMachineRecipeCategory(helper, UID_CRYO, I18nUtil.resolveKey("jei.hbm.cryo_distillator"),
						1, 4,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_REFINERY, I18nUtil.resolveKey("jei.hbm.refinery"),
						0, 1, 1, 4,
						ItemStack.EMPTY),
				new FluidMachineRecipeCategory(helper, UID_FUSION_BREEDER, I18nUtil.resolveKey("jei.hbm.fusion_breeder"),
						1, 1,
						ItemStack.EMPTY),
				new MachineRecipeCategory(helper, UID_FUEL_POOL, I18nUtil.resolveKey("jei.hbm.fuel_pool"),
						1, 1,
						fuelPoolIcon()),
				new MixedMachineRecipeCategory(helper, UID_SOLIDIFIER, I18nUtil.resolveKey("jei.hbm.solidifier"),
						0, 1, 1, 0,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_PYRO_OVEN, I18nUtil.resolveKey("jei.hbm.pyro_oven"),
						1, 1, 1, 1,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_MIXER, I18nUtil.resolveKey("jei.hbm.mixer"),
						1, 0, 2, 1,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_LIQUEFACTION, I18nUtil.resolveKey("jei.hbm.liquefaction"),
						1, 0, 0, 1,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_ARC_FURNACE, I18nUtil.resolveKey("jei.hbm.arc_furnace"),
						1, MixedRecipeWrapper.maxArcFurnaceOutputs(ArcFurnaceRecipes.recipeList), 0, 0,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_OUTGASSER, I18nUtil.resolveKey("jei.hbm.outgasser"),
						1, 1, 0, 1,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_COMBINATION, I18nUtil.resolveKey("jei.hbm.combination"),
						1, 1, 0, 1,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_ELECTROLYSER_METAL, I18nUtil.resolveKey("jei.hbm.electrolyser_metal"),
						1, MixedRecipeWrapper.maxMetalElectrolyserOutputs(ElectrolyserMetalRecipes.recipes), 1, 0,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_ROTARY_FURNACE, I18nUtil.resolveKey("jei.hbm.rotary_furnace"),
						MixedRecipeWrapper.maxRotaryIngredients(RotaryFurnaceRecipes.recipes), 1, 1, 0,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_SOLDERING, I18nUtil.resolveKey("jei.hbm.soldering"),
						MixedRecipeWrapper.maxSolderingIngredients(SolderingRecipes.recipes), 1, 1, 0,
						ItemStack.EMPTY),
				new FluidMachineRecipeCategory(helper, UID_ATMOSPHERE, I18nUtil.resolveKey("jei.hbm.atmosphere"),
						2, 1,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_CYCLOTRON, I18nUtil.resolveKey("jei.hbm.cyclotron"),
						2, 1, 0, 0,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_PARTICLE_ACCELERATOR, I18nUtil.resolveKey("jei.hbm.particle_accelerator"),
						2, 2, 0, 0,
						ItemStack.EMPTY),
				new MixedMachineRecipeCategory(helper, UID_CRUCIBLE, I18nUtil.resolveKey("jei.hbm.crucible"),
						MixedRecipeWrapper.maxCrucibleInputs(CrucibleRecipes.INSTANCE.recipeOrderedList),
						MixedRecipeWrapper.maxCrucibleOutputs(CrucibleRecipes.INSTANCE.recipeOrderedList), 0, 0,
						ItemStack.EMPTY));
	}

	@Override
	public void register(IModRegistry registry) {

		List<GenericRecipeWrapper> generic = new ArrayList<GenericRecipeWrapper>();

		for(BlastFurnaceRecipe recipe : BlastFurnaceRecipesNT.INSTANCE.recipeOrderedList)
			generic.add(new GenericRecipeWrapper(recipe));

		registry.addRecipes(generic, UID_GENERIC);

		List<GenericRecipeWrapper> precass = new ArrayList<GenericRecipeWrapper>();

		for(GenericRecipe recipe : PrecAssRecipes.INSTANCE.recipeOrderedList)
			precass.add(new GenericRecipeWrapper(recipe));

		registry.addRecipes(precass, UID_PRECASS);

		List<IRecipeWrapper> exposure = new ArrayList<IRecipeWrapper>();

		for(ExposureChamberRecipe recipe : ExposureChamberRecipes.recipes)
			exposure.add(new ExposureChamberWrapper(recipe));

		registry.addRecipes(exposure, UID_EXPOSURE);

		registry.addRecipes(AnnihilatorWrapper.wrapAll(), UID_ANNIHILATOR);

		registry.addRecipes(FluidRecipeWrapper.pairTable(FractionRecipes.fractions), UID_FRACTIONING);
		registry.addRecipes(FluidRecipeWrapper.pairTable(CrackingRecipes.cracking), UID_CRACKING);
		registry.addRecipes(FluidRecipeWrapper.hydrotreating(HydrotreatingRecipes.recipes), UID_HYDROTREATING);
		registry.addRecipes(FluidRecipeWrapper.reforming(ReformingRecipes.recipes), UID_REFORMING);
		registry.addRecipes(FluidRecipeWrapper.vacuumRefinery(VacuumRefineryRecipes.recipes), UID_VACUUM_REFINERY);
		registry.addRecipes(MixedRecipeWrapper.electrolyser(ElectrolyserFluidRecipes.recipes), UID_ELECTROLYSER);
		registry.addRecipes(MixedRecipeWrapper.coker(CokerRecipes.recipes), UID_COKER);

		List<IRecipeWrapper> vacuumCircuit = new ArrayList<IRecipeWrapper>();

		for(VacuumCircuitRecipe recipe : VacuumCircuitRecipes.recipes)
			vacuumCircuit.add(new VacuumCircuitWrapper(recipe));

		registry.addRecipes(vacuumCircuit, UID_VACUUM_CIRCUIT);

		registry.addRecipes(FluidRecipeWrapper.alkylation(readable(AlkylationRecipes::getOutput)), UID_ALKYLATION);
		registry.addRecipes(FluidRecipeWrapper.cryo(readable(CryoRecipes::getOutput)), UID_CRYO);
		registry.addRecipes(MixedRecipeWrapper.refinery(readable(RefineryRecipes::getRefinery)), UID_REFINERY);
		registry.addRecipes(FluidRecipeWrapper.fluidBreeder(FluidBreederRecipes.recipes), UID_FUSION_BREEDER);
		registry.addRecipes(FuelPoolWrapper.wrapAll(FuelPoolRecipes.recipes), UID_FUEL_POOL);
		registry.addRecipes(MixedRecipeWrapper.solidifier(SolidificationRecipes.recipes), UID_SOLIDIFIER);
		registry.addRecipes(MixedRecipeWrapper.pyroOven(PyroOvenRecipes.recipes), UID_PYRO_OVEN);
		registry.addRecipes(MixedRecipeWrapper.mixer(MixerRecipes.recipes), UID_MIXER);
		registry.addRecipes(MixedRecipeWrapper.liquefaction(LiquefactionRecipes.recipes), UID_LIQUEFACTION);
		registry.addRecipes(MixedRecipeWrapper.arcFurnace(ArcFurnaceRecipes.recipeList), UID_ARC_FURNACE);
		registry.addRecipes(MixedRecipeWrapper.outgasser(OutgasserRecipes.recipes), UID_OUTGASSER);
		registry.addRecipes(MixedRecipeWrapper.combination(CombinationRecipes.recipes), UID_COMBINATION);
		registry.addRecipes(MixedRecipeWrapper.electrolyserMetal(ElectrolyserMetalRecipes.recipes), UID_ELECTROLYSER_METAL);
		registry.addRecipes(MixedRecipeWrapper.rotaryFurnace(RotaryFurnaceRecipes.recipes), UID_ROTARY_FURNACE);
		registry.addRecipes(MixedRecipeWrapper.soldering(SolderingRecipes.recipes), UID_SOLDERING);
		registry.addRecipes(FluidRecipeWrapper.atmosphere(AtmosphereRecipes.getRecipesMap()), UID_ATMOSPHERE);
		registry.addRecipes(MixedRecipeWrapper.cyclotron(CyclotronRecipes.recipes), UID_CYCLOTRON);
		registry.addRecipes(MixedRecipeWrapper.particleAccelerator(ParticleAcceleratorRecipes.recipes), UID_PARTICLE_ACCELERATOR);
		registry.addRecipes(MixedRecipeWrapper.crucibleAlloying(CrucibleRecipes.INSTANCE.recipeOrderedList), UID_CRUCIBLE);
	}

	/**
	 * The reading for families whose tables are private: no visibility hacking, instead walking its own public "query recipes by type" door across all fluids.
	 * <br>{@link Fluids#getAll()} is an array only finalized after fluid registration, while JEI registration runs after loading completes
	 * (later than even mods' postInit) ⇒ what arrives here is the full set, with recipe tables already read.
	 */
	private static <T> Map<FluidType, T> readable(Function<FluidType, T> lookup) {

		Map<FluidType, T> table = new LinkedHashMap<FluidType, T>();

		for(FluidType type : Fluids.getAll()) {

			T value = lookup.apply(type);

			if(value != null)
				table.put(type, value);
		}

		return table;
	}

	/** The annihilator has no single icon: use the first displayable output as the icon, or hand JEI an empty grid when there is none */
	private static ItemStack annihilatorIcon() {

		for(IRecipeWrapper wrapper : AnnihilatorWrapper.wrapAll())
			return ((AnnihilatorWrapper) wrapper).getOutputItem();

		return ItemStack.EMPTY;
	}

	/** Category icon: the first recipe's own icon, or JEI's empty grid when the table is empty */
	private static ItemStack iconOf(GenericRecipes<?> recipes) {

		for(GenericRecipe recipe : recipes.recipeOrderedList) {
			ItemStack stack = recipe.getIcon();
			if(stack != null && !stack.isEmpty())
				return stack;
		}

		return ItemStack.EMPTY;
	}

	private static ItemStack exposureIcon() {

		for(ExposureChamberRecipe recipe : ExposureChamberRecipes.recipes)
			if(recipe.output != null && !recipe.output.isEmpty())
				return recipe.output;

		return ItemStack.EMPTY;
	}

	/** The vacuum welding bench has no dedicated icon: use the output of the first recipe that has one, else JEI's empty grid */
	private static ItemStack vacuumCircuitIcon() {

		for(VacuumCircuitRecipe recipe : VacuumCircuitRecipes.recipes)
			if(recipe != null && recipe.output != null && !recipe.output.isEmpty())
				return recipe.output;

		return ItemStack.EMPTY;
	}

	/** The cooling pool's block hasn't landed, so no machine icon: the output of the first recipe that has one stands in, else JEI's empty grid */
	private static ItemStack fuelPoolIcon() {

		for(ItemStack output : FuelPoolRecipes.recipes.values())
			if(output != null && !output.isEmpty())
				return output;

		return ItemStack.EMPTY;
	}
}
