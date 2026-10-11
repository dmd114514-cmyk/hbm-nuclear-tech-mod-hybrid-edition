package com.hbm.items.armor;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.interfaces.IArmorModDash;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The V1: the health-as-fuel double-dash piece. Its speed buff uses its own UUID, not the armor's four slots,
 * because it hangs on the chest without competing with the chest's own modifiers for the same key.
 */
public class ItemModV1 extends ItemArmorMod implements IArmorModDash {

	private static final UUID speed = UUID.fromString("1d11e63e-28c4-4e14-b09f-fe0bd1be708f");

	public ItemModV1() {
		super(ArmorModHandler.plate_only, false, true, false, false);
	}

	@Override
	public Multimap<String, AttributeModifier> getModifiers(ItemStack armor) {

		Multimap<String, AttributeModifier> multimap = super.getModifiers(armor);

		multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(speed, "V1 SPEED", 0.5, 2));

		return multimap;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.RED + "BLOOD IS FUEL");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.RED + "  " + stack.getDisplayName() + " (BLOOD IS FUEL)");
	}

	@Override
	public int getDashes() {
		return 3;
	}
}
