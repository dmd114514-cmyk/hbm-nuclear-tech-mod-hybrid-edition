package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * A dropped demon core closes itself and spits out a screwdriver — think twice before picking it up.
 *
 * The core swap uses setEntityItemStack rather than "kill and drop another", preserving the drop entity's own
 * pickup delay and orientation, or it could be re-sucked up the instant it lands.
 */
public class ItemDemonCore extends Item {

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {

		if(entityItem.world.isRemote || !entityItem.onGround)
			return false;

		entityItem.setItem(new ItemStack(ModItems.demon_core_closed));

		EntityItem screwdriver = new EntityItem(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, new ItemStack(ModItems.screwdriver));
		entityItem.world.spawnEntity(screwdriver);

		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.drop") + "]");
	}
}
