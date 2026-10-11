package com.hbm.render;

import com.hbm.blocks.generic.BlockBobble;
import com.hbm.blocks.rail.RailStandardSwitch;
import com.hbm.render.tileentity.RenderBobble;
import com.hbm.render.tileentity.RenderLandmine;
import com.hbm.render.tileentity.RenderMassStorage;
import com.hbm.render.tileentity.RenderProbe;
import com.hbm.render.tileentity.RenderRailLargeSwitch;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;
import com.hbm.util.LoggingUtil;

import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraftforge.fml.client.registry.ClientRegistry;

/**
 * The TESR registry. The old version scattered its ClientRegistry.bindTileEntitySpecialRenderer calls across ClientProxy's
 * several-hundred-line registerRenders, where everyone wanted to squeeze in a line; now it's one entry point, hung off ClientProxy.init
 * with a single line, and per-machine ports only ever add here.
 *
 * init timing is enough: binding just puts an entry into the dispatcher's table, no TE registration event involved,
 * as long as it's done before the first TE enters the frame.
 */
public final class HbmTileRenderers {

	private HbmTileRenderers() { }

	public static void registerAll() {
		int bound = 0;

		// Probe: see the RenderProbe class note
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnchantmentTable.class, new RenderProbe());
		bound++;

		// Standard gauge switch: both sides share one tile class; the renderer swaps model and sign texture by which block it sits on
		ClientRegistry.bindTileEntitySpecialRenderer(RailStandardSwitch.TileEntityRailSwitch.class, new RenderRailLargeSwitch());
		bound++;

		// Mass storage: the front shows the currently stored item, with a stock line and a fill-level color bar
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMassStorage.class, new RenderMassStorage());
		bound++;

		// Bobblehead: the block declares INVISIBLE itself (no body model) — leave it unbound and you get a cell of air
		ClientRegistry.bindTileEntitySpecialRenderer(BlockBobble.TileEntityBobble.class, new RenderBobble());
		bound++;

		// The five landmines: one tile class shared; which one gets drawn is decided by the block's Kind
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLandmine.class, new RenderLandmine());
		bound++;

		// One self-check line: whether binding actually happened — run the client once and read the log
		LoggingUtil.info("[hbm] block renderer binding complete, {} total", bound);
	}
}
