package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

/**
 * Blast radius per warhead and explosion-behavior switches. Field names keep the original mod's spelling — each warhead's explosion code reads them by name.
 *
 * The old path went through Configuration with a Property propX = config.get(...) then setComment per entry; here everything folds into one @Config line;
 * defaults take the def from the old read statements (this file's 21 initial values all match def, so nothing needed renumbering).
 */
@Config(modid = RefStrings.MODID, name = "hbm-bomb")
public class BombConfig {

	/* ---- warhead radii and explosion parameters ---- */

	@Name("Gadget Radius")
	@Comment("Radius of the Gadget")
	public static int gadgetRadius = 150;

	@Name("Boy Radius")
	@Comment("Radius of Little Boy")
	public static int boyRadius = 120;

	@Name("Man Radius")
	@Comment("Radius of Fat Man")
	public static int manRadius = 175;

	@Name("Mike Radius")
	@Comment("Radius of Ivy Mike")
	public static int mikeRadius = 250;

	@Name("Tsar Radius")
	@Comment("Radius of the Tsar Bomba")
	public static int tsarRadius = 500;

	@Name("Prototype Radius")
	@Comment("Radius of the Prototype")
	public static int prototypeRadius = 150;

	@Name("Fleija Radius")
	@Comment("Radius of F.L.E.I.J.A.")
	public static int fleijaRadius = 50;

	@Name("Solinium Radius")
	@Comment("Radius of the blue rinse")
	public static int soliniumRadius = 150;

	@Name("N2 Radius")
	@Comment("Radius of the N2 mine")
	public static int n2Radius = 200;

	@Name("Missile Radius")
	@Comment("Radius of the nuclear missile")
	public static int missileRadius = 100;

	@Name("Mirv Radius")
	@Comment("Radius of a MIRV")
	public static int mirvRadius = 100;

	@Name("Fatman Radius")
	@Comment("Radius of the Fatman Launcher")
	public static int fatmanRadius = 35;

	@Name("Nuka Radius")
	@Comment("Radius of the nuka grenade")
	public static int nukaRadius = 25;

	@Name("A Schrab Radius")
	@Comment("Radius of dropped anti schrabidium")
	public static int aSchrabRadius = 20;

	/* ---- CATEGORY_NUKE ---- */

	@Name("Mk5")
	@Comment("Minimum amount of milliseconds per tick allocated for mk5 chunk processing")
	public static int mk5 = 50;

	@Name("Blast Speed")
	@Comment("Base speed of MK3 system (old and schrabidium) detonations (Blocks / tick)")
	public static int blastSpeed = 1024;

	@Name("Fallout Range")
	@Comment("Radius of fallout area (base radius * value in percent)")
	public static int falloutRange = 100;

	@Name("F Delay")
	@Comment("How many ticks to wait for the next fallout chunk computation")
	public static int fDelay = 4;

	@Name("Limit Explosion Lifespan")
	@Comment("How long an explosion can be unloaded until it dies in seconds. Based of system time. 0 disables the effect")
	public static int limitExplosionLifespan = 0;

	@Name("Chunkloading")
	@Comment("Allows all types of procedural explosions to keep the central chunk loaded and to generate new chunks.")
	public static boolean chunkloading = true;

	@Name("Explosion Algorithm")
	@Comment("Configures the algorithm of mk5 explosion. \n0 = Legacy, 1 = Threaded DDA, 2 = Threaded DDA with damage accumulation.")
	public static int explosionAlgorithm = 2;
}
