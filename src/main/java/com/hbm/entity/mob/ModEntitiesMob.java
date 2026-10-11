package com.hbm.entity.mob;

import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.render.entity.HbmEntityRenderers;

/**
 * The mob family's (entity/mob) registration unit.
 *
 * Its own unit rather than appending to entity/ModEntities: that one is a hot file which the line rules say only shrinks,
 * so every new family builds its own registrar and hangs it in registry/HbmModules.registerAll().
 *
 * Entity ids are **one mod-shared sequence**, not a count local to this file — ModEntities already took 0..2
 * (dummy 0, undead soldier 1, duck 2); this one continues from 3. Issued numbers never move; additions go at the end.
 * Every new registration unit states in its class note which stretch it took, so the next hand can continue at a glance.
 *
 * Phase split identical to ModEntities: preInit only queues EntityEntries (real registration flushes at the registry event,
 * the log line "registered N entities" being the checkpoint), renderers register in registerModels,
 * a client-only step.
 */
public class ModEntitiesMob implements IRegistrar {

	@Override
	public void preInit() {
		HbmRegistry.entity(HbmRegistry.entity(EntityParasiteMaggot.class, "entity_parasite_maggot", 3).tracker(80, 3, true).build());
		// The scutterfish: spawned as an aquatic mob by the Laythe biome with a spawn egg in the old version; both ends live on the world and item sides,
		// so this wires only the entity and its renderer; its issued number is 4, never moves
		HbmRegistry.entity(HbmRegistry.entity(EntityScutterfish.class, "entity_scutterfish", 4).tracker(80, 3, true).build());
		// The sifter eel: a six-form fish, spawned from eggs/deep water in the old version; both ends live on the world and item sides, so this wires only the entity and renderer
		HbmRegistry.entity(HbmRegistry.entity(EntitySifterEel.class, "entity_siftereel", 21).tracker(80, 3, true).build());
		// Two crabs (tropical/terracotta) and the deep-sea squid (seven colors): form decides texture; spawned by the Laythe biome in the old version (the dim side),
		// taking ids 22 and 23 here, never moves
		HbmRegistry.entity(HbmRegistry.entity(EntityScuttlecrab.class, "entity_scuttlecrab", 22).tracker(80, 3, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityDepthSquid.class, "entity_depthsquid", 23).tracker(80, 3, true).build());
		// The ghost: a translucent humanoid, spawned by BossSpawnHandler's structure/boss branch (handler side) in the old version; name follows the old table
		HbmRegistry.entity(HbmRegistry.entity(EntityGhost.class, "entity_ntm_ghost", 24).tracker(80, 3, true).build());
		// The RAD beast: also summoned by BossSpawnHandler's branch (on the world side); name follows the old table;
		// taking id 25 here, never moves
		HbmRegistry.entity(HbmRegistry.entity(EntityRADBeast.class, "entity_ntm_radiation_blaze", 25).tracker(80, 3, true).build());
		// The moon cow: produced by the moon's dimension/spawn side in the old version (dim belongs to another line); name follows the old table;
		// taking id 26 here, never moves
		HbmRegistry.entity(HbmRegistry.entity(EntityMoonCow.class, "entity_moon_cow", 26).tracker(80, 3, true).build());
		// The gold creeper: first of the creeper family, one renderer shared by all (render/entity/mob/RenderCreeperUniversal).
		// Spawned by the world side with a spawn egg in the old version; both ends outside this family; name follows the old table, id 27 here, never moves
		HbmRegistry.entity(HbmRegistry.entity(EntityCreeperGold.class, "entity_mob_gold_creeper", 27).tracker(80, 3, true).build());
		// The flesh creeper: continues the gold creeper's two lines, same family, same renderer; spawned by the Laythe chunk provider in the old version (dim side),
		// name follows the old table, id 28 here, never moves
		HbmRegistry.entity(HbmRegistry.entity(EntityCreeperFlesh.class, "entity_mob_flesh_creeper", 28).tracker(80, 3, true).build());
		// The pigeon: first of the flying family (walk/fly as two state bits on the IFlyingCreature port). Spawned by plains biomes with a spawn egg in the old version;
		// both ends on the world and item sides, so this wires only the entity and renderer; name follows the old table,
		// id 29 here, never moves
		HbmRegistry.entity(HbmRegistry.entity(EntityPigeon.class, "entity_pigeon", 29).tracker(80, 3, true).build());
		// The block spider (taintcrawler on the old table): the one carrying a block, fed by makeBlock from outside
		// (the pollution spread / tainted creeper side lives on the world side, not this family). Its old tracking range was the table's huge value,
		// the same problem as several family mates, so 80 by living-mob scale here; the name still follows the old table,
		// id 30 here, never moves
		HbmRegistry.entity(HbmRegistry.entity(EntityBlockSpider.class, "entity_taintcrawler", 30).tracker(80, 3, true).build());
	}

	@Override
	public void registerModels() {
		HbmEntityRenderers.registerMob();
	}
}
