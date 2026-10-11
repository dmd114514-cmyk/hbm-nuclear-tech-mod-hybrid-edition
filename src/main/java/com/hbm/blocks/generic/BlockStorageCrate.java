package com.hbm.blocks.generic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.config.ServerConfig;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The crate/safe-style "contents travel with the item" container blocks.
 *
 * One block per crate, but their slot counts, containers and GUIs all differ, so the caller passes
 * "which tile entity to build" as a parameter — comparing against ModBlocks by block identity is retired in this version.
 */
public class BlockStorageCrate extends BlockContainerBase implements ILookOverlay, ITooltipProvider {

	/** Only the tungsten crate burns, but the property hangs on the class, so the family's blockstates must list both tiers */
	public static final PropertyBool HEAT = PropertyBool.create("heat");

	private final Supplier<TileEntity> tileFactory;

	public BlockStorageCrate(Material material, Supplier<TileEntity> tileFactory) {
		super(material);
		this.tileFactory = tileFactory;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return tileFactory.get();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, HEAT);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(HEAT) ? 1 : 0;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(HEAT, meta == 1);
	}

	private static boolean dropInv = true;

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

		//whole-crate drop (locked crates included): the contents go into the dropped crate as-is, so breaking a crate isn't losing the goods
		if(!world.isRemote && !ServerConfig.CRATE_KEEP_CONTENTS) {
			dropInv = true;
			if(!player.capabilities.isCreativeMode) {
				world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this)));
			}
			TileEntity inv = world.getTileEntity(pos);
			if(inv instanceof TileEntityLockableBase && ((TileEntityLockableBase) inv).isLocked()) {
				dropInv = false;
			}
			boolean flag = world.setBlockToAir(pos);
			dropInv = true;
			return flag;
		}

		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {

			ItemStack drop = new ItemStack(this);
			TileEntity inv = world.getTileEntity(pos);

			NBTTagCompound nbt = new NBTTagCompound();

			if(inv instanceof IInventory) {
				IInventory contents = (IInventory) inv;

				for(int i = 0; i < contents.getSizeInventory(); i++) {

					ItemStack stack = contents.getStackInSlot(i);
					if(stack == null || stack.isEmpty()) continue;

					NBTTagCompound slot = new NBTTagCompound();
					stack.writeToNBT(slot);
					nbt.setTag("slot" + i, slot);
				}
			}

			if(inv instanceof TileEntityLockableBase) {
				TileEntityLockableBase lockable = (TileEntityLockableBase) inv;

				if(lockable.isLocked()) {
					nbt.setInteger("lock", lockable.getPins());
					nbt.setDouble("lockMod", lockable.getMod());
				}
			}

			if(inv instanceof TileEntityCrateBase && ((TileEntityCrateBase) inv).hasSpiders) {
				nbt.setBoolean("spiders", true);
			}

			if(!nbt.hasNoTags()) {
				nbt.setLong("stacklock", world.rand.nextLong());
				drop.setTagCompound(nbt);
			}

			if(inv instanceof TileEntityCrateBase) {
				TileEntityCrateBase crate = (TileEntityCrateBase) inv;
				if(crate.hasCustomName()) {
					drop.setStackDisplayName(crate.getName());
				}
			}

			if(drop.hasTagCompound()) {
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					CompressedStreamTools.writeCompressed(drop.getTagCompound(), out);

					if(out.toByteArray().length > 6000) {
						player.sendMessage(new TextComponentString(TextFormatting.RED + "Warning: Container NBT exceeds 6kB, contents will be ejected!"));
						world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this)));
						return world.setBlockToAir(pos);
					}

				} catch(IOException e) { }
			}

			world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
		}

		dropInv = false;
		boolean flag = world.setBlockToAir(pos);
		dropInv = true;

		return flag;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		//both hands fire; only the main hand counts, or one click opens the GUI twice
		if(hand != EnumHand.MAIN_HAND) return false;

		if(world.isRemote) {
			return true;
		}

		//no GUI while holding the key kit: that's for locking the crate
		if(player.getHeldItemMainhand().getItem() == ModItems.key_kit) {
			return false;
		}

		if(!player.isSneaking()) {
			TileEntity entity = world.getTileEntity(pos);

			if(entity instanceof TileEntityCrateBase && ((TileEntityCrateBase) entity).canAccess(player)) {
				player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				TileEntityCrateBase.spawnSpiders(player, world, (TileEntityCrateBase) entity);
			}
			return true;
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

		TileEntity inv = world.getTileEntity(pos);

		if(inv instanceof IInventory && stack.hasTagCompound()) {

			IInventory contents = (IInventory) inv;

			for(int i = 0; i < contents.getSizeInventory(); i++) {
				contents.setInventorySlotContents(i, new ItemStack(stack.getTagCompound().getCompoundTag("slot" + i)));
			}

			if(inv instanceof TileEntityLockableBase) {
				TileEntityLockableBase lockable = (TileEntityLockableBase) inv;

				if(stack.getTagCompound().hasKey("lock")) {
					lockable.setPins(stack.getTagCompound().getInteger("lock"));
					lockable.setMod(stack.getTagCompound().getDouble("lockMod"));
					lockable.lock();
				}
			}

			if(inv instanceof TileEntityCrateBase) {
				TileEntityCrateBase crate = (TileEntityCrateBase) inv;
				crate.hasSpiders = stack.getTagCompound().getBoolean("spiders");

				if(stack.hasDisplayName()) {
					crate.setCustomName(stack.getDisplayName());
				}
			}
		}

		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		if(dropInv) {
			TileEntity te = world.getTileEntity(pos);
			Random rand = world.rand;

			if(te instanceof IInventory) {
				IInventory contents = (IInventory) te;

				for(int i = 0; i < contents.getSizeInventory(); i++) {
					ItemStack stack = contents.getStackInSlot(i);

					if(stack == null || stack.isEmpty()) continue;

					float f = rand.nextFloat() * 0.8F + 0.1F;
					float f1 = rand.nextFloat() * 0.8F + 0.1F;
					float f2 = rand.nextFloat() * 0.8F + 0.1F;

					while(stack.getCount() > 0) {
						int j1 = rand.nextInt(21) + 10;

						if(j1 > stack.getCount()) {
							j1 = stack.getCount();
						}

						stack.shrink(j1);
						EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, new ItemStack(stack.getItem(), j1, stack.getMetadata()));

						if(stack.hasTagCompound()) {
							entityitem.getItem().setTagCompound(stack.getTagCompound().copy());
						}

						float f3 = 0.05F;
						entityitem.motionX = (float) rand.nextGaussian() * f3;
						entityitem.motionY = (float) rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) rand.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}
			}
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		return Container.calcRedstoneFromInventory((IInventory) world.getTileEntity(pos));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean ext) {

		if(!stack.hasTagCompound()) return;

		if(stack.getTagCompound().getBoolean("spiders")) {
			if(stack.getTagCompound().hasKey("lock")) {
				list.add(TextFormatting.RED + "This container is locked.");
			}
			list.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + "Skittering emanates from within...");
			return;
		}

		if(stack.getTagCompound().hasKey("lock")) {
			list.add(TextFormatting.RED + "This container is locked.");

			for(int i = 0; i < 104; i++) {
				ItemStack content = new ItemStack(stack.getTagCompound().getCompoundTag("slot" + i));
				if(!content.isEmpty()) {
					list.add(TextFormatting.YELLOW + "It feels heavy...");
					return;
				}
			}
			list.add(TextFormatting.YELLOW + "It feels empty...");
			return;
		}

		List<String> contents = new ArrayList<String>();
		int amount = 0;

		//one sweep at the largest crate's size, so each crate doesn't carry its own copy
		for(int i = 0; i < 104; i++) {
			ItemStack content = new ItemStack(stack.getTagCompound().getCompoundTag("slot" + i));

			if(!content.isEmpty()) {
				amount++;

				if(contents.size() < 10) {
					contents.add(TextFormatting.AQUA + " - " + content.getDisplayName() + (content.getCount() > 1 ? (" x" + content.getCount()) : ""));
				}
			}
		}

		if(!contents.isEmpty()) {
			list.add(TextFormatting.AQUA + "Contains:");
			list.addAll(contents);
			amount -= contents.size();

			if(amount > 0) {
				list.add(TextFormatting.AQUA + "...and " + amount + " more.");
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

		TileEntity te = world.getTileEntity(pos);

		if(!(te instanceof IInventory)) return;

		IInventory inv = (IInventory) te;

		if(!inv.hasCustomName()) return;

		ILookOverlay.printGeneric(event, inv.getName(), 0xffff00, 0x404000, new ArrayList<String>(0));
	}
}
