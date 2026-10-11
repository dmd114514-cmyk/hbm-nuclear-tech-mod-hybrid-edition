package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.network.NetworkHandler;
import com.hbm.network.packet.TESwitchPacket;

import api.hbm.tile.ISwitchSync;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * The standard-gauge switch: the main chain runs straight through, the branch curves in from the left to the same head.
 * One click throws the state; once thrown, only the matching chain lets trains pass (the check lives in canCross).
 */
public class RailStandardSwitch extends BlockRailWaypointSystem {

	/** The branch placeholders' sideways offset from the incoming direction: 2 for the normal switch, 1 for the mirrored one */
	protected int sidingBase() {
		return 2;
	}

	/** Which rotation the side uses: UP is the left hand, DOWN the right */
	protected EnumFacing sidingRotation(EnumFacing dir) {
		return dir.rotateY();
	}

	public RailStandardSwitch() {
		super(Material.IRON);

		RailDef main = new RailDef("main");
		RailDef side = new RailDef("side");
		railDefs.add(main);
		railDefs.add(side);

		defineNodes(main, side);
	}

	/** The two chains' node tables; the only thing that differs across the switch family */
	protected void defineNodes(RailDef main, RailDef side) {

		main.nodes.add(new Vec3d(-8.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(-7.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(6.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(7.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(8.5, 0.1875, 0.5));

		side.nodes.add(new Vec3d(-8.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-7.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-6.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-5.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-4.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-3.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-2.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-1.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-0.5, 0.1875, 4.25));
		side.nodes.add(new Vec3d(0.5, 0.1875, 3.9375));
		side.nodes.add(new Vec3d(1.5, 0.1875, 3.375));
		side.nodes.add(new Vec3d(2.5, 0.1875, 2.4625));
		side.nodes.add(new Vec3d(3.5, 0.1875, 1.75));
		side.nodes.add(new Vec3d(4.5, 0.1875, 1.1875));
		side.nodes.add(new Vec3d(5.5, 0.1875, 0.875));
		side.nodes.add(new Vec3d(6.5, 0.1875, 0.625));
		side.nodes.add(new Vec3d(7.5, 0.1875, 0.5));
		side.nodes.add(new Vec3d(8.5, 0.1875, 0.5));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRailSwitch();
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 7, 7, 1, 0};
	}

	@Override
	public int getOffset() {
		return 7;
	}

	@Override
	public TrackGauge getGauge(World world, int x, int y, int z) {
		return TrackGauge.STANDARD;
	}

	@Override
	public boolean canCross(World world, int x, int y, int z, Vec3d from, Vec3d to, RailDef def) {

		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

		if(!(tile instanceof TileEntityRailSwitch))
			return true;

		EnumFacing dir = facingForMeta(metaAt(world, new BlockPos(x, y, z)) - BlockDummyable.offset);

		if(dir == Library.POS_X && from.x < to.x) return true;
		if(dir == Library.NEG_X && from.x > to.x) return true;
		if(dir == Library.POS_Z && from.z < to.z) return true;
		if(dir == Library.NEG_Z && from.z > to.z) return true;

		if(dir == Library.POS_X && to.x < x + 0.5 + 7) return true;
		if(dir == Library.NEG_X && to.x > x + 0.5 - 7) return true;
		if(dir == Library.POS_Z && to.z < z + 0.5 + 7) return true;
		if(dir == Library.NEG_Z && to.z > z + 0.5 - 7) return true;

		if(((TileEntityRailSwitch) tile).isSwitched) {
			return "side".equals(def.name);
		}

		return "main".equals(def.name);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, net.minecraft.block.state.IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) return true;
		if(player.isSneaking()) return false;
		if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == ModItems.train) return false;

		BlockPos core = this.findCore(world, pos);

		if(core != null) {

			TileEntity tile = world.getTileEntity(core);

			if(tile instanceof TileEntityRailSwitch) {

				TileEntityRailSwitch sw = (TileEntityRailSwitch) tile;
				sw.isSwitched = !sw.isSwitched;
				sw.markDirty();
				// No World.markBlockForUpdate in this version; this flushes state and rendering together
				IBlockState now = world.getBlockState(core);
				world.notifyBlockUpdate(core, now, now, 3);

				// Waiting for the chunk's own flush lags a beat, so the switch position gets its own packet — coordinates must be the core cell; that's where the TE lives
				NetworkHandler.sendToAllAround(
						new TESwitchPacket(core.getX(), core.getY(), core.getZ(), sw.isSwitched),
						new TargetPoint(world.provider.getDimension(), core.getX(), core.getY(), core.getZ(), 1500));
			}
		}

		return true;
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, EnumFacing dir, int o) {

		if(!super.checkRequirement(world, x, y, z, dir, o)) return false;

		return sidingFits(world, x, y, z, dir, dir.rotateY(), 2);
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, EnumFacing dir, int o) {

		super.fillSpace(world, x, y, z, dir, o);

		placeSiding(world, x, y, z, dir, sidingRotation(dir), sidingBase());
	}

	/** Whether the branch cells are clear (incoming reversed plus sideways form the two basis vectors) */
	protected boolean sidingFits(World world, int x, int y, int z, EnumFacing dir, EnumFacing rot, int base) {

		int dX = dir.getOpposite().getFrontOffsetX();
		int dZ = dir.getOpposite().getFrontOffsetZ();
		int rX = rot.getFrontOffsetX();
		int rZ = rot.getFrontOffsetZ();

		for(int i = 0; i < 4; i++) if(!replaces(world, x + dX * (2 + i) + rX * base, y, z + dZ * (2 + i) + rZ * base)) return false;
		for(int i = 0; i < 2; i++) if(!replaces(world, x + dX * (4 + i) + rX * (base + 1), y, z + dZ * (4 + i) + rZ * (base + 1))) return false;
		if(!replaces(world, x + dX * 5 + rX * (base + 2), y, z + dZ * 5 + rZ * (base + 2))) return false;
		for(int j = 0; j < 2; j++) for(int i = 0; i < 2; i++) if(!replaces(world, x + dX * (6 + j) + rX * (base + 1 + i), y, z + dZ * (6 + j) + rZ * (base + 1 + i))) return false;
		if(!replaces(world, x + dX * 7 + rX * (base + 3), y, z + dZ * 7 + rZ * (base + 3))) return false;
		for(int j = 0; j < 7; j++) for(int i = 0; i < 2; i++) if(!replaces(world, x + dX * (8 + j) + rX * (base + 2 + i), y, z + dZ * (8 + j) + rZ * (base + 2 + i))) return false;

		return true;
	}

	protected void placeSiding(World world, int x, int y, int z, EnumFacing dir, EnumFacing rot, int base) {

		// While this batch lays, placeholders must not tear each other down; the one gate lives in MultiblockHandlerXR
		MultiblockHandlerXR.safeRem = true;

		int dX = dir.getOpposite().getFrontOffsetX();
		int dZ = dir.getOpposite().getFrontOffsetZ();
		int rX = rot.getFrontOffsetX();
		int rZ = rot.getFrontOffsetZ();
		int rotMeta = rot.ordinal();
		int backMeta = dir.getOpposite().ordinal();

		for(int i = 0; i < 4; i++) world.setBlockState(new BlockPos(x + dX * (2 + i) + rX * base, y, z + dZ * (2 + i) + rZ * base), getStateFromMeta(rotMeta), 3);
		for(int i = 0; i < 2; i++) world.setBlockState(new BlockPos(x + dX * (4 + i) + rX * (base + 1), y, z + dZ * (4 + i) + rZ * (base + 1)), getStateFromMeta(rotMeta), 3);
		world.setBlockState(new BlockPos(x + dX * 5 + rX * (base + 2), y, z + dZ * 5 + rZ * (base + 2)), getStateFromMeta(rotMeta), 3);
		for(int j = 0; j < 2; j++) for(int i = 0; i < 2; i++) world.setBlockState(new BlockPos(x + dX * (6 + j) + rX * (base + 1 + i), y, z + dZ * (6 + j) + rZ * (base + 1 + i)), getStateFromMeta(backMeta), 3);
		world.setBlockState(new BlockPos(x + dX * 7 + rX * (base + 3), y, z + dZ * 7 + rZ * (base + 3)), getStateFromMeta(rotMeta), 3);
		for(int j = 0; j < 7; j++) for(int i = 0; i < 2; i++) world.setBlockState(new BlockPos(x + dX * (8 + j) + rX * (base + 2 + i), y, z + dZ * (8 + j) + rZ * (base + 2 + i)), getStateFromMeta(backMeta), 3);

		MultiblockHandlerXR.safeRem = false;
	}

	private boolean replaces(World world, int x, int y, int z) {

		BlockPos pos = new BlockPos(x, y, z);

		return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
	}

	/** Records only "thrown or not"; everything else goes to block state and placeholders */
	public static class TileEntityRailSwitch extends TileEntity implements ISwitchSync {

		public boolean isSwitched = false;

		@Override
		public NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			return new SPacketUpdateTileEntity(this.getPos(), 0, this.writeToNBT(new NBTTagCompound()));
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.isSwitched = nbt.getBoolean("isSwitched");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setBoolean("isSwitched", this.isSwitched);
			return nbt;
		}

		/** The packet side only knows this port: writes the switch position into the client's copy for display; never touch the world here */
		@Override
		public void setSwitched(boolean switched) {
			this.isSwitched = switched;
		}
	}
}
