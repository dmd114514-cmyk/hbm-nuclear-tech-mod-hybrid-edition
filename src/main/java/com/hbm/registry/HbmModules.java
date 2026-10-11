package com.hbm.registry;


/**
 * The content assembly roster.
 *
 * This is the **only** place in the project allowed to reference up into content — the assembly point has to know all the parts by definition.
 * Each content subdomain hangs its IRegistrar here; nowhere else may reach back across layers.
 */
public final class HbmModules {

	private HbmModules() { }

	/**
	 * Whenever a new content subdomain joins, hang its registration unit here.
	 *
	 * Only **core-bundled** content goes here; optional content packs go through `com.hbm.module.HbmModuleManager`
	 * (a module's preInit also queues through HbmRegistry only, so both share one registration path),
	 * don't hang modules here — that would make the core import content classes again, and the assembly roster stops being auditable round by round.
	 * The content assembly roster.
	 * the class of bug that's hardest to find, which is why the wrap-up prints the roster for each round's check.
	 */
	public static void registerAll() {
		HbmRegistry.registrar(new com.hbm.potion.HbmPotions());
		// Fluids aren't Forge registry objects; only being on the roster gets them touched by the registerFluids() step
		HbmRegistry.registrar(new FluidContent());
		// Blocks before items: the item side will want ItemBlocks sooner or later, so the blocks have to exist to point at
		HbmRegistry.registrar(new com.hbm.blocks.ModBlocks());
		// The machine vertical slice carries its own blocks and tile entities in a separate unit; no more appending to ModBlocks
		HbmRegistry.registrar(new com.hbm.blocks.machine.ModBlocksMachine());
		// The generic family (falling sand, metal deco blocks and friends) gets its own unit too
		HbmRegistry.registrar(new com.hbm.blocks.generic.ModBlocksGeneric());
		//Gases as their own unit: base material + self-floating gas blocks (the fourteen vent-family pieces wait for a later batch)
		HbmRegistry.registrar(new com.hbm.blocks.gas.ModBlocksGas());
		// The bomb family as its own unit: the five mines share one fuze TE
		HbmRegistry.registrar(new com.hbm.blocks.bomb.ModBlocksBomb());
		// Tile entity mapping: blocks are declared in other units, so TE registry names queue separately — don't miss any
		HbmRegistry.registrar(new com.hbm.tileentity.ModTileEntities());
		HbmRegistry.registrar(new com.hbm.items.ModItems());
		// Entities after items: they only read items' static fields when spawning (the armor set undead soldiers randomly wear); nothing touched while queuing
		HbmRegistry.registrar(new com.hbm.entity.ModEntities());
		// The mob family is the second registration unit outside the hot file; entity ids continue after ModEntities' 0..2
		HbmRegistry.registrar(new com.hbm.entity.mob.ModEntitiesMob());
		// The projectile family is the third: every entity on the throwable-base chain hangs here, ids continuing after mobs
		HbmRegistry.registrar(new com.hbm.entity.projectile.ModEntitiesProjectile());
		// The rolling stock family is the fourth: freight trams and trailers, ids continuing after projectiles
		HbmRegistry.registrar(new com.hbm.entity.train.ModEntitiesTrain());
		// The effect family is the fifth: lingering fire, rainbow clouds and other purely cosmetic/area-effect entities
		HbmRegistry.registrar(new com.hbm.entity.effect.ModEntitiesEffect());
		// The body data layer before the recipe tables: the tables that read bodies (Arc Furnace / Centrifuge family) pull CelestialBody entries in their own init
		HbmRegistry.registrar(new com.hbm.dim.SolarSystemContent());
		// Ore dictionary surface: most recipe inputs key on ore dict names, so this must land before the recipe tables queue
		HbmRegistry.registrar(new com.hbm.inventory.OreDictRegistration());
		// Recipe tables read from disk need items and the ore dict; after all content, and only truly complete by init
		HbmRegistry.registrar(new com.hbm.inventory.recipes.HbmRecipes());
		// Command roster: built at init; the actual hookup waits for the server (see MainRegistry.serverStarting)
		HbmRegistry.registrar(new com.hbm.commands.ModCommands());
		// Connected-texture model loaders only enter ModelLoaderRegistry during the client registerModels pass; their own unit so ClientProxy stays untouched
		HbmRegistry.registrar(new com.hbm.render.block.ct.CTModels());
		// The on-screen info overlay likewise only joins the event bus on the client pass; it's where displayTooltip lands
		HbmRegistry.registrar(new com.hbm.render.util.RenderInfoSystem());
		HbmRegistry.logAssembly();
	}
}
