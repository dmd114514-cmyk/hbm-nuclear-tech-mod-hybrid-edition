package com.hbm.items.tool;

import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.main.NTMSounds;
import com.hbm.util.ContaminationUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

/** The digamma diagnostic. Right-click prints your dose ledgers (radiation/neutron/digamma) to chat. */
public class ItemDigammaDiagnostic extends ItemBase {

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(!world.isRemote) {
			world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.TECH_BOOP), SoundCategory.PLAYERS, 1.0F, 1.0F);
			ContaminationUtil.printDiagnosticData(player);
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
}
