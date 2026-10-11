package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.render.model.ModelArmorEnvsuit;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The envsuit (envsuit_*): the only powered set granting extra mobility, all behavior hanging on the chestplate.
 *
 * Each tick the chestplate first strips the "sprint +10% speed" modifier then re-applies as needed — the old writing exactly;
 * active only while sprinting in a full powered set; underwater it adds night vision + thrust, removing the night vision on surfacing
 * (unless the helmet carries a night-vision insert — that one granted it, so it mustn't be cleared).
 */
public class ArmorEnvsuit extends ArmorFSBPowered {

	public ArmorEnvsuit(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture, maxPower, chargeRate, consumption, drain);
	}

	private static final java.util.UUID speed = java.util.UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");

	@SideOnly(Side.CLIENT)
	private ModelArmorEnvsuit[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slotType, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorEnvsuit[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorEnvsuit(i);
		}

		return models[ModArmor.slotIndex(slotType)];
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		super.onArmorTick(world, player, stack);

		if(this != com.hbm.items.ModItems.envsuit_plate) return;

		Multimap<String, AttributeModifier> map = super.getAttributeModifiers(this.armorType, stack);
		map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(speed, "SQUIRREL SPEED", 0.1D, 0));
		player.getAttributeMap().removeAttributeModifiers(map);

		if(ArmorFSB.hasFSBArmor(player)) {

			if(player.isSprinting()) player.getAttributeMap().applyAttributeModifiers(map);

			if(player.isInWater()) {

				if(!world.isRemote) {
					player.setAir(300);
					player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 15 * 20, 0));
				}

				double mo = 0.1 * player.moveForward;
				Vec3d vec = player.getLookVec();

				player.motionX += vec.x * mo;
				player.motionY += vec.y * mo;
				player.motionZ += vec.z * mo;

			} else {

				boolean canRemoveNightVision = true;
				ItemStack helmet = player.inventory.armorInventory.get(3);
				ItemStack helmetMod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only);

				if(!helmetMod.isEmpty() && helmetMod.getItem() instanceof ItemModNightVision) {
					canRemoveNightVision = false;
				}

				if(!world.isRemote && canRemoveNightVision) {
					player.removePotionEffect(MobEffects.NIGHT_VISION);
				}
			}
		}
	}
}
