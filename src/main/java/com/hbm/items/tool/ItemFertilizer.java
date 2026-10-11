package com.hbm.items.tool;

import com.hbm.items.ItemBase;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.BonemealEvent;

/**
 * The bone powder's sibling: ripens a whole 3x3x3 block at once.
 *
 * Vanilla returns false from it (it clearly worked but tells the game "nothing happened"), so the held stack neither
 * consumes durability-style semantics — but the count does drop. That "looks like nothing happened" behavior is original; not fixed in passing.
 */
public class ItemFertilizer extends ItemBase {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.getHeldItem(hand);

		if(!player.canPlayerEdit(pos, facing, stack)) return EnumActionResult.PASS;

		boolean didSomething = false;

		for(int i = pos.getX() - 1; i <= pos.getX() + 1; i++) {
			for(int j = pos.getY() - 1; j <= pos.getY() + 1; j++) {
				for(int k = pos.getZ() - 1; k <= pos.getZ() + 1; k++) {
					BlockPos target = new BlockPos(i, j, k);
					boolean success = fertilize(world, target, player, hand, stack, target.equals(pos));
					didSomething = didSomething || success;
					if(success && !world.isRemote) world.playEvent(2005, target, 0);
				}
			}
		}

		if(didSomething && !player.capabilities.isCreativeMode) stack.shrink(1);

		return EnumActionResult.PASS;
	}

	/*For machines: ripens with a fake player, no real person needs to stand by */
	public static boolean useFertillizer(ItemStack stack, World world, BlockPos pos) {

		if(!(world instanceof WorldServer)) return false;

		EntityPlayer player = FakePlayerFactory.getMinecraft((WorldServer) world);
		boolean didSomething = false;

		for(int i = pos.getX() - 1; i <= pos.getX() + 1; i++) {
			for(int j = pos.getY() - 1; j <= pos.getY() + 1; j++) {
				for(int k = pos.getZ() - 1; k <= pos.getZ() + 1; k++) {
					BlockPos target = new BlockPos(i, j, k);
					boolean success = fertilize(world, target, player, EnumHand.MAIN_HAND, stack, target.equals(pos));
					didSomething = didSomething || success;
					if(success && !world.isRemote) world.playEvent(2005, target, 0);
				}
			}
		}

		if(didSomething) stack.shrink(1);

		return didSomething;
	}

	public static boolean fertilize(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack stack, boolean force) {

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		BonemealEvent event = new BonemealEvent(player, world, pos, state, hand, stack);
		if(MinecraftForge.EVENT_BUS.post(event)) return false;
		if(event.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) return true;

		if(block instanceof IGrowable) {
			IGrowable growable = (IGrowable) block;

			if(growable.canGrow(world, pos, state, world.isRemote)) {

				if(!world.isRemote) {
					if(force || growable.canUseBonemeal(world, world.rand, pos, state)) {
						growable.grow(world, world.rand, pos, state);
					}
				}

				return true;
			}
		}

		return false;
	}
}
