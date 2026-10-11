package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemStructureSolid extends ItemStructureTool {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.YELLOW + "Click to print a <fillWithMetadataBlocks> or <fillWithBlocks>");
		list.add(TextFormatting.YELLOW + "line with wildcard block and metadata");
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

		// Same as the original: the whole selection comes out as whichever block was clicked last
		Block b = blockAt(world, x, y, z);
		int meta = metaAt(world, x, y, z);

		String line;
		if(meta > 0)
			line = "fillWithMetadataBlocks(world, box, " + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ", " + b.getUnlocalizedName() + ", " + meta + ");\n";
		else
			line = "fillWithBlocks(world, box, " + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ", " + b.getUnlocalizedName() + ");\n";

		System.out.print(line);
		writeToFile(line);
	}
}
