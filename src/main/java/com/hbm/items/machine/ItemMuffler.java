package com.hbm.items.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.NTMSounds;
import com.hbm.sound.SoundRefs;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The muffler insert: click a machine to hush its running sound (tenfold falloff, the check living on
 * {@link TileEntityLoadedBase}'s side), consuming one insert.
 *
 * The old "find the real body behind this position" went through util's CompatExternal, a utility not migrated;
 * the same semantics are resolved inline (mk2's dummy block asks BlockDummyable, everything else goes through IPersistentNBT.resolveCore) —
 * the same convention as the registration side's break-block-spills-data chain.
 */
public class ItemMuffler extends Item {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(world.isRemote) return EnumActionResult.PASS;

		TileEntity tile = coreAt(world, pos);

		if(!(tile instanceof TileEntityLoadedBase)) return EnumActionResult.PASS;

		TileEntityLoadedBase machine = (TileEntityLoadedBase) tile;

		if(machine.muffled) return EnumActionResult.PASS;

		machine.muffled = true;
		machine.markDirty();

		world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundRefs.resolve(new ResourceLocation(NTMSounds.UPGRADE_PLUG)), SoundCategory.BLOCKS, 1.0F, 1.0F);

		player.getHeldItem(hand).shrink(1);

		return EnumActionResult.SUCCESS;
	}

	private TileEntity coreAt(World world, BlockPos pos) {

		Block block = world.getBlockState(pos).getBlock();

		if(block instanceof BlockDummyable) {
			BlockPos core = ((BlockDummyable) block).findCore(world, pos);
			if(core != null) return world.getTileEntity(core);
		}

		return IPersistentNBT.resolveCore(world, pos);
	}
}
