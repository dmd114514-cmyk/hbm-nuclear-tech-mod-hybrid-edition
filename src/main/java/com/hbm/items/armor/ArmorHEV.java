package com.hbm.items.armor;

import com.hbm.render.model.ModelArmorHEV;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The HEV suit (hev_*): the veteran high-tier powered armor, worn with ModelArmorHEV.
 *
 * The old class also carried a self-drawn HUD (health/power/radiation bars, overriding the base's handleOverlay).
 * That door currently has **no callers in the tree** (the base keeps the shell; the client render-event side isn't wired),
 * so it doesn't port yet — ported now it's dead code nobody reaches; add when callers land.
 */
public class ArmorHEV extends ArmorFSBPowered {

	public ArmorHEV(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture, maxPower, chargeRate, consumption, drain);
	}

	@SideOnly(Side.CLIENT)
	private ModelArmorHEV[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slotType, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorHEV[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorHEV(i);
		}

		return models[ModArmor.slotIndex(slotType)];
	}
}
