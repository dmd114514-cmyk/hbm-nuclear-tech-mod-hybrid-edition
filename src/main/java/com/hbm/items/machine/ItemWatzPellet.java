package com.hbm.items.machine;

import java.util.List;
import java.util.Locale;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.function.Function;
import com.hbm.util.function.Function.FunctionLinear;
import com.hbm.util.function.Function.FunctionQuadratic;
import com.hbm.util.function.Function.FunctionSqrt;
import com.hbm.util.function.Function.FunctionSqrtFalling;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/*
 * The Watz isotope fuels (oxidized states).
 *
 * Subtypes here only describe "what it is, what curve it burns on, what color"; the actual reaction runs in the machines —
 * the fuel's burn progress (yield) lives on the item's own NBT, with static access doors here for machines.
 *
 * The color isn't a pile of textures but **one grayscale base re-tinted per pixel at load** (see ItemWatzSprites):
 * the base carries its own shading gradient, and an ItemColors flat tint would flatten it away; the depleted piece (`watz_pellet_depleted`) runs
 * the color through an extra `desaturate` pass.
 */
public class ItemWatzPellet extends ItemEnumMulti {

	public ItemWatzPellet() {
		super(EnumWatzType.class, true, true);
		this.setMaxStackSize(16);
		this.setCreativeTab(ModItems.controlTab);
	}

	public static enum EnumWatzType {

		SCHRABIDIUM(	0x32FFFF, 0x005C5C, 2_000,	20D,	0.01D,		new FunctionLinear(1.5D), new FunctionSqrtFalling(10D), null),
		HES(			0x66DCD6, 0x023933, 1_750,	20D,	0.005D,		new FunctionLinear(1.25D), new FunctionSqrtFalling(15D), null),
		MES(			0xCBEADF, 0x28473C, 1_500,	15D,	0.0025D,	new FunctionLinear(1.15D), new FunctionSqrtFalling(15D), null),
		LES(			0xABB4A8, 0x0C1105, 1_250,	15D,	0.00125D,	new FunctionLinear(1D), new FunctionSqrtFalling(20D), null),
		HEN(			0xA6B2A6, 0x030F03, 0,		10D,	0.0005D,	new FunctionSqrt(100), new FunctionSqrtFalling(10D), null),
		MEU(			0xC1C7BD, 0x2B3227, 0,		10D,	0.0005D,	new FunctionSqrt(75), new FunctionSqrtFalling(10D), null),
		MEP(			0x9AA3A0, 0x111A17, 0,		15D,	0.0005D,	new FunctionSqrt(150), new FunctionSqrtFalling(10D), null),
		LEAD(			0xA6A6B2, 0x03030F, 0,		0,		0.0025D,	null, null, new FunctionSqrt(10)), //the standard absorber; a lead-family cousin, this one is quadratic
		BORON(			0xBDC8D2, 0x29343E, 0,		0,		0.0025D,	null, null, new FunctionLinear(10)), //the improved absorber; linear instead of sqrt
		DU(				0xC1C7BD, 0x2B3227, 0,		0,		0.0025D,	null, null, new FunctionQuadratic(1D, 1D).withDiv(100)), //the depleted-uranium tier, quadratic with a /100 divider
		NQD(			0x4B4B4B, 0x121212, 2_000,	20,		0.01D,		new FunctionLinear(2D), new FunctionSqrt(1D/25D).withOff(25D * 25D), null),
		NQR(			0x2D2D2D, 0x0B0B0B, 2_500,	30,		0.01D,		new FunctionLinear(1.5D), new FunctionSqrt(1D/25D).withOff(25D * 25D), null),
		PU241(			0x78817E, 394240, 1_950,	25,		0.0025D,		new FunctionLinear(1.30D), new FunctionSqrt(2.66D/18D).withOff(24D * 24D), null),
		AMRG(			0x93767B, 0x66474D, 2_888,	48,		0.0035D,		new FunctionLinear(1.33D), new FunctionSqrt(4.33D/25.5D).withOff(28D * 28D), null),
		AMF(			0x93767B, 0x66474D, 2_333,	44,		0.003D,		new FunctionLinear(1.33D), new FunctionSqrt(4.11D/22.2D).withOff(27D * 27D), null),

		CMRG(			0xD8C2C4, 0xAD9799, 2_999,	50,		0.005D,		new FunctionLinear(1.5D), new FunctionSqrt(5.5D/25.5D).withOff(30D * 28D), null),
		CMF(			0xD8C2C4, 0xAD9799, 2_444,	48,		0.0045D,		new FunctionLinear(1.8D), new FunctionSqrt(5.0D/20D).withOff(26D * 24D), null),
		BK247(			0xC2C9C7, 0x8D9592, 3_000,	55,		0.012D,		new FunctionLinear(1.5D), new FunctionSqrt(6.0D/23.5D).withOff(10D * 10D), null),
		CF251(			0x7879B4, 0x4D4E89, 1_250,	60,		0.001D,		new FunctionLinear(1.7D), new FunctionSqrt(6.65D/23.5D).withOff(10D * 10D), null),

		CF252(			0x7879B4, 0x4D4E89, 1_050,	120,		0.0015D,		new FunctionLinear(1.8D), new FunctionSqrt(8.85D/28.8D).withOff(10D * 10D), null),
		ES253(			0xB9BFB2, 0x594E44, 3_750,	70,		0.0001D,		new FunctionLinear(1.3D), new FunctionSqrt(7.0D/27.7D).withOff(10D * 10D), null);

		public double yield = 1_000_000_000;
		public int colorLight;
		public int colorDark;
		public double mudContent;	// how much "mud" per unit of reaction flux
		public double passive;		// the base flux emission
		public double heatEmission;	// reactivity(1) to heat (heat per unit of outbound flux)
		public Function burnFunc;	// flux to reactivity(0) (classic reactivity)
		public Function heatDiv;	// reactivity(0) to reactivity(1), by heat (the temperature coefficient)
		public Function absorbFunc;	// flux to heat (the inert components' flux absorption)

		private EnumWatzType(int colorLight, int colorDark, double passive, double heatEmission, double mudContent, Function burnFunction, Function heatDivisor, Function absorbFunction) {
			this.colorLight = colorLight;
			this.colorDark = colorDark;
			this.passive = passive;
			this.heatEmission = heatEmission;
			this.mudContent = mudContent / 2D;
			this.burnFunc = burnFunction;
			this.heatDiv = heatDivisor;
			this.absorbFunc = absorbFunction;
		}
	}

	/** Out-of-range meta clamps into range, so a corrupted meta in an old save doesn't crash the getters */
	public static EnumWatzType type(int meta) {
		EnumWatzType[] values = EnumWatzType.values();
		return values[Math.abs(meta % values.length)];
	}

	/**
	 * The sprite name per subtype. Only one base exists, `textures/items/watz_pellet.png`,
	 * so **the segment before the first dash is always `items/watz_pellet`** — `TextureAtlasSpriteMutatable` cuts at the first dash
	 * to find the base, and what follows the dash is just a suffix keeping the colorways from colliding. The depleted piece shares the base image,
	 */
	public static String spriteName(net.minecraft.item.Item item, int meta) {
		boolean depleted = item == ModItems.watz_pellet_depleted;
		return "hbm:items/watz_pellet-" + (depleted ? "depleted_" : "") + type(meta).name().toLowerCase(Locale.US);
	}

	/** The depleted color: press the color toward gray first, then darken overall — the old coefficients exactly */
	public static int desaturate(int color) {

		int r = (color & 0xff0000) >> 16;
		int g = (color & 0x00ff00) >> 8;
		int b = (color & 0x0000ff);

		int avg = (r + g + b) / 3;
		double approach = 0.9;
		double mult = 0.75;

		r -= (r - avg) * approach;
		g -= (g - avg) * approach;
		b -= (b - avg) * approach;

		r *= mult;
		g *= mult;
		b *= mult;

		return (r << 16) | (g << 8) | b;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(this != ModItems.watz_pellet) return;

		EnumWatzType num = type(stack.getItemDamage());

		list.add(TextFormatting.GREEN + "Depletion: " + String.format(Locale.US, "%.1f", getDurabilityForDisplay(stack) * 100D) + "%");

		String color = TextFormatting.GOLD + "";
		String reset = TextFormatting.RESET + "";

		if(num.passive > 0) {
			list.add(color + "Base fission rate: " + reset + num.passive);
			list.add(TextFormatting.RED + "Self-igniting!");
		}
		if(num.heatEmission > 0) list.add(color + "Heat per flux: " + reset + num.heatEmission + " TU");
		if(num.burnFunc != null) {
			list.add(color + "Reaction function: " + reset + num.burnFunc.getLabelForFuel());
			list.add(color + "Fuel type: " + reset + num.burnFunc.getDangerFromFuel());
		}
		if(num.heatDiv != null) list.add(color + "Thermal multiplier: " + reset + num.heatDiv.getLabelForFuel() + " TU⁻¹");
		if(num.absorbFunc != null) list.add(color + "Flux capture: " + reset + num.absorbFunc.getLabelForFuel());
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return this == ModItems.watz_pellet && getDurabilityForDisplay(stack) > 0D;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1D - getEnrichment(stack);
	}

	public static double getEnrichment(ItemStack stack) {
		EnumWatzType num = type(stack.getItemDamage());
		return getYield(stack) / num.yield;
	}

	public static double getYield(ItemStack stack) {
		return getDouble(stack, "yield");
	}

	public static void setYield(ItemStack stack, double yield) {
		setDouble(stack, "yield", yield);
	}

	public static void setDouble(ItemStack stack, String key, double value) {
		if(!stack.hasTagCompound()) setNBTDefaults(stack);
		stack.getTagCompound().setDouble(key, value);
	}

	public static double getDouble(ItemStack stack, String key) {
		if(!stack.hasTagCompound()) setNBTDefaults(stack);
		return stack.getTagCompound().getDouble(key);
	}

	private static void setNBTDefaults(ItemStack stack) {
		EnumWatzType num = type(stack.getItemDamage());
		// attach the compound before writing values: otherwise setYield → setDouble re-enters here, an infinite loop
		stack.setTagCompound(new NBTTagCompound());
		setYield(stack, num.yield);
	}

	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) {
		if(this != ModItems.watz_pellet) return;
		setNBTDefaults(stack); // narrow the "not yet initialized" window as much as possible
	}
}
