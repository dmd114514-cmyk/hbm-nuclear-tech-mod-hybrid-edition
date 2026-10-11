package com.hbm.items.armor;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.util.BobMathUtil;

import api.hbm.fluid.IFluidType;
import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The fluid-burning powered armor base: the charge set becomes "fuel", fill/drain both riding NBT's fuel key.
 *
 * Same split as ArmorFSBPowered — the base offers interfaces and settlement only; the specific fuel and numbers come from subclass constructor args.
 * One delta: those interface doors (acceptsFluid/tryFill/...) take an `IFluidType` (api.hbm.fluid) here
 * while the field still stores the old `FluidType`; comparisons convert back to the concrete type first, unresolvable ones refused.
 */
public class ArmorFSBFueled extends ArmorFSB implements IFillableItem {

	FluidType fuelType;
	public int maxFuel = 1;
	public int fillRate;
	public int consumption;
	public int drain;

	public ArmorFSBFueled(ArmorMaterial material, int slot, String texture, FluidType fuelType, int maxFuel, int fillRate, int consumption, int drain) {
		super(material, slot, texture);
		this.fuelType = fuelType;
		this.fillRate = fillRate;
		this.consumption = consumption;
		this.drain = drain;
		this.maxFuel = maxFuel;
	}

	@Override
	public int getFill(ItemStack stack) {

		NBTTagCompound tag = stack.getTagCompound();

		if(tag == null) {
			setFill(stack, maxFuel);
			return maxFuel;
		}

		return tag.getInteger("fuel");
	}

	public void setFill(ItemStack stack, int fill) {

		NBTTagCompound tag = stack.getTagCompound();

		if(tag == null) {
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
		}

		tag.setInteger("fuel", fill);
	}

	public int getMaxFill(ItemStack stack) {
		return this.maxFuel;
	}

	public int getLoadSpeed(ItemStack stack) {
		return this.fillRate;
	}

	public int getUnloadSpeed(ItemStack stack) {
		return 0;
	}

	/*Armor damage still rides vanilla's path, but folded into fuel consumption rather than durability here */
	@Override
	public void setDamage(ItemStack stack, int damage) {
		this.setFill(stack, Math.max(this.getFill(stack) - (damage * consumption), 0));
	}

	@Override
	public boolean isArmorEnabled(ItemStack stack) {
		return getFill(stack) > 0;
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		super.onArmorTick(world, player, stack);

		if(this.drain > 0 && ArmorFSB.hasFSBArmor(player) && !player.capabilities.isCreativeMode && world.getTotalWorldTime() % 10 == 0) {
			this.setFill(stack, Math.max(this.getFill(stack) - this.drain, 0));
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(this.fuelType.getLocalizedName() + ": " + BobMathUtil.getShortNumber(getFill(stack)) + " / " + BobMathUtil.getShortNumber(getMaxFill(stack)));
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getFill(stack) < getMaxFill(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1 - (double) getFill(stack) / (double) getMaxFill(stack);
	}

	@Override
	public boolean acceptsFluid(IFluidType type, ItemStack stack) {
		return type == this.fuelType;
	}

	@Override
	public int tryFill(IFluidType type, int amount, ItemStack stack) {

		if(!acceptsFluid(type, stack)) return amount;

		int toFill = Math.min(amount, this.fillRate);
		toFill = Math.min(toFill, this.maxFuel - this.getFill(stack));
		this.setFill(stack, this.getFill(stack) + toFill);

		return amount - toFill;
	}

	@Override
	public boolean providesFluid(IFluidType type, ItemStack stack) {
		return false;
	}

	@Override
	public int tryEmpty(IFluidType type, int amount, ItemStack stack) {
		return 0;
	}

	@Override
	public IFluidType getFirstFluidType(ItemStack stack) {
		return null;
	}
}
