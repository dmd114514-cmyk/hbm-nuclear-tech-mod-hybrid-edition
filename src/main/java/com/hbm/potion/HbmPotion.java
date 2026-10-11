package com.hbm.potion;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * This mod's potion types.
 *
 * All 15 are registered into the vanilla potion registry, but the behavior side only covers the ones whose dependencies are already in the tree:
 * radiation / radaway / lead / slippery / phosphorus / flashbang / nitan。
 * the rest still have a name but no behavior; what each one is stuck on is written below — don't treat them as done:
 *   · taint —— the immunity list wants EntityCreeperTainted / EntityTaintCrab; the footprint needs the ModBlocks.taint block
 *   · bang / run —— the custom explosion set (bang wants ExplosionLarge's particles, run wants ExplosionVNT's makeAmat)
 *   · death / stability / potionsickness / radx / mutation —— triggered from other systems, no periodic behavior of their own
 *
 * nitan needs someone bumping the counter (drink the item, +1); this class only cares about "3 means overdose".
 */
public class HbmPotion extends Potion {

	public static HbmPotion taint;
	public static HbmPotion radiation;
	public static HbmPotion bang;
	public static HbmPotion mutation;
	public static HbmPotion radx;
	public static HbmPotion lead;
	public static HbmPotion radaway;
	public static HbmPotion phosphorus;
	public static HbmPotion stability;
	public static HbmPotion potionsickness;
	public static HbmPotion death;
	public static HbmPotion run;
	public static HbmPotion nitan;
	public static HbmPotion flashbang;
	public static HbmPotion slippery;

	public HbmPotion(boolean isBadEffect, int liquidColor) {
		super(isBadEffect, liquidColor);
	}

	/**
	 * Builds and queues a potion. It lives here because setIconIndex is protected —
	 * only a subclass can touch it, so external registration units have to come through this door.
	 */
	public static HbmPotion create(String registryName, boolean isBad, int color, String displayName, int iconX, int iconY) {
		HbmPotion potion = new HbmPotion(isBad, color);
		potion.setPotionName(displayName);
		potion.setIconIndex(iconX, iconY);
		return com.hbm.registry.HbmRegistry.potion(potion, registryName);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {

		if(this == taint) return duration % 2 == 0;
		if(this == radiation || this == radaway || this == phosphorus || this == nitan || this == slippery) return true;
		if(this == bang || this == run) return duration <= 10;
		if(this == lead) return duration % 60 == 0;
		return false;
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amplifier) {

		if(entity.world.isRemote) return;

		if(this == radiation) {
			// Goes through ContaminationUtil instead of adding raw numbers: that layer carries the hazmat and creative-mode checks
			ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, (amplifier + 1F) * 0.05F);
		}
		if(this == radaway) {
			HbmLivingProps.incrementRadiation(entity, -(amplifier + 1));
		}
		if(this == lead) {
			// Lead poisoning is absolute damage — hazmat doesn't stop it, you just keep bleeding
			entity.attackEntityFrom(ModDamageSource.lead, amplifier + 1);
		}
		if(this == slippery) {
			// It drifts upward even when standing still; that's this potion's take on being drunk
			entity.motionY += 0.026D;
		}
		if(this == phosphorus) {
			entity.setFire(1);
		}
		if(this == flashbang) {
			if(entity instanceof EntityZombie || entity instanceof EntitySkeleton) entity.setFire(20);
			entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 5, 10));
		}
		if(this == nitan) {
			// Counts drinks only; the 3rd one is an overdose. The counter lives on the player, non-players never hit this
			if(entity instanceof EntityPlayer) {
				HbmPlayerProps props = HbmPlayerProps.getData((EntityPlayer) entity);
				if(props != null && props.nitanCount == 3) entity.attackEntityFrom(ModDamageSource.nitan, 1000);
			}
		}
	}
}
