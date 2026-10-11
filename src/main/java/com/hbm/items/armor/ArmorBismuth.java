package com.hbm.items.armor;

import com.hbm.render.model.ModelArmorBismuth;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The bismuth armor: one look apart from plain FSB armor — worn with the obj-parts model rather than the double-layer biped.
 *
 * The four tier models built lazily per slot: `type` is the old armorType's 0..3 numbering (head/chest/legs/feet),
 * but the argument arriving here is the slot enum, so indices always go through ModArmor.slotIndex — never the enum's own ordinal,
 * since 1.9 added OFFHAND and the two numberings only accidentally agree across 0..3.
 *
 * Only valid on the client: models are render-layer property, and this tree's ResourceManager.getModel parses lazily —
 * server code touching it detonates on the spot, so the whole block is fenced with @SideOnly(CLIENT) and the server keeps vanilla's return.
 */
public class ArmorBismuth extends ArmorFSB {

	public ArmorBismuth(ArmorMaterial material, int slot, String texture) {
		super(material, slot, texture);
	}

	@SideOnly(Side.CLIENT)
	private ModelArmorBismuth[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slotType, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorBismuth[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorBismuth(i);
		}

		return models[ModArmor.slotIndex(slotType)];
	}
}
