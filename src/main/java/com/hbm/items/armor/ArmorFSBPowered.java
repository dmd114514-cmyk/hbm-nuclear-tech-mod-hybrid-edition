package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * The powered armor batch. Charge lives in the armor's own NBT — one long.
 *
 * Read the four as separate things: maxPower is the battery's size, chargeRate how fast external charging fills it,
 * consumption the drain per hit taken, and drain the idle burn per second with the full set on —
 * only the latter two self-deduct inside onArmorTick and setDamage.
 *
 * A battery mod multiplies the capacity, so capacity reads always go through getMaxCharge(stack),
 * never the raw maxPower field: a full suit losing its battery has nowhere to put the excess.
 */
public class ArmorFSBPowered extends ArmorFSB implements IBatteryItem {

	public long maxPower = 1;
	public long chargeRate;
	public long consumption;
	public long drain;

	public ArmorFSBPowered(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture);
		this.maxPower = maxPower;
		this.chargeRate = chargeRate;
		this.consumption = consumption;
		this.drain = drain;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add("Charge: " + BobMathUtil.getShortNumber(getCharge(stack)) + " / " + BobMathUtil.getShortNumber(getMaxCharge(stack)));
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public boolean isArmorEnabled(ItemStack stack) {
		return getCharge(stack) > 0;
	}

	@Override
	public void chargeBattery(ItemStack stack, long i) {

		if(!(stack.getItem() instanceof ArmorFSBPowered))
			return;

		NBTTagCompound tag = tag(stack);
		tag.setLong("charge", tag.getLong("charge") + i);
	}

	@Override
	public void setCharge(ItemStack stack, long i) {

		if(!(stack.getItem() instanceof ArmorFSBPowered))
			return;

		tag(stack).setLong("charge", i);
	}

	@Override
	public void dischargeBattery(ItemStack stack, long i) {

		if(!(stack.getItem() instanceof ArmorFSBPowered))
			return;

		NBTTagCompound tag = tag(stack);

		// untagged armor starts deducting from full, or the first hit drops 0 into negatives
		long charge = stack.hasTagCompound() ? tag.getLong("charge") : getMaxCharge(stack);
		charge -= i;

		tag.setLong("charge", Math.max(charge, 0));
	}

	@Override
	public long getCharge(ItemStack stack) {

		if(!(stack.getItem() instanceof ArmorFSBPowered))
			return 0;

		if(!stack.hasTagCompound()) {
			// new armor defaults to full: this also writes the tag — the old version saved it exactly this way
			tag(stack).setLong("charge", getMaxCharge(stack));
			return getMaxCharge(stack);
		}

		return Math.min(stack.getTagCompound().getLong("charge"), getMaxCharge(stack));
	}

	/** Fetches the tag, creating and attaching one when absent. For the write paths above only; read paths must check hasTagCompound first */
	private static NBTTagCompound tag(ItemStack stack) {

		NBTTagCompound nbt = stack.getTagCompound();

		if(nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}

		return nbt;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getCharge(stack) < getMaxCharge(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1 - (double) getCharge(stack) / (double) getMaxCharge(stack);
	}

	@Override
	public long getMaxCharge(ItemStack stack) {

		if(ArmorModHandler.hasMods(stack)) {

			ItemStack mod = ArmorModHandler.pryMod(stack, ArmorModHandler.battery);

			if(!mod.isEmpty() && mod.getItem() instanceof ItemModBattery)
				return (long) (maxPower * ((ItemModBattery) mod.getItem()).mod);
		}

		return maxPower;
	}

	@Override
	public long getChargeRate(ItemStack stack) {
		return chargeRate;
	}

	@Override
	public long getDischargeRate(ItemStack stack) {
		return 0;
	}

	@Override
	public void setDamage(ItemStack stack, int damage) {
		this.dischargeBattery(stack, damage * consumption);
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {

		super.onArmorTick(world, player, itemStack);

		if(this.drain > 0 && ArmorFSB.hasFSBArmor(player) && !player.capabilities.isCreativeMode)
			this.dischargeBattery(itemStack, drain);
	}
}
