package com.hbm.items.tool;

import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;

import api.hbm.block.IToolable.ToolType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The hurt-capable tool family: inherits the tools, plus a main-hand attack damage entry.
 *
 * Attack damage and that -10% move speed both ride Item's built-in tool UUID — removal matches UUIDs within the same attribute modifier,
 * swap in a different key and a modifier is left stuck on the player, unremovable.
 *
 * The wrench's "shove the player along your view" keeps the old behavior: it only fires for wrench,
 * and wrench here is an ItemWrench (a crafting component), not an instance of this family — so this branch is really a spot reserved
 * for everything outside wrench_archineer; written as an identity check like this, it needs no rework once that one joins the family too.
 */
public class ItemToolingWeapon extends ItemTooling {

	protected float damage = 0;

	public ItemToolingWeapon(ToolType type, int durability, float damage) {
		super(type, durability);
		this.damage = damage;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase attacker) {

		World world = entity.world;

		if(this == ModItems.wrench) {

			Vec3d vec = attacker.getLookVec();

			entity.motionX += vec.x * 0.5;
			entity.motionY += vec.y * 0.5;
			entity.motionZ += vec.z * 0.5;

			world.playSound(null, entity.posX, entity.posY, entity.posZ,
					ItemSounds.resolveVanilla("random.anvil_land"), SoundCategory.PLAYERS, 3.0F, 0.75F);
		}

		return false;
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {

		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);

		if(slot == EntityEquipmentSlot.MAINHAND) {

			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
					new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));

			if(this == ModItems.wrench)
				multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
						new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Wrench modifier", -0.1, 1));
		}

		return multimap;
	}
}
