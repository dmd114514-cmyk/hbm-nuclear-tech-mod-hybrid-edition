package com.hbm.handler;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.interfaces.IKeybindReceiver;
import com.hbm.util.LoggingUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/**
 * The server-side dispatch point for keybinds.
 * <br><br>
 * A separate class because {@link HbmKeybinds} is wall-to-wall client API (KeyBinding, LWJGL) that server code
 * can't reference; this only knows the nested {@link EnumKeybind}, and loading it doesn't trigger the outer class's statics,
 * so sitting on the common side is safe.
 */
public class HbmKeybindsServer {

	/** Logs once when player data is missing, so each keypress doesn't earn a line */
	private static boolean warnedMissingProps = false;

	/** Where key state lands once upstream: bookkeep first, then dispatch the held item's response */
	public static void onPressedServer(EntityPlayer player, EnumKeybind key, boolean state) {

		HbmPlayerProps props = HbmPlayerProps.getData(player);
		if(props == null) {
			if(!warnedMissingProps) {
				warnedMissingProps = true;
				LoggingUtil.warn("[hbm] keybind packet arrived server-side without player extended data; ignored (reported once): {}", key);
			}
			return;
		}

		/*
		 * Bookkeep before dispatch: the toggles (jetpack/magnet/HUD) live in this state,
		 * and the item callbacks after want the post-flip values, not the pre-flip ones.
		 */
		props.setKeyPressed(key, state);

		ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
		if(held != null && !held.isEmpty() && held.getItem() instanceof IKeybindReceiver) {
			IKeybindReceiver rec = (IKeybindReceiver) held.getItem();
			if(rec.canHandleKeybind(player, held, key)) rec.handleKeybind(player, held, key, state);
		}
	}
}
