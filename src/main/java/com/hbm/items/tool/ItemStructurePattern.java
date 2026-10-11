package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemStructurePattern extends ItemStructureTool {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.YELLOW + "Click to print all <placeBlockAtCurrentPosition>");
		list.add(TextFormatting.YELLOW + "lines for the current selection with blocks and metadata");
	}

	@Override
	protected boolean dualUse() {
		return true;
	}

	@Override
	protected void doTheThing(ItemStack stack, World world, int x, int y, int z) {

		BlockPos anchor = getAnchor(stack);
		if(anchor == null) return;

		NBTTagCompound nbt = stack.getTagCompound();
		int minX = Math.min(nbt.getInteger("x"), x) - anchor.getX();
		int minY = Math.min(nbt.getInteger("y"), y) - anchor.getY();
		int minZ = Math.min(nbt.getInteger("z"), z) - anchor.getZ();
		int maxX = Math.max(nbt.getInteger("x"), x) - anchor.getX();
		int maxY = Math.max(nbt.getInteger("y"), y) - anchor.getY();
		int maxZ = Math.max(nbt.getInteger("z"), z) - anchor.getZ();

		StringBuilder message = new StringBuilder();

		for(int ix = minX; ix <= maxX; ix++) {
			for(int iy = minY; iy <= maxY; iy++) {
				for(int iz = minZ; iz <= maxZ; iz++) {

					net.minecraft.util.math.BlockPos p = new net.minecraft.util.math.BlockPos(ix + anchor.getX(), iy + anchor.getY(), iz + anchor.getZ());
					if(blockAt(world, p.getX(), p.getY(), p.getZ()).isAir(world.getBlockState(p), world, p)) continue;

					message.append("placeBlockAtCurrentPosition(world, " + blockAt(world, p.getX(), p.getY(), p.getZ()).getUnlocalizedName() + ", " + metaAt(world, p.getX(), p.getY(), p.getZ()) + ", " + ix + ", " + iy + ", " + iz + ", box);\n");
				}
			}
		}

		System.out.print(message.toString());
		writeToFile(message.toString());
	}
}
