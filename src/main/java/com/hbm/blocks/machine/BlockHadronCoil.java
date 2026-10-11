package com.hbm.blocks.machine;

import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * The hadron collider's coils (nine tiers; `factor` is their share of the magnetic field, read by the collider side per block identity).
 *
 * Looks ride connected textures; the block-side three steps match the connected deco family (see {@link com.hbm.blocks.generic.BlockDecoCT}'s
 * class note): implement the port, swap the container for the context-carrying version, hang world coordinates on the state; the switch stays in the blockstates —
 * those nine's model reads `hbm:ct/blocks/hadron_coil_*`.
 *
 * This family's `canConnect` differs from the deco family: the old check was `block instanceof BlockHadronCoil`,
 * meaning **cross-tier connects too** (a gold coil beside a bismuth coil still seams); copied.
 *
 * The old `getRenderType()=CT.renderID` plus the client's `registerBlockIcons`+`primeReceiver` and the interface's `getFragments`
 * have no landing spot here (block-level render dispatch deleted, textures named by models,
 * fragment rectangles computed live by `CTFragment`), so this version's door narrows to `canConnect` alone.
 */
public class BlockHadronCoil extends BlockBase implements IBlockCT, ITooltipProvider {

	public final int factor;

	public BlockHadronCoil(Material mat, int factor) {
		super(mat);
		this.factor = factor;
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
		return world.getBlockState(neighborPos).getBlock() instanceof BlockHadronCoil;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean ext) {
		// Only the client's ItemBlockBase.addInformation reaches this; the translation facade never resolves on the server
		list.add(I18nUtil.resolveKey("info.coil") + ": " + String.format(Locale.US, "%,d", factor));
	}
}
