package com.hbm.items.special;

import java.util.List;

import com.hbm.util.ContaminationUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The digamma particle (particle_digamma): held in hand it keeps adding digamma dose.
 *
 * The constructor argument is "the particle's half-life (ticks)", so per-second dose takes the reciprocal —
 * the old comment said it plainly: a particle's digamma value is "time until half-life",
 * while ContaminationUtil reads it as "how much to add each time" — different dimensions; never conflate them.
 *
 * The old version also had "landing spawns a singularity (EntityQuasar) and vanishes",
 * and there's no EntityQuasar here (entity side unmigrated) ⇒ that block waits until the singularity lands.
 */
public class ItemDigamma extends Item {

	private final int digamma;

	public ItemDigamma(int digamma) {
		this.digamma = digamma;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

		super.onUpdate(stack, world, entity, slot, selected);

		if(entity instanceof EntityPlayer) {
			ContaminationUtil.applyDigammaData(entity, 1F / ((float) digamma));
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.GOLD + I18nUtil.resolveKey("trait.hlParticle", "1.67*10³⁴a"));
		list.add(TextFormatting.RED + I18nUtil.resolveKey("trait.hlPlayer", (digamma / 20F) + "s"));

		list.add("");
		super.addInformation(stack, world, list, flag);

		float d = ((int) ((1000F / digamma) * 200F)) / 10F;

		list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.digamma") + "]");
		list.add(TextFormatting.DARK_RED + "" + d + "mDRX/s");

		list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.drop") + "]");
	}
}
