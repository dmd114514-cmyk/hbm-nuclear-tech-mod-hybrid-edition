package com.hbm.hazard.type;

import java.util.List;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

/** Explodes on contact with water; standing in rain is hugging a detonator */
public class HazardTypeHydroactive extends HazardTypeBase {

	@Override
	public void onUpdate(EntityLivingBase target, float level, ItemStack stack) {

		if(RadiationConfig.disableHydro)
			return;

		if(target.isWet() && stack.getCount() > 0) {
			stack.setCount(0);
			target.getEntityWorld().newExplosion(null, target.posX, target.posY + target.getEyeHeight() - target.getYOffset(), target.posZ, level, false, true);
		}
	}

	@Override
	public void updateEntity(EntityItem item, float level) {

		if(RadiationConfig.disableHydro)
			return;

		BlockPos pos = new BlockPos(item.posX, item.posY, item.posZ);
		IBlockState state = item.getEntityWorld().getBlockState(pos);

		if(item.isWet() || state.getBlock().getMaterial(state) == Material.WATER) {
			item.setDead();
			item.getEntityWorld().newExplosion(null, item.posX, item.posY + item.height * 0.5, item.posZ, level, false, true);
		}
	}

	@Override
	public void addHazardInformation(EntityPlayer player, List<String> list, float level, ItemStack stack, List<HazardModifier> modifiers) {
		list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.hydro") + "]");
	}
}
