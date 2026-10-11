package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;

/** Client-only appearance switches. */
@Config(modid = RefStrings.MODID, name = "hbm-client")
public class ClientConfig {

	@Name("Crosshairs")
	@Comment("Custom crosshairs for guns/tools")
	public static boolean enableCrosshairs = true;

	@Name("Render Distance Check")
	@Comment("TESRs beyond this distance are skipped outright; turn this off first when frames drop")
	public static boolean enableRenderDistCheck = true;

	@Name("MOTD")
	@Comment("Shows the mod's own title screen copy")
	public static boolean enableMOTD = true;

	@Name("Sound Channels")
	@Comment("Number of channels available to mod sounds; too small and sounds get swallowed")
	@RangeInt(min = 32, max = 4096)
	public static int normalSoundChannels = 200;

	@Name("Load Screen Replacement")
	@Comment("Replaces vanilla's loading screen with the mod's own")
	public static boolean enableLoadScreenReplacement = true;

	@Name("Gun Animation Speed")
	@Comment("Gun animation speed multiplier; the higher, the faster")
	public static double gunAnimationSpeed = 1.0D;

	@Name("Cable Hang")
	@Comment("Wires draw with gravity-sagging segments; off, they draw straight")
	public static boolean renderCableHang = true;

	@Name("DODD RBMK Diagnostic")
	@Comment("The crosshair target draws its own readout on top of the screen: held items first, then blocks, then entities")
	public static boolean doddRbmkDiagnostic = true;

	@Name("Show Block Meta Overlay")
	@Comment("For the block under the crosshair, adds another line with the untranslated name and meta, for debugging blocks")
	public static boolean showBlockMetaOverlay = false;
}
