package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.IBlockNaming;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A pair of "powered lights up, unpowered goes out" lamps (landing this mod's two reinforced lamps: one off block, one on).
 *
 * Shape matches this version's vanilla redstone lamp (javap-verified: {@code BlockRedstoneLight}'s six external doors are exactly the six below:
 * {@code onBlockAdded}／{@code neighborChanged}／{@code updateTick}／{@code getItemDropped}／
 * {@code getItem} / {@code getSilkTouchDrop} included), but that one hard-wires its lamp pairing to {@code Blocks.REDSTONE_LAMP} and
 * {@code Blocks.LIT_REDSTONE_LAMP} (private fields, no overridable door), so our pair can't plug in ⇒ self-built, as noted in the registration comment.
 * The lit one carries a full-light tier ({@code setLightLevel(1.0F)}, as before only the lit piece).
 *
 * The mutual pointing avoids static fields (the old read of {@code ModBlocks.reinforced_lamp_on} breaks if registration order shifts,
 * so the registrar calls {@link #pair(Block)} once per piece after both are built: who points at whom stays obvious, and constructor-order traps vanish.
 *
 * Two more old spots changed faces here, landing on the new ones: silk touch's {@code createStackedBlock(int)} became
 * {@code getSilkTouchDrop(IBlockState)}; the middle-click fetch {@code getItem} now returns an ItemStack
 * and lost one non-World parameter. The four-tick delayed shutoff ({@code scheduleBlockUpdate}) and the "only switch when it should" check
 * both stay; the check's door is now {@code isBlockIndirectlyGettingPowered(BlockPos)} (returns strength; the old one was boolean).
 *
 * Drops, middle-click pickup and silk touch all return the off state: the lit block is just the off one swapped in place while powered,
 */
public class BlockNTMLamp extends Block implements IBlockNaming {

	private final boolean isOn;
	private Block counterpart;

	/** @param isOn whether this is the lit piece; the lit one carries full light */
	public BlockNTMLamp(Material mat, boolean isOn) {
		super(mat);

		this.isOn = isOn;

		if(isOn) setLightLevel(1.0F);
	}

	/** Called by the registrar after both are built: hands over the partner for switching and drops */
	public BlockNTMLamp pair(Block other) {
		this.counterpart = other;
		return this;
	}

	/** Textures belong to JSON models; this only absorbs the old chain; returning this class reaches the later setBlockName */
	public BlockNTMLamp setBlockTextureName(String name) {
		return this;
	}

	/** Same door as {@code BlockBase}'s: the step-sound setter is the protected {@code setSoundType(SoundType)} here */
	public BlockNTMLamp setStepSound(SoundType type) {
		setSoundType(type);
		return this;
	}

	/** This pair's off state: the lit one points at its partner, the off one at itself (falling back to self when unset — a wrong display beats an NPE) */
	private Block offSide() {
		return this.isOn && this.counterpart != null ? this.counterpart : this;
	}

	/** This pair's lit state */
	private Block onSide() {
		return !this.isOn && this.counterpart != null ? this.counterpart : this;
	}

	/** Placement and neighbor changes run the same block: only the server writes, and only when "should switch but hasn't" */
	private void checkPair(World world, BlockPos pos) {
		if(world.isRemote) return;

		if(this.isOn) {
			if(world.isBlockIndirectlyGettingPowered(pos) <= 0) world.scheduleBlockUpdate(pos, this, 4, 0);
		} else if(world.isBlockIndirectlyGettingPowered(pos) > 0) {
			world.setBlockState(pos, this.onSide().getDefaultState(), 2);
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		this.checkPair(world, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		this.checkPair(world, pos);
	}

	/** The lit block's delay elapsed with power already gone: swap back to off */
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if(!world.isRemote && this.isOn && world.isBlockIndirectlyGettingPowered(pos) <= 0) {
			world.setBlockState(pos, this.offSide().getDefaultState(), 2);
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(this.offSide());
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this.offSide());
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(this.offSide());
	}
}
