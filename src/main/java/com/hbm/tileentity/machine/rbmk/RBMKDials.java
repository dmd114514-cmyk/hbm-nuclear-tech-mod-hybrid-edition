package com.hbm.tileentity.machine.rbmk;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.util.GameRuleHelper;

import com.hbm.util.Tuple;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RBMKDials {

	public enum RBMKKeys {
		KEY_SAVE_DIALS("dialSaveDials", true),
		KEY_PASSIVE_COOLING("dialPassiveCooling", 2.5),
		KEY_PASSIVE_COOLING_INNER("dialPassiveCoolingInner", 0.1),
		KEY_COLUMN_HEAT_FLOW("dialColumnHeatFlow", 0.2),
		KEY_FUEL_DIFFUSION_MOD("dialDiffusionMod", 1.0),
		KEY_HEAT_PROVISION("dialHeatProvision", 0.2),
		KEY_COLUMN_HEIGHT("dialColumnHeight", 4),
		KEY_PERMANENT_SCRAP("dialEnablePermaScrap", true),
		KEY_BOILER_HEAT_CONSUMPTION("dialBoilerHeatConsumption", 0.1),
		KEY_CONTROL_SPEED_MOD("dialControlSpeed", 1.0),
		KEY_REACTIVITY_MOD("dialReactivityMod", 1.0),
		KEY_OUTGASSER_MOD("dialOutgasserSpeedMod", 1.0),
		KEY_SURGE_MOD("dialControlSurgeMod", 1.0),
		KEY_FLUX_RANGE("dialFluxRange", 5),
		KEY_REASIM_RANGE("dialReasimRange", 10),
		KEY_REASIM_BOILERS("dialReasimBoilers", false),
		KEY_REASIM_BOILER_SPEED("dialReasimBoilerSpeed", 0.05),
		KEY_DISABLE_MELTDOWNS("dialDisableMeltdowns", false),
		KEY_ENABLE_MELTDOWN_OVERPRESSURE("dialEnableMeltdownOverpressure", false),
		KEY_MODERATOR_EFFICIENCY("dialModeratorEfficiency", 1.0),
		KEY_ABSORBER_EFFICIENCY("dialAbsorberEfficiency", 1.0),
		KEY_REFLECTOR_EFFICIENCY("dialReflectorEfficiency", 1.0),
		KEY_DISABLE_DEPLETION("dialDisableDepletion", false),
		KEY_DISABLE_XENON("dialDisableXenon", false),
		KEY_ABSORBER_HEAT_CONVERSION("dialAbsorberHeatConversion", 0.05);

		public final String keyString;
		public final Object defValue;

		RBMKKeys(String key, Object def) {
			keyString = key;
			defValue = def;
		}
	}

	public static HashMap<RBMKKeys, List<Tuple.Pair<World, Object>>> gameRules = new HashMap<>();

	public static void createDials(World world) {
		createDials(world, false);
	}

	public static void createDials(World world, boolean forceRecreate) {
		GameRules rules = world.getGameRules();

		for(RBMKKeys key : RBMKKeys.values())
			gameRules.put(key, new ArrayList<>());
		refresh(world);

		if(!rules.getBoolean(RBMKKeys.KEY_SAVE_DIALS.keyString) || forceRecreate) {
			for(RBMKKeys key : RBMKKeys.values())
				rules.setOrCreateGameRule(key.keyString, String.valueOf(key.defValue));
		}
	}


	/**
	 * Refresh all gamerules.
	 * @param world World to refresh for.
	 */
	public static void refresh(World world) {
		List<Tuple.Pair<World, Object>> toRemove = new ArrayList<>();
		for(List<Tuple.Pair<World, Object>> values : gameRules.values()) {
			for(Tuple.Pair<World, Object> rulePair : values)
				if(rulePair.key == world)
					toRemove.add(rulePair);

			for(Tuple.Pair<World, Object> pair : toRemove)
				values.remove(pair);

			toRemove.clear();
		}

		gameRules.get(RBMKKeys.KEY_PASSIVE_COOLING).add(new Tuple.Pair<>(world, GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_PASSIVE_COOLING, 0.0D)));
		gameRules.get(RBMKKeys.KEY_PASSIVE_COOLING_INNER).add(new Tuple.Pair<>(world, GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_PASSIVE_COOLING_INNER, 0.0D)));
		gameRules.get(RBMKKeys.KEY_COLUMN_HEAT_FLOW).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedDouble(world, RBMKKeys.KEY_COLUMN_HEAT_FLOW, 0.0D, 1.0D)));
		gameRules.get(RBMKKeys.KEY_FUEL_DIFFUSION_MOD).add(new Tuple.Pair<>(world, GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_FUEL_DIFFUSION_MOD, 0.0D)));
		gameRules.get(RBMKKeys.KEY_HEAT_PROVISION).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedDouble(world, RBMKKeys.KEY_HEAT_PROVISION, 0.0D, 1.0D)));
		gameRules.get(RBMKKeys.KEY_COLUMN_HEIGHT).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedInt(world, RBMKKeys.KEY_COLUMN_HEIGHT, 2, 16) - 1));
		gameRules.get(RBMKKeys.KEY_PERMANENT_SCRAP).add(new Tuple.Pair<>(world, world.getGameRules().getBoolean(RBMKKeys.KEY_PERMANENT_SCRAP.keyString)));
		gameRules.get(RBMKKeys.KEY_BOILER_HEAT_CONSUMPTION).add(new Tuple.Pair<>(world, GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_BOILER_HEAT_CONSUMPTION, 0D)));
		gameRules.get(RBMKKeys.KEY_CONTROL_SPEED_MOD).add(new Tuple.Pair<>(world, GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_CONTROL_SPEED_MOD, 0.0D)));
		gameRules.get(RBMKKeys.KEY_REACTIVITY_MOD).add(new Tuple.Pair<>(world, GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_REACTIVITY_MOD, 0.0D)));
		gameRules.get(RBMKKeys.KEY_OUTGASSER_MOD).add(new Tuple.Pair<>(world, GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_OUTGASSER_MOD, 0.0D)));
		gameRules.get(RBMKKeys.KEY_SURGE_MOD).add(new Tuple.Pair<>(world, GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_SURGE_MOD, 0.0D)));
		gameRules.get(RBMKKeys.KEY_FLUX_RANGE).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedInt(world, RBMKKeys.KEY_FLUX_RANGE, 1, 100)));
		gameRules.get(RBMKKeys.KEY_REASIM_RANGE).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedInt(world, RBMKKeys.KEY_REASIM_RANGE, 1, 100)));
		gameRules.get(RBMKKeys.KEY_REASIM_BOILERS).add(new Tuple.Pair<>(world, world.getGameRules().getBoolean(RBMKKeys.KEY_REASIM_BOILERS.keyString) || GeneralConfig.enable528ReasimBoilers));
		gameRules.get(RBMKKeys.KEY_REASIM_BOILER_SPEED).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedDouble(world, RBMKKeys.KEY_REASIM_BOILER_SPEED, 0.0D, 1.0D)));
		gameRules.get(RBMKKeys.KEY_DISABLE_MELTDOWNS).add(new Tuple.Pair<>(world, world.getGameRules().getBoolean(RBMKKeys.KEY_DISABLE_MELTDOWNS.keyString)));
		gameRules.get(RBMKKeys.KEY_ENABLE_MELTDOWN_OVERPRESSURE).add(new Tuple.Pair<>(world, world.getGameRules().getBoolean(RBMKKeys.KEY_ENABLE_MELTDOWN_OVERPRESSURE.keyString)));
		gameRules.get(RBMKKeys.KEY_MODERATOR_EFFICIENCY).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedDouble(world, RBMKKeys.KEY_MODERATOR_EFFICIENCY, 0.0D, 1.0D)));
		gameRules.get(RBMKKeys.KEY_ABSORBER_EFFICIENCY).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedDouble(world, RBMKKeys.KEY_ABSORBER_EFFICIENCY, 0.0D, 1.0D)));
		gameRules.get(RBMKKeys.KEY_ABSORBER_HEAT_CONVERSION).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedDouble(world, RBMKKeys.KEY_ABSORBER_HEAT_CONVERSION, 0.0D, 1.0D)));
		gameRules.get(RBMKKeys.KEY_REFLECTOR_EFFICIENCY).add(new Tuple.Pair<>(world, GameRuleHelper.getClampedDouble(world, RBMKKeys.KEY_REFLECTOR_EFFICIENCY, 0.0D, 1.0D)));
		gameRules.get(RBMKKeys.KEY_DISABLE_DEPLETION).add(new Tuple.Pair<>(world, world.getGameRules().getBoolean(RBMKKeys.KEY_DISABLE_DEPLETION.keyString)));
		gameRules.get(RBMKKeys.KEY_DISABLE_XENON).add(new Tuple.Pair<>(world, world.getGameRules().getBoolean(RBMKKeys.KEY_DISABLE_XENON.keyString)));
	}

	/**
	 * Gets a gamerule from the internal cache.
	 * This will not work if called on the client.
	 * @param world World to get the gamerule for.
	 * @param rule Rule to get.
	 * @return The rule in an Object.
	 */
	public static Object getGameRule(World world, RBMKKeys rule) {
		if(world.isRemote) {
			MainRegistry.logger.error("Attempted to grab cached gamerules on client side, returning default value.");
			MainRegistry.logger.error("Gamerule: {}, Default Value: {}.", rule.keyString, rule.defValue.toString());
			return rule.defValue;
		}
		return getGameRule(world, rule, false);
	}

	public static Object getGameRule(World world, RBMKKeys rule, boolean isIteration) {
		List<Tuple.Pair<World, Object>> rulesList = new ArrayList<>();

		for(Tuple.Pair<World, Object> rulePair : gameRules.get(rule)) {
			if(rulePair.key == world) {
				rulesList.add(rulePair);
			}
		}

		if(rulesList.isEmpty()) {
			if(isIteration)
				throw new NullPointerException("Cannot find gamerule for dial " + rule.keyString + " after creation.");
			else {
				world.getGameRules().setOrCreateGameRule(rule.keyString, rule.defValue.toString()); // fuck
				refresh(world);
				return getGameRule(world, rule, true);
			}
		} else if(rulesList.size() > 1)
			// what??? why???
			MainRegistry.logger.warn("Duplicate values for gamerules detected! Found {} rules for gamerule {}", rulesList.size(), rule.keyString);

		return rulesList.get(0).value; // realistically there should only be one of this gamerule that satisfies the filter sooooo...
	}

	public static double getPassiveCooling(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_PASSIVE_COOLING);
	}

	public static double getPassiveCoolingInner(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_PASSIVE_COOLING_INNER);
	}

	public static double getColumnHeatFlow(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_COLUMN_HEAT_FLOW);
	}

	public static double getFuelDiffusionMod(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_FUEL_DIFFUSION_MOD);
	}

	public static double getFuelHeatProvision(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_HEAT_PROVISION);
	}

	public static int getColumnHeight(World world) {
		return (int) getGameRule(world, RBMKKeys.KEY_COLUMN_HEIGHT);
	}

	public static boolean getPermaScrap(World world) {
		return (boolean) getGameRule(world, RBMKKeys.KEY_PERMANENT_SCRAP);
	}

	public static double getBoilerHeatConsumption(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_BOILER_HEAT_CONSUMPTION);
	}

	public static double getControlSpeed(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_CONTROL_SPEED_MOD);
	}

	public static double getReactivityMod(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_REACTIVITY_MOD);
	}

	public static double getOutgasserMod(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_OUTGASSER_MOD);
	}

	public static double getSurgeMod(World world) {
		if(world.isRemote) { // The control rods use this gamerule for RBMK diag, which happens to be calculated on the client side. whoops!
			return GameRuleHelper.getDoubleMinimum(world, RBMKKeys.KEY_PASSIVE_COOLING, 0.0D);
		}
		return (double) getGameRule(world, RBMKKeys.KEY_SURGE_MOD);
	}

	public static int getFluxRange(World world) {
		return (int) getGameRule(world, RBMKKeys.KEY_FLUX_RANGE);
	}

	public static int getReaSimRange(World world) {
		return (int) getGameRule(world, RBMKKeys.KEY_REASIM_RANGE);
	}

	public static boolean getReasimBoilers(World world) {
		return (boolean) getGameRule(world, RBMKKeys.KEY_REASIM_BOILERS);
	}

	public static double getReaSimBoilerSpeed(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_REASIM_BOILER_SPEED);
	}

	public static boolean getMeltdownsDisabled(World world) {
		return (boolean) getGameRule(world, RBMKKeys.KEY_DISABLE_MELTDOWNS);
	}

	public static boolean getOverpressure(World world) {
		return (boolean) getGameRule(world, RBMKKeys.KEY_ENABLE_MELTDOWN_OVERPRESSURE);
	}

	public static double getModeratorEfficiency(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_MODERATOR_EFFICIENCY);
	}

	public static double getAbsorberEfficiency(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_ABSORBER_EFFICIENCY);
	}

	public static double getAbsorberHeatConversion(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_ABSORBER_HEAT_CONVERSION);
	}

	public static double getReflectorEfficiency(World world) {
		return (double) getGameRule(world, RBMKKeys.KEY_REFLECTOR_EFFICIENCY);
	}

	public static boolean getDepletion(World world) {
		return !((boolean) getGameRule(world, RBMKKeys.KEY_DISABLE_DEPLETION));
	}

	public static boolean getXenon(World world) {
		return !((boolean) getGameRule(world, RBMKKeys.KEY_DISABLE_XENON));
	}
}
