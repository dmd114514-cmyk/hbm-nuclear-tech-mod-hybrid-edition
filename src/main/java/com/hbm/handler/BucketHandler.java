package com.hbm.handler;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The master table for custom "right-click an empty bucket, scoop a block": register "which block becomes which bucket" into buckets, this cashes it in.
 *
 * The event shape changed this version: the result field went private and the setter is setFilledBucket (not setFilledResult),
 * the hit point merged into one BlockPos (`getTarget().getBlockPos()`), and before scooping you must confirm the hit really was a block.
 *
 * The table gets filled by the content side (the old version's few put lines in items/ModItems); this class only executes, silent when unwired.
 */
public class BucketHandler {

	public static BucketHandler INSTANCE = new BucketHandler();
	public Map<Block, Item> buckets = new HashMap<Block, Item>();

	private BucketHandler() {
	}

	@SubscribeEvent
	public void onBucketFill(FillBucketEvent event) {

		ItemStack result = fillCustomBucket(event.getWorld(), event.getTarget());

		if(result == null)
			return;

		event.setFilledBucket(result);
		event.setResult(Event.Result.ALLOW);
	}

	private ItemStack fillCustomBucket(World world, RayTraceResult pos) {

		if(pos.typeOfHit != RayTraceResult.Type.BLOCK)
			return null;

		BlockPos hit = pos.getBlockPos();
		IBlockState state = world.getBlockState(hit);
		Block block = state.getBlock();

		Item bucket = buckets.get(block);
		if(bucket != null && block.getMetaFromState(state) == 0) {
			world.setBlockToAir(hit);
			return new ItemStack(bucket);
		} else
			return null;

	}
}
