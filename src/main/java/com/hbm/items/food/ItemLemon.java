package com.hbm.items.food;

import java.util.List;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * The bulk food family (lemon, cereal, meat pie, those three meds...): values ride constructor args, special pieces add state effects on top.
 *
 * The parent is this tree's {@link ItemFoodBase}, so CT's food-domain knobs reach the whole family's hunger/saturation.
 *
 * Descriptions still hard-branch per piece as the old version did — the old code even kept its own "this should really be an interface" note,
 * but interfacing would drag the whole family's registry names; copied verbatim this round, to be collected when A4 unifies GUI/item descriptions.
 */
public class ItemLemon extends ItemFoodBase {

	public ItemLemon(int hunger, float saturation, boolean isDogFood) {
		super(hunger, saturation, isDogFood);

		// The old line compared {@code ModItems.med_*} at construction, when the fields were still null, so it never worked
		if(this == ModItems.med_ipecac || this == ModItems.med_ptsd) {
			this.setAlwaysEdible();
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		if(this == ModItems.lemon) {
			list.add(I18nUtil.resolveKey("item.lemon.desc"));
		}

		if(this == ModItems.med_ipecac) {
			// the trailing letter of the key is Cyrillic с, not Latin c — the lang file stores that spelling too; changing it breaks the lookup
			String[] lines = I18nUtil.resolveKeyArray("item.med_ipecac.desс");
			for(String line : lines) {
				list.add(line);
			}
		}

		if(this == ModItems.med_ptsd) {
			String[] lines = I18nUtil.resolveKeyArray("item.med_ptsd.desc");
			for(String line : lines) {
				list.add(line);
			}
		}

		if(this == ModItems.med_schizophrenia) {
			String[] lines = I18nUtil.resolveKeyArray("item.med_schizophrenia.desc");
			for(String line : lines) {
				list.add(line);
			}
		}

		if(this == ModItems.loops) {
			list.add(I18nUtil.resolveKey("item.loops.desc"));
		}

		if(this == ModItems.loop_stew) {
			list.add(I18nUtil.resolveKey("item.loop_stew.desc"));
		}

		if(this == ModItems.twinkie) {
			list.add(I18nUtil.resolveKey("item.twinkie.desc"));
		}

		if(this == ModItems.pudding) {
			String[] lines = I18nUtil.resolveKeyArray("item.pudding.desc");
			for(String line : lines) {
				list.add(line);
			}
		}

		if(this == ModItems.ingot_semtex) {
			String[] lines = I18nUtil.resolveKeyArray("item.ingot_semtex.desc");
			for(String line : lines) {
				list.add(line);
			}
		}

		// peas is another class (ItemPeas), so this branch never hits within this family; the old version wrote it that way — kept as a placeholder
		if(this == ModItems.peas) {
			list.add(I18nUtil.resolveKey("item.peas.desc"));
		}

		if(this == ModItems.quesadilla) {
			list.add(I18nUtil.resolveKey("item.cheese_quesadilla.desc"));
		}

		if(this == ModItems.flesh_burger) {
			list.add(I18nUtil.resolveKey("item.flesh_burger.desc"));
		}
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if(this == ModItems.med_ipecac || this == ModItems.med_ptsd) {
			// hunger level 49 = one bite zeroes saturation
			player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 50, 49));

			HbmPlayerProps props = HbmPlayerProps.getData(player);
			if(props.nitanCount > 0) {
				player.removePotionEffect(HbmPotion.nitan);
				props.nitanCount = 0;
			}

			// packets and sounds run server-side only: the old homegrown packet stack couldn't send from the client (effectively silent),
			// and this tree uses a real SimpleNetworkWrapper, where a client sending a to-client packet points the wrong way.
			// the effects above still run both sides, outside this gate.
			if(!world.isRemote) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setInteger("entity", player.getEntityId());
				AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0), new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 25));

				// The old version played hbm:entity.vomit, but that string never had an entry in sounds.json (nor an ogg),
				// i.e. it never sounded. Re-pointed at the existing player.vomit; the vomiting sound itself matches the animan one.
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("player.vomit"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}

		if(this == ModItems.med_schizophrenia) {
		}

		if(this == ModItems.loop_stew) {
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20 * 20, 1));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 20, 2));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 1));
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 20 * 20, 2));
		}

		if(this == ModItems.s_cream) {
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20 * 20, 2));
		}
	}

	/**
	 * The bowl versions leave an empty bowl. The old override was {@code onEaten}; this version's slot became
	 * {@code onItemUseFinish} — let the parent settle food and effects first, then swap the returned stack for the bowl.
	 */
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {

		ItemStack result = super.onItemUseFinish(stack, world, entity);

		if(this == ModItems.loop_stew) {
			return new ItemStack(Items.BOWL);
		}

		return result;
	}
}
