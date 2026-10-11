package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The graphite pile's fuel rods (MK2). The rod itself is just a tube; the temperament is all in the enum:
 * how many neutrons it emits on its own, how much external flux amplifies it, how long it burns, and what it becomes when spent.
 *
 * Tooltips wrap by pixel width. 1.7.10 used the homegrown autoBreak; the 1.12.2 note reads
 * "pixel wrapping belongs to the GUI layer, arriving with the GUI migration" — vanilla's own
 * `listFormattedStringToWidth` is used here directly; same semantics, no need to wait for that utility.
 */
public class ItemPileRodMK2 extends ItemEnumMulti {

	/*  _____
	 * /     \
	 * \_____/
	 * |     |
	 * |     |
	 * |     |
	 * |     |
	 * \_____/
	 *
	 * Not terribly sophisticated,
	 * it's a cylinder.
	 */

	public static final String KEY_NBT_DEPLETION = "depletion";

	public ItemPileRodMK2() {
		super(EnumPileRod.class, true, true);
	}

	public static enum EnumPileRod {
		/* 0 */ RA226BE(1D),
		/* 1 */ PO210BE(1D),
		/* 2 */ ZR(		0D,      0D, 0D,    2),
		/* 3 */ NU(		1D, 25_000D, 0.25D, 4),
		/* 4 */ PU239(	1D,    500D, 0.5D,  5),
		/* 5 */ RGP(	1D,  1_000D, 0.5D,  6),
		/* 6 */ WASTE(	1D,      0D, 1.5D,  6);

		public double reactionMult = 1.0D;
		public double life = 1_000D;
		public double heatMult = 0.0D;
		public double neutronSource = 0D;
		public int turnsInto;

		private EnumPileRod(double neutronSource) {
			this.neutronSource = neutronSource;
			this.reactionMult = 0;
			this.life = 0;
			this.heatMult = 0;
		}

		private EnumPileRod(double reaction, double life, double heat, int turnsInto) {
			this.reactionMult = reaction;
			this.life = life;
			this.heatMult = heat;
			this.turnsInto = turnsInto;
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		EnumPileRod rod = (EnumPileRod) grab(theEnum, stack.getItemDamage());

		if(rod.life > 0) {
			list.add("Lifetime: " + (int) Math.round(rod.life));
			double depletion = getDepletionPercent(stack);
			if(depletion > 0) list.add("Depletion: " + (int) Math.round(depletion) + "%");
		}

		String desc = I18nUtil.resolveKey(this.getUnlocalizedName(stack) + ".desc");

		// Wrapping wants the font renderer, a client thing; when the server can't get one the whole block spits out
		String[] lines = (world != null && world.isRemote)
				? Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(desc, 225).toArray(new String[0])
				: new String[] { desc };

		for(String line : lines) list.add(TextFormatting.YELLOW + line);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {

		EnumPileRod rod = (EnumPileRod) grab(theEnum, stack.getItemDamage());
		double life = rod.life;
		if(life <= 0) return 0D;

		return getDepletion(stack) / life;
	}

	public static double getDepletionPercent(ItemStack stack) {

		if(stack == null || stack.isEmpty()) return 0D;

		EnumPileRod rod = grabRod(stack);
		double life = rod.life;
		if(life <= 0) return 0D;

		return (getDepletion(stack) / life) * 100;
	}

	public static double getDepletion(ItemStack stack) {

		if(!stack.hasTagCompound()) return 0D;

		return stack.getTagCompound().getDouble(KEY_NBT_DEPLETION);
	}

	public static void setDepletion(ItemStack stack, double depletion) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setDouble(KEY_NBT_DEPLETION, depletion);
	}

	public static double getReactivity(ItemStack stack, double inFlux) {

		EnumPileRod rod = grabRod(stack);
		double outFlux = rod.neutronSource;

		if(rod.reactionMult > 0) outFlux += BobMathUtil.squirt(inFlux) * rod.reactionMult;

		return outFlux;
	}

	public static double getHeatPerNeutron(ItemStack stack) {
		return grabRod(stack).heatMult;
	}

	public static ItemStack react(ItemStack stack, double inFlux) {

		EnumPileRod rod = grabRod(stack);
		if(rod.life <= 0) return stack;

		double dep = getDepletion(stack) + inFlux;

		if(dep < rod.life) {
			setDepletion(stack, dep);
			return stack;
		}

		// burnt through: the same rod becomes its designated successor tier outright
		return new ItemStack(stack.getItem(), 1, rod.turnsInto);
	}

	private static EnumPileRod grabRod(ItemStack stack) {
		EnumPileRod[] values = EnumPileRod.values();
		return values[Math.abs(stack.getItemDamage() % values.length)];
	}
}
