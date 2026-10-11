package com.hbm.inventory;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotSmelting extends SlotItemHandler {

	private EntityPlayer thePlayer;
	private int itemCountBuffer;

	public SlotSmelting(EntityPlayer player, IItemHandler handler, int id, int x, int y) {
		super(handler, id, x, y);
		this.thePlayer = player;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {

		if(this.getHasStack()) {
			this.itemCountBuffer += Math.min(amount, this.getStack().getCount());
		}

		return super.decrStackSize(amount);
	}

	// 1.12.2 renamed onPickupFromSlot to onTake, and the stack must go back
	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack) {
		this.onCrafting(stack);
		return super.onTake(player, stack);
	}

	@Override
	protected void onCrafting(ItemStack stack, int amount) {
		this.itemCountBuffer += amount;
		this.onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {

		stack.onCrafting(this.thePlayer.world, this.thePlayer, this.itemCountBuffer);

		if(!this.thePlayer.world.isRemote) {

			int buffer = this.itemCountBuffer;
			float exp = FurnaceRecipes.instance().getSmeltingExperience(stack);
			int remainingExp;

			if(exp == 0.0F) {
				buffer = 0;

			} else if(exp < 1.0F) {
				remainingExp = MathHelper.floor(buffer * exp);

				if(remainingExp < MathHelper.ceil(buffer * exp) && (float) Math.random() < buffer * exp - remainingExp) {
					++remainingExp;
				}

				buffer = remainingExp;
			}

			while(buffer > 0) {
				remainingExp = EntityXPOrb.getXPSplit(buffer);
				buffer -= remainingExp;
				this.thePlayer.world.spawnEntity(new EntityXPOrb(this.thePlayer.world, this.thePlayer.posX, this.thePlayer.posY + 0.5D, this.thePlayer.posZ + 0.5D, remainingExp));
			}
		}

		this.itemCountBuffer = 0;

		FMLCommonHandler.instance().firePlayerSmeltedEvent(thePlayer, stack);

		// 1.12.2 replaced achievements with advancements and AchievementList is gone entirely; the old iron-ingot/cooked-fish triggers end here
	}
}
