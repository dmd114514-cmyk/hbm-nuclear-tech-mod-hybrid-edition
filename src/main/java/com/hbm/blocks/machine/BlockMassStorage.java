package com.hbm.blocks.machine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The mass storage block: one texture per capacity, the front rotating by facing.
 * The old version packed subtype and facing into one meta (meta = subtype + 4×facing); this splits them into two properties
 */
public class BlockMassStorage extends BlockContainerBase implements ILookOverlay, ITooltipProvider {

	public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.Plane.HORIZONTAL);

	/** Material pairs with capacity: the enum name is the texture suffix (the standard box has none) */
	public static enum Variant implements IStringSerializable {
		IRON("iron", 10_000),
		DESH("desh", 100_000),
		STANDARD("", 1_000_000),
		WOOD("wood", 100);

		private final String suffix;
		private final int capacity;

		Variant(String suffix, int capacity) {
			this.suffix = suffix;
			this.capacity = capacity;
		}

		public int getCapacity() {
			return capacity;
		}

		@Override
		public String getName() {
			return this.suffix.isEmpty() ? "standard" : this.suffix;
		}
	}

	public BlockMassStorage() {
		super(Material.IRON);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, Variant.IRON).withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT, FACING);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState()
				.withProperty(VARIANT, Variant.values()[meta % 4])
				.withProperty(FACING, EnumFacing.HORIZONTALS[(meta / 4) % 4]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).ordinal() + 4 * state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMassStorage(this.getCapacity(meta));
	}

	public int getCapacity(int meta) {
		return Variant.values()[meta % 4].getCapacity();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(hand != EnumHand.MAIN_HAND) return false;

		if(world.isRemote) {
			return true;
		}

		//no GUI while holding the key kit
		if(player.getHeldItemMainhand().getItem() == ModItems.key_kit) {
			return false;
		}

		if(!player.isSneaking()) {
			TileEntity entity = world.getTileEntity(pos);

			if(entity instanceof TileEntityMassStorage && ((TileEntityMassStorage) entity).canAccess(player)) {
				player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}

		return false;
	}

	private static boolean dropInv = true;

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {

			ItemStack drop = new ItemStack(this, 1, this.getMetaFromState(world.getBlockState(pos)));
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

			if(inv instanceof TileEntityMassStorage) {
				TileEntityMassStorage storage = (TileEntityMassStorage) inv;
				nbt.setInteger("stack", storage.getStockpile());
			}

			if(!nbt.hasNoTags()) {
				drop.setTagCompound(nbt);
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
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

		TileEntity inv = world.getTileEntity(pos);

		if(inv instanceof IInventory && stack.hasTagCompound()) {

			IInventory contents = (IInventory) inv;

			for(int i = 0; i < contents.getSizeInventory(); i++) {
				contents.setInventorySlotContents(i, new ItemStack(stack.getTagCompound().getCompoundTag("slot" + i)));
			}

			if(inv instanceof TileEntityMassStorage) {
				TileEntityMassStorage storage = (TileEntityMassStorage) inv;

				if(stack.getTagCompound().hasKey("lock")) {
					storage.setPins(stack.getTagCompound().getInteger("lock"));
					storage.setMod(stack.getTagCompound().getDouble("lockMod"));
					storage.lock();
				}

				storage.setStockpile(stack.getTagCompound().getInteger("stack"));
			}
		}

		super.onBlockPlacedBy(world, pos, state, placer, stack);

		//the front faces the player who placed it (the old version computed meta from yaw; this uses the property directly)
		world.setBlockState(pos, world.getBlockState(pos).withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		if(dropInv) {
			TileEntity te = world.getTileEntity(pos);
			Random rand = world.rand;

			if(te instanceof IInventory) {
				IInventory contents = (IInventory) te;

				for(int i = 0; i < contents.getSizeInventory(); i++) {

					if(i == 1) continue; //the filter cell doesn't drop

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
		return ((TileEntityMassStorage) world.getTileEntity(pos)).redstone;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean ext) {

		if(!stack.hasTagCompound()) return;

		ItemStack type = new ItemStack(stack.getTagCompound().getCompoundTag("slot1"));

		if(!type.isEmpty()) {
			list.add(TextFormatting.GOLD + type.getDisplayName());
			list.add(String.format(Locale.US, "%,d", stack.getTagCompound().getInteger("stack")) + " / " + String.format(Locale.US, "%,d", getCapacity(stack.getMetadata())));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

		TileEntity te = world.getTileEntity(pos);

		if(!(te instanceof TileEntityMassStorage)) return;

		TileEntityMassStorage storage = (TileEntityMassStorage) te;

		List<String> text = new ArrayList<String>();
		String title = "Empty";
		boolean full = storage.getType() != null;

		if(full) {

			title = storage.getType().getDisplayName();
			text.add(String.format(Locale.US, "%,d", storage.getStockpile()) + " / " + String.format(Locale.US, "%,d", storage.getCapacity()));

			double percent = (double) storage.getStockpile() / (double) storage.getCapacity();
			int charge = (int) Math.floor(percent * 10_000D);
			int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);

			text.add("&[" + color + "&]" + (charge / 100D) + "%");
		}

		ILookOverlay.printGeneric(event, title, full ? 0xffff00 : 0x00ffff, full ? 0x404000 : 0x004040, text);
	}
}
