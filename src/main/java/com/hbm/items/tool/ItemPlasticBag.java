package com.hbm.items.tool;

import com.hbm.inventory.container.ContainerPlasticBag;
import com.hbm.inventory.gui.GUIPlasticBag;
import com.hbm.items.ItemInventory;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.ItemStackUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The plastic bag (plastic_bag): one cell, one item, opened from hand.
 * The hazard shielding recognizes it (HazardTransformerRadiationContainer checks exactly this piece),
 * so "one bag, one item" is semantically required, not laziness.
 */
public class ItemPlasticBag extends Item implements IGUIProvider {

	public ItemPlasticBag() {
		this.setMaxStackSize(1);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 1;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(!world.isRemote) player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerPlasticBag(player.inventory, new InventoryPlasticBag(player, player.getHeldItemMainhand()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUIPlasticBag(player.inventory, new InventoryPlasticBag(player, player.getHeldItemMainhand()));
	}

	/** The one-cell view glued to the item's NBT: contents travel with the bag */
	public static class InventoryPlasticBag extends ItemInventory {

		public InventoryPlasticBag(EntityPlayer player, ItemStack bag) {

			this.player = player;
			this.target = bag;
			this.slots = new ItemStack[this.getSizeInventory()];

			if(!bag.hasTagCompound()) bag.setTagCompound(new NBTTagCompound());

			ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(bag, slots.length);

			if(fromNBT != null) {
				System.arraycopy(fromNBT, 0, slots, 0, slots.length);
			}
		}

		@Override
		public int getSizeInventory() {
			return 1;
		}

		@Override
		public String getName() {
			return "container.plasticBag";
		}

		@Override
		public boolean hasCustomName() {
			return target.hasDisplayName();
		}

		@Override
		public ITextComponent getDisplayName() {
			return new TextComponentString(this.hasCustomName() ? target.getDisplayName() : net.minecraft.util.text.translation.I18n.translateToLocal(this.getName()));
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public boolean isEmpty() {
			for(ItemStack stack : slots) {
				if(stack != null && !stack.isEmpty()) return false;
			}
			return true;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			return this.takeStackOnClosing(index);
		}

		@Override public int getField(int id) { return 0; }
		@Override public void setField(int id, int value) { }
		@Override public int getFieldCount() { return 0; }

		@Override
		public void clear() {
			for(int i = 0; i < slots.length; i++) slots[i] = null;
		}
	}
}
