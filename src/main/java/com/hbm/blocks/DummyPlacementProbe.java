package com.hbm.blocks;

import java.io.File;

import com.hbm.blocks.rail.RailStandardSwitch;
import com.hbm.main.MainRegistry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

/**
 * The dev-time live placement probe: lays fake multiblocks down in a **real world** to verify what synthetic geometry can't reach
 * (where the core gets pushed during placement, the placeholder shape laid out, core lookups, orphan self-cleanup after breaking).
 *
 * Runs once only when the marker file <code>hbm-dummy-probe.txt</code> sits in the working directory; real environments won't have it.
 * Any block would do; the standard straight rail was picked as the one leaning hardest on mirror math (dims U0 D0 N2 S2 W1 E0, extrapolate 2).
 */
public class DummyPlacementProbe {

	private static final String MARKER = "hbm-dummy-probe.txt";
	private static final String LOG = "dummy-probe";

	private static boolean ran = false;
	private static int ticks = 0;

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {

		if(event.phase != Phase.END || ran)
			return;

		if(++ticks < 40)
			return;

		if(!new File(MARKER).isFile())
			return;

		ran = true;

		try {
			run(FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0]);
		} catch(Exception ex) {
			MainRegistry.logger.warn("{}: probe crashed - {}", LOG, ex.toString());
			ex.printStackTrace();
		}
	}

	private static void run(WorldServer world) {

		BlockPos spawn = world.getSpawnPoint();

		clearArea(world, spawn, 60);

		int fails = 0;

		// The short family fits in an 8-block view; switch branches lay out to fourteen, so the window widens and the spots spread further apart
		fails += trial(world, (BlockDummyable) ModBlocks.rail_large_straight, spawn, 8, 0);
		fails += trial(world, (BlockDummyable) ModBlocks.rail_large_switch, spawn.add(0, 0, 60), 18, 0);
		fails += trial(world, (BlockDummyable) ModBlocks.rail_large_switch_flipped, spawn.add(0, 0, 140), 18, 0);

		MainRegistry.logger.info("{} verdict: {}", LOG, fails == 0 ? "in-world four-direction placement/core return/orphan self-cleanup all passed" : fails + " mismatches");
	}

	/** Places one block four ways in a real world, checking core, return-to-core, and leftovers after breaking the core */
	private static int trial(WorldServer world, BlockDummyable block, BlockPos origin, int radius, int unused) {

		if(block == null) {
			MainRegistry.logger.warn("{}: test block not registered, skipping", LOG);
			return 1;
		}

		FakePlayer player = FakePlayerFactory.getMinecraft(world);
		int fails = 0;

		for(EnumFacing facing : new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST }) {

			// which way we're standing and facing: yaw 0/90/180/-90 → the four branches of the old i→facing mapping
			player.rotationYaw = facing == EnumFacing.NORTH ? 0F : facing == EnumFacing.EAST ? 90F : facing == EnumFacing.SOUTH ? 180F : -90F;

			int reach = facing == EnumFacing.NORTH ? 0 : facing == EnumFacing.SOUTH ? 2 * radius : facing == EnumFacing.WEST ? 0 : 2 * radius;
			BlockPos here = facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH ? origin.add(0, 0, reach) : origin.add(reach, 0, 0);
			BlockPos clicked = here.down();

			clearArea(world, here, radius);

			// The path a player can actually take: initial meta from the clicked face first, then onBlockPlacedBy lays out the whole structure
			IBlockState initial = block.getStateFromMeta(EnumFacing.UP.ordinal());
			world.setBlockState(clicked, initial, 3);
			block.onBlockPlacedBy(world, clicked, initial, player, new ItemStack(block));

			StringBuilder line = new StringBuilder();
			int cores = 0;
			int dummies = 0;
			int lost = 0;
			BlockPos core = null;

			for(int dx = -radius; dx <= radius; dx++)
				for(int dz = -radius; dz <= radius; dz++) {

					BlockPos cell = clicked.add(dx, 0, dz);
					IBlockState state = world.getBlockState(cell);

					if(state.getBlock() != block)
						continue;

					int meta = block.getMetaFromState(state);

					if(BlockDummyable.isCore(meta)) {
						cores++;
						core = cell;
						line.append(" C").append(meta).append("@").append(dx).append(",").append(dz);
					} else {
						dummies++;
						BlockPos back = block.findCore(world, cell);
						if(back == null || core != null && !back.equals(core))
							lost++;
						line.append(" ").append(meta).append("@").append(dx).append(",").append(dz);
					}
				}

			boolean facingRight = core != null && BlockDummyable.facingForMeta(block.getMetaFromState(world.getBlockState(core)) - BlockDummyable.offset) == facing;

			if(!facingRight || cores != 1 || lost != 0)
				fails++;

			MainRegistry.logger.info("{} placed {} {}: core={} placeholders={} unfindable cores={} facing ok={}", LOG, block.getRegistryName(), facing.getName(), cores, dummies, lost, facingRight ? "YES" : "NO");
			MainRegistry.logger.info("{} grid map {} {}:{} ({} cells total)", LOG, block.getRegistryName(), facing.getName(), line.toString(), cores + dummies);

			// TE-bearing families get another "real player click" pass: toggle once and check the core's switch bit flipped (sync packet included)
			if(block instanceof RailStandardSwitch) {

				TileEntity te = core != null ? world.getTileEntity(core) : null;
				RailStandardSwitch.TileEntityRailSwitch sw = te instanceof RailStandardSwitch.TileEntityRailSwitch
						? (RailStandardSwitch.TileEntityRailSwitch) te : null;
				boolean before = sw != null && sw.isSwitched;

				block.onBlockActivated(world, clicked, world.getBlockState(clicked), player, EnumHand.MAIN_HAND, facing, 0F, 0F, 0F);

				boolean flipped = sw != null && sw.isSwitched != before;
				if(!flipped)
					fails++;

				MainRegistry.logger.info("{} toggled {} {}: core has TE={} flip={} current={}", LOG, block.getRegistryName(),
						facing.getName(), sw != null, flipped ? "YES" : "NO", sw == null ? "-" : String.valueOf(sw.isSwitched));
			}

			// Break the core and see whether orphaned neighbors clean themselves up (the old path was destroyIfOrphan inside neighborChanged)
			if(core != null) {
				world.setBlockToAir(core);
				for(int dx = -radius; dx <= radius; dx++)
					for(int dz = -radius; dz <= radius; dz++) {
						BlockPos cell = clicked.add(dx, 0, dz);
						if(world.getBlockState(cell).getBlock() == block)
							block.neighborChanged(world.getBlockState(cell), world, cell, Blocks.AIR, cell);
					}
			}

			int left = 0;

			for(int dx = -radius; dx <= radius; dx++)
				for(int dz = -radius; dz <= radius; dz++)
					if(world.getBlockState(clicked.add(dx, 0, dz)).getBlock() == block)
						left++;

			if(left > 0)
				fails++;

			MainRegistry.logger.info("{} leftover placeholder cells after core removal {} {}: {}", LOG, block.getRegistryName(), facing.getName(), left);

			clearArea(world, here, radius + 2);
		}

		return fails;
	}

	private static void clearArea(World world, BlockPos center, int radius) {

		for(int dx = -radius; dx <= radius; dx++)
			for(int dz = -radius; dz <= radius; dz++)
				for(int dy = -2; dy <= 4; dy++)
					world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState(), 3);

		for(int dx = -radius; dx <= radius; dx++)
			for(int dz = -radius; dz <= radius; dz++)
				world.setBlockState(center.add(dx, -3, dz), Blocks.STONE.getDefaultState(), 3);
	}
}
