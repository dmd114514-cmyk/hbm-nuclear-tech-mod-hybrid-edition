package com.hbm.items.armor;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * The insert plates: chest-only, occupying the ballistic insert slot, folding hits taken into their own durability.
 *
 * Four multipliers covering total damage, projectiles, explosions and speed; durability is fixed in the constructor, displayed as "damage as inverted HP".
 * Two special cases hang separately — the polonium one feeds radiation back to the wearer, the ERA one detonates when blasted
 */
public class ItemModInsert extends ItemArmorMod {

	float damageMod;
	float projectileMod;
	float explosionMod;
	float speed;

	public boolean corrosionProtection = false;

	public ItemModInsert(int durability, float damageMod, float projectileMod, float explosionMod, float speed) {
		super(ArmorModHandler.kevlar, false, true, false, false);
		this.damageMod = damageMod;
		this.projectileMod = projectileMod;
		this.explosionMod = explosionMod;
		this.speed = speed;
		this.setMaxDamage(durability);
	}

	public ItemModInsert withCorrosionProtection() {
		corrosionProtection = true;
		return this;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(corrosionProtection)
			list.add(TextFormatting.GOLD + "Corrosive atmosphere protection");
		if(damageMod != 1F)
			list.add(TextFormatting.RED + (damageMod < 1 ? "-" : "+") + Math.abs(Math.round((1F - damageMod) * 100)) + "% damage");
		if(projectileMod != 1F)
			list.add(TextFormatting.YELLOW + "-" + Math.round((1F - projectileMod) * 100) + "% projectile damage");
		if(explosionMod != 1F)
			list.add(TextFormatting.YELLOW + "-" + Math.round((1F - explosionMod) * 100) + "% explosion damage");
		if(speed != 1F)
			list.add(TextFormatting.BLUE + "-" + Math.round((1F - speed) * 100) + "% speed");

		if(this == ModItems.insert_polonium)
			list.add(TextFormatting.DARK_RED + "+100 RAD/s");

		list.add((stack.getMaxDamage() - stack.getItemDamage()) + "/" + stack.getMaxDamage() + "HP");

		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {

		List<String> desc = new ArrayList<String>();

		if(damageMod != 1F)
			desc.add((damageMod < 1 ? "-" : "+") + Math.abs(Math.round((1F - damageMod) * 100)) + "% dmg");
		if(projectileMod != 1F)
			desc.add("-" + Math.round((1F - projectileMod) * 100) + "% proj");
		if(explosionMod != 1F)
			desc.add("-" + Math.round((1F - explosionMod) * 100) + "% exp");
		if(speed != 1F)
			desc.add("-" + Math.round((1F - speed) * 100) + "% speed");

		if(this == ModItems.insert_polonium)
			desc.add("+100 RAD/s");

		String join = String.join(" / ", desc);

		list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (" + join + " / " + (stack.getMaxDamage() - stack.getItemDamage()) + "HP)");
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		event.setAmount(event.getAmount() * damageMod);

		if(event.getSource().isProjectile())
			event.setAmount(event.getAmount() * projectileMod);

		if(event.getSource().isExplosion())
			event.setAmount(event.getAmount() * explosionMod);

		ItemStack insert = ArmorModHandler.pryMods(armor)[ArmorModHandler.kevlar];

		if(insert == null)
			return;

		insert.setItemDamage(insert.getItemDamage() + 1);

		if(!event.getEntityLiving().world.isRemote && this == ModItems.insert_era) {
			EntityLivingBase victim = event.getEntityLiving();
			victim.world.newExplosion(victim, victim.posX, victim.posY - victim.getYOffset() + victim.height * 0.5, victim.posZ, 0.05F, false, false);
		}

		if(insert.getItemDamage() >= insert.getMaxDamage()) {
			ArmorModHandler.removeMod(armor, ArmorModHandler.kevlar);
		} else {
			ArmorModHandler.applyMod(armor, insert);
		}
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(!entity.world.isRemote && this == ModItems.insert_polonium) {
			HbmLivingProps.incrementRadiation(entity, 100F);
		}
	}

	/*The old version returned null outright at speed 1; the base hands back an empty list here, so take the empty list and only multiply where it differs */
	@Override
	public Multimap<String, AttributeModifier> getModifiers(ItemStack armor) {

		Multimap<String, AttributeModifier> multimap = super.getModifiers(armor);

		if(speed != 1F && armor.getItem() instanceof ItemArmor) {
			multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
					new AttributeModifier(ArmorModHandler.UUIDs[ModArmor.slotIndex(((ItemArmor) armor.getItem()).armorType)], "NTM Armor Mod Speed", -1F + speed, 2));
		}

		return multimap;
	}
}
