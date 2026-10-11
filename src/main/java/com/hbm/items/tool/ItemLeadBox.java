package com.hbm.items.tool;

import com.hbm.inventory.container.ContainerLeadBox;
import com.hbm.inventory.gui.GUILeadBox;
import com.hbm.items.ItemInventory;
import com.hbm.items.block.ItemBlockStorageCrate;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The lead box (containment_box): a hand-opened little box, 20 cells, one item per cell,
 * contents in the item's own NBT, so the GUI is the item's own (riding GUIHandler's "held provider" path).
 *
 * One item per cell is the whole point: shielding counts per piece for radioactive goods; stacks would muddy the math.
 */
public class ItemLeadBox extends Item implements IGUIProvider {

	public ItemLeadBox() {
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
		return new ContainerLeadBox(player.inventory, new InventoryLeadBox(player, player.getHeldItemMainhand()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUILeadBox(player.inventory, new InventoryLeadBox(player, player.getHeldItemMainhand()));
	}

	/** The lead box contents view glued to the item's NBT */
	public static class InventoryLeadBox extends ItemInventory {

		public InventoryLeadBox(EntityPlayer player, ItemStack box) {

			this.player = player;
			this.target = box;
			this.slots = new ItemStack[this.getSizeInventory()];

			if(!box.hasTagCompound()) box.setTagCompound(new NBTTagCompound());

			ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(box, slots.length);

			if(fromNBT != null) {
				System.arraycopy(fromNBT, 0, slots, 0, slots.length);
			}
		}

		@Override
		public int getSizeInventory() {
			return 20;
		}

		@Override
		public String getName() {
			return "container.leadBox";
		}

		@Override
		public boolean hasCustomName() {
			return target.hasDisplayName();
		}

		@Override
		public net.minecraft.util.text.ITextComponent getDisplayName() {
			return new net.minecraft.util.text.TextComponentString(this.hasCustomName() ? target.getDisplayName() : net.minecraft.util.text.translation.I18n.translateToLocal(this.getName()));
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			// crates themselves don't fit in the lead box: their contents aren't in NBT, so "boxing a crate" is meaningless
			return stack.isEmpty() || !(stack.getItem() instanceof ItemBlockStorageCrate);
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

		@Override
		public boolean isUsableByPlayer(EntityPlayer player) {
			return player.getHeldItemMainhand() == this.target;
		}
	}
}
