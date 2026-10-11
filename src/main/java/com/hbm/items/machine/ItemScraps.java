package com.hbm.items.machine;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.items.IColoredItem;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemAutogen;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Foundry scraps. meta is the material id; how much the pile holds rides NBT's `amount`.
 *
 * It has no shape (`super(null)`), so it skips the autogen path — all metas share one base image;
 * only the "molten" and "additive" states swap to two others, expressed by the model JSON's `overrides`
 * (predicates registered by this class), not per-meta texture swaps.
 */
public class ItemScraps extends ItemAutogen implements IColoredItem {

	public ItemScraps() {
		super(null);
		this.setMaxStackSize(64);
	}

	/** All metas point at the same model; molten/additive taken over by overrides */
	@Override
	public String[] subtypeModels() {

		int max = 0;
		for(NTMMaterial mat : Mats.orderedList) if(mat.id > max) max = mat.id;

		String[] names = new String[max + 1];
		String name = this.getRegistryName().getResourcePath();

		for(NTMMaterial mat : Mats.orderedList) names[mat.id] = name;

		return names;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.smeltable == SmeltingBehavior.SMELTABLE || mat.smeltable == SmeltingBehavior.ADDITIVE) {
				list.add(new ItemStack(this, 1, mat.id));
			}
		}
	}

	public static boolean isLiquid(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().getBoolean("liquid");
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {

		// only the molten version tints by melt color; the solid scrap is a real-object texture and mustn't be tinted
		if(!isLiquid(stack)) return 0xFFFFFF;

		NTMMaterial mat = Mats.matById.get(stack.getItemDamage());
		return mat != null ? mat.moltenColor : 0xFFFFFF;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		MaterialStack contents = getMats(stack);

		if(contents == null) return "Foundry Scraps";

		String matName = contents.material.getUnlocalizedName();

		if(isLiquid(stack)) return I18nUtil.resolveKey(matName);

		return I18nUtil.resolveKey(this.getUnlocalizedNameInefficiently(stack) + ".name", I18nUtil.resolveKey(matName)).trim();
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		MaterialStack contents = getMats(stack);
		if(contents == null) return;

		boolean shift = world != null && world.isRemote && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		list.add(Mats.formatAmount(contents.amount, shift));

		if(isLiquid(stack) && contents.material.smeltable == SmeltingBehavior.ADDITIVE) {
			list.add(TextFormatting.DARK_RED + "Additive, not castable!");
		}
	}

	/** Which material and how much a scrap pile carries — exactly what `Mats.scrapMats` plugs into */
	public static MaterialStack getMats(ItemStack stack) {

		if(stack.isEmpty() || stack.getItem() != ModItems.scraps) return null;

		NTMMaterial mat = Mats.matById.get(stack.getItemDamage());
		if(mat == null) return null;

		int amount = MaterialShapes.INGOT.q(1);
		if(stack.hasTagCompound()) amount = stack.getTagCompound().getInteger("amount");

		return new MaterialStack(mat, amount);
	}

	public static ItemStack create(MaterialStack stack) {
		return create(stack, false);
	}

	public static ItemStack create(MaterialStack stack, boolean liquid) {

		if(stack.material == null) return new ItemStack(ModItems.nothing);

		ItemStack scrap = new ItemStack(ModItems.scraps, 1, stack.material.id);
		scrap.setTagCompound(new NBTTagCompound());
		scrap.getTagCompound().setInteger("amount", stack.amount);
		if(liquid) scrap.getTagCompound().setBoolean("liquid", true);

		return scrap;
	}
}
