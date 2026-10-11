package com.hbm.items.armor;

import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.main.MainRegistry;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * WD-40: armor wear halved and halved again, at the price of +2 max HP while mounted and a red dust puff when hit.
 *
 * Wear recovery walks metadata back one step, so an already-pristine piece never triggers it in vain.
 */
public class ItemModWD40 extends ItemArmorMod {

	public ItemModWD40() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(flicker(TextFormatting.BLUE, TextFormatting.YELLOW) + "Highly reduces damage taken by armor, +2 HP");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(flicker(TextFormatting.BLUE, TextFormatting.YELLOW) + "  " + stack.getDisplayName() + " (-80% armor wear / +2 HP)");
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		if(!event.getEntityLiving().world.isRemote && armor.getMetadata() > 0 && event.getEntityLiving().getRNG().nextInt(5) != 0)
			armor.setItemDamage(armor.getMetadata() - 1);
	}

	@Override
	public Multimap<String, AttributeModifier> getModifiers(ItemStack armor) {

		Multimap<String, AttributeModifier> multimap = super.getModifiers(armor);

		multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(),
				new AttributeModifier(ArmorModHandler.UUIDs[ModArmor.slotIndex(((ItemArmor) armor.getItem()).armorType)], "NTM Armor Mod Health", 4, 0));

		return multimap;
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(!entity.world.isRemote || entity.hurtTime <= 0)
			return;

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "vanillaExt");
		data.setString("mode", "reddust");
		data.setDouble("posX", entity.posX + (entity.getRNG().nextDouble() - 0.5) * entity.width * 2);
		data.setDouble("posY", entity.posY + entity.getRNG().nextDouble() * entity.height);
		data.setDouble("posZ", entity.posZ + (entity.getRNG().nextDouble() - 0.5) * entity.width * 2);
		data.setDouble("mX", 0.01);
		data.setDouble("mY", 0.5);
		data.setDouble("mZ", 0.8);
		MainRegistry.proxy.effectNT(data);
	}
}
