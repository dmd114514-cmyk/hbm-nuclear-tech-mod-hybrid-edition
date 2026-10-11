package com.hbm.main;

import com.hbm.handler.HbmKeybinds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * The server proxy. Every "client-only" action is a no-op here,
 * and the client proxy extends it with the real implementations, so common code just calls without side checks everywhere.
 */
public class ServerProxy {

	// Slot numbers for floating tips; the client queues by number, the server packets by number — both sides must share the same numbering
	public static final int ID_DUCK = 0;
	public static final int ID_FILTER = 1;
	public static final int ID_COMPASS = 2;
	public static final int ID_CABLE = 3;
	public static final int ID_DRONE = 4;
	public static final int ID_JETPACK = 5;
	public static final int ID_MAGNET = 6;
	public static final int ID_HUD = 7;
	public static final int ID_DETONATOR = 8;
	public static final int ID_FLUID_ID = 9;
	public static final int ID_FAN_MODE = 10;
	public static final int ID_TOOLABILITY = 11;
	public static final int ID_GAS_HAZARD = 12;
	public static final int ID_WRENCH = 13;
	public static final int ID_PAGER_DYN = 1000;

	public void preInit(FMLPreInitializationEvent event) { }

	public void init(FMLInitializationEvent event) { }

	public void postInit(FMLPostInitializationEvent event) { }

	/** Particle requests: dropped on the server */
	public void spawnParticle(double x, double y, double z, String type, float... args) { }

	/** Custom effect packets: dropped on the server */
	public void effectNT(NBTTagCompound data) { }

	public void playSound(String sound, Object data) { }

	/** A sound played only on this machine; the server has no "this machine", dropped */
	public void playSoundClient(double x, double y, double z, String sound, float volume, float pitch) { }

	/** The "displaced by effects" entity list: the server never keeps one, so always false */
	public boolean isVanished(Entity e) {
		return false;
	}

	/** On-screen floating tips (wrench/equipment info and such); the server needn't care */
	public void displayTooltip(String msg, int time, int id) { }

	public EntityPlayer me() {
		return null;
	}

	/**
	 * Whether a given hbm keybind is held right now. The server has no keys, so always false —
	 * server-side key state reads go through {@code HbmPlayerProps.getKeyPressed}, the copy the client syncs over.
	 * <br><br>
	 * This only references the nested {@link HbmKeybinds.EnumKeybind}, and loading it doesn't trigger the outer class's statics,
	 * so those KeyBindings are never touched on the server.
	 */
	public boolean getIsKeyPressed(HbmKeybinds.EnumKeybind key) {
		return false;
	}

	public boolean advancedTooltips() {
		return false;
	}

	/** Opens an external link; only the client can do that */
	public void openLink(String url) { }

	/** The current language code; the server has no "language" */
	public String getLanguageCode() {
		return "en_us";
	}

	/** Item name tinting; a client-only need */
	public int getStackColor(ItemStack stack, boolean amplify) {
		return 0x000000;
	}
}
