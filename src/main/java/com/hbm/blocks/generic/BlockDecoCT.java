package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;
import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * The connected metal deco blocks (nine in the family: eight deco_* plus laythe_root).
 *
 * Same-family neighbors seam together, different families draw their own; the one exception is deco_steel and deco_rusty_steel connecting each other
 * (the old "i don't care" special case — rusted against clean steel isn't a continuity break).
 *
 * Wiring connected textures takes three block-side steps, all in this class (the model side untouched):
 *   1) {@code implements IBlockCT}, overriding {@code canConnect} when the rule isn't the default;
 *   2) {@code createBlockState()} swaps the container for the version carrying that unlisted context;
 *   3) {@code getExtendedState} hangs this cell's world coordinates onto the state.
 * The last two feed the renderer world coordinates; omitting them doesn't crash — it just degrades to the plain "nothing connects" look. The
 * blockstate JSON: that tier's model reads {@code hbm:ct/blocks/<base name>}, and the loader fetches
 * {@code hbm:blocks/<base name>} plus the same-named {@code _ct} image.
 *
 * This version's changed faces:
 *   · the old {@code getRenderType()=CT.renderID} has no landing spot (block-level render dispatch deleted) ⇒ blockstates carry it now
 *   · the old client {@code registerBlockIcons}' {@code primeReceiver} and the interface's {@code getFragments}
 *     are dropped as a set: textures are no longer registered by blocks (models name them), and the twenty fragment rectangles are computed live by {@code CTFragment},
 *     so this door narrows to {@code canConnect} here;
 *   · {@code canConnect(IBlockAccess, int x, int y, int z, Block)} became the version taking an {@code IBlockState} plus two
 *     {@code BlockPos}es, with the neighbor block fetched live from the world at {@code neighborPos}.
 *
 * ⚠ Two skipped and logged (both were no-ops for this family even in the old version; per "old no-ops don't port, no shells without callers" they stay out):
 *   · {@code allowFortune} / {@code noFortune()} only fed the line below;
 *   · {@code quantityDroppedWithBonus} only works when "the dropped thing isn't the block's own item form", and all nine family pieces
 *     {@code getItemDropped} to themselves ⇒ the old condition was always false. This version's drops take §15's table route anyway,
 *     where the context has no tool and fortune can't reach tables in the first place. Neither side holds, so both stay out;
 *     the registrar thereby loses the "eight pieces with noFortune, deco_stainless without" tier difference, noted here.
 *
 * Drops still come from vanilla's line (itself), skipping the {@code BlockLoot} bridge, hence no table.
 */
public class BlockDecoCT extends BlockBase implements IBlockCT {

	public BlockDecoCT(Material mat) {
		super(mat);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return CTContext.container(this, super.createBlockState());
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return CTContext.attach(world, pos, state);
	}

	@Override
	public boolean canConnect(IBlockAccess world, IBlockState selfState, BlockPos selfPos, BlockPos neighborPos) {

		Block neighbor = world.getBlockState(neighborPos).getBlock();

		if(this == ModBlocksGeneric.deco_steel && neighbor == ModBlocksGeneric.deco_rusty_steel)
			return true;
		if(this == ModBlocksGeneric.deco_rusty_steel && neighbor == ModBlocksGeneric.deco_steel)
			return true;

		return this == neighbor;
	}
}
