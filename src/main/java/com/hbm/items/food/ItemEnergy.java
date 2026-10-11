package com.hbm.items.food;

import java.util.List;

import com.hbm.config.VersatileConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.interfaces.Spaghetti;
import com.hbm.items.ModItems;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Effects hard-branch by "which item this is" (the old version wrote it that way, its own author flagging it Spaghetti),
 * so each drink must be its own ItemEnergy instance — no sharing one.
 *
 * Three old branches not ported, all affecting only "one extra thing on drinking", not healing or potions:
 *   · chocolate_milk's nitroglycerin blast: {@code ExplosionLarge} unmigrated;
 *   · coffee_radium's achievement: achievements are a separate line here, with no {@code MainRegistry.achRadium};
 *   · bottle_sparkle / bottle_rad's polaroid-switched tooltip lines: no {@code MainRegistry.polaroidID} here,
 *     so the non-polaroid line always wins.
 * Also: bottles want an opener, and {@code bottle_opener} rides WeaponSpecial, unregistered,
 * so the opener gate currently always reads "no tool" — it clears once the weapon side lands.
 */
@Spaghetti("wtf is this shit")
public class ItemEnergy extends Item {

	private Item container = null;
	private Item cap = null;
	private boolean requiresOpener = false;

	public ItemEnergy() {
		this.setCreativeTab(ModItems.consumableTab);
	}

	public ItemEnergy makeCan() {
		this.container = ModItems.can_empty;
		this.cap = ModItems.ring_pull;
		this.requiresOpener = false;
		this.setContainerItem(this.container);
		return this;
	}

	public ItemEnergy makeBottle(Item bottle, Item cap) {
		this.container = bottle;
		this.cap = cap;
		this.requiresOpener = true;
		this.setContainerItem(this.container);
		this.setCreativeTab(ModItems.consumableTab);
		return this;
	}

	public ItemEnergy makeGlass() {
		this.requiresOpener = false;
		return this;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase living) {

		if(!(living instanceof EntityPlayer)) return stack;

		EntityPlayer player = (EntityPlayer) living;

		if(!player.capabilities.isCreativeMode) stack.shrink(1);

		if(!world.isRemote) {

			if(player instanceof FakePlayer) {
				world.newExplosion(player, player.posX, player.posY, player.posZ, 5F, true, true);
				return stack;
			}

			VersatileConfig.applyPotionSickness(player, 5);

			if(this == ModItems.can_smart) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 0));
			}
			if(this == ModItems.can_creature) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 30 * 20, 1));
			}
			if(this == ModItems.can_redbomb) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30 * 20, 1));
			}
			if(this == ModItems.can_mrsugar) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30 * 20, 2));
			}
			if(this == ModItems.can_overcharge) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 0));
			}
			if(this == ModItems.can_luna) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 30 * 20, 2));
			}
			if(this == ModItems.can_bepis) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 3));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 3));
			}
			if(this == ModItems.can_breen) {
				player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 30 * 20, 0));
			}
			if(this == ModItems.can_mug) {
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 3 * 60 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60 * 20, 2));
			}
			// chocolate_milk: wire once ExplosionLarge.explode migrates
			if(this == ModItems.bottle_nuka) {
				player.heal(4F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 30 * 20, 1));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 5.0F);
			}
			if(this == ModItems.bottle_cherry) {
				player.heal(6F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30 * 20, 2));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 5.0F);
			}
			if(this == ModItems.glass_smilk) {
				player.heal(6F);
				this.setContainerItem(ModItems.glass_empty);
				this.container = ModItems.glass_empty;
			}
			if(this == ModItems.teacup) {
				player.heal(3F);
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 4));

				this.setContainerItem(ModItems.teacup_empty);
				this.container = ModItems.teacup_empty;
			}
			if(this == ModItems.bottle_honey) {
				player.heal(9F);
				float digamma = HbmLivingProps.getDigamma(player);
				HbmLivingProps.setDigamma(player, Math.max(digamma - 0.3F, 0F));

				this.setContainerItem(Items.GLASS_BOTTLE);
				this.container = Items.GLASS_BOTTLE;
			}
			if(this == ModItems.bottle_quantum) {
				player.heal(10F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 1));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 15.0F);
			}
			if(this == ModItems.bottle2_korl) {
				player.heal(6);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 2));
			}
			if(this == ModItems.bottle2_fritz) {
				player.heal(6);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30 * 20, 2));
			}
			if(this == ModItems.bottle_sparkle) {
				player.heal(10F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 120 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 120 * 20, 1));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 5.0F);
			}
			if(this == ModItems.bottle_rad) {
				player.heal(10F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 120 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 120 * 20, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 120 * 20, 1));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 15.0F);
			}
			if(this == ModItems.coffee) {
				player.heal(10);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 2));
				this.setContainerItem(ModItems.cmug_empty);
				this.container = ModItems.cmug_empty;
			}
			if(this == ModItems.coffee_radium) {
				player.heal(10);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 2));
				HbmLivingProps.incrementRadiation(player, 500F);
			}

			if(!player.capabilities.isCreativeMode) {

				if(this.cap != null)
					player.inventory.addItemStackToInventory(new ItemStack(this.cap));

				if(this.container != null) {
					if(stack.getCount() <= 0)
						return new ItemStack(this.container);
					player.inventory.addItemStackToInventory(new ItemStack(this.container));
				}
			}

			player.inventoryContainer.detectAndSendChanges();
		}

		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Spaghetti("cover yourself in oil")
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(VersatileConfig.hasPotionSickness(player)) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		if(this.requiresOpener && !hasOpener(player)) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

		player.setActiveHand(hand);

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	/*The old InventoryPlayer.hasItem(Item): only the ItemStack-comparing door remains here, so compare by item itself */
	private boolean hasOpener(EntityPlayer player) {

		for(ItemStack inventoryStack : player.inventory.mainInventory) {
			if(!inventoryStack.isEmpty() && inventoryStack.getItem() == ModItems.bottle_opener) return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(this == ModItems.can_smart) {
			list.add("Cheap and full of bubbles");
		}
		if(this == ModItems.can_creature) {
			list.add("Basically gasoline in a tin can");
		}
		if(this == ModItems.can_redbomb) {
			list.add("Liquefied explosives");
		}
		if(this == ModItems.can_mrsugar) {
			list.add("An intellectual drink, for the chosen ones!");
		}
		if(this == ModItems.can_overcharge) {
			list.add("Possible side effects include heart attacks, seizures or zombification");
		}
		if(this == ModItems.can_luna) {
			list.add("Contains actual selenium and star metal. Tastes like night.");
		}
		if(this == ModItems.can_bepis) {
			list.add("beppp");
		}
		if(this == ModItems.can_breen) {
			list.add("Don't drink the water. They put something in it, to make you forget.");
			list.add("I don't even know how I got here.");
		}
		if(this == ModItems.chocolate_milk) {
			list.add("Regular chocolate milk. Safe to drink.");
			list.add("Totally not made from nitroglycerine.");
		}
		if(this == ModItems.bottle_nuka) {
			list.add("Contains about 210 kcal and 1500 mSv.");
		}
		if(this == ModItems.bottle_cherry) {
			list.add("Now with severe radiation poisoning in every seventh bottle!");
		}
		if(this == ModItems.bottle_quantum) {
			list.add("Comes with a colorful mix of over 70 isotopes!");
		}
		if(this == ModItems.bottle2_korl) {
			list.add("Contains actual orange juice!");
		}
		if(this == ModItems.bottle2_fritz) {
			list.add("moremore caffeine");
		}
		if(this == ModItems.bottle_sparkle) {
			list.add("The most delicious beverage in the wasteland!");
		}
		if(this == ModItems.bottle_rad) {
			list.add("Tastes like radish and radiation.");
		}

		if(this.requiresOpener) list.add("[Requires bottle opener]");
	}
}
