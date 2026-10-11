package com.hbm.hazard.type;

import java.util.List;

import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Neutron irradiation. One layer more than plain proximity radiation: the dose is burned into the item's NBT and keeps decaying after you walk away with it,
 * so an activated lead block can still poison people after it leaves your inventory.
 */
public class HazardTypeNeutron extends HazardTypeBase {

	/** The NBT key for activated dose; both decay and the readout use it */
	public static final String NEUTRON_KEY = "ntmNeutron";

	@Override
	public void onUpdate(EntityLivingBase target, float level, ItemStack stack) {

		if(RadiationConfig.disableNeutron)
			return;

		boolean reacher = false;

		// In 528 mode the grabber does nothing, so the realistic inverse-square branch below is dead too
		if(target instanceof EntityPlayer && !GeneralConfig.enable528)
			reacher = ((EntityPlayer) target).inventory.hasItemStack(new ItemStack(ModItems.reacher));

		level *= stack.getCount();

		if(level > 0) {
			float rad = (level / 20F) * ContaminationUtil.calculateRadiationMod(target);

			if(GeneralConfig.enable528 && reacher) {
				rad = (float) (rad / 49F);	//More realistic function for 528: x / distance^2
			} else if(reacher) {
				rad = (float) BobMathUtil.squirt(rad);
			}

			if(target instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) target;
				for(ItemStack held : player.inventory.mainInventory)
					apply(held, rad);
				for(ItemStack held : player.inventory.armorInventory)
					apply(held, rad);
			}

			ContaminationUtil.contaminate(target, HazardType.NEUTRON, ContaminationType.CREATIVE, rad);
		}
	}

	@Override
	public void updateEntity(EntityItem item, float level) { }

	@Override
	@SideOnly(Side.CLIENT)
	public void addHazardInformation(EntityPlayer player, List<String> list, float level, ItemStack stack, List<HazardModifier> modifiers) {

		level = HazardModifier.evalAllModifiers(stack, player, level, modifiers);

		if(level < 1e-5 || RadiationConfig.disableNeutron)
			return;

		list.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("trait.neutron") + "]");
		String neut = "" + (Math.floor(level * 1000) / 1000);
		list.add(TextFormatting.LIGHT_PURPLE + (neut + "RAD/s^2"));

		if(stack.getCount() > 1) {
			list.add(TextFormatting.LIGHT_PURPLE + "Stack: " + ((Math.floor(level * 1000 * stack.getCount()) / 1000) + "RAD/s^2"));
		}
	}

	/** Adds an activation dose onto an item; the caller vouches this is something on a player */
	public static void apply(ItemStack stack, float rad) {

		// In 1.12.2 an empty slot is EMPTY, not null — inventory walks hand it over too, and without this check we'd write NBT onto an empty stack
		if(stack == null || stack.isEmpty())
			return;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		float activation = stack.getTagCompound().getFloat(NEUTRON_KEY);
		stack.getTagCompound().setFloat(NEUTRON_KEY, activation + (rad / stack.getCount()) / 10);
	}

	/** Multiplies the activation dose on everything carried by factor each tick; factor below 1 is decay */
	public static void decay(ItemStack stack, float factor) {

		if(stack == null || stack.isEmpty())
			return;
		if(!stack.hasTagCompound())
			return;

		float activation = stack.getTagCompound().getFloat(NEUTRON_KEY);
		stack.getTagCompound().setFloat(NEUTRON_KEY, activation * factor);

		if(activation < 1e-5)
			stack.getTagCompound().removeTag(NEUTRON_KEY);

		if(stack.getTagCompound().hasNoTags())
			stack.setTagCompound((NBTTagCompound) null);
	}

}
