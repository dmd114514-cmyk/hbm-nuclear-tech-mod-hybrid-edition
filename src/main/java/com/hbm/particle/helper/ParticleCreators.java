package com.hbm.particle.helper;

import java.util.HashMap;

import com.hbm.main.MainRegistry;

/**
 * The effect creators' dispatch table: type string → which Creator draws it.
 *
 * can be copied verbatim without waiting for an interface change on this side:
 *   if(ParticleCreators.particleCreators.containsKey(type))
 *       ParticleCreators.particleCreators.get(type).makeParticle(world, player, man, rand, x, y, z, data);
 * Outside the table, effectNT keeps its own few steps (also as before): return early when world is null,
 * TextureManager from {@code mc.getTextureManager()}, player from {@code mc.player},
 * coordinates read from data's posX/posY/posZ, type being data.getString("type").
 *
 * The first seven entries map one-to-one onto 1.7.10's ParticleCreators; the ones after are types the old version never tabled but wrote as
 * inline branches in ClientProxy.effectNT (gasfire/duoewe/duodec/duststorm/flare/giblets); since util/ParticleUtil and various entities send those types,
 * they join the same table so nothing stays "sent but invisible".
 * First load logs the registration count; one client run verifies how many actually hung — compiling tells you nothing.
 */
public class ParticleCreators {

	public static final HashMap<String, IParticleCreator> particleCreators = new HashMap<String, IParticleCreator>();

	static {
		particleCreators.put("explosionLarge", new ExplosionCreator());
		particleCreators.put("casingNT", new CasingCreator());
		particleCreators.put("flamethrower", new FlameCreator());
		particleCreators.put("explosionSmall", new ExplosionSmallCreator());
		particleCreators.put("blackPowder", new BlackPowderCreator());
		particleCreators.put("ashes", new AshesCreator());
		particleCreators.put("skeleton", new SkeletonCreator());

		// These were if-branches inside effectNT in the old version, not table entries; only in the table do they become visible
		particleCreators.put("gasfire", new GasFlameCreator());
		particleCreators.put("duoewe", new DuoDecCreator());
		particleCreators.put("duodec", new DuoDec2Creator());
		particleCreators.put("duststorm", new DustStormCreator());
		particleCreators.put("flare", new FlareCreator());

		// Giblets are also an inline branch, and this one carries the "erase the original locally on the client" step — see GibletsCreator
		particleCreators.put("giblets", new GibletsCreator());

		// Bucket B of docs/effect-types-dispatch.md: particles long ported and already being sent from main, previously dropped silently
		particleCreators.put("amat", new EffectBranchCreators.Amat());
		particleCreators.put("debug", new EffectBranchCreators.DebugText());
		particleCreators.put("casing", new EffectBranchCreators.CasingEject());
		particleCreators.put("haze", new EffectBranchCreators.Haze());
		particleCreators.put("tau", new EffectBranchCreators.Tau());
		particleCreators.put("exhaust", new EffectBranchCreators.Exhaust());
		particleCreators.put("smoke", new EffectBranchCreators.Smoke());

		// Three more from buckets C/D, sent but silently dropped: foundry is the casting spout's metal stream (particle long ported),
		// sweat is the debris living things shed, vanillaExt the long vanilla-particle mode list
		particleCreators.put("foundry", new EffectBranchCreators.Foundry());
		particleCreators.put("sweat", new EffectBranchCreators.Sweat());
		particleCreators.put("vanillaExt", new EffectBranchCreators.VanillaExt());

		// radFog is another of the old inline branches: the sender is already in place (ParticleUtil.spawnRadFog, the radiation side spreads fog per chunk),
		// and without registering it you get can send, cannot see.
		particleCreators.put("radFog", new EffectBranchCreators.RadFog());

		MainRegistry.logger.info("[hbm] effect factory registration complete, {} total: {}", particleCreators.size(), particleCreators.keySet());
	}
}
