package com.hbm.items.armor;

import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.render.model.ModelArmorBJ;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The blackjack armor. Worn with the ModelArmorBJ obj parts.
 *
 * Plus a self-destruct rule: wearing only the helmet without the full powered set, the helmet gets yanked off and stuffed back in your face,
 * dealing one lethal lunar shot on the spot. The old version read armorInventory[3] directly; it's a NonNullList here,
 * so clearing that cell is set(3, EMPTY) — the order matching ArmorFSB's comment: feet/legs/chest/head.
 */
public class ArmorBJ extends ArmorFSBPowered {

	public ArmorBJ(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture, maxPower, chargeRate, consumption, drain);
	}

	@SideOnly(Side.CLIENT)
	private ModelArmorBJ[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slotType, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorBJ[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorBJ(i);
		}

		return models[ModArmor.slotIndex(slotType)];
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		super.onArmorTick(world, player, stack);

		if(this == ModItems.bj_helmet && ArmorFSB.hasFSBArmorIgnoreCharge(player) && !ArmorFSB.hasFSBArmor(player)) {

			ItemStack helmet = player.inventory.armorInventory.get(3);

			if(!player.inventory.addItemStackToInventory(helmet)) {
				player.dropItem(helmet, false);
			}

			player.inventory.armorInventory.set(3, ItemStack.EMPTY);

			player.attackEntityFrom(ModDamageSource.lunar, 1000);
		}
	}
}
