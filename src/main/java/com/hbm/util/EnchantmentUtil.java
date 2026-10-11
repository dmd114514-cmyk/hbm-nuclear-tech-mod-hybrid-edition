package com.hbm.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Small tools for enchantments and XP.
 *
 * Two deltas from the old version; read before copying:
 * · `Enchantment.effectId` is a private field now; ids come from static {@link Enchantment#getEnchantmentID(Enchantment)};
 * · the NBT face is unchanged, still an `ench` list with `id`/`lvl` shorts (verified: this version's EnchantmentHelper uses exactly those strings).
 */
public class EnchantmentUtil {

	/**
	 * Adds an enchantment of the given level to the supplied itemstack
	 * @param stack
	 * @param enchantment
	 * @param level
	 */
	public static void addEnchantment(ItemStack stack, Enchantment enchantment, int level) {
		if(stack == null) return;
		stack.addEnchantment(enchantment, level);
	}

	/**
	 * Removes an enchantment from the given itemstack, regardless of level
	 * @param stack
	 * @param enchantment
	 */
	public static void removeEnchantment(ItemStack stack, Enchantment enchantment) {

		if(stack == null || stack.getEnchantmentTagList() == null) return;

		int id = Enchantment.getEnchantmentID(enchantment);

		int i = 0;
		for( ; i < stack.getEnchantmentTagList().tagCount(); i++) {
			if(stack.getEnchantmentTagList().getCompoundTagAt(i).getShort("id") == id)
				break;
		}

		if(i < stack.getEnchantmentTagList().tagCount())
			stack.getEnchantmentTagList().removeTag(i);

		if(stack.getEnchantmentTagList().tagCount() == 0)
			stack.getTagCompound().removeTag("ench");
	}

	public static int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
		if(stack.getEnchantmentTagList() == null) return 0;

		int id = Enchantment.getEnchantmentID(enchantment);

		for(int i = 0; i < stack.getEnchantmentTagList().tagCount(); i++) {
			if(stack.getEnchantmentTagList().getCompoundTagAt(i).getShort("id") == id)
				return stack.getEnchantmentTagList().getCompoundTagAt(i).getShort("lvl");
		}

		return 0;
	}

	/**
	 * Returns the size of the XP bar for the given level
	 * @param level
	 * @return
	 */
	public static int xpBarCap(int level) {
		return level >= 30 ? 62 + (level - 30) * 7 : (level >= 15 ? 17 + (level - 15) * 3 : 17);
	}

	/**
	 *
	 * @param targetXp
	 * @return
	 */
	public static int getLevelForExperience(int xp) {

		int level = 0;

		while (true) {

			int xpCap = xpBarCap(level);

			if (xp < xpCap)
				return level;

			xp -= xpCap;
			level++;
		}
	}

	/**
	 * Identical to EntityPlayer.addExperience but without increasing the player's score
	 * @param player
	 * @param xp
	 */
	public static void addExperience(EntityPlayer player, int xp, boolean silent) {

		int j = Integer.MAX_VALUE - player.experienceTotal;

		if(xp > j) {
			xp = j;
		}

		player.experience += (float) xp / (float) player.xpBarCap();

		for(player.experienceTotal += xp; player.experience >= 1.0F; player.experience /= (float) player.xpBarCap()) {
			player.experience = (player.experience - 1.0F) * (float) player.xpBarCap();

			if(silent)
				addExperienceLevelSilent(player, 1);
			else
				player.addExperienceLevel(1);
		}
	}

	public static void setExperience(EntityPlayer player, int xp) {

		player.experienceLevel = 0;
		player.experience = 0.0F;
		player.experienceTotal = 0;

		addExperience(player, xp, true);
	}

	public static void addExperienceLevelSilent(EntityPlayer player, int level) {
		player.experienceLevel += level;

		if(player.experienceLevel < 0) {
			player.experienceLevel = 0;
			player.experience = 0.0F;
			player.experienceTotal = 0;
		}
	}

	/** Fun fact: experienceTotal lies and has no actual purpose other than misleading people! */
	public static int getTotalExperience(EntityPlayer player) {
		int xp = 0;

		/* count only completed levels */
		for(int i = 0; i < player.experienceLevel; i++) {
			xp += xpBarCap(i);
		}

		xp += xpBarCap(player.experienceLevel) * player.experience;

		return xp;
	}
}
