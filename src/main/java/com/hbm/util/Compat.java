package com.hbm.util;

import java.util.ArrayList;
import java.util.List;

import com.hbm.handler.HazmatRegistry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/*Where cross-version/cross-mod compat odds and ends live */
public final class Compat {

	/*
	 * Optional mod ids. These names feed compat branching — a typo neither errors nor warns, it just takes the wrong branch silently,
	 * so they're centralized here; no hand-typed strings in business code.
	 */
	public static final String MOD_GT6 = "gregtech";
	public static final String MOD_GCC = "GalacticraftCore";
	public static final String MOD_AR = "advancedrocketry";
	public static final String MOD_EF = "etfuturum";
	public static final String MOD_REC = "ReactorCraft";
	public static final String MOD_TIC = "TConstruct";
	public static final String MOD_RC = "Railcraft";
	public static final String MOD_TC = "tc";
	public static final String MOD_EIDS = "endlessids";
	public static final String MOD_ANG = "angelica";
	public static final String MOD_BOP = "BiomesOPlenty";
	public static final String MOD_COFH = "CoFHCore";
	public static final String MOD_TOR = "Torcherino";
	public static final String MOD_OC = "OpenComputers";

	private Compat() { }

	public static boolean isModLoaded(String modid) {
		return Loader.isModLoaded(modid);
	}

	/*Coordinate-checked TE fetch; an out-of-range y is simply null, not an exception from the world */
	public static TileEntity getTileStandard(World world, int x, int y, int z) {
		if(y < 0 || y > 255) return null;
		return world.getTileEntity(new BlockPos(x, y, z));
	}

	public static TileEntity getTileStandard(IBlockAccess world, int x, int y, int z) {
		if(y < 0 || y > 255) return null;
		return world.getTileEntity(new BlockPos(x, y, z));
	}

	/**
	 * Fetches one of another mod's items by name; null when absent.
	 * Every compat check takes this "missing means skip" path; an uninstalled mod must never crash this mod.
	 */
	public static Item tryLoadItem(String domain, String name) {
		return ForgeRegistries.ITEMS.getValue(new ResourceLocation(domain, name));
	}

	/*
	 * Other mods' hazmat radiation resistance. Same convention as the HazmatRegistry batch:
	 * total coefficient split across pieces by head 0.2 / chest 0.4 / legs 0.3 / boots 0.1.
	 */
	public static void registerCompatHazmat() {

		double helmet = 0.2D;
		double chest = 0.4D;
		double legs = 0.3D;
		double boots = 0.1D;

		double p90 = 1.0D; // 90%
		double p99 = 2D; // 99%

		tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.radiation.head", p90 * helmet);
		tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.radiation.chest", p90 * chest);
		tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.radiation.legs", p90 * legs);
		tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.radiation.boots", p90 * boots);

		tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.universal.head", p99 * helmet);
		tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.universal.chest", p99 * chest);
		tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.universal.legs", p99 * legs);
		tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.universal.boots", p99 * boots);

		tryRegisterHazmat(MOD_REC, "reactorcraft_item_hazhelmet", p99 * helmet);
		tryRegisterHazmat(MOD_REC, "reactorcraft_item_hazchest", p99 * chest);
		tryRegisterHazmat(MOD_REC, "reactorcraft_item_hazlegs", p99 * legs);
		tryRegisterHazmat(MOD_REC, "reactorcraft_item_hazboots", p99 * boots);

		tryRegisterHazmat(MOD_EF, "netherite_helmet", p90 * helmet);
		tryRegisterHazmat(MOD_EF, "netherite_chestplate", p90 * chest);
		tryRegisterHazmat(MOD_EF, "netherite_leggings", p90 * legs);
		tryRegisterHazmat(MOD_EF, "netherite_boots", p90 * boots);
	}

	private static void tryRegisterHazmat(String mod, String name, double resistance) {
		Item item = tryLoadItem(mod, name);
		if(item != null) {
			HazmatRegistry.registerHazmat(item, resistance);
		}
	}

	/**
	 * Reads an AE2 storage cell's contents into a list, for the hazard side to inventory.
	 * <br><br>
	 * Tag names belong to AE2: "it" is the item kind count, "#i" the i-th item, "@i" its amount; read-only from this mod.
	 * Two deltas this version: ItemStack.loadItemStackFromNBT is gone, replaced by new ItemStack(compound);
	 * and AE2 writes no tag for empty cells rather than an empty stack, so hasKey("id") must come before isEmpty,
	 * or unreadable cells would count as valid items in the dose.
	 * The outer try stays from the old version: the cell was written by another mod; a broken structure should lose a few items, not break the whole hazard settlement.
	 */
	public static List<ItemStack> scrapeItemFromME(ItemStack meDrive) {
		List<ItemStack> stacks = new ArrayList<ItemStack>();

		try {
			if(meDrive != null && meDrive.hasTagCompound()) {
				NBTTagCompound nbt = meDrive.getTagCompound();
				int types = nbt.getShort("it"); // ITEM_TYPE_TAG

				for(int i = 0; i < types; i++) {
					NBTBase stackTag = nbt.getTag("#" + i);

					if(stackTag instanceof NBTTagCompound) {
						NBTTagCompound compound = (NBTTagCompound) stackTag;

						if(!compound.hasKey("id")) continue;

						ItemStack stack = new ItemStack(compound);
						if(stack.isEmpty()) continue;

						stack.setCount(nbt.getInteger("@" + i));
						stacks.add(stack);
					}
				}
			}
		} catch(Exception ex) { }

		return stacks;
	}
}
