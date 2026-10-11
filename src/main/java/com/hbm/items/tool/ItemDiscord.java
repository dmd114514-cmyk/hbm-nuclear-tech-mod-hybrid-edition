package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.lib.Library;
import com.hbm.main.NTMSounds;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The dissonance wand. Right-click aimed at a block and it teleports you to right in front of the cell you're looking at.
 *
 * The old version derived the landing offset via `ForgeDirection.getOrientation(sideHit)`; here `RayTraceResult.sideHit`
 * is already an `EnumFacing`, so take the offset straight from it (DOWN..EAST share the same numbers on both; see deltas §14).
 */
public class ItemDiscord extends ItemBase {

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);
		RayTraceResult hit = Library.rayTrace(player, 100, 1);

		if(hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {

			if(!world.isRemote) {

				if(player.isRiding())
					player.dismountRidingEntity();

				Vec3d vec = hit.hitVec;
				EnumFacing side = hit.sideHit;

				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolveVanilla(NTMSounds.VANILLA_TELEPORT), SoundCategory.PLAYERS, 1.0F, 1.0F);
				player.setPositionAndUpdate(vec.x + side.getFrontOffsetX(), vec.y + side.getFrontOffsetY() - 1, vec.z + side.getFrontOffsetZ());
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolveVanilla(NTMSounds.VANILLA_TELEPORT), SoundCategory.PLAYERS, 1.0F, 1.0F);
				player.fallDistance = 0.0F;
			}

			for(int i = 0; i < 32; ++i)
				world.spawnParticle(EnumParticleTypes.PORTAL, player.posX, player.posY + player.getRNG().nextDouble() * 2.0D, player.posZ, player.getRNG().nextGaussian(), 0.0D, player.getRNG().nextGaussian());
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "I've seen the Rod of Discord and honestly");
		list.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "it's not as amazing as people say.");
		list.add("");
		list.add(TextFormatting.RED + "" + TextFormatting.ITALIC + "Rod of Discord is crucial in so many boss fights.");
		list.add(TextFormatting.RED + "" + TextFormatting.ITALIC + "Imagine getting coiled by worm bosses.");
		list.add("");
		list.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "Oh, you mean the Terraria item.");
		list.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "Idk about that thing.");
	}
}
