package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.BlockLoot;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A thin layer spread on the ground: four of them, foam / borax / fallen leaves / oil stains (ModBlocksGeneric's foam_layer, sand_boron_layer,
 * leaves_layer, oil_spill) share this class; they differ only in material, step sound, texture, and whether something else can cover them up.
 *
 * Thickness has eight steps (0..7). The old version kept it in the three low bits of meta; here it has to be a block property to persist at all, so the blockstate lists every value
 * {@code thickness=0..7} and the geometry comes from eight JSON element models (see item 1 below).
 *
 * This version's changed faces:
 *   1) The old {@code setBlockBounds} plus {@code renderAsNormalBlock=false} pair is gone (block-level render dispatch was removed)
 *      => each step's geometry goes to JSON: step 0 keeps the generator's placeholder file name {@code models/block/<registry name>.json}
 *      (2/16 tall), steps 1..7 each add a {@code <registry name>_N.json} (2*(1+N)/16 tall, N=1..7),
 *      the item icon still points at step 0's model - the old {@code setBlockBoundsForItemRender} drew exactly step 0;
 *      all four textures are opaque pixels (waste_leaves has transparency in its palette but never actually uses it), so {@code getBlockLayer} is not overridden;
 *   2) The visual and collision boxes were already different heights in the old version (visual 2*(1+j)/16, collision j*0.125); copied as-is, not corrected;
 *      the two hooks also use different coordinate spaces here - {@code getBoundingBox} hands back a 0..1 local box,
 *      {@code getCollisionBoundingBox} a world-space box; at j=0 the collision box flattens to zero height, blocking nothing, same shape as the old version;
 *   3) {@code onNeighborBlockChange} became {@code neighborChanged}; if it cannot hold it clears to air (the old one dropped nothing anyway);
 *      {@code isOpaqueCube} and the support test moved to the {@code BlockPos} / {@code IBlockState} variants,
 *      the support test follows the same form already registered in {@link BlockFallout} ({@code isLeaves(state,...)} / {@code state.isOpaqueCube()});
 *   4) The old {@code isReplaceable} recognised itself with {@code this == ModBlocks.leaves_layer}, comparing identity against a static field in the registry book,
 *      so whether it matched depended on assignment order; now it is a constructor argument {@code alwaysReplaceable}: foam / leaves / oil stains are always coverable,
 *      borax takes the old version's other branch (not at full thickness, otherwise it asks the material itself);
 *   5) Drops: the old {@code getItemDropped} returned null, meaning mining yields nothing; here section 15 hands that to a table
 *      - {@code harvestBlock} is overridden to go through the {@link com.hbm.blocks.BlockLoot} bridge, one empty table each
 *      {@code hbm:blocks/<registry name>} ({@code {"pools":[]}}); a pack author who wants drops just adds a pool, no code change.
 *      the old {@code quantityDropped((meta&7)+1)} never multiplied anything with an empty item, and the two signatures here carry no meta, so it stays out;
 *      One more difference logged: the old silk touch dropped the current thickness step, while the silk touch branch of {@code BlockLoot} here hands over
 *      {@code new ItemStack(block)}, i.e. meta always 0 and the thickness does not come back (fixing it needs a property-to-meta mapping on the block item - that is the items line's gap);
 *   6) The whole body of the old {@code updateTick} was commented out (vanish above light 11) and stays out; the force-render-top line is copied.
 *
 * Warning: one thing skipped and logged - the old {@code canPlaceBlockAt} opened with {@code block instanceof RBMKDebris || block instanceof ZirnoxDestroy...}
 *   not done here: neither of those two even has a registered block name in this tree and nothing references them, so landing it would be a shell with no callers.
 *   whoever brings those two in can wire this support check back while they are at it (foam sitting directly on reactor debris / blown-up thorium crates).
 *
 * Warning: the calling side has not caught up. The old code that writes these four blocks is {@code WasteLeaves} (leaves falling), {@code MapGenBubble} (oil stains into terrain),
 *   {@code XFactoryTool} (rounds laying down foam / borax), {@code TileEntityMachineDrain} (drains leaking oil),
 *   and {@code RBMKDebris*} / {@code ZirnoxDestroyed} (eating foam / borax to put out fires) - none of them registered yet.
 *   So this batch only guarantees step 0 is placeable from the creative tab and that stacking / collapsing behaves right; who actually spreads it to step seven waits on those classes landing.
 *   Stacking and collapsing cannot be checked headless (they need a real world ticking), so the runtime evidence for this batch stops at registration counts and model logs.
 */
public class BlockLayering extends BlockBase {

	/** Where the old three low bits of meta, the eight thickness steps, live in this version */
	public static final PropertyInteger THICKNESS = PropertyInteger.create("thickness", 0, 7);

	/** The eight visual boxes (local coords 0..1), precomputed from the old (2*(1+j))/16; the index is the thickness step */
	private static final AxisAlignedBB[] VISUAL = new AxisAlignedBB[8];

	static {

		for(int j = 0; j < 8; j++) {
			VISUAL[j] = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2.0D * (j + 1) / 16.0D, 1.0D);
		}
	}

	/** True for foam / leaves / oil stains (the three that compared identity against a registry-book field in the old code), false for borax */
	private final boolean alwaysReplaceable;

	public BlockLayering(Material material, boolean alwaysReplaceable) {
		super(material);
		this.alwaysReplaceable = alwaysReplaceable;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, THICKNESS);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(THICKNESS, meta & 7);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(THICKNESS).intValue();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return VISUAL[state.getValue(THICKNESS).intValue()];
	}

	/** The old value here is j*0.125, not the same height as the visual boxes above; copied as-is. This version wants world coordinates */
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {

		int j = state.getValue(THICKNESS).intValue();

		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + j * 0.125D, pos.getZ() + 1.0D);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {

		BlockPos below = pos.down();
		IBlockState state = world.getBlockState(below);
		Block block = state.getBlock();

		// Warning: the old version also counted reactor debris or a blown-up thorium crate underneath as support; those two are unregistered and have no callers here => skipped, see the class comment

		if(block == Blocks.ICE || block == Blocks.PACKED_ICE) return false;
		if(block.isLeaves(state, world, below)) return true;
		if(block == this && (block.getMetaFromState(state) & 7) == 7) return true;

		return state.isOpaqueCube() && block.getMaterial(state).blocksMovement();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos fromPos) {

		if(!this.canPlaceBlockAt(world, pos)) {
			world.setBlockToAir(pos);
		}
	}

	/** Nothing drops: mining yields nothing; drops are decided by the four empty hbm:blocks/<registry name> tables (see item 5 in the class comment) */
	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		BlockLoot.harvest(world, pos, state, player, stack);
	}

	/** The old version returned null here; this version hands it to a table. The line stays so that even Silk Touch drops nothing has somewhere to be said */
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.UP ? true : super.shouldSideBeRendered(state, world, pos, side);
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, BlockPos pos) {

		if(this.alwaysReplaceable) return true;

		IBlockState state = world.getBlockState(pos);
		return this.getMetaFromState(state) >= 7 ? false : state.getMaterial().isReplaceable();
	}
}
