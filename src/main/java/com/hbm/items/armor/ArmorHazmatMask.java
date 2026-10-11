package com.hbm.items.armor;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.render.model.ModelM65;
import api.hbm.hazard.HazardClass;
import com.hbm.util.ArmorUtil;

import api.hbm.item.IGasMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The hooded hazmat helmet, accepting filter inserts.
 *
 * One honest note: the old version also had a `getArmorModel` returning the M65 head model (for the red/grey pair),
 * that model class lives in the render layer's `com.hbm.render.model`; the item domain only hooks it, never builds one — wired now per
 * the ArmorAshGlasses precedent; non-red-grey pairs still take super (equivalent to the old null, i.e. the plain humanoid layer).
 */
public class ArmorHazmatMask extends ArmorHazmat implements IGasMask {

	@SideOnly(Side.CLIENT)
	private ModelM65 modelM65;

	public ArmorHazmatMask(ArmorMaterial material, int slot, String texture) {
		super(material, slot, texture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {

		if(this == ModItems.hazmat_helmet_red || this == ModItems.hazmat_helmet_grey) {
			if(this.modelM65 == null) {
				this.modelM65 = new ModelM65();
			}
			return this.modelM65;
		}

		return super.getArmorModel(entity, stack, slot, defaultModel);
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

		super.addInformation(stack, world, list, flag);
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
