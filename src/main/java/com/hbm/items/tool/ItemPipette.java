package com.hbm.items.tool;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IColoredItem;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluid.IFluidType;
import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/**
 * The pipettes. Three same-named different specs: regular 1000mB, borosilicate 1000mB but corrosion-proof, lab 50mB and corrosion-proof.
 *
 * Amounts store in NBT's three shorts per the old convention (type / fill / capacity), with a default filled in when untagged —
 * so one grabbed straight from creative is "empty but initialized", never rewritten on every read.
 *
 * Looks: the old version was "base + a second overlay pass, swapping to pipette_empty when empty"; here one model with two layers —
 * layer0 the tube, layer1 the fluid column tinted by fluid. ⚠ The difference: **empty/full texture swaps can't happen here**:
 * layers switch only by meta or registered property predicates, while the amount lives in NBT — so the empty tube draws the overlay too,
 * just untinted. Restoring that two-texture difference needs the render-layer route (the (b) class of §16).
 */
public class ItemPipette extends Item implements IFillableItem, IColoredItem {

	public ItemPipette() {
		this.setMaxDamage(1);
	}

	public int getMaxFill() {
		return this == ModItems.pipette_laboratory ? 50 : 1_000;
	}

	public void initNBT(ItemStack stack) {

		stack.setTagCompound(new NBTTagCompound());
		this.setFill(stack, Fluids.NONE, (short) 0);
		stack.getTagCompound().setShort("capacity", (short) this.getMaxFill());
	}

	public FluidType getType(ItemStack stack) {

		if(!stack.hasTagCompound())
			initNBT(stack);

		return Fluids.fromID(stack.getTagCompound().getShort("type"));
	}

	public int getCapacity(ItemStack stack) {

		if(!stack.hasTagCompound())
			initNBT(stack);

		return stack.getTagCompound().getShort("capacity");
	}

	public void setFill(ItemStack stack, FluidType type, short fill) {

		if(!stack.hasTagCompound())
			initNBT(stack);

		stack.getTagCompound().setShort("type", (short) type.getID());
		stack.getTagCompound().setShort("fill", fill);
	}

	@Override
	public int getFill(ItemStack stack) {

		if(!stack.hasTagCompound())
			initNBT(stack);

		return stack.getTagCompound().getShort("fill");
	}

	/*Only the regular one gets "splashed" by corrosive fluids; borosilicate and lab ones are fine with the same fluids */
	public boolean willFizzle(IFluidType type) {

		FluidType fluid = concrete(type);

		if(this != ModItems.pipette || fluid == null)
			return false;

		return fluid.isCorrosive() && fluid != Fluids.PEROXIDE;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(!stack.hasTagCompound())
			initNBT(stack);

		if(world.isRemote)
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);

		if(this.getFill(stack) == 0) {

			int a;

			if(this == ModItems.pipette_laboratory)
				a = !player.isSneaking() ? Math.min(this.getCapacity(stack) + 1, 50) : Math.max(this.getCapacity(stack) - 1, 1);
			else
				a = !player.isSneaking() ? Math.min(this.getCapacity(stack) + 50, 1_000) : Math.max(this.getCapacity(stack) - 50, 50);

			stack.getTagCompound().setShort("capacity", (short) a);
			player.sendMessage(new TextComponentString(a + "/" + this.getMaxFill() + "mB"));
		} else {
			player.sendMessage(new TextComponentTranslation("desc.item.pipette.noEmpty"));
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(this == ModItems.pipette_laboratory) {
			list.add(I18nUtil.resolveKey("desc.item.pipette.corrosive"));
			list.add(I18nUtil.resolveKey("desc.item.pipette.laboratory"));
		}
		if(this == ModItems.pipette_boron)
			list.add(I18nUtil.resolveKey("desc.item.pipette.corrosive"));
		if(this == ModItems.pipette)
			list.add(I18nUtil.resolveKey("desc.item.pipette.noCorrosive"));

		FluidType type = this.getType(stack);

		list.add("Fluid: " + (type == null ? "-" : type.getLocalizedName()));
		list.add("Amount: " + this.getFill(stack) + "/" + this.getCapacity(stack) + "mB (" + this.getMaxFill() + "mB)");
	}

	@Override
	public boolean acceptsFluid(IFluidType type, ItemStack stack) {

		FluidType fluid = concrete(type);

		if(fluid == null)
			return false;

		return (fluid == this.getType(stack) || this.getFill(stack) == 0) && !fluid.isAntimatter();
	}

	@Override
	public int tryFill(IFluidType type, int amount, ItemStack stack) {

		if(!acceptsFluid(type, stack))
			return amount;

		if(this.getFill(stack) == 0)
			this.setFill(stack, concrete(type), (short) 0);

		int req = this.getCapacity(stack) - this.getFill(stack);
		int toFill = Math.min(req, amount);

		this.setFill(stack, this.getType(stack), (short) (this.getFill(stack) + toFill));

		if(this.getFill(stack) > 0 && willFizzle(type))
			stack.setCount(0);

		return amount - toFill;
	}

	@Override
	public boolean providesFluid(IFluidType type, ItemStack stack) {
		return this.getType(stack) == type;
	}

	@Override
	public int tryEmpty(IFluidType type, int amount, ItemStack stack) {

		if(!providesFluid(type, stack))
			return amount;

		int toUnload = Math.min(amount, this.getFill(stack));
		this.setFill(stack, concrete(type), (short) (this.getFill(stack) - toUnload));

		if(this.getFill(stack) == 0)
			this.setFill(stack, Fluids.NONE, (short) 0);

		return toUnload;
	}

	@Override
	public IFluidType getFirstFluidType(ItemStack stack) {
		return this.getType(stack);
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {

		if(tintIndex == 0)
			return 0xFFFFFF;

		FluidType type = this.getType(stack);

		if(type == null || type.getColor() < 0)
			return 0xFFFFFF;

		return type.getColor();
	}

	/*The interface only knows IFluidType, while the corrosion/antimatter checks live on the concrete FluidType */
	private static FluidType concrete(IFluidType type) {
		return type instanceof FluidType ? (FluidType) type : null;
	}
}
