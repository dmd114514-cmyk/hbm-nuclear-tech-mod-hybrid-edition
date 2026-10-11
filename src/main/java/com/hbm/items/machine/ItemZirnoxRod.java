package com.hbm.items.machine;

import java.util.List;
import java.util.Locale;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The high-temp gas reactor's fuel rods. Lifespan rides NBT's `life`, the durability bar drawn by the fraction of max life.
 * Two of the metas are breeding rods (`breeding`), swapping to a different tooltip set.
 */
public class ItemZirnoxRod extends ItemEnumMulti {

	public ItemZirnoxRod() {
		super(EnumZirnoxType.class, true, true);
		this.setMaxStackSize(1);
		this.canRepair = false;
	}

	public static void incrementLifeTime(ItemStack stack) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("life", stack.getTagCompound().getInteger("life") + 1);
	}

	public static void setLifeTime(ItemStack stack, int time) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("life", time);
	}

	public static int getLifeTime(ItemStack stack) {

		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getInteger("life");
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) getLifeTime(stack) / (double) ((EnumZirnoxType) grab(theEnum, stack.getItemDamage())).maxLife;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "_" + grab(theEnum, stack.getItemDamage()).name().toLowerCase(Locale.US);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		EnumZirnoxType type = (EnumZirnoxType) grab(theEnum, stack.getItemDamage());

		list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmk.depletion", ((int) ((((double) getLifeTime(stack)) / (double) type.maxLife) * 100000)) / 1000D + "%"));

		String[] loc = type.breeding
				? I18nUtil.resolveKeyArray("desc.item.zirnoxBreedingRod", BobMathUtil.getShortNumber(type.maxLife))
				: I18nUtil.resolveKeyArray("desc.item.zirnoxRod", type.heat, BobMathUtil.getShortNumber(type.maxLife));

		for(String s : loc) list.add(s);
	}

	public static enum EnumZirnoxType {
		NATURAL_URANIUM_FUEL(250_000, 30),
		URANIUM_FUEL(200_000, 50),
		TH232(20_000, 0, true),
		THORIUM_FUEL(200_000, 40),
		MOX_FUEL(165_000, 75),
		PLUTONIUM_FUEL(175_000, 65),
		U233_FUEL(150_000, 100),
		U235_FUEL(165_000, 85),
		LES_FUEL(150_000, 150),
		LITHIUM(20_000, 0, true),
		ZFB_MOX(50_000, 35);

		public final int maxLife;
		public final int heat;
		public final boolean breeding;

		private EnumZirnoxType(int life, int heat, boolean breeding) {
			this.maxLife = life;
			this.heat = heat;
			this.breeding = breeding;
		}

		private EnumZirnoxType(int life, int heat) {
			this.maxLife = life;
			this.heat = heat;
			this.breeding = false;
		}
	}
}
