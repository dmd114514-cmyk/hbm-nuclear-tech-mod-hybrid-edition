package com.hbm.inventory.recipes;

import com.hbm.crafting.SerializableRecipe;
import com.hbm.inventory.material.MatDistribution;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.registry.IRegistrar;

/**
 * The recipe tables' registration entry: lines the families up into the framework, with disk reads and template writes run by the framework at postInit.
 *
 * At init rather than preInit — the tables build ItemStacks from item fields and query the ore dict,
 * all of which needs content fully registered; at preInit it's all null.
 */
public class HbmRecipes implements IRegistrar {

	@Override
	public void init() {
		// Blueprint pools are static books; clear once before reloading so last round's entries don't stack onto this round's recipes
		GenericRecipes.clearPools();
		SerializableRecipe.addHandler(CrucibleRecipes.INSTANCE);
		// The fluid processing family: relative order follows the old recipeHandlers layout; don't shuffle it
		SerializableRecipe.addHandler(new VacuumRefineryRecipes());
		SerializableRecipe.addHandler(new FractionRecipes());
		SerializableRecipe.addHandler(new CrackingRecipes());
		SerializableRecipe.addHandler(new ReformingRecipes());
		SerializableRecipe.addHandler(new HydrotreatingRecipes());
		SerializableRecipe.addHandler(new FluidBreederRecipes());
		SerializableRecipe.addHandler(new CompressorRecipes());
		SerializableRecipe.addHandler(new ElectrolyserFluidRecipes());
		// The metal electrolysis table: right after fluid electrolysis in the old version (the compressor's table sat before it, already in place here)
		SerializableRecipe.addHandler(new ElectrolyserMetalRecipes());
		// The arc welder table: after the two electrolysis tables and before the rotary furnace in the old version (the rotary furnace hasn't landed; the relative spot holds)
		SerializableRecipe.addHandler(new ArcWelderRecipes());
		SerializableRecipe.addHandler(new AlkylationRecipes());
		SerializableRecipe.addHandler(new AtmosphereRecipes());
		// The crucible materials table: before CryoRecipes and before ArcFurnaceRecipes in the old version (the arc furnace eats materialEntries)
		SerializableRecipe.addHandler(new MatDistribution());
		SerializableRecipe.addHandler(new CryoRecipes());
		// The press/refinery/coker/breeder family (likewise in the old relative order)
		SerializableRecipe.addHandler(new PressRecipes());
		// The shredder table: after the blast furnace and before the arc welder in the old version (the non-NT blast furnace hasn't landed, so it follows the press)
		SerializableRecipe.addHandler(new ShredderRecipes());
		// The arc welder table: right after the press in the old version. It was held because it eats GeneralConfig's two LBSM switches, both in place now
		SerializableRecipe.addHandler(new SolderingRecipes());
		// The combination furnace table: between the arc welder and the refinery in the old version (the blast furnace one hasn't landed; the relative spot is claimed)
		SerializableRecipe.addHandler(new CombinationRecipes());
		// The centrifuge table: right after the combination furnace in the old version (the crystallizer's table follows it)
		SerializableRecipe.addHandler(new CentrifugeRecipes());
		// The crystallizer table: right after the centrifuge in the old version
		SerializableRecipe.addHandler(new CrystallizerRecipes());
		SerializableRecipe.addHandler(new RefineryRecipes());
		// The liquefier table: before the solidifier in the old version
		SerializableRecipe.addHandler(new LiquefactionRecipes());
		// The solidifier table: before the coker in the old version
		SerializableRecipe.addHandler(new SolidificationRecipes());
		SerializableRecipe.addHandler(new CokerRecipes());
		// The pyrolysis table: between the coker and the breeder in the old version; the bedrock roasting section is skip-noted inside the table
		SerializableRecipe.addHandler(new PyroOvenRecipes());
		SerializableRecipe.addHandler(new BreederRecipes());
		// The cyclotron table: right after the breeder in the old version (the FuelPool table is still held, see below)
		SerializableRecipe.addHandler(new CyclotronRecipes());
		// The fuel pool table: right after the cyclotron in the old version; the RBMK rod section is skip-noted inside the table
		SerializableRecipe.addHandler(new FuelPoolRecipes());
		// The mixer table: after the cooling pool in the old version; that one is still held, so it sits after Cyclotron for now
		SerializableRecipe.addHandler(new MixerRecipes());
		// The outgasser table: right after the mixer in the old version
		SerializableRecipe.addHandler(new OutgasserRecipes());
		SerializableRecipe.addHandler(new VacuumCircuitRecipes());
		SerializableRecipe.addHandler(new CustomMachineRecipes());
		// The rotary furnace / exposure chamber / particle accelerator family + the new NT blast furnace table
		SerializableRecipe.addHandler(new RotaryFurnaceRecipes());
		SerializableRecipe.addHandler(new ExposureChamberRecipes());
		SerializableRecipe.addHandler(new ParticleAcceleratorRecipes());
		// The chemical factory's table: in the old GENERIC section it sat after the crucible and before PUREX (the family's AssemblyMachine table hasn't landed), claimed before the NT blast furnace to hold the relative order
		SerializableRecipe.addHandler(ChemicalPlantRecipes.INSTANCE);
		// The PUREX extraction table: right after the chemical factory's within the old GENERIC family
		SerializableRecipe.addHandler(PUREXRecipes.INSTANCE);
		SerializableRecipe.addHandler(BlastFurnaceRecipesNT.INSTANCE);
		// The precision assembler: the blueprint booklet's producer and BrokenItem's main source
		SerializableRecipe.addHandler(PrecAssRecipes.INSTANCE);
		// The annihilator milestone table (the whole thing gates on enable528, empty by default)
		SerializableRecipe.addHandler(new AnnihilatorRecipes());
		// The arc furnace table brings up the rear: last in the old version because it eats the arcSmeltable accumulated by the ore dict surface and the two books filled by the material distribution table
		SerializableRecipe.addHandler(new ArcFurnaceRecipes());
	}
}
