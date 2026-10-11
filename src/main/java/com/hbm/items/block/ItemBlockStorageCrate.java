package com.hbm.items.block;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.ServerConfig;
import com.hbm.inventory.container.ContainerCrateDesh;
import com.hbm.inventory.container.ContainerCrateIron;
import com.hbm.inventory.container.ContainerCrateSteel;
import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.inventory.container.ContainerSafe;
import com.hbm.inventory.gui.GUICrateDesh;
import com.hbm.inventory.gui.GUICrateIron;
import com.hbm.inventory.gui.GUICrateSteel;
import com.hbm.inventory.gui.GUICrateTungsten;
import com.hbm.inventory.gui.GUISafe;
import com.hbm.items.ItemBlockBase;
import com.hbm.items.ItemInventory;
import com.hbm.items.tool.ItemKey;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;
import com.hbm.tileentity.machine.storage.TileEntityCrateDesh;
import com.hbm.tileentity.machine.storage.TileEntityCrateIron;
import com.hbm.tileentity.machine.storage.TileEntityCrateSteel;
import com.hbm.tileentity.machine.storage.TileEntityCrateTungsten;
import com.hbm.tileentity.machine.storage.TileEntitySafe;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The crates'/safe's block item: opens straight from hand (contents travel with the item, so the GUI is provided here),
 * and the crate's "inventory" is an InventoryCrate view hugging the item's NBT.
 */
public class ItemBlockStorageCrate extends ItemBlockBase implements IGUIProvider {

	public ItemBlockStorageCrate(Block block) {
		super(block);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 1;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		//with hand-open crates allowed, sneak distinguishes "place"
		if(ServerConfig.CRATE_OPEN_HELD && !player.isSneaking()) {
			return EnumActionResult.PASS;
		}

		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(!ServerConfig.CRATE_OPEN_HELD) {
			return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		}

		if(!world.isRemote && stack.getCount() == 1) {

			//a locked crate first wants the matching key in hand
			if(stack.hasTagCompound() && stack.getTagCompound().hasKey("lock")) {

				for(ItemStack item : player.inventory.mainInventory) {

					if(item == null || item.isEmpty()) continue;
					if(!(item.getItem() instanceof ItemKey)) continue;
					if(!item.hasTagCompound()) continue;

					if(item.getTagCompound().getInteger("pins") == stack.getTagCompound().getInteger("lock")) {
						TileEntityCrateBase.spawnSpiders(player, world, stack);
						player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
						break;
					}
				}

				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}

			TileEntityCrateBase.spawnSpiders(player, world, stack);
			player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {

		ItemStack held = player.getHeldItemMainhand();
		Block block = Block.getBlockFromItem(held.getItem());

		if(block == ModBlocks.crate_iron) return new ContainerCrateIron(player.inventory, new InventoryCrate(player, held));
		if(block == ModBlocks.crate_steel) return new ContainerCrateSteel(player.inventory, new InventoryCrate(player, held));
		if(block == ModBlocks.crate_desh) return new ContainerCrateDesh(player.inventory, new InventoryCrate(player, held));
		if(block == ModBlocks.crate_tungsten) return new ContainerCrateTungsten(player.inventory, new InventoryCrate(player, held));
		if(block == ModBlocks.safe) return new ContainerSafe(player.inventory, new InventoryCrate(player, held));

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {

		ItemStack held = player.getHeldItemMainhand();
		Block block = Block.getBlockFromItem(held.getItem());

		if(block == ModBlocks.crate_iron) return new GUICrateIron(player.inventory, new InventoryCrate(player, held));
		if(block == ModBlocks.crate_steel) return new GUICrateSteel(player.inventory, new InventoryCrate(player, held));
		if(block == ModBlocks.crate_desh) return new GUICrateDesh(player.inventory, new InventoryCrate(player, held));
		if(block == ModBlocks.crate_tungsten) return new GUICrateTungsten(player.inventory, new InventoryCrate(player, held));
		if(block == ModBlocks.safe) return new GUISafe(player.inventory, new InventoryCrate(player, held));

		return null;
	}

	/** The crate contents view glued to the item's NBT: held in hand, it is the crate */
	public static class InventoryCrate extends ItemInventory {

		public static Random rand = new Random();

		public InventoryCrate(EntityPlayer player, ItemStack crate) {
			this.player = player;
			this.target = crate;

			this.slots = new ItemStack[this.getSizeInventory()];

			if(!target.hasTagCompound()) {
				target.setTagCompound(new NBTTagCompound());
				target.getTagCompound().setLong("stacklock", rand.nextLong());
			}

			for(int i = 0; i < slots.length; i++) {
				this.slots[i] = new ItemStack(target.getTagCompound().getCompoundTag("slot" + i));
			}
		}

		/** Builds a throwaway tile entity from the item's block, just to read slot count and name */
		public static TileEntityCrateBase findCrateType(Item crate) {

			Block block = Block.getBlockFromItem(crate);

			if(block == ModBlocks.crate_iron) return new TileEntityCrateIron();
			if(block == ModBlocks.crate_steel) return new TileEntityCrateSteel();
			if(block == ModBlocks.crate_desh) return new TileEntityCrateDesh();
			if(block == ModBlocks.crate_tungsten) return new TileEntityCrateTungsten();
			if(block == ModBlocks.safe) return new TileEntitySafe();

			return null;
		}

		@Override
		public int getSizeInventory() {
			TileEntityCrateBase tile = findCrateType(target.getItem());
			return tile == null ? 0 : tile.getSizeInventory();
		}

		@Override
		public String getName() {
			TileEntityCrateBase tile = findCrateType(target.getItem());

			if(tile == null) return "";

			if(hasCustomName()) {
				tile.setCustomName(target.getDisplayName());
			}

			return tile.getName();
		}

		@Override
		public boolean hasCustomName() {
			return target.hasDisplayName();
		}

		@Override
		public ITextComponent getDisplayName() {
			return new TextComponentString(this.getName());
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer player) {
			return player.getHeldItemMainhand() == this.target;
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
		public void markDirty() {

			//pre-existing NBT (lock, spider and friends) must survive; only the slots section gets overwritten
			NBTTagCompound nbt = target.hasTagCompound() ? target.getTagCompound() : new NBTTagCompound();
			int invSize = this.getSizeInventory();

			nbt.removeTag("stacklock");

			for(int i = 0; i < invSize; i++) {

				ItemStack stack = this.getStackInSlot(i);

				if(stack == null || stack.isEmpty()) {
					nbt.removeTag("slot" + i);
					continue;
				}

				NBTTagCompound slot = new NBTTagCompound();
				stack.writeToNBT(slot);
				nbt.setTag("slot" + i, slot);
			}

			//carrying stacklock is what keeps it from stacking
			nbt.setLong("stacklock", rand.nextLong());

			target.setTagCompound(nbt);
		}

		@Override
		public void closeInventory(EntityPlayer player) {
			super.closeInventory(player);

			//the 6kB gate is the base class's checkNBT
			target.setTagCompound(checkNBT(target.getTagCompound()));
			player.inventoryContainer.detectAndSendChanges();

			if(target.hasTagCompound()) {
				target.getTagCompound().removeTag("stacklock");

				if(target.getTagCompound().hasNoTags()) {
					target.setTagCompound(null); //with nothing else left, clear the compound so the crate can stack again
				} else {
					target.getTagCompound().setLong("stacklock", rand.nextLong());
				}
			}
		}
	}
}
