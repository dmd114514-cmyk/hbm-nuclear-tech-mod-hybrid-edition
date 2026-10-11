package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockEnums;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The stalactite and stalagmite pair (stalactite hangs down, stalagmite stands up), eight material tiers each
 * (sulfur / asbestos / flesh / ice / snow / three bug tiers); the tier table is {@link BlockEnums.EnumStalagmiteType}.
 *
 * Both share this one class; facing is a constructor argument, not a property: the old version also registered two instances ({@code this == ModBlocks.…}
 * was an identity check), collapsed here into a {@code hangsFromCeiling} boolean — no meta, no state.
 * Subtypes ride the base {@link BlockEnumMulti}'s {@code variant} property, with translation keys and textures in the "base name.enum lowercase"
 * family style (the lang's 16 entries and {@code stalagmite.<tier>.png} are in place accordingly).
 *
 * This version's changed faces:
 *   1) the old {@code getRenderType()=1} cross-drawing has no landing spot (block-level render dispatch deleted) ⇒ geometry goes to the
 *      JSON model ({@code parent: block/cross}, like the {@code BlockMush} batch), transparency via
 *      {@code getBlockLayer()=CUTOUT}；
 *   2) support checks: the old {@code World.doesBlockHaveSolidTopSurface} and three-int {@code isSideSolid} became
 *      {@code IBlockState.isSideSolid(world, pos, side)}, entered via {@code canPlaceBlockAt(World, BlockPos)} carrying BlockPlacement context;
 *   3) {@code onNeighborBlockChange→neighborChanged}, dropping as an item per vanilla when unsupported (same as {@code BlockMush});
 *   4) the old {@code getCollisionBoundingBoxFromPool} returning null = blocks nothing; this version gives {@code NULL_AABB}
 *      (here a null from {@code getCollisionBoundingBox} reads as "use the full-cube placeholder", different semantics);
 *   5) drops still come from vanilla's line without the {@code BlockLoot} bridge, hence no table; but {@code damageDropped} doesn't carry over,
 *      because the drops are other items (the sulfur-powder set) and the base's variant-indexed damage was something the old version
 *      explicitly didn't want.
 *
 * Drops align tier by tier with the old version: sulfur→{@code sulfur}, asbestos→{@code powder_asbestos}, flesh→{@code flesh},
 * ice→{@code powder_ice}, snow→vanilla snowballs; the three bug tiers fell through the old switch to {@code return null} —
 * copied here (faithful, unfixed).
 *
 * ⚠ One skipped and logged: the old static {@code getMetaFromResource(Block, int)} (mapping the tumor cell into the flesh tier's meta) served
 * the old {@code world/feature/OreCave} cave generation; this tree's feature isn't registered and has no callers ⇒ not ported;
 * restore when the world line brings OreCave back — the two blocks' facing-meta conversion comes along then too.
 */
public class BlockStalagmite extends BlockEnumMulti<BlockEnums.EnumStalagmiteType> {

	/** true = hanging from the ceiling (stalactite); false = standing from the ground (stalagmite) */
	private final boolean hangsFromCeiling;

	public BlockStalagmite(boolean hangsFromCeiling) {
		super(Material.ROCK, true, true);
		this.hangsFromCeiling = hangsFromCeiling;
	}

	@Override
	public Class<BlockEnums.EnumStalagmiteType> getEnumClass() {
		return BlockEnums.EnumStalagmiteType.class;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	/** The old getCollisionBoundingBoxFromPool returning null = blocks nothing */
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	/*Can it hold: stalagmites want solid below, stalactites solid above — one-to-one with the old two checks */
	public boolean canBlockStay(World world, BlockPos pos) {

		if(this.hangsFromCeiling) {
			BlockPos up = pos.up();
			return world.getBlockState(up).isSideSolid(world, up, EnumFacing.DOWN);
		}

		BlockPos down = pos.down();
		return world.getBlockState(down).isSideSolid(world, down, EnumFacing.UP);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos) && this.canBlockStay(world, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {

		super.neighborChanged(state, world, pos, blockIn, fromPos);

		if(!this.canBlockStay(world, pos)) {
			this.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	/*Drops tier by tier to each item; the three bug tiers had no case in the old switch = nothing drops, copied here */
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		switch(state.getValue(this.getVariant())) {
		case SULFUR:
			return ModItems.sulfur;
		case ASBESTOS:
			return ModItems.powder_asbestos;
		case FLESH:
			return ModItems.flesh;
		case ICE:
			return ModItems.powder_ice;
		case SNOW:
			return Items.SNOWBALL;
		default:
			return null;
		}
	}

	/*Drops are other items, so damage must zero out (exactly what the old getDropsWithoutDamage did) */
	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}
}
