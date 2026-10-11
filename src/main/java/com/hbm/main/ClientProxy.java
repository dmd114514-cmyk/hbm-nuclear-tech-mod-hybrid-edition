package com.hbm.main;

import java.awt.Desktop;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hbm.handler.HbmKeybinds;
import com.hbm.lib.RefStrings;
import com.hbm.particle.helper.IParticleCreator;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.registry.HbmRegistry;
import com.hbm.sound.SoundRefs;
import com.hbm.util.LoggingUtil;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The client proxy. The whole class exists only on the client (@SideOnly erases it on the server),
 * so any client API used here is safe.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		// Let Forge's own OBJ loader claim the hbm domain so JSON models can reference "hbm:xxx.obj" directly
		net.minecraftforge.client.model.obj.OBJLoader.INSTANCE.addDomain(com.hbm.lib.RefStrings.MODID);

		// Model registration can only happen on the client; item/block registryNames were set during the queuing stage
		HbmRegistry.registerModels();
		com.hbm.module.HbmModuleManager.registerModels();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		// The client event handlers hang explicitly here: this class is only ever newed on the client, the server never runs this line,
		// which beats writing side checks in common code. A line is logged after mounting, so whether registration happened becomes checkable in the log.
		MinecraftForge.EVENT_BUS.register(ClientProxy.class);
		// The hazard readout column in item tooltips: the handler class is named only here and never loads on the server
		MinecraftForge.EVENT_BUS.register(com.hbm.hazard.HazardTooltipHandler.class);
		// Crosshair overlay: each machine/tool implements ILookOverlay itself; the readouts dispatch from this one spot
		MinecraftForge.EVENT_BUS.register(LookOverlayHandler.class);
		// Upstream key state sync: KeyInputEvent/MouseInputEvent both land here
		MinecraftForge.EVENT_BUS.register(HbmKeybinds.class);
		LoggingUtil.info("[hbm] client event handlers registered: RenderLivingEvent.Pre + ClientTickEvent + ItemTooltipEvent(hazard readout) + RenderGameOverlayEvent.Pre(crosshair overlay) + KeyInput/MouseInput(keybind sync");

		// TESR bindings go through the render package's own registry, ahead of the first TE on screen
		com.hbm.render.HbmTileRenderers.registerAll();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
	}

	@Override
	public EntityPlayer me() {
		return Minecraft.getMinecraft().player;
	}

	/**
	 * Keybind enum → whether that key is held on this machine right now.
	 *
	 * Each entry maps to one of {@link HbmKeybinds}' keybinds and reads {@code isKeyDown()}:
	 * that method already factors in conflict context/modifiers in this version, whereas the old {@code getIsKeyPressed()}
	 * only saw the raw state. JETPACK reads vanilla's jump key like the old version, not an hbm keybind; SLAM never was in this table
	 * (slamKey reads differently), left empty here too.
	 */
	@Override
	public boolean getIsKeyPressed(HbmKeybinds.EnumKeybind key) {

		switch(key) {
		case JETPACK:			return Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
		case TOGGLE_JETPACK:	return HbmKeybinds.jetpackKey.isKeyDown();
		case TOGGLE_MAGNET:		return HbmKeybinds.magnetKey.isKeyDown();
		case TOGGLE_HEAD:		return HbmKeybinds.hudKey.isKeyDown();
		case RELOAD:			return HbmKeybinds.reloadKey.isKeyDown();
		case DASH:				return HbmKeybinds.dashKey.isKeyDown();
		case TRAIN:				return HbmKeybinds.trainKey.isKeyDown();
		case CRANE_UP:			return HbmKeybinds.craneUpKey.isKeyDown();
		case CRANE_DOWN:		return HbmKeybinds.craneDownKey.isKeyDown();
		case CRANE_LEFT:		return HbmKeybinds.craneLeftKey.isKeyDown();
		case CRANE_RIGHT:		return HbmKeybinds.craneRightKey.isKeyDown();
		case CRANE_LOAD:		return HbmKeybinds.craneLoadKey.isKeyDown();
		case ABILITY_CYCLE:		return HbmKeybinds.abilityCycle.isKeyDown();
		case ABILITY_ALT:		return HbmKeybinds.abilityAlt.isKeyDown();
		case TOOL_ALT:			return HbmKeybinds.copyToolAlt.isKeyDown();
		case TOOL_CTRL:			return HbmKeybinds.copyToolCtrl.isKeyDown();
		case GUN_PRIMARY:		return HbmKeybinds.gunPrimaryKey.isKeyDown();
		case GUN_SECONDARY:		return HbmKeybinds.gunSecondaryKey.isKeyDown();
		case GUN_TERTIARY:		return HbmKeybinds.gunTertiaryKey.isKeyDown();
		}

		return false;
	}

	/**
	 * Custom effect packets: looks up the creator by type and hands over the NBT to draw.
	 *
	 * Unknown types are dropped outright — the old table still carried a string of MK1-era stray effects (waterSplash and friends),
	 * but nothing in this tree emits those types; register them in the same shape when a real producer appears, don't hard-code here.
	 */
	@Override
	public void effectNT(NBTTagCompound data) {

		World world = Minecraft.getMinecraft().world;
		if(world == null) // back at the main menu, one or two packets may still be in flight
			return;

		String type = data.getString("type");
		IParticleCreator creator = ParticleCreators.particleCreators.get(type);

		if(creator == null) {
			// each unregistered type logs once: a missed registration means "sent but invisible", and without a log line it stays silent forever
			if(unknownTypes.add(type))
				LoggingUtil.warn("[hbm] effect type has no registered factory; ignored (reported once per type): {}", type);
			return;
		}

		creator.makeParticle(world, Minecraft.getMinecraft().player, Minecraft.getMinecraft().getTextureManager(), world.rand,
				data.getDouble("posX"), data.getDouble("posY"), data.getDouble("posZ"), data);
	}

	@Override
	public boolean advancedTooltips() {
		return Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
	}

	@Override
	public void openLink(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch(Exception ex) {
			// failing to open the browser at worst hides the link from the user; not worth crashing the game over
		}
	}

	@Override
	public String getLanguageCode() {
		return Minecraft.getMinecraft().gameSettings.language.toLowerCase(java.util.Locale.ROOT);
	}

	@Override
	public void playSoundClient(double x, double y, double z, String sound, float volume, float pitch) {
		// this entry carries no category; the old equivalent followed the master volume only, so MASTER it is rather than guessing from sounds.json
		SoundEvent event = SoundRefs.resolve(new ResourceLocation(sound));
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(event, SoundCategory.MASTER, volume, pitch, (float) x, (float) y, (float) z));
	}

	/** Unregistered effect types log once each, so they neither spam nor stay silent */
	private static final Set<String> unknownTypes = new HashSet();

	/** Entity id → the millisecond to hide it until. A one-frame scratchpad; never saved, never shared across sides */
	private static final Map<Integer, Long> vanished = new HashMap();

	/**
	 * Hides the original so the effect takes over. Skeletonizing, ashing — every "the entity is already a pile of particles" packet leans on this,
	 * or vanilla keeps painting a layer of something that should no longer exist.
	 */
	public static void vanish(int ent) {
		vanish(ent, 2000);
	}

	public static void vanish(int ent, int duration) {
		vanished.put(ent, System.currentTimeMillis() + duration);
	}

	@Override
	public boolean isVanished(Entity e) {
		if(e == null) return false;
		Long until = vanished.get(e.getEntityId());
		return until != null && until > System.currentTimeMillis();
	}

	/** Entities on the list skip rendering entirely; once the effect passes, the entry expires and the entity is drawn again */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRenderLivingPre(RenderLivingEvent.Pre event) {
		if(MainRegistry.proxy.isVanished(event.getEntity()))
			event.setCanceled(true);
	}

	private static boolean devTitleSet = false;

	/**
	 * The dev title. Vanilla only sets it once inside createDisplay() with no updateWindowTitle,
	 * so setting it once here is permanent, no need to re-set every tick.
	 *
	 * "the first client tick's START" is the signal because the display is guaranteed to exist by then;
	 * any earlier (preInit) throws outright, since the window doesn't exist yet.
	 */
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {

		if(devTitleSet || event.phase != Phase.START)
			return;

		devTitleSet = true;

		// even without -PdevTitle, overwrite vanilla's hard-coded "Minecraft 1.12.2", or inspection can't tell this run belongs to this mod
		String dev = System.getProperty("hbm.devTitle");

		if(dev == null || dev.isEmpty()) {
			Display.setTitle("HBM-Hybrid | " + RefStrings.VERSION);
			MainRegistry.logger.info("dev-title: (no devTitle passed) {}", RefStrings.VERSION);
			return;
		}

		Display.setTitle("HBM-Hybrid | " + dev + " | " + RefStrings.VERSION);
		MainRegistry.logger.info("dev-title: {}", dev);
	}
}
