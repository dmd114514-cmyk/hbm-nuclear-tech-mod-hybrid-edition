package com.hbm.items.armor;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The armor mods. Constructor pins two things: which slots this fits, and which armor NBT slot it occupies once applied.
 *
 * The two are separate — landing pads fit all four slots but only the boots_only cell actually reads it.
 * modUpdate / modDamage / getModifiers are doors for the event dispatch side; the base gives empty bodies,
 * so unoverridden means "fits on but does nothing".
 */
public class ItemArmorMod extends Item {

	public final int type;
	public final boolean helmet;
	public final boolean chestplate;
	public final boolean leggings;
	public final boolean boots;

	public ItemArmorMod(int type, boolean helmet, boolean chestplate, boolean leggings, boolean boots) {
		this.type = type;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;

		this.setMaxStackSize(1);
		this.setCreativeTab(ModItems.consumableTab);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("armorMod.applicableTo"));

		if(helmet && chestplate && leggings && boots) {
			list.add("  " + I18nUtil.resolveKey("armorMod.all"));
		} else {

			if(helmet)
				list.add("  " + I18nUtil.resolveKey("armorMod.helmets"));
			if(chestplate)
				list.add("  " + I18nUtil.resolveKey("armorMod.chestplates"));
			if(leggings)
				list.add("  " + I18nUtil.resolveKey("armorMod.leggings"));
			if(boots)
				list.add("  " + I18nUtil.resolveKey("armorMod.boots"));
		}

		list.add(TextFormatting.DARK_PURPLE + "Slot:");

		switch(this.type) {
		case ArmorModHandler.helmet_only: list.add("  " + I18nUtil.resolveKey("armorMod.type.helmet")); break;
		case ArmorModHandler.plate_only: list.add("  " + I18nUtil.resolveKey("armorMod.type.chestplate")); break;
		case ArmorModHandler.legs_only: list.add("  " + I18nUtil.resolveKey("armorMod.type.leggings")); break;
		case ArmorModHandler.boots_only: list.add("  " + I18nUtil.resolveKey("armorMod.type.boots")); break;
		case ArmorModHandler.servos: list.add("  " + I18nUtil.resolveKey("armorMod.type.servo")); break;
		case ArmorModHandler.cladding: list.add("  " + I18nUtil.resolveKey("armorMod.type.cladding")); break;
		case ArmorModHandler.kevlar: list.add("  " + I18nUtil.resolveKey("armorMod.type.insert")); break;
		case ArmorModHandler.extra: list.add("  " + I18nUtil.resolveKey("armorMod.type.special")); break;
		case ArmorModHandler.battery: list.add("  " + I18nUtil.resolveKey("armorMod.type.battery")); break;
		}
	}

	/*The old line was super.getAttributeModifiers(armor); both Item overloads want a slot here, and the base gives */
	public Multimap<String, AttributeModifier> getModifiers(ItemStack armor) {
		return ArrayListMultimap.create();
	}

	public void modUpdate(EntityLivingBase entity, ItemStack armor) { }

	public void modDamage(LivingHurtEvent event, ItemStack armor) { }

	@SideOnly(Side.CLIENT)
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(stack.getDisplayName());
	}

	@SideOnly(Side.CLIENT)
	public void modRender(RenderPlayerEvent.SetArmorModel event, ItemStack armor) { }

	/*A few mods' tooltip colors jitter by clock milliseconds, written twice in the old version; collected into one door here — same jitter, */
	protected static String flicker(TextFormatting a, TextFormatting b) {
		return "" + (System.currentTimeMillis() % 1000 < 500 ? a : b);
	}
}
