package com.hbm.render.tileentity;

import com.hbm.blocks.rail.RailStandardSwitch;
import com.hbm.blocks.rail.RailStandardSwitchFlipped;
import com.hbm.render.loader.HFRWavefrontObject;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The standard gauge switch's in-world drawing; both sides share one renderer.
 *
 * The two share one tile class (the reversed switch only swaps the siding's nodes and parts), so binding can only branch by which block the tile sits on:
 * model and sign textures are each a new pair of names, while the body section still uses the block's own rail_standard_straight.
 *
 * Body and sign draw into the same buffer in two passes: the sign swaps between SignStraight / SignTurn with the tile's switch bit,
 * which is exactly why this goes through TESR and not the block state — one geometry, two textures, one bit.
 */
@SideOnly(Side.CLIENT)
public class RenderRailLargeSwitch extends TileEntityObjRenderer<RailStandardSwitch.TileEntityRailSwitch> {

	/** Body texture: the same one the block's own placeholder JSON references, already in the atlas */
	private static final String SPRITE_RAIL = "hbm:blocks/rail_standard_straight";
	private static final String SPRITE_SIGN = "hbm:blocks/rail_switch_sign";
	private static final String SPRITE_SIGN_FLIPPED = "hbm:blocks/rail_switch_sign_flipped";

	private static final String MODEL = "models/blocks/rail_standard_switch.obj";
	private static final String MODEL_FLIPPED = "models/blocks/rail_standard_switch_flipped.obj";

	/** The only way to tell the two sides apart */
	private static boolean isFlipped(RailStandardSwitch.TileEntityRailSwitch tile) {
		return tile.getBlockType() instanceof RailStandardSwitchFlipped;
	}

	@Override
	protected String modelPath(RailStandardSwitch.TileEntityRailSwitch tile) {
		return isFlipped(tile) ? MODEL_FLIPPED : MODEL;
	}

	@Override
	protected Pose pose(RailStandardSwitch.TileEntityRailSwitch tile) {

		int meta = tile.getBlockMetadata();

		// The old RailStandardSwitch.renderWorld facing table: meta 12 turns 180°, 14 turns 270°, 15 turns 90°, 13 stays as-is
		float yaw = 0F;
		if(meta == 15) yaw = (float) Math.PI * 0.5F;
		else if(meta == 12) yaw = (float) Math.PI;
		else if(meta == 14) yaw = (float) Math.PI * 1.5F;

		return corePose(meta, yaw);
	}

	@Override
	protected void draw(RailStandardSwitch.TileEntityRailSwitch tile, HFRWavefrontObject model, Pose pose) {

		boolean flipped = isFlipped(tile);

		drawPart(model, "Rail", SPRITE_RAIL, pose);

		// The sign: the switch bit picks which part set; both sides use the same part names, different textures
		String part = tile.isSwitched ? "SignTurn" : "SignStraight";

		drawPart(model, part, flipped ? SPRITE_SIGN_FLIPPED : SPRITE_SIGN, pose);
	}

	@Override
	protected boolean isGlobal() {
		// The switch's true body is one cell but the body runs fifteen cells down the siding; this version culls by the tile's render box, and without the global flag one camera move swallows the whole track
		return true;
	}
}
