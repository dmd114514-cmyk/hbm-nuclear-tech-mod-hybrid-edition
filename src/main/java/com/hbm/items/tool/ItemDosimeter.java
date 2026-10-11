package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.main.NTMSounds;
import com.hbm.util.ContaminationUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

/**
 * The pocket dosimeter. One notch more sensitive than the handheld geiger (starts ticking at low doses); right-click reads your accumulated dose, not the current field.
 *
 * The `x >= 1 && x >= 2`-style lines in the tier table are old code verbatim, not typos to "fix" into `&&`/`||`:
 * they decide which tiers make it into the candidate pool, and touching them changes the feel.
 */
public class ItemDosimeter extends ItemBase {

	Random rand = new Random();

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

		if(!(entity instanceof EntityLivingBase) || world.isRemote)
			return;

		float x = HbmLivingProps.getRadBuf((EntityLivingBase)entity);

		if(world.getTotalWorldTime() % 5 == 0) {

			if(x > 1E-5) {
				if(world.rand.nextFloat() > x) return;

				List<Integer> list = new ArrayList<Integer>();

				if(x < 0.5) list.add(0);
				if(x < 1) list.add(1);
				if(x >= 0.5 && x < 2) list.add(2);
				if(x >= 1 && x >= 2) list.add(3);

				int r = list.get(rand.nextInt(list.size()));

				if(r > 0)
					world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve(NTMSounds.GEIGER_PREFIX + r), SoundCategory.PLAYERS, 1.0F, 1.0F);

			} else if(rand.nextInt(100) == 0) {
				world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve(NTMSounds.GEIGER_PREFIX + 1), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(!world.isRemote) {
			world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.TECH_BOOP), SoundCategory.PLAYERS, 1.0F, 1.0F);
			ContaminationUtil.printDosimeterData(player);
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
}
