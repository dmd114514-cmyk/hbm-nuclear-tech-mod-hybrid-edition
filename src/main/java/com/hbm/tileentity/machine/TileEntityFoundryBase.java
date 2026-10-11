package com.hbm.tileentity.machine;

import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.material.MaterialStack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * The base for everything on the foundry chain that holds exactly one material at a time, either storing it or passing it down: channels, tanks, outlets all belong here.
 * Any type or amount change nudges a block update so neighbors and the render layer keep up.
 */
public abstract class TileEntityFoundryBase extends TileEntityLoadedBase implements ICrucibleAcceptor, ICopiable, ITickable {

	public NTMMaterial type;
	protected NTMMaterial lastType;
	public int amount;
	protected int lastAmount;

	@Override
	public void update() {
		if(this.isLoaded) updateEntity();
	}

	public void updateEntity() {

		if(this.lastType == this.type && this.lastAmount == this.amount)
			return;

		this.lastType = this.type;
		this.lastAmount = this.amount;

		// The client side only redraws; subclasses whose contents change every tick can switch that off
		if(world.isRemote && !shouldClientReRender())
			return;

		refresh();
	}

	/** Things that update very often should leave this off; turn it on only for "changes once every few ticks" */
	protected boolean shouldClientReRender() {
		return true;
	}

	/** No markBlockForUpdate in this version; this one flushes state and rendering together */
	public void refresh() {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
	}

	/**
	 * A shovel or a broken block clears this cell's material into one lump of scrap: into the inventory if it fits, else on the ground.
	 * player passed as null means nobody is around (the block-removed path).
	 */
	public void clearToScrap(World world, BlockPos pos, EntityPlayer player) {

		if(this.amount <= 0) return;

		ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(this.type, this.amount));

		if(player == null || !player.inventory.addItemStackToInventory(scrap)) {
			world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, scrap));
		} else {
			player.inventoryContainer.detectAndSendChanges();
		}

		this.amount = 0;
		this.type = null;
		this.markDirty();
		refresh();
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.type = Mats.matById.get(nbt.getInteger("type"));
		this.amount = nbt.getInteger("amount");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if(this.type == null) {
			nbt.setInteger("type", -1);
		} else {
			nbt.setInteger("type", this.type.id);
		}

		nbt.setInteger("amount", this.amount);

		return nbt;
	}

	public abstract int getCapacity();

	/**
	 * Whether a pile of material can enter this vessel comes down to two things: does the type match, is there room.
	 */
	public boolean standardCheck(World world, BlockPos pos, EnumFacing side, MaterialStack<?> stack) {
		if(this.type != null && this.type != materialOf(stack) && this.amount > 0) return false; //already holding a different type, refuse
		if(this.amount >= this.getCapacity()) return false; //full, refuse
		return true;
	}

	/**
	 * The shared intake step used by both pouring and flowing: type follows the feed, whatever didn't fit goes back as-is.
	 */
	public MaterialStack<?> standardAdd(World world, BlockPos pos, EnumFacing side, MaterialStack<?> stack) {

		this.type = materialOf(stack);

		if(stack.amount + this.amount <= this.getCapacity()) {
			this.amount += stack.amount;
			return null;
		}

		int required = this.getCapacity() - this.amount;
		this.amount = this.getCapacity();

		stack.amount -= required;

		return stack;
	}

	/** The interface's material type is generic (the api layer doesn't know content-layer materials) while this family's feed is always NTM material */
	protected static NTMMaterial materialOf(MaterialStack<?> stack) {
		return (NTMMaterial) stack.material;
	}

	/** Standard check, no extra conditions */
	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) {
		return this.standardCheck(world, new BlockPos(x, y, z), side, stack);
	}

	/** Standard intake, nothing special */
	@Override
	public MaterialStack<?> flow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) {
		return this.standardAdd(world, new BlockPos(x, y, z), side, stack);
	}

	/** Standard check plus one rule: only accepts what's poured in from above */
	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) {
		if(side != EnumFacing.UP) return false;
		return this.standardCheck(world, new BlockPos(x, y, z), side, stack);
	}

	/** Standard intake, nothing special */
	@Override
	public MaterialStack<?> pour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) {
		return this.standardAdd(world, new BlockPos(x, y, z), side, stack);
	}

	@Override
	public NBTTagCompound getSettings(World world, BlockPos pos) {
		NBTTagCompound nbt = new NBTTagCompound();
		if(type != null) nbt.setIntArray("matFilter", new int[]{ type.id });
		return nbt;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, BlockPos pos) {

	}
}
