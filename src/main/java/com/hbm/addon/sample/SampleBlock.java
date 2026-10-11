package com.hbm.addon.sample;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Sample block: deliberately carries a facing property as a living example of "IBlockState + IProperty instead of int meta".
 */
public class SampleBlock extends Block {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	// Order copies the vanilla horizontal index convention so getMetaFromState / getStateFromMeta always pair up
	private static final EnumFacing[] FACE_ORDER = { EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST };

	public SampleBlock() {
		super(Material.IRON);
		setUnlocalizedName("hbm.sample_block");
		setCreativeTab(SampleCreativeTab.INSTANCE);
		setHardness(3.0F);
		setResistance(10.0F);
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, FACE_ORDER[meta & 3]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		EnumFacing facing = state.getValue(FACING);
		for(int i = 0; i < FACE_ORDER.length; i++) {
			if(FACE_ORDER[i] == facing) return i;
		}
		return 0;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		int index = Math.floorMod((int) Math.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5D), 4);
		world.setBlockState(pos, state.withProperty(FACING, FACE_ORDER[index]), 2);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(world.isRemote) return true;

		// Both hands fire the right-click in 1.12.2; without a hand check the message doubles up
		if(hand == EnumHand.MAIN_HAND) {
			player.sendMessage(new TextComponentString("HBM skeleton sample: this face faces " + state.getValue(FACING)));
		}
		return true;
	}
}
