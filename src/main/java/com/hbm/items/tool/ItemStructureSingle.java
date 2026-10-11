package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemStructureSingle extends ItemStructureTool {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.YELLOW + "Click to print exactly one <placeBlockAtCurrentPosition>");
		list.add(TextFormatting.YELLOW + "line with the targeted block and metadata");
	}

	@Override
	protected void doTheThing(ItemStack stack, World world, int x, int y, int z) {

		BlockPos anchor = getAnchor(stack);
		if(anchor == null) return;

		NBTTagCompound nbt = stack.getTagCompound();
		int ix = x - anchor.getX();
		int iy = y - anchor.getY();
		int iz = z - anchor.getZ();

		Block b = blockAt(world, x, y, z);
		int meta = metaAt(world, x, y, z);

		String message = "placeBlockAtCurrentPosition(world, " + b.getUnlocalizedName() + ", " + meta + ", " + ix + ", " + iy + ", " + iz + ", box);\n";
		System.out.print(message);
		writeToFile(message);
	}
}
