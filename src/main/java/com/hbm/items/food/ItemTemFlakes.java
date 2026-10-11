package com.hbm.items.food;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

/**
 * tem's cereal. All three tiers restore two hearts, differing only in packaging — vanilla's writing,
 * with the three tooltips deliberately growing more disdainful per tier.
 */
public class ItemTemFlakes extends ItemFoodBase {

	public ItemTemFlakes(int hunger, float saturation, boolean isDogFood) {
		super(hunger, saturation, isDogFood);
		this.setHasSubtypes(true);
		this.setAlwaysEdible();
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		// healing rides the food domain's `heal` knob; absent from the table it stays the original 2HP
		player.heal(tweakFood(stack, "heal", 2F));
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
		list.add(new ItemStack(this, 1, 2));
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(stack.getItemDamage() == 0) list.add("Heals 2HP DISCOUNT FOOD OF TEM!!!");
		if(stack.getItemDamage() == 1) list.add("Heals 2HP food of tem");
		if(stack.getItemDamage() == 2) list.add("Heals food of tem (expensiv)");
	}
}
