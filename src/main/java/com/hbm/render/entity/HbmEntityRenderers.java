package com.hbm.render.entity;

import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityCloudSolinium;
import com.hbm.entity.effect.EntityCloudTom;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.entity.effect.EntityQuasar;
import com.hbm.entity.effect.EntityRagingVortex;
import com.hbm.entity.effect.EntityVortex;
import com.hbm.entity.mob.EntityBlockSpider;
import com.hbm.entity.mob.EntityCreeperFlesh;
import com.hbm.entity.mob.EntityCreeperGold;
import com.hbm.entity.mob.EntityDepthSquid;
import com.hbm.entity.mob.EntityDummy;
import com.hbm.entity.mob.EntityGhost;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.mob.EntityMoonCow;
import com.hbm.entity.mob.EntityParasiteMaggot;
import com.hbm.entity.mob.EntityPigeon;
import com.hbm.entity.mob.EntityRADBeast;
import com.hbm.entity.mob.EntityScutterfish;
import com.hbm.entity.mob.EntityScuttlecrab;
import com.hbm.entity.mob.EntitySifterEel;
import com.hbm.entity.mob.EntityUndeadSoldier;
import com.hbm.entity.projectile.EntityCog;
import com.hbm.entity.projectile.EntityCoin;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.entity.projectile.EntitySawblade;
import com.hbm.entity.train.TrainCargoTram;
import com.hbm.entity.train.TrainCargoTramTrailer;
import com.hbm.lib.RefStrings;
import com.hbm.render.entity.effect.RenderBlackHole;
import com.hbm.render.entity.effect.RenderCloudFleija;
import com.hbm.render.entity.effect.RenderCloudRainbow;
import com.hbm.render.entity.effect.RenderCloudSolinium;
import com.hbm.render.entity.effect.RenderCloudTom;
import com.hbm.render.entity.effect.RenderEMPBlast;
import com.hbm.render.entity.effect.RenderQuasar;
import com.hbm.render.entity.item.RenderTrainCargoTram;
import com.hbm.render.entity.item.RenderTrainCargoTramTrailer;
import com.hbm.render.entity.mob.RenderBlockSpider;
import com.hbm.render.entity.mob.RenderCreeperUniversal;
import com.hbm.render.entity.mob.RenderDuck;
import com.hbm.render.entity.mob.RenderDummy;
import com.hbm.render.entity.mob.RenderEntityMulti;
import com.hbm.render.entity.mob.RenderGhost;
import com.hbm.render.entity.mob.RenderMaggot;
import com.hbm.render.entity.mob.RenderMoonCow;
import com.hbm.render.entity.mob.RenderPigeon;
import com.hbm.render.entity.mob.RenderRADBeast;
import com.hbm.render.entity.mob.RenderScutter;
import com.hbm.render.entity.mob.RenderUndeadSoldier;
import com.hbm.render.entity.projectile.RenderCog;
import com.hbm.render.entity.projectile.RenderCoin;
import com.hbm.render.entity.projectile.RenderRubble;
import com.hbm.render.entity.projectile.RenderSawblade;
import com.hbm.render.model.ModelBlockSpider;
import com.hbm.render.model.ModelDepthSquid;
import com.hbm.render.model.ModelMoonCow;
import com.hbm.render.model.ModelPigeon;
import com.hbm.render.model.ModelScuttlecrab;
import com.hbm.render.model.ModelSifterEel;
import com.hbm.util.LoggingUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

/**
 * The entity renderer registry, split by family into five methods, each called once by that family's registration unit in its registerModels() (the client-only step).
 *
 * Why the renderers can't be written inline in the registration units: a unit gets new'd as early as server preInit, and at that moment the whole class goes through
 * bytecode verification, while explicit lambdas compile into **a method of the unit class itself** whose signature carries the renderer interface's client type
 * (net.minecraft.client.renderer.entity.Render); to judge that return value the verifier must load that class, which a dedicated server doesn't have,
 * and the server start dies right on the registry/HbmModules registration line. Method references (RenderDummy::new) don't produce such a bridge, so only the lambda-carrying
 * few would crash — but the shape of the hazard is the same, and one client-typed lambda added to a unit brings it right back, so whole-family registration collects here.
 *
 * The class header may still import this class: imports don't enter bytecode, and calls into this class resolve lazily — the server never walks that far, so it never loads it.
 * What actually must be held is "the unit's own method signatures and class body contain no client types".
 *
 * Same shape as render/HbmTileRenderers — that one collects tile renderers, called by ClientProxy.init; this collects entity renderers,
 * called by each registration unit's client step, and neither end needs to stuff lines into the shared ClientProxy.
 */
public final class HbmEntityRenderers {

	private HbmEntityRenderers() { }

	/** The entity/ModEntities family: dummies, undying soldiers, ducks */
	public static void registerBase() {
		RenderingRegistry.registerEntityRenderingHandler(EntityDummy.class, RenderDummy::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityUndeadSoldier.class, RenderUndeadSoldier::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDuck.class, RenderDuck::new);
		log(3, "base family");
	}

	/** The entity/mob family: maggot, scutterfish, sifter eel, scuttle crab, depths squid, ghost, rad beast, moon cow, gold and flesh creepers, pigeon, block spider */
	public static void registerMob() {
		RenderingRegistry.registerEntityRenderingHandler(EntityParasiteMaggot.class, RenderMaggot::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityScutterfish.class, RenderScutter::new);
		// Six forms, one model; the texture is decided by the form
		RenderingRegistry.registerEntityRenderingHandler(EntitySifterEel.class, manager ->
				new RenderEntityMulti(manager, new ModelSifterEel(), EntitySifterEel.SifterEel.class, 0.5F));
		// Crab and squid likewise one model, many textures, each decided by its own form enum
		RenderingRegistry.registerEntityRenderingHandler(EntityScuttlecrab.class, manager ->
				new RenderEntityMulti(manager, new ModelScuttlecrab(), EntityScuttlecrab.Scuttlecrab.class, 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityDepthSquid.class, manager ->
				new RenderEntityMulti(manager, new ModelDepthSquid(), EntityDepthSquid.DepthSquid.class, 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityGhost.class, RenderGhost::new);
		// Rad beast: blaze skeleton + an M65 mask layer; the body goes through RenderLiving's ordinary single-texture lane
		RenderingRegistry.registerEntityRenderingHandler(EntityRADBeast.class, RenderRADBeast::new);
		// Moon cow: vanilla's cow model plus a glass visor; shadow size stays the old 0.7F
		RenderingRegistry.registerEntityRenderingHandler(EntityMoonCow.class, manager ->
				new RenderMoonCow(manager, new ModelMoonCow(), 0.7F));
		// Gold creeper: the family shares that generic renderer, each body texture its own; the charged overlay is the vanilla renderer's own this version
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperGold.class, manager ->
				new RenderCreeperUniversal(manager, new ResourceLocation(RefStrings.MODID, "textures/entity/creeper_gold.png")));
		// Flesh creeper: the same generic renderer, only the body texture swapped
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperFlesh.class, manager ->
				new RenderCreeperUniversal(manager, new ResourceLocation(RefStrings.MODID, "textures/entity/creeper_flesh.png")));
		// Pigeon: its own rig, one body texture; the landing-swing interpolation set doubles as whole-body rotation in the renderer
		RenderingRegistry.registerEntityRenderingHandler(EntityPigeon.class, manager ->
				new RenderPigeon(manager, new ModelPigeon()));
		// Block spider: legs are its own OBJ, the back block fetched and drawn live at render time from the entity's sync, and the model binds the block atlas itself
		RenderingRegistry.registerEntityRenderingHandler(EntityBlockSpider.class, RenderBlockSpider::new);
		log(12, "mob family");
	}

	/** The entity/projectile family: gear, saw blade, coin, building debris */
	public static void registerProjectile() {
		RenderingRegistry.registerEntityRenderingHandler(EntityCog.class, RenderCog::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySawblade.class, RenderSawblade::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCoin.class, RenderCoin::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityRubble.class, RenderRubble::new);
		log(4, "ballistic family");
	}

	/** The entity/train family: freight tram and trailer */
	public static void registerTrain() {
		RenderingRegistry.registerEntityRenderingHandler(TrainCargoTram.class, RenderTrainCargoTram::new);
		RenderingRegistry.registerEntityRenderingHandler(TrainCargoTramTrailer.class, RenderTrainCargoTramTrailer::new);
		log(2, "railcar family");
	}

	/** The entity/effect family: the four clouds, EMP and the black hole family; lingering fire was a renderer that drew nothing, not registered this version */
	public static void registerEffect() {
		RenderingRegistry.registerEntityRenderingHandler(EntityCloudFleijaRainbow.class, RenderCloudRainbow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCloudFleija.class, RenderCloudFleija::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCloudSolinium.class, RenderCloudSolinium::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCloudTom.class, RenderCloudTom::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityEMPBlast.class, RenderEMPBlast::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBlackHole.class, RenderBlackHole::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityVortex.class, RenderBlackHole::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityRagingVortex.class, RenderBlackHole::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityQuasar.class, RenderQuasar::new);
		log(9, "effect family");
	}

	/** Self-check: whether a family's renderers really got hung can only be said here — the server never sees these lines */
	private static void log(int count, String family) {
		LoggingUtil.info("[hbm] entity renderers registered: {} ({})", count, family);
	}
}
