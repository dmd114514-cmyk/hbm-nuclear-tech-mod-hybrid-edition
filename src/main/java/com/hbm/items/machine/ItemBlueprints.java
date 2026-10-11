package com.hbm.items.machine;

import java.util.List;
import java.util.Map.Entry;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The blueprints. Which recipe pool it carries is in NBT's `pool` — the blueprint itself has no meta,
 * one item covering a whole pool, so NBT distinguishes rather than subtypes.
 *
 * The pool prefix (discover / secret / 528) decides which of four images it wears. The old version picked among four icons in `getIcon`;
 * item looks are model-JSON-driven here, and one item swapping textures by NBT can only ride `overrides` predicates,
 * so the four colorways land in `blueprints.json`'s overrides, predicates hung in ItemsMachine's model registration section.
 */
public class ItemBlueprints extends Item {

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(Entry<String, List<String>> pool : GenericRecipes.blueprintPools.entrySet()) {
			String poolName = pool.getKey();
			if(!poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) list.add(make(poolName));
		}
	}

	/*Copies a blueprint off a sheet of paper; the secret pools refuse copying. */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(world.isRemote) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		if(!stack.hasTagCompound()) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

		String poolName = stack.getTagCompound().getString("pool");
		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET))
			return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

		int paperSlot = player.inventory.getSlotFor(new ItemStack(Items.PAPER));
		if(paperSlot < 0) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

		player.inventory.decrStackSize(paperSlot, 1);
		player.swingArm(hand);

		ItemStack copy = stack.copy();
		copy.setCount(1);

		if(player.capabilities.isCreativeMode) {
			player.dropItem(copy, false);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(stack.getCount() < stack.getMaxStackSize()) stack.grow(1);
		else if(!player.inventory.addItemStackToInventory(copy)) player.dropItem(copy, false);

		// the held stack's count is changed server-side; without a push the client shows the old count forever
		player.inventoryContainer.detectAndSendChanges();
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(!stack.hasTagCompound()) return;

		String poolName = stack.getTagCompound().getString("pool");
		List<String> pool = GenericRecipes.blueprintPools.get(poolName);

		if(pool == null || pool.isEmpty()) return;

		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) {
			list.add(TextFormatting.RED + "Cannot be copied!");
		} else {
			list.add(TextFormatting.YELLOW + "Right-click to copy (requires paper)");
		}

		for(String name : pool) {
			GenericRecipe recipe = GenericRecipes.nameToRecipeGlobal.get(name);
			if(recipe != null) list.add(recipe.getLocalizedName());
		}
	}

	public static String grabPool(ItemStack stack) {
		if(stack == null) return null;
		if(stack.getItem() != ModItems.blueprints) return null;
		if(!stack.hasTagCompound()) return null;
		if(!stack.getTagCompound().hasKey("pool")) return null;
		return stack.getTagCompound().getString("pool");
	}

	/*The prefix check for the model predicate; null stacks or pool-less ones all take the default blueprint */
	private static boolean hasPrefix(ItemStack stack, String prefix) {
		String pool = grabPool(stack);
		return pool != null && pool.startsWith(prefix);
	}

	public static boolean isDiscover(ItemStack stack) {
		return hasPrefix(stack, GenericRecipes.POOL_PREFIX_DISCOVER);
	}

	public static boolean isSecret(ItemStack stack) {
		return hasPrefix(stack, GenericRecipes.POOL_PREFIX_SECRET);
	}

	public static boolean is528(ItemStack stack) {
		return hasPrefix(stack, GenericRecipes.POOL_PREFIX_528);
	}

	public static ItemStack make(String pool) {
		ItemStack stack = new ItemStack(ModItems.blueprints);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("pool", pool);
		stack.setTagCompound(nbt);
		return stack;
	}
}
