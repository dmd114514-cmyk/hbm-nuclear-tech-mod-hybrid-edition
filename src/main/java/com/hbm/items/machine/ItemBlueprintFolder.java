package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.IMultiModelItem;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

/**
 * The blueprint folder. Tearing a page yields a blueprint; the folder itself comes in meta 0/1/2:
 * respectively yielding only the `alt.`, `discover.` and `secret.` prefixed pools.
 *
 * The three folders each carry one texture, with model names that can't be derived (not the `<name>_<meta>` pattern),
 * so they self-report `subtypeModels()`. The old creative tab listed only 0 and 1 — the secret folder comes from machines/loot,
 */
public class ItemBlueprintFolder extends Item implements IMultiModelItem {

	public ItemBlueprintFolder() {
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	@Override
	public String[] subtypeModels() {
		String name = this.getRegistryName().getResourcePath();
		return new String[] { name, name + "_discover", name + "_secret" };
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(int i = 0; i < 2; i++) list.add(new ItemStack(this, 1, i));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(world.isRemote) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

		List<String> pools = new ArrayList<String>();

		for(String pool : GenericRecipes.blueprintPools.keySet()) {
			if(stack.getItemDamage() == 0 && pool.startsWith(GenericRecipes.POOL_PREFIX_ALT)) pools.add(pool);
			if(stack.getItemDamage() == 1 && pool.startsWith(GenericRecipes.POOL_PREFIX_DISCOVER)) pools.add(pool);
			if(stack.getItemDamage() == 2 && pool.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) pools.add(pool);
		}

		if(pools.isEmpty()) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

		stack.shrink(1);

		String chosen = pools.get(player.getRNG().nextInt(pools.size()));
		ItemStack blueprint = ItemBlueprints.make(chosen);

		if(!player.inventory.addItemStackToInventory(blueprint)) player.dropItem(blueprint, false);

		// the torn page is decremented server-side; without a push the client keeps holding the old folder
		player.inventoryContainer.detectAndSendChanges();
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
}
