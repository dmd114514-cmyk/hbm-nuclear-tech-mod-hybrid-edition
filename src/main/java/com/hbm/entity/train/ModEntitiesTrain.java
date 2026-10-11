package com.hbm.entity.train;

import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.render.entity.HbmEntityRenderers;

/**
 * The rolling stock family's (entity/train) registration unit.
 *
 * Its own unit rather than appending to entity/ModEntities: that one is a hot file; every new family builds its own registrar
 * and hangs it in registry/HbmModules.registerAll().
 *
 * Entity ids are one mod-shared sequence: ModEntities holds 0..2, ModEntitiesMob 3..4,
 * ModEntitiesProjectile 5..6; this one continues from 7. Issued numbers never move; additions go at the end.
 *
 * Tracking ranges copy the old EntityMappings' two lines verbatim (250 blocks, no velocity packets) — not omitted,
 * because the registration unit's default range and this version's entity tracking table are different things; omitting changes behavior.
 *
 * Phase split as with the other registration units: preInit only queues EntityEntries (real registration flushes at the registry event),
 * renderers register in registerModels, a client-only step.
 */
public class ModEntitiesTrain implements IRegistrar {

	@Override
	public void preInit() {
		HbmRegistry.entity(HbmRegistry.entity(TrainCargoTram.class, "entity_ntm_cargo_tram", 7).tracker(250, 1, false).build());
		HbmRegistry.entity(HbmRegistry.entity(TrainCargoTramTrailer.class, "entity_ntm_cargo_tram_trailer", 8).tracker(250, 1, false).build());
	}

	@Override
	public void registerModels() {
		HbmEntityRenderers.registerTrain();
	}
}
