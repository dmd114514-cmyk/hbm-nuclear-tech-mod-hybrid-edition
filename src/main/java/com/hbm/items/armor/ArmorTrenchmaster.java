package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.render.model.ModelArmorTrenchmaster;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The trenchmaster (trenchmaster_*): the whole set is "unbreakable + crit-block + self-blast immunity" buffs,
 * values riding base ArmorFSB's effect tables, worn with ModelArmorTrenchmaster.
 *
 * The old version also overrode two combat doors (handleHurt immunizing its own blasts, handleAttack blocking with a one-in-three chance
 * and a clank), but this version's base doors have **no callers in the tree** (empty shells left; who fires the events?),
 * so they don't port — ported, they're dead code. The two static checks isTrenchMaster/hasAoS stay,
 * asked by the weapon side (IMagazine/ItemGunBaseNT in the old version); weapons haven't migrated here yet.
 */
public class ArmorTrenchmaster extends ArmorFSB {

	public ArmorTrenchmaster(ArmorMaterial material, int slot, String texture) {
		super(material, slot, texture);
		this.setMaxDamage(0);
	}

	@SideOnly(Side.CLIENT)
	private ModelArmorTrenchmaster[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slotType, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorTrenchmaster[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorTrenchmaster(i);
		}

		return models[ModArmor.slotIndex(slotType)];
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.moreAmmo"));
	}

	/*Full trenchmaster set, powered: the weapon side's buff check (reload timing, magazine capacity and friends) */
	public static boolean isTrenchMaster(EntityPlayer player) {

		if(player == null) return false;

		ItemStack plate = player.inventory.armorInventory.get(2);

		return plate.getItem() == ModItems.trenchmaster_plate && ArmorFSB.hasFSBArmor(player);
	}

	/*The helmet carrying the "AoS card": the weapon side's other buff tier */
	public static boolean hasAoS(EntityPlayer player) {

		if(player == null) return false;

		ItemStack helmet = player.inventory.armorInventory.get(3);

		if(!helmet.isEmpty()) {
			ItemStack[] mods = ArmorModHandler.pryMods(helmet);
			ItemStack mod = mods[ArmorModHandler.helmet_only];
			return !mod.isEmpty() && mod.getItem() == ModItems.card_aos;
		}

		return false;
	}
}
