package com.hbm.items.special;

import java.util.List;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.config.WeaponConfig;
import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityRagingVortex;
import com.hbm.entity.effect.EntityVortex;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The "explode on landing" family: antimatter cores, singularities, black holes, dead man's switch detonators — the item entity settles its own account
 * the moment it touches ground (detonation, vortex, or simply deleting itself), so the checks live in {@link #onEntityItemUpdate},
 * not in the item's own logic.
 *
 * Two old branches didn't come over; add them when their classes land (both only affect "what happens on the ground", not the item's usability):
 *   · cell_anti_schrabidium's fólkvangr field: {@code EntityNukeExplosionMK3} not migrated;
 *   · crystal_xen's floating explosion: {@code ExplosionChaos} not migrated.
 * Also: beta rides this class too, and it's still unregistered, so its "vanish on touch" branch is permanently false for now.
 */
public class ItemDrop extends Item {

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {

		ItemStack stack = entityItem.getItem();

		if(this == ModItems.beta) {
			entityItem.setDead();
			return true;
		}

		if(stack.getItem() == ModItems.detonator_deadman) {
			if(!entityItem.world.isRemote) {

				if(stack.getTagCompound() != null) {

					BlockPos pos = new BlockPos(stack.getTagCompound().getInteger("x"), stack.getTagCompound().getInteger("y"), stack.getTagCompound().getInteger("z"));

					if(entityItem.world.getBlockState(pos).getBlock() instanceof IBomb) {
						((IBomb)entityItem.world.getBlockState(pos).getBlock()).explode(entityItem.world, pos);

						if(GeneralConfig.enableExtendedLogging)
							MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + pos + " by dead man's switch!");
					}
				}

				entityItem.world.createExplosion(entityItem, entityItem.posX, entityItem.posY, entityItem.posZ, 0.0F, true);
				entityItem.setDead();
			}
		}

		if(stack.getItem() == ModItems.detonator_de) {
			if(!entityItem.world.isRemote && WeaponConfig.dropDead) {
				entityItem.world.createExplosion(entityItem, entityItem.posX, entityItem.posY, entityItem.posZ, 15.0F, true);

				if(GeneralConfig.enableExtendedLogging)
					MainRegistry.logger.log(Level.INFO, "[DET] Detonated dead man's explosive at " + ((int)entityItem.posX) + " / " + ((int)entityItem.posY) + " / " + ((int)entityItem.posZ) + "!");
			}

			entityItem.setDead();
		}

		if(entityItem.onGround) {

			if(stack.getItem() == ModItems.cell_antimatter && WeaponConfig.dropCell) {
				if(!entityItem.world.isRemote)
					new ExplosionVNT(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, 5F).makeAmat().explode();
			}
			if(stack.getItem() == ModItems.particle_amat && WeaponConfig.dropCell) {
				if(!entityItem.world.isRemote)
					new ExplosionVNT(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, 5F).makeAmat().explode();
			}
			if(stack.getItem() == ModItems.pellet_antimatter && WeaponConfig.dropCell) {
				if(!entityItem.world.isRemote)
					new ExplosionVNT(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, 20F).makeAmat().explode();
			}
			// cell_anti_schrabidium: wire once EntityNukeExplosionMK3 (with the EntityCloudFleija family) migrates
			if(stack.getItem() == ModItems.singularity && WeaponConfig.dropSing) {
				if(!entityItem.world.isRemote)
					spawnAt(entityItem, new EntityVortex(entityItem.world, 1.5F));
			}
			if(stack.getItem() == ModItems.singularity_counter_resonant && WeaponConfig.dropSing) {
				if(!entityItem.world.isRemote)
					spawnAt(entityItem, new EntityVortex(entityItem.world, 2.5F));
			}
			if(stack.getItem() == ModItems.singularity_super_heated && WeaponConfig.dropSing) {
				if(!entityItem.world.isRemote)
					spawnAt(entityItem, new EntityVortex(entityItem.world, 2.5F));
			}
			if(stack.getItem() == ModItems.black_hole && WeaponConfig.dropSing) {
				if(!entityItem.world.isRemote)
					spawnAt(entityItem, new EntityBlackHole(entityItem.world, 1.5F));
			}
			if(stack.getItem() == ModItems.singularity_spark && WeaponConfig.dropSing) {
				if(!entityItem.world.isRemote)
					spawnAt(entityItem, new EntityRagingVortex(entityItem.world, 3.5F));
			}
			// crystal_xen: wire once ExplosionChaos.floater/move migrates

			entityItem.setDead();
			return true;
		}

		return false;
	}

	/** Spawns the new entity at the landed item's position */
	private void spawnAt(EntityItem entityItem, net.minecraft.entity.Entity entity) {
		entity.posX = entityItem.posX;
		entity.posY = entityItem.posY;
		entity.posZ = entityItem.posZ;
		entityItem.world.spawnEntity(entity);
	}

	@Override
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {

		if(this == ModItems.cell_antimatter) {
			list.add("Warning: Exposure to matter will");
			list.add("lead to violent annihilation!");
		}
		if(this == ModItems.pellet_antimatter) {
			list.add("Very heavy antimatter cluster.");
			list.add("Gets rid of black holes.");
		}
		if(this == ModItems.cell_anti_schrabidium) {
			list.add("Warning: Exposure to matter will");
			list.add("create a fólkvangr field!");
		}
		if(this == ModItems.singularity) {
			list.add("You may be asking:");
			list.add("\"But HBM, a manifold with an undefined");
			list.add("state of spacetime? How is this possible?\"");
			list.add("Long answer short:");
			list.add("\"I have no idea!\"");
		}
		if(this == ModItems.singularity_counter_resonant) {
			list.add("Nullifies resonance of objects in");
			list.add("non-euclidean space, creates variable");
			list.add("gravity well. Spontaneously spawns");
			list.add("tesseracts. If a tesseract happens to");
			list.add("appear near you, do not look directly");
			list.add("at it.");
		}
		if(this == ModItems.singularity_super_heated) {
			list.add("Continuously heats up matter by");
			list.add("resonating every planck second.");
			list.add("Tends to catch fire or to create");
			list.add("small plasma arcs. Not edible.");
		}
		if(this == ModItems.black_hole) {
			list.add("Contains a regular singularity");
			list.add("in the center. Large enough to");
			list.add("stay stable. It's not the end");
			list.add("of the world as we know it,");
			list.add("and I don't feel fine.");
		}
		if(this == ModItems.detonator_deadman) {
			list.add("Shift right-click to set position,");
			list.add("drop to detonate!");
			if(itemstack.getTagCompound() == null) {
				list.add("No position set!");
			} else {
				list.add("Set pos to " + itemstack.getTagCompound().getInteger("x") + ", " + itemstack.getTagCompound().getInteger("y") + ", " + itemstack.getTagCompound().getInteger("z"));
			}
		}
		if(this == ModItems.detonator_de) {
			list.add("Explodes when dropped!");
		}

		list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.drop") + "]");
	}

	/** Only the dead man's switch eats this door: sneak-right-click records coordinates into NBT; everything else stays vanilla */
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(this != ModItems.detonator_deadman)
			return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);

		ItemStack stack = player.getHeldItem(hand);

		if(stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());

		if(player.isSneaking()) {

			stack.getTagCompound().setInteger("x", pos.getX());
			stack.getTagCompound().setInteger("y", pos.getY());
			stack.getTagCompound().setInteger("z", pos.getZ());

			if(world.isRemote)
				player.sendMessage(new TextComponentString("Position set!"));

			world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.TECH_BOOP), SoundCategory.PLAYERS, 2.0F, 1.0F);

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
