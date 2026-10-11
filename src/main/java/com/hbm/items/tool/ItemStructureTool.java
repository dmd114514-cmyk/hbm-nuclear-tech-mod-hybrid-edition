package com.hbm.items.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.items.ItemBase;
import com.hbm.main.MainRegistry;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The structure samplers' common base: right-click the bedrock anchor ({@code ModBlocks.structure_anchor}) for the origin,
 * then one or two blocks for the selection, printing Mineways/structure-style script lines and writing
 * {@code hbm/structureOutput.txt} (disk writes only with debug mode on).
 *
 * Anchor and selection both live in NBT, coordinates using fauxpointtwelve's mutable BlockPos — this is the
 * "one instance moved around repeatedly" usage.
 */
public abstract class ItemStructureTool extends ItemBase implements ILookOverlay {

	private File file = new File(MainRegistry.configDir, "structureOutput.txt");
	private FileWriter writer;

	protected void writeToFile(String message) {

		if(!GeneralConfig.enableDebugMode)
			return;

		try {
			if(!file.exists()) file.createNewFile();
			if(writer == null) writer = new FileWriter(file, true);

			writer.write(message);
			writer.flush();
		} catch(IOException e) {
			System.out.print("ItemStructureTool encountered an IOException!");
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(getAnchor(stack) == null)
			list.add(TextFormatting.RED + "No anchor set! Right click an anchor to get started.");

		if(GeneralConfig.enableDebugMode)
			list.add(TextFormatting.GREEN + "Will write to \"structureOutput.txt\" in hbmConfig.");
	}

	public static BlockPos getAnchor(ItemStack stack) {

		if(!stack.hasTagCompound())
			return null;

		NBTTagCompound nbt = stack.getTagCompound();
		return new BlockPos(nbt.getInteger("anchorX"), nbt.getInteger("anchorY"), nbt.getInteger("anchorZ"));
	}

	public static void setAnchor(ItemStack stack, int x, int y, int z) {

		if(stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("anchorX", x);
		stack.getTagCompound().setInteger("anchorY", y);
		stack.getTagCompound().setInteger("anchorZ", z);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos, EnumHand hand, EnumFacing face, float cx, float cy, float cz) {

		ItemStack stack = player.getHeldItem(hand);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(world.getBlockState(pos).getBlock() == ModBlocks.structure_anchor) {
			setAnchor(stack, x, y, z);
			return EnumActionResult.SUCCESS;
		}

		if(getAnchor(stack) == null)
			return EnumActionResult.PASS;

		if(!this.dualUse() && world.isRemote) {
			this.doTheThing(stack, world, x, y, z);
			return EnumActionResult.SUCCESS;
		}

		if(!stack.hasTagCompound())
			return EnumActionResult.PASS;

		NBTTagCompound nbt = stack.getTagCompound();

		if(!nbt.hasKey("x")) {
			nbt.setInteger("x", x);
			nbt.setInteger("y", y);
			nbt.setInteger("z", z);
		} else {
			if(world.isRemote)
				this.doTheThing(stack, world, x, y, z);
			nbt.removeTag("x");
			nbt.removeTag("y");
			nbt.removeTag("z");
		}

		return EnumActionResult.SUCCESS;
	}

	protected boolean dualUse() {
		return false;
	}

	protected abstract void doTheThing(ItemStack stack, World world, int x, int y, int z);

	protected static Block blockAt(World world, int x, int y, int z) {
		return world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock();
	}

	protected static int metaAt(World world, int x, int y, int z) {
		net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		return state.getBlock().getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, net.minecraft.util.math.BlockPos pos) {

		EntityPlayer player = Minecraft.getMinecraft().player;
		ItemStack stack = player == null ? ItemStack.EMPTY : player.getHeldItemMainhand();
		List<String> text = new ArrayList<String>();

		BlockPos anchor = getAnchor(stack);

		if(anchor == null) {
			text.add(TextFormatting.RED + "No Anchor");
		} else {

			int dX = pos.getX() - anchor.getX();
			int dY = pos.getY() - anchor.getY();
			int dZ = pos.getZ() - anchor.getZ();
			text.add(TextFormatting.YELLOW + "Position: " + dX + " / " + dY + " / " + dZ);

			if(this.dualUse() && stack.hasTagCompound() && stack.getTagCompound().hasKey("x")) {
				NBTTagCompound nbt = stack.getTagCompound();
				int sX = Math.abs(pos.getX() - nbt.getInteger("x")) + 1;
				int sY = Math.abs(pos.getY() - nbt.getInteger("y")) + 1;
				int sZ = Math.abs(pos.getZ() - nbt.getInteger("z")) + 1;
				text.add(TextFormatting.GOLD + "Selection: " + sX + " / " + sY + " / " + sZ);
			}
		}

		if(player != null && player.isSneaking())
			text.add("B: " + blockAt(world, pos.getX(), pos.getY(), pos.getZ()).getUnlocalizedName() + ", M: " + metaAt(world, pos.getX(), pos.getY(), pos.getZ()));

		ILookOverlay.printGeneric(event, this.getItemStackDisplayName(stack), 0xffff00, 0x404000, text);
	}
}
