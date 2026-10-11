package com.hbm.wiaj;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A thrown-together IBlockAccess whose only job is rendering blocks "somewhere that isn't a world" — it knows blocks and next to nothing else.
 *
 * The old version kept its three dimensions as parallel blocks/meta arrays; this one doesn't split them: the properties live in the IBlockState anyway,
 * ask the block when you need meta (getMetaFromState). The x/y/z read/write surface is still here; anything out of bounds counts as air.
 *
 * Light keeps the old "just return 15" value, no sky<<20|block<<4 packing — that's how the old version did it,
 * and nobody saw a problem back then. If one day the jar renders dark, the root cause is this line — don't go fix the callers.
 */
public class WorldInAJar implements IBlockAccess {

	public int sizeX;
	public int sizeY;
	public int sizeZ;

	public int lightlevel = 15;

	private IBlockState[][][] states;
	private TileEntity[][][] tiles;

	public WorldInAJar(int x, int y, int z) {
		this.sizeX = x;
		this.sizeY = y;
		this.sizeZ = z;

		allocate();
	}

	public void nuke() {
		allocate();
	}

	private void allocate() {
		this.states = new IBlockState[sizeX][sizeY][sizeZ];
		this.tiles = new TileEntity[sizeX][sizeY][sizeZ];
	}

	public IBlockState getState(int x, int y, int z) {

		if(outOfBounds(x, y, z)) return Blocks.AIR.getDefaultState();

		return this.states[x][y][z] != null ? this.states[x][y][z] : Blocks.AIR.getDefaultState();
	}

	public Block getBlock(int x, int y, int z) {
		return getState(x, y, z).getBlock();
	}

	public int getBlockMetadata(int x, int y, int z) {
		if(outOfBounds(x, y, z)) return 0;

		IBlockState state = getState(x, y, z);
		return state.getBlock().getMetaFromState(state);
	}

	public void setBlock(int x, int y, int z, Block block, int meta) {
		setState(x, y, z, block.getStateFromMeta(meta));
	}

	public void setState(int x, int y, int z, IBlockState state) {
		if(outOfBounds(x, y, z)) return;

		this.states[x][y][z] = state;
	}

	public TileEntity getTileEntity(int x, int y, int z) {
		if(outOfBounds(x, y, z)) return null;

		return this.tiles[x][y][z];
	}

	public void setTileEntity(int x, int y, int z, TileEntity tile) {
		if(outOfBounds(x, y, z)) return;

		this.tiles[x][y][z] = tile;
	}

	private boolean outOfBounds(int x, int y, int z) {
		return x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ;
	}

	/*
	 * the IBlockAccess face
	 */

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return getState(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return getTileEntity(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		IBlockState state = getState(pos.getX(), pos.getY(), pos.getZ());
		return state.getBlock().isAir(state, this, pos);
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		if(outOfBounds(pos.getX(), pos.getY(), pos.getZ())) return _default;

		IBlockState state = getState(pos.getX(), pos.getY(), pos.getZ());
		return state.getBlock().isSideSolid(state, this, pos, side);
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return 0;
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return this.lightlevel;
	}

	// Biomes mean nothing to us; add a raw mask layer if per-chunk coloring ever matters
	@Override
	@SideOnly(Side.CLIENT)
	public Biome getBiome(BlockPos pos) {
		return Biomes.PLAINS;
	}

	@Override
	public WorldType getWorldType() {
		return WorldType.FLAT;
	}
}
