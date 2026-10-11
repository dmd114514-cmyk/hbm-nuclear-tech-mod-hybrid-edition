package com.hbm.handler;

import com.hbm.tileentity.IGUIProvider;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * Routes every openGui request back to its real provider by coordinates: check the tile entity, then the block itself,
 * then the held item, and finally x/y/z doubles as an entity id on the last leg (that's how the remote detonator console opens).
 *
 * Providers are always {@link IGUIProvider}; this class carries zero GUI knowledge — each machine family implements those two methods and done.
 */
public class GUIHandler implements IGuiHandler {

	@Override
	public Container getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		IGUIProvider provider = getProvider(player, world, x, y, z);
		if(provider == null)
			return null;
		return provider.provideContainer(id, player, world, new BlockPos(x, y, z));
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		IGUIProvider provider = getProvider(player, world, x, y, z);
		if(provider == null)
			return null;
		return provider.provideGUI(id, player, world, new BlockPos(x, y, z));
	}

	/** The provider lookup order is fixed; subclasses don't get to re-order it — when both TE and block implement it on one cell, the TE wins */
	private IGUIProvider getProvider(EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);

		if(world.isBlockLoaded(pos)) {
			if(world.getTileEntity(pos) instanceof IGUIProvider)
				return (IGUIProvider) world.getTileEntity(pos);

			Block block = world.getBlockState(pos).getBlock();
			if(block instanceof IGUIProvider)
				return (IGUIProvider) block;
		}

		ItemStack held = player.getHeldItem(net.minecraft.util.EnumHand.MAIN_HAND);
		if(held != null && !held.isEmpty() && held.getItem() instanceof IGUIProvider)
			return (IGUIProvider) held.getItem();

		Entity entity = world.getEntityByID(x);
		if(entity instanceof IGUIProvider)
			return (IGUIProvider) entity;

		return null;
	}
}
