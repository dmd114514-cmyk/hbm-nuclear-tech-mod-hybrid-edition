package com.hbm.items.special;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.config.VersatileConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * One syringe rules them all: injectors, first aid kits, rad purges, filters, jetpack cans, lie detectors — one class,
 * one item instance per good, branching via `this == ModItems.xxx`.
 *
 * So this family's registration must land as a batch: miss one and it's a mute item with no behavior; this class's branches merely
 * go forever unwalked. The reverse holds too — registering an instance with no written branch makes right-click dead.
 */
@Spaghetti("fuck")
public class ItemSyringe extends ItemBase {

	Random rand = new Random();

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(this == ModItems.syringe_antidote && !VersatileConfig.hasPotionSickness(player)) {
			if(!world.isRemote) {
				player.clearActivePotions();

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_empty))) {
					player.dropItem(new ItemStack(ModItems.syringe_empty, 1, 0), false);
				}

				VersatileConfig.applyPotionSickness(player, 5);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.syringe_awesome && !VersatileConfig.hasPotionSickness(player)) {
			if(!world.isRemote) {
				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_empty))) {
					player.dropItem(new ItemStack(ModItems.syringe_empty, 1, 0), false);
				}

				// The old one was "every good effect maxed for 50 seconds"; tiers copied verbatim
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 50 * 20, 9));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 50 * 20, 9));
				player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 50 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 50 * 20, 24));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 50 * 20, 9));
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 50 * 20, 6));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 50 * 20, 9));
				player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 50 * 20, 9));
				player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 50 * 20, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 4));
				player.addPotionEffect(new PotionEffect(HbmPotion.radx, 50 * 20, 9));

				VersatileConfig.applyPotionSickness(player, 5);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.syringe_poison) {
			if(!world.isRemote) {
				// The old version: self-injection picked between two dedicated damage sources, injecting others counted on their tab
				player.attackEntityFrom(player.getRNG().nextBoolean() ? ModDamageSource.euthanizedSelf : ModDamageSource.euthanizedSelf2, 30);

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_empty))) {
					player.dropItem(new ItemStack(ModItems.syringe_empty, 1, 0), false);
				}
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.syringe_metal_stimpak && !VersatileConfig.hasPotionSickness(player)) {
			if(!world.isRemote) {
				player.heal(5);

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(stack.getCount() <= 0) {
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(ModItems.syringe_metal_empty));
				}

				if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
					player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
				}

				VersatileConfig.applyPotionSickness(player, 5);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.syringe_metal_medx && !VersatileConfig.hasPotionSickness(player)) {
			if(!world.isRemote) {
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 4 * 60 * 20, 2));

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(stack.getCount() <= 0) {
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(ModItems.syringe_metal_empty));
				}

				if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
					player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
				}

				VersatileConfig.applyPotionSickness(player, 5);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.syringe_metal_psycho && !VersatileConfig.hasPotionSickness(player)) {
			if(!world.isRemote) {
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 2 * 60 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 2 * 60 * 20, 0));

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(stack.getCount() <= 0) {
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(ModItems.syringe_metal_empty));
				}

				if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
					player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
				}

				VersatileConfig.applyPotionSickness(player, 5);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.syringe_metal_super && !VersatileConfig.hasPotionSickness(player)) {
			if(!world.isRemote) {
				player.heal(25);
				player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 0));

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(stack.getCount() <= 0) {
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(ModItems.syringe_metal_empty));
				}

				if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
					player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
				}

				VersatileConfig.applyPotionSickness(player, 15);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.med_bag && !VersatileConfig.hasPotionSickness(player)) {
			if(!world.isRemote) {
				player.setHealth(player.getMaxHealth());

				player.removePotionEffect(MobEffects.BLINDNESS);
				player.removePotionEffect(MobEffects.NAUSEA);
				player.removePotionEffect(MobEffects.MINING_FATIGUE);
				player.removePotionEffect(MobEffects.HUNGER);
				player.removePotionEffect(MobEffects.SLOWNESS);
				player.removePotionEffect(MobEffects.POISON);
				player.removePotionEffect(MobEffects.WEAKNESS);
				player.removePotionEffect(MobEffects.WITHER);
				player.removePotionEffect(HbmPotion.radiation);

				VersatileConfig.applyPotionSickness(player, 15);

				stack.shrink(1);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.radaway) {
			if(!world.isRemote) {
				player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 14, 9));

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.radaway"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.radaway_strong) {
			if(!world.isRemote) {
				int duration = 35;
				int level = 9;

				if(!player.isPotionActive(HbmPotion.radaway)) {
					player.addPotionEffect(new PotionEffect(HbmPotion.radaway, duration, level));
				} else {

					int d = player.getActivePotionEffect(HbmPotion.radaway).getDuration() + duration;
					player.addPotionEffect(new PotionEffect(HbmPotion.radaway, d, level));
				}

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.radaway"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.radaway_flush) {
			if(!world.isRemote) {
				player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 50, 19));

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.radaway"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.syringe_taint) {
			if(!world.isRemote) {
				player.addPotionEffect(new PotionEffect(HbmPotion.taint, 60 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}

			if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
				player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
			}

			if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.bottle2_empty))) {
				player.dropItem(new ItemStack(ModItems.bottle2_empty, 1, 0), false);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		/* The three attachment-swapping syringes only handle the "already worn" case, so all checks read from the armor slots */
		if(this == ModItems.gas_mask_filter_mono && player.inventory.armorInventory.get(3) != null && player.inventory.armorInventory.get(3).getItem() == ModItems.gas_mask_mono) {
			if(!world.isRemote) {
				if(player.inventory.armorInventory.get(3).getItemDamage() == 0)
					return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

				player.inventory.armorInventory.get(3).setItemDamage(0);

				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.FILTER_SCREW), SoundCategory.PLAYERS, 1.0F, 1.0F);
				stack.shrink(1);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.jetpack_tank && player.inventory.armorInventory.get(2) != null) {

			if(!world.isRemote) {

				ItemStack jetpack = player.inventory.armorInventory.get(2);

				if(jetpack == null)
					return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

				if(jetpack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(jetpack)) {
					jetpack = ArmorModHandler.pryMods(jetpack)[ArmorModHandler.plate_only];
				}

				if(jetpack == null || !(jetpack.getItem() instanceof IFillableItem))
					return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

				IFillableItem fillable = (IFillableItem) jetpack.getItem();

				if(!fillable.acceptsFluid(Fluids.KEROSENE, jetpack))
					return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

				if(fillable.tryFill(Fluids.KEROSENE, 1000, jetpack) < 1000) {
					world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.jetpacktank"), SoundCategory.PLAYERS, 1.0F, 1.0F);
					stack.shrink(1);
				}

				if(jetpack.getItem() != player.inventory.armorInventory.get(2).getItem())
					ArmorModHandler.applyMod(player.inventory.armorInventory.get(2), jetpack);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.lox_tank && player.inventory.armorInventory.get(3) != null) {
			if(!world.isRemote) {
				ItemStack helmet = player.inventory.armorInventory.get(3);

				if(ArmorModHandler.hasMods(helmet)) {
					ItemStack tankStack = ArmorModHandler.pryMods(helmet)[ArmorModHandler.plate_only];
					if(tankStack == null || !(tankStack.getItem() instanceof IFillableItem))
						return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

					IFillableItem fillable = (IFillableItem) tankStack.getItem();

					if(!fillable.acceptsFluid(Fluids.OXYGEN, tankStack))
						return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

					if(fillable.tryFill(Fluids.OXYGEN, 1000, tankStack) < 1000) {
						world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.jetpacktank"), SoundCategory.PLAYERS, 1.0F, 1.0F);
						stack.shrink(1);
					}

					ArmorModHandler.applyMod(player.inventory.armorInventory.get(3), tankStack);
				}
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		if(this == ModItems.cbt_device) {
			if(!world.isRemote) {
				player.addPotionEffect(new PotionEffect(HbmPotion.bang, 30, 0));

				stack.shrink(1);
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("item.vice"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase entityPlayer) {
		World world = entity.world;

		if(this == ModItems.syringe_metal_stimpak && !VersatileConfig.hasPotionSickness(entity)) {
			if(!world.isRemote) {
				entity.heal(5);
				VersatileConfig.applyPotionSickness(entity, 5);

				stack.shrink(1);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(entityPlayer instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entityPlayer;
					if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
						player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
					}
				}
			}
		}

		if(this == ModItems.syringe_metal_medx && !VersatileConfig.hasPotionSickness(entity)) {
			if(!world.isRemote) {
				entity.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 4 * 60 * 20, 2));
				VersatileConfig.applyPotionSickness(entity, 5);

				stack.shrink(1);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(entityPlayer instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entityPlayer;
					if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
						player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
					}
				}
			}
		}

		if(this == ModItems.syringe_metal_psycho && !VersatileConfig.hasPotionSickness(entity)) {
			if(!world.isRemote) {
				entity.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 2 * 60 * 20, 0));
				entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 2 * 60 * 20, 0));
				VersatileConfig.applyPotionSickness(entity, 5);

				stack.shrink(1);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(entityPlayer instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entityPlayer;
					if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
						player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
					}
				}
			}
		}

		if(this == ModItems.syringe_metal_super && !VersatileConfig.hasPotionSickness(entity)) {
			if(!world.isRemote) {
				entity.heal(25);
				entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 0));
				VersatileConfig.applyPotionSickness(entity, 15);

				stack.shrink(1);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(entityPlayer instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entityPlayer;
					if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
						player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
					}
				}
			}
		}

		if(this == ModItems.syringe_taint) {
			if(!world.isRemote) {
				entity.addPotionEffect(new PotionEffect(HbmPotion.taint, 60 * 20, 0));
				entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));

				stack.shrink(1);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(entityPlayer instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entityPlayer;
					if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.syringe_metal_empty))) {
						player.dropItem(new ItemStack(ModItems.syringe_metal_empty, 1, 0), false);
					}
					if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.bottle2_empty))) {
						player.dropItem(new ItemStack(ModItems.bottle2_empty, 1, 0), false);
					}
				}
			}
		}

		if(this == ModItems.syringe_mkunicorn) {
			if(!world.isRemote) {
				HbmLivingProps.setContagion(entity, 3 * 60 * 60 * 20);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);
				stack.shrink(1);

				if(GeneralConfig.enableExtendedLogging)
					MainRegistry.logger.log(Level.INFO, "[MKU] " + entityPlayer.getName() + " used an MKU syringe!");
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack itemstack, World world, List<String> list, net.minecraft.client.util.ITooltipFlag flag) {
		if(this == ModItems.syringe_antidote) {
			list.add("Removes all potion effects");
		}
		if(this == ModItems.syringe_awesome) {
			list.add("Every good effect for 50 seconds");
		}
		if(this == ModItems.syringe_metal_medx) {
			list.add("Resistance III for 4 minutes");
		}
		if(this == ModItems.syringe_metal_psycho) {
			list.add("Resistance I for 2 minutes");
			list.add("Strength I for 2 minutes");
		}
		if(this == ModItems.syringe_metal_stimpak) {
			list.add("Heals 2.5 hearts");
		}
		if(this == ModItems.syringe_metal_super) {
			list.add("Heals 25 hearts");
			list.add("Slowness I for 10 seconds");
		}
		if(this == ModItems.syringe_poison) {
			list.add("Deadly");
		}
		if(this == ModItems.med_bag) {
			list.add("Full heal, regardless of max health");
			list.add("Removes negative effects");
		}
		if(this == ModItems.radaway) {
			list.add("Removes 140 RAD");
		}
		if(this == ModItems.radaway_strong) {
			list.add("Removes 350 RAD");
		}
		if(this == ModItems.radaway_flush) {
			list.add("Removes 1000 RAD");
		}
		if(this == ModItems.syringe_taint) {
			list.add("Tainted I for 60 seconds");
			list.add("Nausea I for 5 seconds");
			list.add("Cloud damage + taint = tainted heart effect");
		}
		if(this == ModItems.gas_mask_filter) {
			list.add("Repairs worn gas mask");
		}
		if(this == ModItems.gas_mask_filter_mono) {
			list.add("Repairs worn monoxide mask");
		}
		if(this == ModItems.jetpack_tank) {
			list.add("Fills worn jetpack with up to 1000mB of kerosene");
		}
		if(this == ModItems.lox_tank) {
			list.add("Fills a worn PLSS with 1000mB of oxygen");
		}
		if(this == ModItems.gun_kit_1) {
			list.add("Repairs all weapons in hotbar by 10%");
		}
		if(this == ModItems.gun_kit_2) {
			list.add("Repairs all weapons in hotbar by 50%");
		}

		if(this == ModItems.syringe_mkunicorn) {
			list.add(TextFormatting.RED + "?");
		}
	}
}
