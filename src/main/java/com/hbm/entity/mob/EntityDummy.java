package com.hbm.entity.mob;

import com.hbm.lib.RefStrings;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The training dummy: stands and takes hits, health written on its nameplate, right-click to dress it in armor.
 */
public class EntityDummy extends EntityLiving implements IAnimals {

	// Drops via table (§15): pack authors edit the one in assets/hbm/loot_tables/entities/ to change them
	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(RefStrings.MODID, "entities/entity_ntm_test_dummy");

	public EntityDummy(World world) {
		super(world);
	}

	// The old version did interact(player) + setCurrentItemOrArmor(4 - armorType); both changed name/type here:
	// EntityLiving made processInitialInteract final; the overridable layer is processInteract (which adds a hand),
	// armorType is already the slot enum (HEAD=4/CHEST=3/LEGS=2/FEET=1, same order as the old 4-armorType),
	// so no conversion is needed — hand the slot straight to setItemStackToSlot
	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {

		ItemStack held = player.getHeldItem(hand);

		if(!held.isEmpty() && held.getItem() instanceof ItemArmor) {
			ItemArmor armor = (ItemArmor) held.getItem();
			this.setItemStackToSlot(armor.armorType, held.copy());
		}

		return super.processInteract(player, hand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean getAlwaysRenderNameTagForRender() { return true; }

	// The old version overrode getCommandSenderName; merged into getName here
	@Override
	public String getName() {
		return (int) (this.getHealth() * 10) / 10F + " / " + (int) (this.getMaxHealth() * 10) / 10F;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) { }
}