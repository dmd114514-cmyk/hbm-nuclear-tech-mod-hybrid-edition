package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.container.ContainerToolBox;
import com.hbm.inventory.gui.GUIToolBox;
import com.hbm.items.ItemInventory;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.ItemStackUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The toolbox: a 24-slot (3 rows × 8) box you carry, contents stored in the item's own NBT.
 * Plain right-click = swaps the whole hotbar row into the box and puts the box's top row onto the hotbar; shift right-click = opens the GUI to sort at leisure.
 *
 * The GUI is provided by the item itself (riding GUIHandler's "held provider" route), same pattern as the lead box and plastic bag.
 *
 * The old two textures (open kit_toolbox_empty / closed kit_toolbox) didn't come over: item models here are a single layer0,
 * and swapping layers by NBT wants an extra override handler, while open/closed changes no gameplay — so the icon is always the closed one.
 * The isOpen tag itself stays — it's half of the "force vanilla to resend the inventory after closing the GUI" trick.
 */
public class ItemToolBox extends Item implements IGUIProvider {

	public ItemToolBox() {
		this.setMaxStackSize(1);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 1;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add("Click with the toolbox to swap hotbars in/out of the toolbox.");
		tooltip.add("Shift-click with the toolbox to open the toolbox.");
	}

	/** Finds the rows still holding anything (a row of eight counts as active if even one slot isn't empty) */
	public List<Integer> getActiveRows(ItemStack box) {

		ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(box, 24);
		if(stacks == null) return new ArrayList<>();

		List<Integer> activeRows = new ArrayList<>();

		for(int row = 0; row < 3; row++) {
			for(int slot = 0; slot < 8; slot++) {
				if(stacks[row * 8 + slot] != null) {
					activeRows.add(row);
					break;
				}
			}
		}

		return activeRows;
	}

	/**
	 * The hotbar/row swap: the current hotbar row goes into the box whole, the box's topmost active row comes out to take its place,
	 * and the remaining active rows each shift up one; other toolboxes that show up beyond the held one get spat back out — no toolboxes inside toolboxes.
	 */
	public void moveRows(ItemStack box, EntityPlayer player) {

		ItemStack[] endingHotBar = new ItemStack[9];
		ItemStack[] stacksToTransferToBox = new ItemStack[8];

		boolean hasToolbox = false;
		int extraToolboxes = 0;

		for(int i = 0; i < 9; i++) {

			ItemStack slot = player.inventory.getStackInSlot(i);

			if(!slot.isEmpty() && slot.getItem() == ModItems.toolbox && i != player.inventory.currentItem) {

				extraToolboxes++;
				player.dropItem(slot, true);
				player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);

			} else if(i == player.inventory.currentItem) {
				hasToolbox = true;
				endingHotBar[i] = slot;
			} else {
				stacksToTransferToBox[i - (hasToolbox ? 1 : 0)] = slot.isEmpty() ? null : slot;
			}
		}

		if(extraToolboxes > 0) {
			String warn = extraToolboxes == 1 ? "You can't toolbox a toolbox... " : "You can't toolbox a toolbox... (x" + extraToolboxes + ")";
			player.sendMessage(new TextComponentString(TextFormatting.RED + warn));
		}

		ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(box, 24);
		ItemStack[] endingStacks = new ItemStack[24];

		int lowestActiveIndex = Integer.MAX_VALUE;
		int lowestInactiveIndex = Integer.MAX_VALUE;

		if(stacks != null) {

			List<Integer> activeRows = getActiveRows(box);

			for(int i = 0; i < 3; i++) {
				if(activeRows.contains(i))
					lowestActiveIndex = Math.min(i, lowestActiveIndex);
				else
					lowestInactiveIndex = Math.min(i, lowestInactiveIndex);
			}

			// With all three rows full there's no empty row to land in, so the last row becomes the target — it gets shoved out right after
			if(lowestInactiveIndex > 2)
				lowestInactiveIndex = 2;
			else
				lowestInactiveIndex = Math.max(0, lowestInactiveIndex - 1);

			for(Integer activeRowIndex : activeRows) {

				int activeIndex = 8 * activeRowIndex;

				// The topmost active row exits, taking over the hotbar
				if(activeRowIndex == lowestActiveIndex) {
					hasToolbox = false;
					for(int i = 0; i < 9; i++) {
						if(i == player.inventory.currentItem) {
							hasToolbox = true;
							continue;
						}
						endingHotBar[i] = stacks[activeIndex + i - (hasToolbox ? 1 : 0)];
					}
					continue;
				}

				System.arraycopy(stacks, activeIndex, endingStacks, 8 * (activeRowIndex - 1), 8);
			}
		} else {
			// With an empty tag lowestInactiveIndex stays at MAX_VALUE and the hotbar row would copy out of bounds
			lowestInactiveIndex = 0;
		}

		System.arraycopy(stacksToTransferToBox, 0, endingStacks, lowestInactiveIndex * 8, 8);

		for(int i = 0; i < endingHotBar.length; i++) {
			player.inventory.setInventorySlotContents(i, endingHotBar[i] == null ? ItemStack.EMPTY : endingHotBar[i]);
		}

		box.setTagCompound(new NBTTagCompound());
		ItemStackUtil.addStacksToNBT(box, endingStacks);
		// Over 6kB and the contents get dumped on the ground: ItemInventory.markDirty already runs this check, so borrow its pass
		new InventoryToolBox(player, box).markDirty();
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(!world.isRemote) {
			if(!player.isSneaking()) {
				moveRows(stack, player);
				player.inventoryContainer.detectAndSendChanges();
			} else {
				if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setBoolean("isOpen", true);
				player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
			}
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerToolBox(player.inventory, new InventoryToolBox(player, player.getHeldItemMainhand()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUIToolBox(player.inventory, new InventoryToolBox(player, player.getHeldItemMainhand()));
	}

	/** The toolbox contents view glued to the item's NBT */
	public static class InventoryToolBox extends ItemInventory {

		public InventoryToolBox(EntityPlayer player, ItemStack box) {

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
			return 24;
		}

		@Override
		public String getName() {
			return "container.toolBox";
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
		public void closeInventory(EntityPlayer player) {
			super.closeInventory(player);

			// Changing only the items tag doesn't make vanilla see the inventory as changed; touching one more unread tag forces a resend
			this.target.getTagCompound().removeTag("isOpen");
			this.target.getTagCompound().setInteger("rand", player.world.rand.nextInt());
			player.inventoryContainer.detectAndSendChanges();
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			return stack.isEmpty() || stack.getItem() != ModItems.toolbox;
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
