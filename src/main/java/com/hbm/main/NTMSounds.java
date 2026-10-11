package com.hbm.main;

import com.hbm.interfaces.NotableComments;

/**
 * Sound names live here, so nobody has to memorize them or flip through tables.
 *
 * Values are always **registry-name strings**, never SoundEvent objects: this mod's sounds register from assets/hbm/sounds.json during resource
 * loading, fetched on the spot via {@code SoundRefs.resolve(new ResourceLocation(name))}, so the constants owe nothing to registration timing.
 *
 * All values lowercase, and keys in sounds.json plus the ogg filenames must match that shape: lookups fold names to lowercase,
 * and a sound only truly plays when code string = key = path = filename agree all four ways.
 *
 * The twelve vanilla names all changed in this version (checked against the strings actually registered in SoundEvents' static init in the jar);
 * not one old name survives — copying the 1.7 strings verbatim yields silence. Those twelve are bare vanilla names without a domain,
 * so look them up under the vanilla domain, don't stitch them under hbm.
 */
@NotableComments
public class NTMSounds {

	/// chambering / ejecting ///
	public static final String GUN_REVOLVER_COCK = "hbm:weapon.reload.revolvercock";
	public static final String GUN_REVOLVER_CLOSE = "hbm:weapon.reload.revolverclose";
	public static final String GUN_REVOLVER_SPIN = "hbm:weapon.reload.revolverspin";
	public static final String GUN_PISTOL_COCK = "hbm:weapon.reload.pistolcock";
	public static final String GUN_MAG_SMALL_REMOVE = "hbm:weapon.reload.magsmallremove";
	public static final String GUN_MAG_SMALL_INSERT = "hbm:weapon.reload.magsmallinsert";
	public static final String GUN_MAG_REMOVE = "hbm:weapon.reload.magremove";
	public static final String GUN_MAG_INSERT = "hbm:weapon.reload.maginsert";
	public static final String GUN_CANISTER_INSERT = "hbm:weapon.reload.insertcanister";
	public static final String GUN_ROCKET_INSERT = "hbm:weapon.reload.insertrocket";
	public static final String GUN_BOLT_OPEN = "hbm:weapon.reload.boltopen";
	public static final String GUN_BOLT_CLOSE = "hbm:weapon.reload.boltclose";
	public static final String GUN_RIFLE_COCK = "hbm:weapon.reload.riflecock";
	public static final String GUN_LEVER_COCK = "hbm:weapon.reload.levercock";
	public static final String GUN_SHOTGUN_LOAD = "hbm:weapon.reload.shotgunreload";
	public static final String GUN_SHOTGUN_OPEN = "hbm:weapon.reload.shotguncockopen";
	public static final String GUN_SHOTGUN_CLOSE = "hbm:weapon.reload.shotguncockclose";
	public static final String GUN_SHOTGUN_COCK = "hbm:weapon.reload.shotguncock";
	public static final String GUN_GRENADE_RELOAD = "hbm:weapon.glreload";
	public static final String GUN_GRENADE_OPEN = "hbm:weapon.glopen";
	public static final String GUN_GRENADE_CLOSE = "hbm:weapon.glclose";
	public static final String GUN_SCREW = "hbm:weapon.reload.screw";
	public static final String GUN_COIL_RELOAD = "hbm:weapon.coilgunreload";
	public static final String GUN_IMPACT = "hbm:weapon.reload.impact"; // knocking on the weapon, or a magazine hitting the ground
	public static final String GUN_LATCH_OPEN = "hbm:weapon.reload.openlatch";
	public static final String GUN_VALVE = "hbm:weapon.reload.pressurevalve";
	public static final String GUN_FATMAN_RELOAD = "hbm:weapon.reload.fatmanfull";
	public static final String GRENADE_TECH = "hbm:weapon.reload.grenadetech";
	public static final String GRENADE_NUKA = "hbm:weapon.reload.grenadenuka";

	/// clunks ///
	public static final String GUN_WHACK = "hbm:weapon.foley.gunwhack";

	/// gunfire ///
	public static final String GUN_LOCKON = "hbm:weapon.fire.lockon";
	public static final String GUN_SMACK = "hbm:weapon.fire.smack"; // hitting people with the butt of the gun
	public static final String GUN_STAB_A_FUCKER = "hbm:weapon.fire.stab"; // bayonet to the ribs
	public static final String GUN_SHREDDER_CYCLE = "hbm:weapon.fire.shreddercycle";
	public static final String GUN_DRY_FIRE = "hbm:weapon.reload.dryfireclick";
	public static final String GUN_POWDER_FIRE = "hbm:weapon.fire.blackpowder";
	public static final String GUN_RIFLE_FIRE = "hbm:weapon.fire.rifle";
	public static final String GUN_RIFLE_SILENCER = "hbm:weapon.fire.silenced";
	public static final String GUN_HEAVY_RIFLE_FIRE = "hbm:weapon.fire.rifleheavy";
	public static final String GUN_ASSAULT_FIRE = "hbm:weapon.fire.assault";
	public static final String GUN_HEAVY_REVOLVER_FIRE = "hbm:weapon.44shoot";
	public static final String GUN_AMAT_FIRE = "hbm:weapon.fire.amat";
	public static final String GUN_AMAT_SILENCER = "hbm:weapon.silencershoot";
	public static final String GUN_SHOTGUN_FIRE = "hbm:weapon.fire.shotgun";
	public static final String GUN_SPAS_FIRE = "hbm:weapon.shotgunshoot";
	public static final String GUN_LIBERATOR_FIRE = "hbm:weapon.fire.shotgunalt";
	public static final String GUN_SHREDDER_FIRE = "hbm:weapon.fire.shotgunauto";
	public static final String GUN_GREASEGUN_FIRE = "hbm:weapon.fire.greasegun";
	public static final String GUN_STARF_FIRE = "hbm:weapon.fire.pistollight";
	public static final String GUN_PISTOL_FIRE = "hbm:weapon.fire.pistol";
	public static final String GUN_UZI_FIRE = "hbm:weapon.fire.uzi";
	public static final String GUN_ABERRATOR_FIRE = "hbm:weapon.fire.aberrator";
	public static final String GUN_UNDERBARREL_FIRE = "hbm:weapon.hkshoot";
	public static final String GUN_CONGO_FIRE = "hbm:weapon.glshoot";
	public static final String GUN_CHARGE_FIRE = "hbm:weapon.fire.grenade";
	public static final String GUN_FLAMER_LOOP = "hbm:weapon.fire.flameloop";
	public static final String GUN_FATMAN_FIRE = "hbm:weapon.fire.fatman";
	public static final String GUN_MINIGUN_FIRE = "hbm:weapon.calshoot";
	public static final String GUN_LASER_GATLING_FIRE = "hbm:weapon.fire.lasergatling";
	public static final String GUN_LASER_PISTOL_FIRE = "hbm:weapon.fire.laserpistol";
	public static final String GUN_LASER_RIFLE_FIRE = "hbm:weapon.fire.laser";
	public static final String GUN_COIL_FIRE = "hbm:weapon.coilgunshoot";
	public static final String GUN_TAU_FIRE = "hbm:weapon.fire.tau";
	public static final String GUN_TAU_STOPFIRE = "hbm:weapon.fire.taurelease"; // the electric crackle when the tau cannon stops firing
	public static final String GUN_TAU_LOOP = "hbm:weapon.fire.tauloop";
	public static final String GUN_TESLA_FIRE = "hbm:weapon.fire.tesla";
	public static final String GUN_TESLA_BLAST = "hbm:entity.ufoblast"; // that discharge blast sound
	public static final String GUN_MK108_FIRE = "hbm:weapon.fire.mk108";
	public static final String GUN_ROCKET_FIRE = "hbm:weapon.rpgshoot";
	public static final String GUN_EXTINGUISHER_FIRE = "hbm:weapon.extinguisher";
	public static final String GUN_PLEASE_REMOVE_MY_EARDRUMS_THANKS = "hbm:weapon.fire.loudestnoiseonearth"; // the mad bowstring's launch
	public static final String GUN_VYLET_PONY_CUTIEMARKS_AND_THE_THINGS_THAT_BIND_US_INTRO_JINGLE = "hbm:weapon.fire.vstar"; // those who know, know

	/// weapon stunts ///
	public static final String GUN_GO_GO_GADGET_FUCK_EVERYTHING_IN_THIS_GENERAL_DIRECTION = "hbm:alarm.trainhorn";
	public static final String GUN_SOLDIER_TF2_BOAT_EXE_WAV_MP3 = "hbm:weapon.boat";
	public static final String GUN_MINI_NUKE_EXPLOSION = "hbm:weapon.mukeexplosion";

	/// turrets ///
	public static final String TURRET_50BMG = "hbm:turret.chekhov_fire";
	public static final String TURRET_CIWS_RELOAD = "hbm:turret.howard_reload";

	/// voices ///
	public static final String PLAYER_GULP = "hbm:player.gulp";
	public static final String PLAYER_GROAN = "hbm:player.groan";

	/// blocks ///
	public static final String BLOCK_PLUSHY = "hbm:block.squeakytoy"; // a squeeze
	public static final String BLOCK_HUNDUNS_MAGNIFICENT_HOWL = "hbm:block.hunduns_magnificent_howl"; // yuri's tragic howl
	public static final String BLOCK_FALLOUT_3_POPUP = "hbm:block.bobble";
	public static final String LEVER_START = "hbm:block.leverstart";
	public static final String LEVER_STOP = "hbm:block.leverstop";
	public static final String SPARK = "hbm:block.spark";

	/// machine sounds, mostly loops ///
	public static final String ELECTRIC_MOTOR_LOOP = "hbm:block.motor";
	public static final String ENGINE_LOOP = "hbm:block.engine"; // diesel
	public static final String TURBINE_LARGE_LOOP = "hbm:block.largeturbinerunning";
	public static final String TURBINE_LEVI_LOOP = "hbm:block.chungusturbinerunning"; // the big boy turbine
	public static final String FEL_LOOP = "hbm:block.fel";
	public static final String ELECTRIC_HUM_LOOP = "hbm:block.electrichum";
	public static final String FUSION_REACTOR_LOOP = "hbm:block.fusionreactorrunning";
	public static final String BOILER_LOOP = "hbm:block.boiler";
	public static final String BOILER_GROAN = "hbm:block.boilergroan";
	public static final String CENTRIFUGE_LOOP = "hbm:block.centrifugeoperate";
	public static final String TURBOFAN_LOOP = "hbm:block.turbofanoperate";
	public static final String TURBOFAN_DAMAGE = "hbm:block.damage";
	public static final String ASSEMBLER_STRIKE = "hbm:block.assemblerstrike";
	public static final String ASSEMBLER_CUT = "hbm:block.assemblercut";
	public static final String ASSEMBLER_START = "hbm:block.assemblerstart";
	public static final String ASSEMBLER_STOP = "hbm:block.assemblerstop";
	public static final String CHEMPLANT_LOOP = "hbm:block.chemicalplant";
	public static final String HEPHAESTUS_LOOP = "hbm:block.hephaestusrunning";
	public static final String STEAM_ENGINE_HIT = "hbm:block.steamengineoperate";
	public static final String REACTOR_GEIGER_LOOP = "hbm:block.reactorloop";

	/// misc ///
	public static final String TECH_BOOP = "hbm:item.techboop"; // boop
	public static final String TECH_BLEEP = "hbm:item.techbleep"; // blee-blop-blee-blop
	public static final String UPGRADE_PLUG = "hbm:item.upgradeplug"; // click
	public static final String UNPACK = "hbm:item.unpack";
	public static final String RIVET_GUN = "hbm:item.boltgun";
	public static final String GEIGER_PREFIX = "hbm:item.geiger"; // just a prefix; append 1-6 for a complete sound name
	public static final String FILTER_SCREW = "hbm:item.gasmaskscrew";
	public static final String SUIT_BATTERY = "hbm:item.battery";
	public static final String CRATE_OPEN = "hbm:block.crateopen";
	public static final String CRATE_CLOSE = "hbm:block.crateclose";
	public static final String PADLOCK = "hbm:block.lockhang";
	public static final String SPRAY_CAN = "hbm:item.spray";
	public static final String REPAIR = "hbm:item.repair";
	public static final String GAVEL = "hbm:weapon.whack";
	public static final String BONK = "hbm:weapon.bonk";
	public static final String STOP = "hbm:weapon.stop"; // halt!
	public static final String BANG = "hbm:weapon.bang"; // frying pan
	public static final String SLICE = "hbm:weapon.slice";
	public static final String KAPENG = "hbm:weapon.kapeng"; // third-degree burn
	public static final String METAL_IMPACT = "hbm:block.metalimpact"; // ouch

	/// the pile of vanilla ones too lazy to look up one by one ///
	public static final String VANILLA_ORB = "entity.experience_orb.pickup"; // the xp orb pickup ding
	public static final String VANILLA_PLINK = "entity.item.break"; // the crunch when a tool runs out
	public static final String VANILLA_FIREWORKS_BANG = "entity.firework.blast"; // firework going off
	public static final String VANILLA_HISS = "block.fire.extinguish"; // fire put out
	public static final String VANILLA_FIRE = "block.fire.ambient"; // campfire crackle
	public static final String VANILLA_IGNITE = "item.flintandsteel.use"; // flint and steel
	public static final String VANILLA_MINECART = "entity.minecart.riding"; // minecart rolling loop
	public static final String VANILLA_GIB = "entity.zombie.break_door_wood"; // zombie bashing a wooden door down
	public static final String VANILLA_TELEPORT = "entity.endermen.teleport"; // enderman teleport
	public static final String VANILLA_ANVIL = "block.anvil.land"; // anvil landing
	public static final String VANILLA_PISTON_OUT = "block.piston.extend"; // piston extending
	public static final String VANILLA_PISTON_IN = "block.piston.contract"; // piston retracting
}
