package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemStructureRandomly extends ItemStructureTool {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.YELLOW + "Click to print a <randomlyFillWithBlocks>");
		list.add(TextFormatting.YELLOW + "line with the targeted block.");
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

		Block b = blockAt(world, x, y, z);

		String line = "randomlyFillWithBlocks(world, box, rand, <limit>, " + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ", " + b.getUnlocalizedName() + ");\n";
		System.out.print(line);
		writeToFile(line);
	}
}
