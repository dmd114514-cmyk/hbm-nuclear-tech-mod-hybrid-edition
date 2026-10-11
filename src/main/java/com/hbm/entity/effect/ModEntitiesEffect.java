package com.hbm.entity.effect;

import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.render.entity.HbmEntityRenderers;

/**
 * The registration unit for the effect family (the entity/effect package).
 *
 * Entity ids are one mod-shared sequence: ModEntities holds 0..2, ModEntitiesMob 3..4,
 * ModEntitiesProjectile 5..6, ModEntitiesTrain 7..8; this one continues from 9.
 * Issued numbers never move; additions go at the end.
 *
 * Tracking ranges follow the old EntityMappings values (lingering fire 250 with no velocity packets, rainbow cloud 1000).
 * The old version hung a draw-nothing renderer on lingering fire; the equivalent here is simply not registering it —
 * an entity without a renderer just doesn't draw in 1.12.2, so that empty shell class isn't worth porting.
 *
 * Phase split as with the other registration units: preInit only queues EntityEntries (real registration flushes at the registry event),
 * renderers register in registerModels, a client-only step.
 */
public class ModEntitiesEffect implements IRegistrar {

	@Override
	public void preInit() {
		HbmRegistry.entity(HbmRegistry.entity(EntityFireLingering.class, "entity_fire_lingering", 9).tracker(250, 1, false).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityCloudFleijaRainbow.class, "entity_cloud_rainbow", 10).tracker(1000, 1, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityCloudFleija.class, "entity_cloud_fleija", 11).tracker(500, 1, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityEMPBlast.class, "entity_emp_blast", 12).tracker(1000, 1, true).build());
		// Solinium cloud: the old version shared the name "entity_cloud_rainbow" with the rainbow cloud (1.7 didn't dedupe); this registry throws on duplicates, hence the rename
		HbmRegistry.entity(HbmRegistry.entity(EntityCloudSolinium.class, "entity_cloud_solinium", 13).tracker(1000, 1, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityCloudTom.class, "entity_moonstone_blast", 14).tracker(1000, 1, true).build());
		// The black hole family: the parent and two vortices; renderers hang together in HbmEntityRenderers.registerEffect()
		HbmRegistry.entity(HbmRegistry.entity(EntityBlackHole.class, "entity_black_hole", 17).tracker(250, 1, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityVortex.class, "entity_vortex", 18).tracker(250, 1, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityRagingVortex.class, "entity_raging_vortex", 19).tracker(250, 1, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityQuasar.class, "entity_digamma_quasar", 20).tracker(250, 1, true).build());
	}

	@Override
	public void registerModels() {
		HbmEntityRenderers.registerEffect();
	}
}
