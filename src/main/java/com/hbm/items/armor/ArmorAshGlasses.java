package com.hbm.items.armor;

import com.hbm.render.model.ModelGoggles;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The ash goggles: one head slot, a goggles model overriding the vanilla helmet layer.
 * The old version extended ItemArmor directly on vanilla iron material, skipping HBM's armor base — it only shields the eyes,
 * taking no part in radiation or hazmat checks; the grey zone (BlockGasBase, BlockAshes) only asks whether this slot is worn.
 */
public class ArmorAshGlasses extends ItemArmor {

	@SideOnly(Side.CLIENT)
	private ModelGoggles model;

	public ArmorAshGlasses(ArmorMaterial mat, EntityEquipmentSlot slot) {
		super(mat, 0, slot);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {

		if(model == null) model = new ModelGoggles();
		return model;
	}
}
