package com.hbm.entity;

import com.hbm.entity.mob.EntityDummy;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.mob.EntityUndeadSoldier;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.render.entity.HbmEntityRenderers;

/**
 * The entity family's registration unit.
 *
 * Two things, two phases: preInit queues the EntityEntries (real registration flushes at the registry event,
 * which already logs the "registered N entities" count — one client run checks how many hung),
 * registerModels runs client-only, handing the whole family's renderers to render/entity/HbmEntityRenderers at that step (why that class is separate: its own class note).
 *
 * The id is the **roster index**, same shape as 1.7.10's `int id = 0; ... registerEntity(clazz, name, id++)`:
 * the old version checked duplicates and threw on collision, so once an order ships it never moves; additions go at the end.
 *
 * Tracking range is always explicit: without .tracker the registration unit's range is 0, and visibility is judged by min(range, view distance),
 * so 0 means this entity reaches no player at all. These three used the spawn-egg overload in the old version (color passed as range,
 * numerically huge), leaving nothing to copy; they take vanilla living's 80 blocks / 3 ticks / velocity packets instead.
 */
public class ModEntities implements IRegistrar {

	@Override
	public void preInit() {

		int id = 0;

		HbmRegistry.entity(HbmRegistry.entity(EntityDummy.class, "entity_ntm_test_dummy", id++).tracker(80, 3, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityUndeadSoldier.class, "entity_undead_soldier", id++).tracker(80, 3, true).build());
		HbmRegistry.entity(HbmRegistry.entity(EntityDuck.class, "entity_fucc_a_ducc", id++).tracker(80, 3, true).build());
	}

	@Override
	public void registerModels() {
		HbmEntityRenderers.registerBase();
	}
}
