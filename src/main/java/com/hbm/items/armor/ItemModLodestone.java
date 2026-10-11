package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The lodestone family: yanks nearby drops toward you; all four slots, riding the add-on slots.
 *
 * range only decides how far it grabs; the pull is fixed, with a little vertical lift so
 * ground-hugging items don't just spin in place. Players can switch the behavior off.
 */
public class ItemModLodestone extends ItemArmorMod {

	int range;

	public ItemModLodestone(int range) {
		super(ArmorModHandler.extra, true, true, true, true);
		this.range = range;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.DARK_GRAY + "Attracts nearby items");
		list.add(TextFormatting.DARK_GRAY + "Item attraction range: " + range);
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.DARK_GRAY + "  " + stack.getDisplayName() + " (Magnetic range: " + range + ")");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(entity instanceof EntityPlayer && !HbmPlayerProps.getData((EntityPlayer) entity).enableMagnet)
			return;

		List<EntityItem> items = entity.world.getEntitiesWithinAABB(EntityItem.class, entity.getEntityBoundingBox().expand(range, range, range));

		for(EntityItem item : items) {

			Vec3d vec = new Vec3d(entity.posX - item.posX, entity.posY - item.posY, entity.posZ - item.posZ).normalize();

			item.motionX += vec.x * 0.05;
			item.motionY += vec.y * 0.05;
			item.motionZ += vec.z * 0.05;

			if(vec.y > 0 && item.motionY < 0.04)
				item.motionY += 0.2;
		}
	}
}
