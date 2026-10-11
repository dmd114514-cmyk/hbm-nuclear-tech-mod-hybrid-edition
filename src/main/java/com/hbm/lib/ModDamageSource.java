package com.hbm.lib;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

/**
 * Home base for this mod's damage sources.
 * All built through DamageSource's builder chain and referenced as static fields by business code —
 * don't new one on the spot. Damage sources get compared against each other; a fresh instance everywhere kills any unified interception.
 */
public class ModDamageSource extends DamageSource {

	public static DamageSource nuclearBlast = new DamageSource("nuclearBlast").setExplosion();
	public static DamageSource mudPoisoning = new DamageSource("mudPoisoning").setDamageBypassesArmor();
	public static DamageSource acid = new DamageSource("acid");
	public static DamageSource euthanizedSelf = new DamageSource("euthanizedSelf").setDamageBypassesArmor();
	public static DamageSource euthanizedSelf2 = new DamageSource("euthanizedSelf2").setDamageBypassesArmor();
	public static DamageSource tauBlast = new DamageSource("tauBlast").setDamageBypassesArmor().setExplosion();
	public static DamageSource radiation = new DamageSource("radiation").setDamageBypassesArmor();
	public static DamageSource digamma = new DamageSource("digamma").setDamageIsAbsolute().setDamageBypassesArmor().setDamageAllowedInCreativeMode();
	public static DamageSource suicide = new DamageSource("suicide").setProjectile();
	public static DamageSource rubble = new DamageSource("rubble").setProjectile();
	public static DamageSource shrapnel = new DamageSource("shrapnel").setProjectile();
	public static DamageSource blackhole = new DamageSource("blackhole").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource turbofan = new DamageSource("blender").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource meteorite = new DamageSource("meteorite").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource boxcar = new DamageSource("boxcar").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource boat = new DamageSource("boat").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource building = new DamageSource("building").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource taint = new DamageSource("taint").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource ams = new DamageSource("ams").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource amsCore = new DamageSource("amsCore").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource broadcast = new DamageSource("broadcast").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource bang = new DamageSource("bang").setDamageIsAbsolute().setDamageBypassesArmor();

	/** Cloud/soot-type damage: armor-piercing and absolute, nothing blocks it */
	public static DamageSource cloud = new DamageSource("cloud").setDamageIsAbsolute().setDamageBypassesArmor();

	public static DamageSource pc = new DamageSource("pc").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource lead = new DamageSource("lead").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource enervation = new DamageSource("enervation").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource electricity = new DamageSource("electricity").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource exhaust = new DamageSource("exhaust").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource spikes = new DamageSource("spikes").setDamageBypassesArmor();
	public static DamageSource lunar = new DamageSource("lunar").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource monoxide = new DamageSource("monoxide").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource asbestos = new DamageSource("asbestos").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource blacklung = new DamageSource("blacklung").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource mku = new DamageSource("mku").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource vacuum = new DamageSource("vacuum").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource overdose = new DamageSource("overdose").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource microwave = new DamageSource("microwave").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource eve = new DamageSource("eve").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource oxyprime = new DamageSource("oxyprime").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource oiledup = new DamageSource("oiledup").setDamageIsAbsolute().setDamageBypassesArmor();
	public static DamageSource run = new DamageSource("run").setDamageIsAbsolute().setDamageBypassesArmor();

	/** Still deals damage in creative, same class as digamma: the kind you can't dodge */
	public static DamageSource nitan = new DamageSource("nitan").setDamageIsAbsolute().setDamageBypassesArmor().setDamageAllowedInCreativeMode();

	/**
	 * Type names for the bullet-hell damage family. These strings double as death-message translation keys
	 * (death.attack.<name>); renaming one means adding one to the lang file — don't treat them as ordinary constants.
	 */
	public static final String s_bullet = "revolverBullet";
	public static final String s_emplacer = "chopperBullet";
	public static final String s_tau = "tau";
	public static final String s_combineball = "cmb";
	public static final String s_zomg_prefix = "subAtomic";
	public static final String s_euthanized = "euthanized";
	public static final String s_emp = "electrified";
	public static final String s_flamethrower = "flamethrower";
	public static final String s_immolator = "plasma";
	public static final String s_cryolator = "ice";
	public static final String s_laser = "laser";
	public static final String s_boil = "boil";
	public static final String s_acid = "acidPlayer";
	public static final String heal = "healPlayer";

	/** Indirect damage must pass the "real culprit" and "the thing that actually hit" separately, or death messages blame the wrong party */
	public static DamageSource causeBulletDamage(Entity base, Entity ent) {
		return (new EntityDamageSourceIndirect(s_bullet, base, ent)).setProjectile();
	}

	public static DamageSource causeDisplacementDamage(Entity ent, Entity hit) {
		return (new EntityDamageSourceIndirect(s_emplacer, ent, hit)).setProjectile();
	}

	public static DamageSource causeTauDamage(Entity ent, Entity hit) {
		return (new EntityDamageSourceIndirect(s_tau, ent, hit)).setProjectile().setDamageBypassesArmor();
	}

	public static DamageSource causeCombineDamage(Entity ent, Entity hit) {
		return (new EntityDamageSourceIndirect(s_combineball, ent, hit)).setProjectile().setDamageBypassesArmor();
	}

	/** The sub-atomic bomb comes in six independent types for tiers 1~5, purely so death messages can tell them apart */
	public static DamageSource causeSubatomicDamage(Entity ent, Entity hit) {
		return (new EntityDamageSourceIndirect(s_zomg_prefix + (ent.getEntityWorld().rand.nextInt(5) + 1), ent, hit)).setProjectile().setDamageBypassesArmor();
	}

	public static DamageSource euthanized(Entity ent, Entity hit) {
		return (new EntityDamageSourceIndirect(s_euthanized, ent, hit)).setDamageBypassesArmor();
	}

	/** Whether a hit is a tau bolt: indirect damage only — block damage has no "source projectile" */
	public static boolean getIsTau(DamageSource source) {
		if(source instanceof EntityDamageSourceIndirect) {
			return ((EntityDamageSourceIndirect) source).damageType.equals(s_tau);
		}
		return false;
	}

	public static boolean getIsSubatomic(DamageSource source) {
		if(source instanceof EntityDamageSourceIndirect) {
			String s = ((EntityDamageSourceIndirect) source).damageType;
			return s.startsWith(s_zomg_prefix);
		}
		return false;
	}

	public ModDamageSource(String damageType) {
		super(damageType);
	}
}
