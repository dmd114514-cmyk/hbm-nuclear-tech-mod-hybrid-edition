package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import com.hbm.util.i18n.I18nUtil;

import java.util.List;

public interface ITooltipProvider {

	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean ext);

	public default void addStandardInfo(ItemStack stack, EntityPlayer player, List<String> list, boolean ext) {
		this.addStandardInfo(stack, ((Block) this).getUnlocalizedName() + ".desc", player, list, ext);
	}

	public default void addStandardInfo(ItemStack stack, String name, EntityPlayer player, List<String> list, boolean ext) {

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			for(String s : I18nUtil.resolveKeyArray(name)) list.add(TextFormatting.YELLOW + s);
		} else {
			list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC +"Hold <" +
					TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "LSHIFT" +
					TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "> to display more info");
		}
	}

	// 1.12.2's rarity constants went all-caps
	public default EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.COMMON;
	}
}
