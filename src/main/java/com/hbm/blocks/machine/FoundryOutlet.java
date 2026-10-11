package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.machine.TileEntityFoundryOutlet;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.block.IToolable;
import api.hbm.material.MaterialStack;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The casting outlet: the channel's spout, catching material below the front face and pouring it down.
 *
 * The old version packed facing into meta (tiers 2 to 5); this version uses an EnumFacing property with the meta numbering kept,
 * so stored maps and hand-written blockstates still line up. The facing convention is unified as "front toward where the placer was looking",
 * with intake honoring only the back cell.
 */
public class FoundryOutlet extends BlockContainerBase implements ICrucibleAcceptor, ILookOverlay, IToolable {

	/** The front face — the side the metal stream falls from */
	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.Plane.HORIZONTAL);

	/** Draws half a block tall, the protruding stub posed by the front direction */
	private static final double TOP = 0.5;
	private static final double CORE_MIN = 0.3125;
	private static final double CORE_MAX = 0.6875;
	private static final double NOZZLE_MIN = 0.625;
	private static final double NOZZLE_MAX = 1;

	public FoundryOutlet() {
		super(Material.ROCK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	/** Meta keeps the old four tiers: 2 south, 3 north, 4 east, 5 west */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, facingForMeta(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return metaForFacing(state.getValue(FACING));
	}

	public static EnumFacing facingForMeta(int meta) {

		if(meta == 3) return EnumFacing.NORTH;
		if(meta == 4) return EnumFacing.EAST;
		if(meta == 5) return EnumFacing.WEST;
		return EnumFacing.SOUTH;
	}

	public static int metaForFacing(EnumFacing facing) {

		if(facing == EnumFacing.NORTH) return 3;
		if(facing == EnumFacing.EAST) return 4;
		if(facing == EnumFacing.WEST) return 5;
		return 2;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryOutlet();
	}

	/** The front faces where the placer was looking, matching the old yaw-derived meta */
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing()), 2);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {

		EnumFacing facing = state.getValue(FACING);
		AxisAlignedBB bb = nozzleBox(pos, facing);

		if(entityBox.intersects(bb)) {
			collidingBoxes.add(bb);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return localNozzleBox(state.getValue(FACING));
	}

	/** The protrusions in local coordinates */
	private static AxisAlignedBB localNozzleBox(EnumFacing facing) {

		if(facing == EnumFacing.SOUTH) return new AxisAlignedBB(CORE_MIN, 0, NOZZLE_MIN, CORE_MAX, TOP, NOZZLE_MAX);
		if(facing == EnumFacing.NORTH) return new AxisAlignedBB(CORE_MIN, 0, 0, CORE_MAX, TOP, CORE_MIN);
		if(facing == EnumFacing.EAST) return new AxisAlignedBB(NOZZLE_MIN, 0, CORE_MIN, NOZZLE_MAX, TOP, CORE_MAX);
		return new AxisAlignedBB(0, 0, CORE_MIN, CORE_MIN, TOP, CORE_MAX);
	}

	private static AxisAlignedBB nozzleBox(BlockPos pos, EnumFacing facing) {
		AxisAlignedBB local = localNozzleBox(facing);
		return new AxisAlignedBB(pos.getX() + local.minX, pos.getY() + local.minY, pos.getZ() + local.minZ, pos.getX() + local.maxX, pos.getY() + local.maxY, pos.getZ() + local.maxZ);
	}

	/** The outlet doesn't pour; it just hands material downward */
	@Override public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) { return false; }
	@Override public MaterialStack<?> pour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) { return stack; }

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialFlow(world, x, y, z, side, stack);
	}

	@Override
	public MaterialStack<?> flow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).flow(world, x, y, z, side, stack);
	}

	/** Scrap in hand + click = set this as the filter material; bare-hand click = invert the redstone */
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			return true;
		}

		if(hand != EnumHand.MAIN_HAND || player.isSneaking()) {
			return true;
		}

		TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(pos);
		ItemStack held = player.getHeldItemMainhand();

		if(held != null && !held.isEmpty() && held.getItem() == ModItems.scraps) {
			Mats.MaterialStack mats = ItemScraps.getMats(held);
			if(mats != null) {
				tile.filter = mats.material;
			}
		} else {
			tile.invertRedstone = !tile.invertRedstone;
		}

		tile.markDirty();
		tile.refresh();

		return true;
	}

	/** The screwdriver clears the filter; the hand drill toggles "only pass" vs "only block" */
	@Override
	public boolean onScrew(World world, EntityPlayer player, BlockPos pos, EnumFacing side, float fX, float fY, float fZ, ToolType tool) {

		if(world.isRemote) {
			return tool == ToolType.SCREWDRIVER || tool == ToolType.HAND_DRILL;
		}

		if(tool == ToolType.SCREWDRIVER) {
			TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(pos);
			tile.filter = null;
			tile.invertFilter = false;
			tile.markDirty();
			tile.refresh();
			return true;
		}

		if(tool == ToolType.HAND_DRILL) {
			TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(pos);
			tile.invertFilter = !tile.invertFilter;
			tile.markDirty();
			tile.refresh();
			return true;
		}

		return false;
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
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

		TileEntityFoundryOutlet outlet = (TileEntityFoundryOutlet) world.getTileEntity(pos);
		List<String> text = new ArrayList<String>();

		if(outlet.filter != null) {
			text.add(TextFormatting.YELLOW + I18nUtil.resolveKey("foundry.filter", outlet.filter.getLocalizedName()));
		}
		if(outlet.invertFilter) {
			text.add(TextFormatting.YELLOW + I18nUtil.resolveKey("foundry.invertFilter"));
		}
		if(outlet.invertRedstone) {
			text.add(TextFormatting.DARK_RED + I18nUtil.resolveKey("foundry.inverted"));
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getUnlocalizedName() + ".name"), 0xFF4000, 0x401000, text);
	}
}
