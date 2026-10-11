package com.hbm.items.special;

import java.util.function.BiConsumer;

import com.hbm.items.ItemCustomLore;
import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;
import com.hbm.sound.SoundRefs;
import com.hbm.util.Tuple.Pair;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

/**
 * A consumable that decides what it does when used: behavior arrives as two delegates (on use / on hit),
 * so subclasses don't each need a class — that's how the old version consolidated it; same here.
 *
 * Only the "blood bag / rad pill" slice has landed (three radaway tiers + iv_empty/iv_blood),
 * because the syringe batch (syringe_*) already has its own ItemSyringe in this version; no duplicates.
 */
public class ItemSimpleConsumable extends ItemCustomLore {

	private BiConsumer<ItemStack, EntityPlayer> useAction;
	private BiConsumer<ItemStack, EntityPlayer> useActionServer;
	private BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> hitAction;

	public ItemSimpleConsumable() {
		this.setCreativeTab(ModItems.consumableTab);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(this.useAction != null) this.useAction.accept(stack, player);

		if(!world.isRemote && this.useActionServer != null) this.useActionServer.accept(stack, player);

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {

		if(this.hitAction != null) this.hitAction.accept(stack, new Pair<>(target, attacker));

		return false;
	}

	/** Drinking one returns a container: count down one, make some noise, put the container back in the inventory (dropped at your feet if it won't fit) */
	public static void giveSoundAndDecrement(ItemStack stack, EntityLivingBase entity, String sound, ItemStack container) {

		stack.shrink(1);
		entity.world.playSound(null, entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ,
				SoundRefs.resolve(new ResourceLocation(sound)), SoundCategory.PLAYERS, 1.0F, 1.0F);
		tryAddItem(entity, container);
	}

	public static void addPotionEffect(EntityLivingBase entity, net.minecraft.potion.Potion effect, int duration, int level) {

		if(!entity.isPotionActive(effect)) {
			entity.addPotionEffect(new PotionEffect(effect, duration, level));
		} else {
			int d = entity.getActivePotionEffect(effect).getDuration() + duration;
			entity.addPotionEffect(new PotionEffect(effect, d, level));
		}
	}

	public static void tryAddItem(EntityLivingBase entity, ItemStack stack) {

		if(!(entity instanceof EntityPlayer)) return;

		EntityPlayer player = (EntityPlayer) entity;

		if(!player.inventory.addItemStackToInventory(stack)) {
			player.dropItem(stack, false);
		}
	}

	public static void doRadaway(ItemStack stack, EntityPlayer user, int duration) {
		giveSoundAndDecrement(stack, user, "hbm:item.radaway", new ItemStack(ModItems.iv_empty));
		addPotionEffect(user, HbmPotion.radaway, duration, 0);
	}

	public ItemSimpleConsumable setUseAction(BiConsumer<ItemStack, EntityPlayer> delegate) {
		this.useAction = delegate;
		return this;
	}

	public ItemSimpleConsumable setUseActionServer(BiConsumer<ItemStack, EntityPlayer> delegate) {
		this.useActionServer = delegate;
		return this;
	}

	public ItemSimpleConsumable setHitAction(BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> delegate) {
		this.hitAction = delegate;
		return this;
	}

}
