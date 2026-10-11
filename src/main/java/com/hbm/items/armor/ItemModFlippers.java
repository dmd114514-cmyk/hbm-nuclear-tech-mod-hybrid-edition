package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The flippers: boots slot, pushing the player forward through water.
 *
 * The old version also had a yield clause — "the armor itself is an envsuit ({@code ArmorEnvsuit}) and the full set is FSB, so skip" —
 * but the envsuit family hasn't migrated and that half-condition is false today — left unwritten until it lands together with {@code hasFSB}.
 * This version's look vector is the immutable {@code Vec3d} (the old {@code Vec3}'s in-place multiply has no writable fields),
 * so the multiply folds into the addition.
 */
public class ItemModFlippers extends ItemArmorMod {

	public ItemModFlippers() {
		super(ArmorModHandler.boots_only, false, false, false, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.BLUE + "Increases swim speed");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (increased swim speed)");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(!(entity instanceof EntityPlayer)) return;
		EntityPlayer player = (EntityPlayer) entity;

		if(!entity.isInWater()) return;

		double mo = 0.08 * player.moveForward;
		Vec3d vec = entity.getLookVec();

		entity.motionX += vec.x * mo;
		entity.motionY += vec.y * mo;
		entity.motionZ += vec.z * mo;
	}
}
