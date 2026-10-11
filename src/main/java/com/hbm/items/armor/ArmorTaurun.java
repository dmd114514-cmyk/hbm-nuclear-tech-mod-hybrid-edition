package com.hbm.items.armor;

import com.hbm.render.model.ModelArmorTaurun;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Its only difference from the base is which obj-parts model it wears; the hook is this version's four-arg getArmorModel.
 *
 * The four tiers lazily built per slot, indices through ModArmor.slotIndex (the old 0..3); the whole block only holds on the client —
 * ResourceManager.getModel parses lazily and the server touching models would blow up.
 *
 * The held/inventory looks (the old getRenderer) bound to the deleted IItemRenderer — another kind of work, not done here
 */
public class ArmorTaurun extends ArmorFSB {

	public ArmorTaurun(ArmorMaterial material, int slot, String texture) {
		super(material, slot, texture);
	}

	@SideOnly(Side.CLIENT)
	private ModelArmorTaurun[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slotType, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorTaurun[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorTaurun(i);
		}

		return models[ModArmor.slotIndex(slotType)];
	}
}
