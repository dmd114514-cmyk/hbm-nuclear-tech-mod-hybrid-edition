package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The night vision module: helmet-only, maintaining night vision while a powered suit is live and the player's HUD is on.
 *
 * The 15-second duration (not 1) keeps it from flickering under client lag; when switched off it clears only the copy
 * it applied itself (judged by the armor NBT marker), never the night vision gained elsewhere.
 */
public class ItemModNightVision extends ItemArmorMod {

	private static final String NIGHT_VISION_ACTIVE_NBT_KEY = "ITEM_MOD_NV_ACTIVE";

	public ItemModNightVision() {
		super(ArmorModHandler.helmet_only, true, false, false, false);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.AQUA + I18nUtil.resolveKey("item.night_vision.description.item"));
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("item.night_vision.description.in_armor", stack.getDisplayName()));
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(entity.world.isRemote || !(entity instanceof EntityPlayer))
			return;

		if(!(armor.getItem() instanceof ArmorFSBPowered) || !ArmorFSB.hasFSBArmor((EntityPlayer) entity))
			return;

		EntityPlayer player = (EntityPlayer) entity;

		if(HbmPlayerProps.getData(player).enableHUD) {
			entity.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 15 * 20, 0));
			if(!armor.hasTagCompound()) {
				armor.setTagCompound(new NBTTagCompound());
			}
			if(!armor.getTagCompound().hasKey(NIGHT_VISION_ACTIVE_NBT_KEY)) {
				armor.getTagCompound().setBoolean(NIGHT_VISION_ACTIVE_NBT_KEY, true);
			}

			if(entity.getRNG().nextInt(200) == 0) {
				armor.damageItem(1, entity);
			}
		} else if(armor.hasTagCompound() && armor.getTagCompound().hasKey(NIGHT_VISION_ACTIVE_NBT_KEY)) {
			entity.removePotionEffect(MobEffects.NIGHT_VISION);
			armor.getTagCompound().removeTag(NIGHT_VISION_ACTIVE_NBT_KEY);
		}
	}
}
