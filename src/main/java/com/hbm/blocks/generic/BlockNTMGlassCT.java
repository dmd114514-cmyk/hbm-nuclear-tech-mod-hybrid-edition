package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.IBlockNaming;
import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;

import net.minecraft.block.BlockBreakable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The connecting glass family (ten pieces: {@code reinforced_glass} / {@code reinforced_laminate} plus the {@code glass_*} set).
 *
 * Shape and looks ride the base {@code BlockBreakable} line: no opaque faces, shared faces skipped against the same kind,
 * and what's truly this family's is three things — whether breaking drops itself, which render layer, and the three connect steps.
 *
 * Connected textures wire per this version's three steps (same as {@link BlockDecoCT}'s class note): implement the port, swap the container for the context-carrying version,
 * hang world coordinates on the state; the switch is in the blockstates, with the ten models reading {@code hbm:ct/blocks/<base name>}.
 * {@code canConnect} isn't overridden — the old family never overrode it; the default "connects to same-named only" carries.
 *
 * This version's changed faces:
 *   · the old base constructor was {@code BlockBreakable(String, Material, boolean)}; this version's is
 *     {@code BlockBreakable(Material, boolean ignoreSimilarity)} (field name javap-verified): the name argument belongs to
 *     JSON models and the registry name here, moved to the front; the old boolean in that position was also false (= don't ignore similarity),
 *   · the old {@code getRenderBlockPass()} reporting 0/1 two passes has no door here (block-level render dispatch deleted),
 *     and the closest effective tier is {@code getBlockLayer()}, so the old {@code int layer} argument became a
 *     {@code BlockRenderLayer}: 0 → CUTOUT, 1 → TRANSLUCENT. The mapping is derived from both versions' semantics;
 *     whether the look matches vanilla wasn't eyeballed — only "models and textures load, no purple-black" was verified;
 *   · the old {@code renderAsNormalBlock()=false} is carried by {@code BlockBreakable.isOpaqueCube→false} here;
 *   · both drop overrides exist: what {@code dropBlockAsItem} actually walks here is the {@code IBlockState}-taking one,
 *     so writing only the old {@code quantityDropped(Random)} does nothing.
 *
 * ⚠ An old typo copied and logged (harmless here, not dug into): on the {@code glass_quartz} chain
 * the {@code setBlockTextureName} string missed its colon ({@code RefStrings.MODID + "glass_quartz"}),
 * which in the old version really did overwrite the constructor's correct name; here {@code setBlockTextureName} is only an absorber and textures belong to
 * JSON, so the string does nothing now. It stays verbatim in the registrar — changing it would break the old paper trail.
 */
public class BlockNTMGlassCT extends BlockBreakable implements IBlockCT, IBlockNaming {

	private final boolean dropsItself;
	private final BlockRenderLayer layer;

	/** @param dropsItself whether breaking drops itself (the old doesDrop); @param layer this version's render layer, mapping from the old pass number */
	public BlockNTMGlassCT(Material mat, boolean dropsItself, BlockRenderLayer layer) {
		super(mat, false);
		this.dropsItself = dropsItself;
		this.layer = layer;
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
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return this.layer;
	}

	@Override
	public int quantityDropped(Random rand) {
		return this.dropsItself ? 1 : 0;
	}

	/** The drop actually walks this one here; both are given, since overriding only one does nothing */
	@Override
	public int quantityDropped(IBlockState state, int fortune, Random rand) {
		return this.dropsItself ? 1 : 0;
	}

	/** The old one returned true (silk touch takes the glass block itself); copied */
	@Override
	protected boolean canSilkHarvest() {
		return true;
	}

	/** Textures belong to JSON; this only catches the old chain; returning this class reaches the later setBlockName */
	public BlockNTMGlassCT setBlockTextureName(String name) {
		return this;
	}

	/** Same as {@link #setBlockTextureName}: the setter is protected here, so the chained door is opened locally */
	public BlockNTMGlassCT setStepSound(SoundType type) {
		this.setSoundType(type);
		return this;
	}
}
