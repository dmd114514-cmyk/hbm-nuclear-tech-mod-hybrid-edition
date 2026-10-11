package com.hbm.entity.projectile;

import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.render.entity.HbmEntityRenderers;

/**
 * The projectile family's (entity/projectile) registration unit.
 *
 * Its own unit rather than appending to entity/ModEntities: that one is a hot file which the line rules say only shrinks,
 * so every new family builds its own registrar and hangs it in registry/HbmModules.registerAll().
 *
 * Entity ids are one mod-shared sequence: ModEntities holds 0..2, ModEntitiesMob 3..4,
 * this one continues from 5. Issued numbers never move; additions go at the end.
 *
 * Phase split as with the other registration units: preInit only queues EntityEntries (real registration flushes at the registry event),
 * renderers register in registerModels, a client-only step.
 *
 * Tracking range follows the old EntityMappings' two lines (1000 blocks, velocity packets); not collapsed to the default —
 * the registration unit's default range is 0, which is not "whatever the old version had".
 */
public class ModEntitiesProjectile implements IRegistrar {

	@Override
	public void preInit() {
		HbmRegistry.entity(HbmRegistry.entity(EntityCog.class, "entity_stray_cog", 5).tracker(1000, 1, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntitySawblade.class, "entity_stray_saw", 6).tracker(1000, 1, true).build());
		// The coin: this family's third, taking the mod-shared sequence's next free id
		HbmRegistry.entity(HbmRegistry.entity(EntityCoin.class, "entity_coin", 15).tracker(1000, 1, true).build());
		// Building rubble: the flying speck a shattered block becomes
		HbmRegistry.entity(HbmRegistry.entity(EntityRubble.class, "entity_rubble", 16).tracker(1000, 1, true).build());
	}

	@Override
	public void registerModels() {
		HbmEntityRenderers.registerProjectile();
	}
}
