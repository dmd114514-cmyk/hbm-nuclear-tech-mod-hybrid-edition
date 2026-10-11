package com.hbm.handler;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.interfaces.IKeybindReceiver;
import com.hbm.main.MainRegistry;
import com.hbm.network.NetworkHandler;
import com.hbm.network.packet.KeybindPacket;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The keybind table: definitions and registration entry for every hbm keybind (client).
 *
 * What content actually consumes is the nested {@link EnumKeybind} — item keybind callbacks and the key state in player extension data
 * only know that enum, never the KeyBinding fields here; the enum is a nested class, so loading it doesn't trigger this class's statics,
 * which makes server-side references safe too (the old version and the EX version are both shaped this way).
 *
 * One API delta this version: `KeyBinding(String, int, String)` keeps the old parameter order, copy as-is;
 * but `getIsKeyPressed()` became `isKeyDown()`, and `getKeyCode()` is unchanged.
 */
public class HbmKeybinds {

	public static final String category = "hbm.key";

	public static KeyBinding calculatorKey = new KeyBinding(category + ".calculator", Keyboard.KEY_N, category);
	public static KeyBinding jetpackKey = new KeyBinding(category + ".toggleBack", Keyboard.KEY_C, category);
	public static KeyBinding magnetKey = new KeyBinding(category + ".toggleMagnet", Keyboard.KEY_Z, category);
	public static KeyBinding hudKey = new KeyBinding(category + ".toggleHUD", Keyboard.KEY_V, category);
	public static KeyBinding dashKey = new KeyBinding(category + ".dash", Keyboard.KEY_LSHIFT, category);
	public static KeyBinding trainKey = new KeyBinding(category + ".trainInv", Keyboard.KEY_R, category);
	public static KeyBinding slamKey = new KeyBinding(category + ".slamkey", Keyboard.KEY_LCONTROL, category);

	public static KeyBinding qmaw = new KeyBinding(category + ".qmaw", Keyboard.KEY_F1, category);

	public static KeyBinding abilityCycle = new KeyBinding(category + ".ability", -99, category);
	public static KeyBinding abilityAlt = new KeyBinding(category + ".abilityAlt", Keyboard.KEY_LMENU, category);
	public static KeyBinding copyToolAlt = new KeyBinding(category + ".copyToolAlt", Keyboard.KEY_LMENU, category);
	public static KeyBinding copyToolCtrl = new KeyBinding(category + ".copyToolCtrl", Keyboard.KEY_LCONTROL, category);

	public static KeyBinding reloadKey = new KeyBinding(category + ".reload", Keyboard.KEY_R, category);
	public static KeyBinding gunPrimaryKey = new KeyBinding(category + ".gunPrimary", -100, category);
	public static KeyBinding gunSecondaryKey = new KeyBinding(category + ".gunSecondary", -99, category);
	public static KeyBinding gunTertiaryKey = new KeyBinding(category + ".gunTertitary", -98, category);

	public static KeyBinding craneUpKey = new KeyBinding(category + ".craneMoveUp", Keyboard.KEY_UP, category);
	public static KeyBinding craneDownKey = new KeyBinding(category + ".craneMoveDown", Keyboard.KEY_DOWN, category);
	public static KeyBinding craneLeftKey = new KeyBinding(category + ".craneMoveLeft", Keyboard.KEY_LEFT, category);
	public static KeyBinding craneRightKey = new KeyBinding(category + ".craneMoveRight", Keyboard.KEY_RIGHT, category);
	public static KeyBinding craneLoadKey = new KeyBinding(category + ".craneLoad", Keyboard.KEY_RETURN, category);

	/** Registration point: this version hangs it in {@code com.hbm.main.ClientProxy#init} (that spot already carries the "client-only things hang here" rule); the old version called it once inside MainRegistry */
	public static void register() {
		ClientRegistry.registerKeyBinding(calculatorKey);
		ClientRegistry.registerKeyBinding(jetpackKey);
		ClientRegistry.registerKeyBinding(magnetKey);
		ClientRegistry.registerKeyBinding(hudKey);
		ClientRegistry.registerKeyBinding(dashKey);
		ClientRegistry.registerKeyBinding(trainKey);
		ClientRegistry.registerKeyBinding(slamKey);

		ClientRegistry.registerKeyBinding(qmaw);

		ClientRegistry.registerKeyBinding(reloadKey);
		ClientRegistry.registerKeyBinding(gunPrimaryKey);
		ClientRegistry.registerKeyBinding(gunSecondaryKey);
		ClientRegistry.registerKeyBinding(gunTertiaryKey);

		ClientRegistry.registerKeyBinding(craneUpKey);
		ClientRegistry.registerKeyBinding(craneDownKey);
		ClientRegistry.registerKeyBinding(craneLeftKey);
		ClientRegistry.registerKeyBinding(craneRightKey);
		ClientRegistry.registerKeyBinding(craneLoadKey);
		ClientRegistry.registerKeyBinding(abilityCycle);
		ClientRegistry.registerKeyBinding(abilityAlt);
		ClientRegistry.registerKeyBinding(copyToolAlt);
		ClientRegistry.registerKeyBinding(copyToolCtrl);
	}

	/*
	 * The upstream channel for key state: when the client notices "a key's current state differs from what was last synced" it sends a packet,
	 * so the server's keysPressed always follows the client. The dispatch point registers in {@code ClientProxy#init}.
	 *
	 * The old version paired this half with a handleOverlap that lit same-key keybinds together by hand; this version doesn't need it: Forge reworked
	 * {@code KeyBinding.setKeyBindState} to walk every keybind on that keyCode (checked against the official patch),
	 * and the mouse queue also calls {@code setKeyBindState(button - 100)} before firing MouseInputEvent,
	 * so the gun keybinds with negative keycodes read fine here.
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void keyEvent(KeyInputEvent event) {
		syncKeyStates();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void mouseEvent(MouseInputEvent event) {
		syncKeyStates();
	}

	/** Compares each keybind's "held right now on the client" against the last synced value; only changed bits go out as packets */
	private static void syncKeyStates() {

		EntityPlayer player = MainRegistry.proxy.me();
		if(player == null || player.world == null) return;	// still sitting in the main menu, nothing to sync

		HbmPlayerProps props = HbmPlayerProps.getData(player);
		if(props == null) return;

		for(EnumKeybind key : EnumKeybind.values()) {
			boolean last = props.getKeyPressed(key);
			boolean current = MainRegistry.proxy.getIsKeyPressed(key);

			if(last == current) continue;

			props.setKeyPressed(key, current);
			NetworkHandler.sendToServer(new KeybindPacket(key, current));
			onPressedClient(player, key, current);
		}
	}

	/** The client pass for held items; the server pass lives in {@link HbmKeybindsServer} — one interface, dispatched once per side */
	public static void onPressedClient(EntityPlayer player, EnumKeybind key, boolean state) {

		ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
		if(held == null || held.isEmpty()) return;
		if(!(held.getItem() instanceof IKeybindReceiver)) return;

		IKeybindReceiver rec = (IKeybindReceiver) held.getItem();
		if(rec.canHandleKeybind(player, held, key)) rec.handleKeybindClient(player, held, key, state);
	}

	/**
	 * The keybind spots still skipped, pending their dependencies; don't write a second mechanism here:
	 *
	 * · the gun half of {@code handleOverlap} ("shooting displaces attack/pickup while holding a gun"): wants the {@code ItemGunBaseNT}
	 *   family; the sedna guns haven't landed. The same-named keybind linkage is already covered by the platform's setKeyBindState, no patch needed.
	 * · {@code postClientTick}: the old block existed to give "abilityCycle bound to right-click" a beat; also waiting on the gun family;
	 *   everything in this queue's global heartbeat implements {@code IHbmTickReceiver}, and that port only fires server-side.
	 * · {@code keyEvent} also opened the calculator GUI in the old version, which wants {@code com.hbm.inventory.gui.GUICalculator}.
	 * · {@code qmaw} registers keybinds only and stays out of {@code EnumKeybind}, same as the old version — the old switch didn't know it either.
	 */
	public static enum EnumKeybind {
		JETPACK,
		TOGGLE_JETPACK,
		TOGGLE_MAGNET,
		TOGGLE_HEAD,
		DASH,
		SLAM,
		TRAIN,
		CRANE_UP,
		CRANE_DOWN,
		CRANE_LEFT,
		CRANE_RIGHT,
		CRANE_LOAD,
		ABILITY_CYCLE,
		ABILITY_ALT,
		TOOL_ALT,
		TOOL_CTRL,
		GUN_PRIMARY,
		GUN_SECONDARY,
		GUN_TERTIARY,
		RELOAD
	}
}
