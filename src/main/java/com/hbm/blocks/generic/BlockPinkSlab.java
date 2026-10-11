package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.IBlockNaming;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The pink wood slab and its full-block twin (pink_slab / pink_double_slab).
 *
 * This family has one material, so it skips {@link BlockMultiSlab}'s subtype property table — the existing
 * blockstates are written for the two {@code half} tiers, and adding a variant would mean rewriting assets.
 * Top/bottom halves, drop counts and placement picking all ride the base {@code BlockSlab}: doubles drop 2 ({@code quantityDropped}),
 * and which item drops is forwarded to the single slab by the {@code getItemDropped} below — the old {@code Item.getItemFromBlock(ModBlocks.…)} line's job,
 * except the "single slab" arrives as a constructor argument per the house style, not a static field read (statics break when registration order shifts).
 *
 * Two more old spots with no counterpart here, logged not ported:
 *   1) {@code createStackedBlock} (folding the full block into two singles on silk touch) — this version's {@code BlockSlab.canSilkHarvest} returns false outright
 *      (bytecode-verified: the whole slab family refuses silk touch), leaving that line caller-less;
 *   2) {@code func_150002_b(int)} returning null meant "no subtype name" — this version's {@code getTranslationKey} counterpart
 *      likewise returns the bare suffix-less name, keeping the old {@code tile.pink_slab.name} lang working as-is.
 *
 * Hardness and blast were never set in the old version; likewise unset here. Neither piece enters the creative tab (the old {@code setCreativeTab(null)} carried).
 */
public class BlockPinkSlab extends BlockSlab implements IBlockNaming {

	private final boolean doubleSlab;
	private final Block single;

	/** @param single the single slab itself; the single passes null. The full block's drops and item form both point at it */
	public BlockPinkSlab(boolean isDouble, Block single, Material mat) {
		super(mat);

		this.doubleSlab = isDouble;
		this.single = single;
		this.useNeighborBrightness = true;

		this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, EnumBlockHalf.BOTTOM));
	}

	/** Textures belong to JSON models; the old chained writing is caught here; the return type must be this class, or the chain breaks */
	public BlockPinkSlab setBlockTextureName(String name) {
		return this;
	}

	/** Same door as {@code BlockBase}'s: step sounds became the protected {@code setSoundType(SoundType)} here */
	public BlockPinkSlab setStepSound(SoundType type) {
		setSoundType(type);
		return this;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		// the full block also carries half: this method runs inside super(), when isDouble is still unassigned and unreadable
		return new BlockStateContainer(this, HALF);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return !this.isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP ? 8 : 0;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(this.single != null ? this.single : this);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public boolean isDouble() {
		return this.doubleSlab;
	}

	/** One material only, so the slab's own property tier stands in; this mod's item side never reads it (ItemBlockBase doesn't ask) */
	@Override
	public IProperty<?> getVariantProperty() {
		return HALF;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
		return EnumBlockHalf.BOTTOM;
	}

	@Override
	public String getUnlocalizedName(int meta) {
		return super.getUnlocalizedName();
	}
}
