package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.main.NTMSounds;
import com.hbm.util.ContaminationUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

/**
 * The handheld geiger counter. Carried on you it clicks in bursts by received dose; right-click prints the reading to chat.
 *
 * The click tier is "pick a tier by dose into the candidate pool, then roll from it", not "whichever tier you're in, use that one" —
 * higher doses stuff the pool with higher tiers, so it sounds like it speeds up. These thresholds match the old version line for line;
 * don't casually "correct" it into a table lookup — that changes the feel.
 */
public class ItemGeigerCounter extends ItemBase {

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

				if(x < 1) list.add(0);
				if(x < 5) list.add(0);
				if(x < 10) list.add(1);
				if(x > 5 && x < 15) list.add(2);
				if(x > 10 && x < 20) list.add(3);
				if(x > 15 && x < 25) list.add(4);
				if(x > 20 && x < 30) list.add(5);
				if(x > 25) list.add(6);

				int r = list.get(rand.nextInt(list.size()));

				if(r > 0)
					world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve(NTMSounds.GEIGER_PREFIX + r), SoundCategory.PLAYERS, 1.0F, 1.0F);
			} else if(rand.nextInt(50) == 0) {
				world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve(NTMSounds.GEIGER_PREFIX + 1), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	static void setFloat(ItemStack stack, float value, String name) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setFloat(name, value);
	}

	public static float getFloat(ItemStack stack, String name) {
		if(stack.hasTagCompound())
			return stack.getTagCompound().getFloat(name);

		return 0;
	}

	public static int check(World world, int x, int y, int z) {
		int rads = (int)Math.ceil(ChunkRadiationManager.proxy.getRadiation(world, x, y, z));
		return rads;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(!world.isRemote) {
			world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("hbm:item.techBoop"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			ContaminationUtil.printGeigerData(player);
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
}
