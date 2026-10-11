package com.hbm.items;

import java.util.List;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.IPersistentNBT;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The block items' unified base.
 * In 1.12.2 ItemBlock itself doesn't know subtype names; this asks the block,
 * or every multi-subtype block like stone_resource would show one name in inventories.
 */
public class ItemBlockBase extends ItemBlock {

	public ItemBlockBase(Block block) {
		super(block);
		// The block's registry name doesn't flow to the ItemBlock automatically; connect it by hand or the creative tab and models both lose the name
		this.setRegistryName(block.getRegistryName());
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if(this.block instanceof IBlockMulti) {
			return ((IBlockMulti) this.block).getUnlocalizedName(stack);
		}
		return super.getUnlocalizedName(stack);
	}

	/**
	 * When the block wants to say something beyond its drops, this forwards it — tooltips live on the item side here,
	 * while the old dozens of blocks wrote their info on ITooltipProvider; without the hook they'd all go mute.
	 * Must be client-side marked: this method touches Minecraft, and the server loading it is the old side-gate story again.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		EntityPlayer player = Minecraft.getMinecraft().player;

		if(this.block instanceof ITooltipProvider)
			((ITooltipProvider) this.block).addInformation(stack, player, list, flag.isAdvanced());

		if(this.block instanceof IPersistentInfoProvider && stack.hasTagCompound()
				&& stack.getTagCompound().hasKey(IPersistentNBT.NBT_PERSISTENT_KEY)) {

			NBTTagCompound data = stack.getTagCompound().getCompoundTag(IPersistentNBT.NBT_PERSISTENT_KEY);
			((IPersistentInfoProvider) this.block).addInformation(stack, data, player, list, flag.isAdvanced());
		}
	}
}
