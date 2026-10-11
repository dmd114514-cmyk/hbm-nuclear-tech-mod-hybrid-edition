package com.hbm.items.armor;

import java.util.ArrayList;
import java.util.List;

import api.hbm.hazard.HazardClass;
import com.hbm.util.ArmorUtil;

import api.hbm.item.IGasMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The liquidator's hooded version, accepting filter inserts. Look notes same as {@link ArmorLiquidator}. */
public class ArmorLiquidatorMask extends ArmorLiquidator implements IGasMask {

	public ArmorLiquidatorMask(ArmorMaterial material, int slot, String texture) {
		super(material, slot, texture);
	}

	@Override
	public ArrayList<HazardClass> getBlacklist(ItemStack stack, EntityLivingBase entity) {
		return new ArrayList<HazardClass>(); // the full hood blocks every hazard class
	}

	@Override
	public ItemStack getFilter(ItemStack stack, EntityLivingBase entity) {
		return ArmorUtil.getGasMaskFilter(stack);
	}

	@Override
	public void installFilter(ItemStack stack, EntityLivingBase entity, ItemStack filter) {
		ArmorUtil.installGasMaskFilter(stack, filter);
	}

	@Override
	public void damageFilter(ItemStack stack, EntityLivingBase entity, int damage) {

		ItemStack filter = ArmorUtil.getGasMaskFilter(stack);

		if(filter == null || filter.getMaxDamage() == 0)
			return;

		filter.setItemDamage(filter.getItemDamage() + damage);

		if(filter.getItemDamage() > filter.getMaxDamage())
			ArmorUtil.removeFilter(stack);
		else
			ArmorUtil.installGasMaskFilter(stack, filter);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		ArmorUtil.addGasMaskTooltip(stack, Minecraft.getMinecraft().player, list, flag.isAdvanced());
	}

	@Override
	public boolean isFilterApplicable(ItemStack stack, EntityLivingBase entity, ItemStack filter) {
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(player.isSneaking()) {

			ItemStack filter = this.getFilter(stack, player);

			if(filter != null) {
				ArmorUtil.removeFilter(stack);

				if(!player.inventory.addItemStackToInventory(filter)) {
					player.dropItem(filter, true);
				}

				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
		}

		return super.onItemRightClick(world, player, hand);
	}
}
