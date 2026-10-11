package com.hbm.items.machine;

import java.util.List;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The infinite fluid source: can-style machines fill from it without consuming it.
 *
 * The old version also had a requiresTable branch — querying the host body's CBT_Water trait every tick and writing the result into
 * the "noAtmo"/"fluid" tags, with the tooltip then noting "this body has no matching fluid table". That half leans on
 * com.hbm.dim's CelestialBody/WorldProviderOrbit/CBT_Water, a face not migrated yet ⇒ this batch lands only
 * the bucket itself: requiresTable stays as a semantic bit with no writer ⇒ canOperate always true and the tooltip stays quiet;
 * once dim lands, copy the old onUpdate back verbatim — not one public door needs changing.
 */
public class ItemInfiniteFluid extends Item {

	private FluidType type;
	private int amount;
	private int chance;
	private boolean requiresTable;

	public ItemInfiniteFluid(FluidType type, int amount) {
		this(type, amount, 1, false);
	}

	public ItemInfiniteFluid(FluidType type, int amount, int chance) {
		this(type, amount, chance, false);
	}

	public ItemInfiniteFluid(FluidType type, int amount, boolean requiresTable) {
		this(type, amount, 1, requiresTable);
	}

	public ItemInfiniteFluid(FluidType type, int amount, int chance, boolean requiresTable) {
		this.type = type;
		this.amount = amount;
		this.chance = chance;
		this.requiresTable = requiresTable;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		if(canOperate(stack)) return;

		FluidType currentFluid = Fluids.fromID(stack.getTagCompound().getInteger("fluid"));

		if(currentFluid == Fluids.NONE) {
			list.add("Current environment has insufficient vapor pressure");
		} else {
			list.add("Current environment has an incompatible fluid table");
		}

		list.add("Requires: " + type.getLocalizedName() + " table");

		if(currentFluid == Fluids.NONE) {
			list.add("Releasing 0.2atm of vapor into the atmosphere will add a table on dry celestial bodies");
		} else {
			list.add("Current environment: " + currentFluid.getLocalizedName() + " table");
		}
	}

	public FluidType getType() { return this.type; }
	public int getAmount() { return this.amount; }
	public int getChance() { return this.chance; }
	public boolean isRequiresTable() { return this.requiresTable; }
	/*The infinite bucket ignores pressure; other sources only work at zero pressure */
	public boolean allowPressure(int pressure) { return this == ModItems.fluid_barrel_infinite || pressure == 0; }
	public boolean canOperate(ItemStack stack) { return stack.getTagCompound() == null || !stack.getTagCompound().getBoolean("noAtmo"); }
}
