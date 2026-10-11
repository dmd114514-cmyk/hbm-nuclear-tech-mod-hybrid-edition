package com.hbm.items.weapon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

/**
 * The butter sword: hit a squid and it explodes. Wood-tier material, so beyond this it's nothing special.
 */
public class BSword extends ItemSword {

	public BSword(ToolMaterial material) {
		super(material);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {

		if(target instanceof EntitySquid)
			target.world.createExplosion(attacker, target.posX, target.posY, target.posZ, 2.0F, true);

		return super.hitEntity(stack, target, attacker);
	}
}
