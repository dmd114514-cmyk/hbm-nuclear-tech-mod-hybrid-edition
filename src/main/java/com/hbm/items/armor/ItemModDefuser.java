package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The defuser: sweeps nearby creepers every second, stripping the whole "swelling" AI and dropping a fuse; that hit counts as the defuser's own
 *
 * The old version also wrote dataWatcher slot 18 once (1.7.10's creeper state index);
 * state is a private DataParameter here and setCreeperState syncs itself — writing it again would be reaching into someone else's state.
 */
public class ItemModDefuser extends ItemArmorMod {

	public ItemModDefuser() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.YELLOW + "Defuses nearby creepers");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.YELLOW + "  " + stack.getDisplayName() + " (Defuses creepers)");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		World world = entity.world;

		if(world.isRemote || world.getTotalWorldTime() % 20 != 0)
			return;

		List<EntityCreeper> creepers = world.getEntitiesWithinAABB(EntityCreeper.class, entity.getEntityBoundingBox().grow(5, 5, 5));

		for(EntityCreeper creeper : creepers)
			castrateCreeper(creeper, entity, true);
	}

	/*Stripping the swell AI completes the defusing; the fuse drop and that hit only happen when something was actually stripped, so */
	public static boolean castrateCreeper(EntityCreeper creeper, EntityLivingBase entity, boolean dropItem) {

		creeper.setCreeperState(-1);

		if(creeper.world.isRemote)
			return false;

		EntityAIBase toRem = null;

		for(EntityAITasks.EntityAITaskEntry entry : creeper.tasks.taskEntries) {

			if(entry.action instanceof EntityAICreeperSwell) {
				toRem = entry.action;
				break;
			}
		}

		if(toRem == null)
			return false;

		creeper.tasks.removeTask(toRem);

		if(dropItem) {
			creeper.world.playSound(null, creeper.posX, creeper.posY, creeper.posZ,
					ItemSounds.resolve("item.pinBreak"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			creeper.dropItem(ModItems.safety_fuse, 1);
			creeper.attackEntityFrom(entity != null ? DamageSource.causeMobDamage(entity) : DamageSource.MAGIC, 1.0F);
			creeper.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 0, 200));
		}

		creeper.getEntityData().setBoolean("hfr_defused", true);

		return true;
	}
}
