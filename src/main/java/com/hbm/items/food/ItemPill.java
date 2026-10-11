package com.hbm.items.food;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.config.VersatileConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * The pill family: zero food, edible anytime ({@code setAlwaysEdible}), with the real work in the eating settlement.
 *
 * The parent is this tree's {@link ItemFoodBase}, not vanilla {@code ItemFood}: so CT's food knobs
 * (`set`/`multiply` on hunger/saturation) reach these pills too — the table covers the whole family.
 *
 * Effects settle server-side only — this version's {@code ItemFood.onItemUseFinish} calls
 * {@code onFoodEaten} on both sides, and without this gate healing doubles up.
 */
public class ItemPill extends ItemFoodBase {

	public ItemPill(int hunger) {
		super(hunger, false);
		this.setAlwaysEdible();
	}

	private Random rand = new Random();

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) {

			VersatileConfig.applyPotionSickness(player, 5);

			if(this == ModItems.pill_iodine) {
				player.removePotionEffect(MobEffects.BLINDNESS);
				player.removePotionEffect(MobEffects.NAUSEA); // the old confusion is NAUSEA in this tree's mapping
				player.removePotionEffect(MobEffects.MINING_FATIGUE);
				player.removePotionEffect(MobEffects.HUNGER);
				player.removePotionEffect(MobEffects.SLOWNESS);
				player.removePotionEffect(MobEffects.POISON);
				player.removePotionEffect(MobEffects.WEAKNESS);
				player.removePotionEffect(MobEffects.WITHER);
				player.removePotionEffect(HbmPotion.radiation);
			}

			if(this == ModItems.plan_c) {
				for(int i = 0; i < 10; i++) {
					player.attackEntityFrom(rand.nextBoolean() ? ModDamageSource.euthanizedSelf : ModDamageSource.euthanizedSelf2, 1000);
				}
			}

			if(this == ModItems.pill_red) {
				player.addPotionEffect(new PotionEffect(HbmPotion.death, 60 * 60 * 20, 0));
			}

			if(this == ModItems.radx) {
				player.addPotionEffect(new PotionEffect(HbmPotion.radx, 3 * 60 * 20, 0));
			}

			if(this == ModItems.siox) {
				HbmLivingProps.setAsbestos(player, 0);
				HbmLivingProps.setBlackLung(player, Math.min(HbmLivingProps.getBlackLung(player), HbmLivingProps.MAX_BLACKLUNG / 5));
			}

			if(this == ModItems.pill_herbal) {
				HbmLivingProps.setAsbestos(player, 0);
				HbmLivingProps.setBlackLung(player, Math.min(HbmLivingProps.getBlackLung(player), HbmLivingProps.MAX_BLACKLUNG / 5));
				HbmLivingProps.incrementRadiation(player, -100F);

				player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 10 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 10 * 60 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 10 * 60 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.POISON, 5 * 20, 2));

				// the herb is poisonous in itself: this clears the potion-sickness antidote list, undispellable even by milk
				PotionEffect eff = new PotionEffect(HbmPotion.potionsickness, 10 * 60 * 20);
				eff.setCurativeItems(new ArrayList<ItemStack>());
				player.addPotionEffect(eff);
			}

			if(this == ModItems.xanax) {
				float digamma = HbmLivingProps.getDigamma(player);
				HbmLivingProps.setDigamma(player, Math.max(digamma - 0.5F, 0F));
			}

			if(this == ModItems.animan) {
				int x = MathHelper.floor(player.posX);
				int y = MathHelper.floor(player.posY);
				int z = MathHelper.floor(player.posZ);
				world.playSound(null, x, y, z, ItemSounds.resolve("player.oil"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				player.addPotionEffect(new PotionEffect(HbmPotion.run, 10 * 20, 0));

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "normal");
				nbt.setInteger("count", 25);
				nbt.setInteger("entity", player.getEntityId());
				AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0), new TargetPoint(player.dimension, x, y, z, 25));

				world.playSound(null, x, y, z, ItemSounds.resolve("player.vomit"), SoundCategory.PLAYERS, 1.0F, 1.0F);
				player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 60, 19));
			}

			if(this == ModItems.chocolate) {
				if(rand.nextInt(25) == 0) {
					player.attackEntityFrom(ModDamageSource.overdose, 1000);
				}
				// the old digSpeed/moveSpeed/jump are HASTE/SPEED/JUMP_BOOST in this tree's mapping
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 60 * 20, 3));
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 3));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 60 * 20, 3));
			}

			if(this == ModItems.fmn) {
				float digamma = HbmLivingProps.getDigamma(player);
				HbmLivingProps.setDigamma(player, Math.min(digamma, 2F));
				player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0));
			}

			if(this == ModItems.five_htp) {
				HbmLivingProps.setDigamma(player, 0);
				player.addPotionEffect(new PotionEffect(HbmPotion.stability, 10 * 60 * 20, 0));
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		// Descriptions live in the lang as "<registry name>.desc", split on '$'; no key, no line
		String unloc = this.getUnlocalizedName() + ".desc"; //this should really be an interface...
		String loc = I18nUtil.resolveKey(unloc);

		if(!unloc.equals(loc)) {
			String[] locs = loc.split("\\$");

			for(String s : locs) {
				list.add(s);
			}
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 10;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(!VersatileConfig.hasPotionSickness(player)) {
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}
}
