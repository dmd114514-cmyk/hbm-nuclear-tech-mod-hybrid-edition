package com.hbm.blocks.generic;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.hbm.blocks.BlockBase;
import com.hbm.config.GeneralConfig;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The mushroom that grows on the wasteland by itself: mush (the only BlockMush-family piece the old version registered).
 *
 * This family and the wasteland family are mutual — standing on any of the seven waste_earth / waste_mycelium / trinitite / block_waste pieces
 * keeps it alive ({@link BlockWasteEarth#canSustainPlant} recognizes exactly this class's {@link IPlantable}),
 * and its own random tick upgrades the wasteland below into waste mycelium and spreads outward.
 *
 * This version's changed faces:
 *   1) the old {@code setBlockBounds} + {@code renderAsNormalBlock=false} + {@code getRenderType()=1} set —
 *      the "0.2-wide cross" is gone (block-level render dispatch deleted) ⇒ geometry goes to the JSON model ({@code block/cross}),
 *      atlas cutting to {@code getBlockLayer()=CUTOUT}, collision to {@code getCollisionBoundingBox→NULL_AABB},
 *      and the entity box to {@code getBoundingBox→MUSH_AABB} (the old 0.3~0.7 / 0~0.4 ring);
 *   2) {@code canPlaceBlockAt} / {@code canSustainPlant} / {@code isOpaqueCube} all became the {@code BlockPos}- and
 *      {@code IBlockState}-taking versions, with {@code onNeighborBlockChange→neighborChanged};
 *   3) the old three-int {@code world.getBlock(ix, iz, iy)}-style fetches became per-cell ({@code getBlockState(pos)});
 *   4) drops still come from vanilla's one line (itself), skipping the {@code BlockLoot} bridge, hence no table.
 *
 * ⚠ Two skipped and logged:
 *   · the old {@code canPlaceBlockOn(Block)} had no callers in this family (dead code there too), dropped per "old dead code doesn't port";
 *   · the three {@code IGrowable} methods (40% bonemeal roll → growing the giant mushroom {@code com.hbm.world.feature.…})
 *     aren't done at all — that feature belongs to the world line and isn't registered; better bonemeal does nothing than a wrong-looking giant mushroom,
 *
 * Also logged, an old bug copied without digging: in {@code updateTick}'s spread loop the x/y/z axes are used interchangeably
 * (the census loop reads {@code (ix, iz, iy)}, while picking the next cell derives {@code iy} from z and {@code iz} from y),
 * it ran that way before, moved verbatim this batch — behavioral parity first.
 */
public class BlockMush extends BlockBase implements IPlantable {

	/*The old setBlockBounds(0.5-f, 0, 0.5-f, 0.5+f, 2f, 0.5+f) ring with f=0.2 */
	private static final AxisAlignedBB MUSH_AABB = new AxisAlignedBB(0.30000001192092894D, 0.0D, 0.30000001192092894D, 0.699999988079071D, 0.4000000059604645D, 0.699999988079071D);

	private static final Set<Block> canGrowOn = new HashSet<Block>();

	public BlockMush(Material mat) {
		super(mat);
		this.setTickRandomly(true);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos) && this.canBlockStay(world, pos);
	}

	public boolean canBlockStay(World world, BlockPos pos) {

		int y = pos.getY();

		if(y >= 0 && y < 256) {
			BlockPos down = pos.down();
			IBlockState ground = world.getBlockState(down);
			Block block = ground.getBlock();
			return block.canSustainPlant(ground, world, down, EnumFacing.UP, this) || canMushGrowHere(world, pos);
		}

		return false;
	}

	public boolean canMushGrowHere(World world, BlockPos pos) {

		if(canGrowOn.isEmpty()) {
			canGrowOn.add(ModBlocksGeneric.waste_earth);
			canGrowOn.add(ModBlocksGeneric.waste_mycelium);
			canGrowOn.add(ModBlocksGeneric.waste_trinitite);
			canGrowOn.add(ModBlocksGeneric.waste_trinitite_red);
			canGrowOn.add(ModBlocksGeneric.block_waste);
			canGrowOn.add(ModBlocksGeneric.block_waste_painted);
			canGrowOn.add(ModBlocksGeneric.block_waste_vitrified);
		}

		return canGrowOn.contains(world.getBlockState(pos.down()).getBlock());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MUSH_AABB;
	}

	/** The old getCollisionBoundingBoxFromPool returning null = blocks nothing; same shape here */
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {

		super.neighborChanged(state, world, pos, blockIn, fromPos);
		this.checkAndDropBlock(world, pos, state);
	}

	/** Falls as an item when it loses support */
	protected void checkAndDropBlock(World world, BlockPos pos, IBlockState state) {

		if(!this.canBlockStay(world, pos)) {
			this.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		this.checkAndDropBlock(world, pos, state);

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if(GeneralConfig.enableMycelium && world.getBlockState(pos.down()).getBlock() == ModBlocksGeneric.waste_earth && rand.nextInt(5) == 0)
			world.setBlockState(pos.down(), ModBlocksGeneric.waste_mycelium.getDefaultState());

		if(rand.nextInt(25) != 0)
			return;

		int range = 4;
		int maxShroom = 3;
		int ix;
		int iy;
		int iz;

		for(ix = x - range; ix <= x + range; ++ix) {
			for(iy = y - range; iy <= y + range; ++iy) {
				for(iz = z - 1; iz <= z + 1; ++iz) {

					if(world.getBlockState(new BlockPos(ix, iz, iy)).getBlock() == this) {

						--maxShroom;

						if(maxShroom <= 0)
							return;
					}
				}
			}
		}

		ix = x + rand.nextInt(5) - 2;
		iy = z + rand.nextInt(2) - rand.nextInt(2);
		iz = y + rand.nextInt(5) - 2;

		for(int l1 = 0; l1 < 4; ++l1) {

			BlockPos probe = new BlockPos(ix, iy, iz);

			if(world.isAirBlock(probe) && this.canMushGrowHere(world, probe)) {
				x = ix;
				z = iy;
				y = iz;
			}

			ix = x + rand.nextInt(5) - 2;
			iy = z + rand.nextInt(2) - rand.nextInt(2);
			iz = y + rand.nextInt(5) - 2;
		}

		BlockPos target = new BlockPos(ix, iy, iz);

		if(world.isAirBlock(target) && this.canMushGrowHere(world, target))
			world.setBlockState(target, this.getDefaultState(), 2);
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Cave;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return this.getDefaultState();
	}
}
