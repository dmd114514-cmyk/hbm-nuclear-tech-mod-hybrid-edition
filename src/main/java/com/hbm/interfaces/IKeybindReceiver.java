package com.hbm.interfaces;

import com.hbm.handler.HbmKeybinds.EnumKeybind;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Keybind response port for held items.
 * <br><br>
 * One keypress dispatches twice: the client pass is local only (mode switch, aiming and other feedback that shouldn't wait on a server round-trip),
 * the server pass is where state actually changes. Hence two separate methods, both default-empty — items that only care about the server pass
 * aren't forced to write the client half.
 * <br><br>
 * Different address from the old version: it lived in {@code com.hbm.items}; this version keeps all family interfaces under
 * {@code com.hbm.interfaces} per the standing convention ({@link IItemHUD} is the same-shape precedent).
 */
public interface IKeybindReceiver {

	/** Does this item want to answer this key right now; false makes the keybind transparent to it */
	public boolean canHandleKeybind(EntityPlayer player, ItemStack stack, EnumKeybind keybind);

	/** The server pass */
	public void handleKeybind(EntityPlayer player, ItemStack stack, EnumKeybind keybind, boolean state);

	/** The client pass */
	public default void handleKeybindClient(EntityPlayer player, ItemStack stack, EnumKeybind keybind, boolean state) { }
}
