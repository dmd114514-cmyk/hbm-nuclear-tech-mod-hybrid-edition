package com.hbm.capability;

import com.hbm.lib.RefStrings;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyHandlerMK2;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * The unified exit for capabilities. Two routes:
 *
 * ① The machine base implements {@link ICapabilityHost} and answers directly in getCapability/hasCapability with
 *    `return CapabilityHandler.get(this, capability, facing);` (subclasses override accessors only,
 *    no per-machine getCapability);
 * ② Machines that skip the base class get a capability attached by onAttachTile if they hold storage/tanks but don't answer themselves.
 * Both routes obey "fill gaps, never overwrite": whatever a machine answers for itself, this never touches — no capability registered twice.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class CapabilityHandler {

	public static final ResourceLocation KEY_ENERGY = new ResourceLocation(RefStrings.MODID, "energy");
	public static final ResourceLocation KEY_ITEM = new ResourceLocation(RefStrings.MODID, "item");
	public static final ResourceLocation KEY_FLUID = new ResourceLocation(RefStrings.MODID, "fluid");

	private CapabilityHandler() { }

	/** For base-class getCapability: fetches the handler the machine holds, by capability type */
	public static <T> T get(TileEntity te, Capability<T> capability, EnumFacing facing) {
		if(te == null || capability == null) return null;

		if(capability == CapabilityEnergy.ENERGY) {
			IEnergyStorage storage = energyStorageOf(te);
			if(storage != null) return CapabilityEnergy.ENERGY.cast(storage);
		}

		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			IFluidHandler handler = fluidHandlerOf(te);
			if(handler != null) return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler);
		}

		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			IItemHandler handler = itemHandlerOf(te);
			if(handler != null) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
		}

		return null;
	}

	public static boolean has(TileEntity te, Capability<?> capability, EnumFacing facing) {
		return get(te, capability, facing) != null;
	}

	/** Energy the machine reports: base-class override wins, then the old-school "the machine itself is an IEnergyHandlerMK2" */
	public static IEnergyStorage energyStorageOf(TileEntity te) {
		if(te instanceof ICapabilityHost) {
			IEnergyStorage storage = ((ICapabilityHost) te).getEnergyStorage();
			if(storage != null) return storage;
		}
		return te instanceof IEnergyHandlerMK2 ? new HbmEnergyStorage((IEnergyHandlerMK2) te) : null;
	}

	/** Inventory the machine reports: base-class override wins, then the old style of implementing IItemHandler directly */
	public static IItemHandler itemHandlerOf(TileEntity te) {
		if(te instanceof ICapabilityHost) {
			IItemHandler handler = ((ICapabilityHost) te).getItemHandler();
			if(handler != null) return handler;
		}
		return te instanceof IItemHandler ? (IItemHandler) te : null;
	}

	/** Tanks the machine reports: base-class override wins, then the old style of implementing IFluidHandler directly */
	public static IFluidHandler fluidHandlerOf(TileEntity te) {
		if(te instanceof ICapabilityHost) {
			IFluidHandler handler = ((ICapabilityHost) te).getFluidHandler();
			if(handler != null) return handler;
		}
		return te instanceof IFluidHandler ? (IFluidHandler) te : null;
	}

	@SubscribeEvent
	public static void onAttachTile(AttachCapabilitiesEvent<TileEntity> event) {

		TileEntity te = event.getObject();

		//Anyone that can answer for itself (or whose base class can) is none of our business; we only backfill machines holding a handler that stay silent
		if(te instanceof ICapabilityProvider) return;

		IEnergyStorage energy = energyStorageOf(te);
		if(energy != null) event.addCapability(KEY_ENERGY, new CapabilityEnergyProvider(energy));

		IFluidHandler fluid = fluidHandlerOf(te);
		if(fluid != null) event.addCapability(KEY_FLUID, new CapabilityFluidProvider(fluid));

		IItemHandler item = itemHandlerOf(te);
		if(item != null) event.addCapability(KEY_ITEM, new CapabilityItemProvider(item));
	}

	@SubscribeEvent
	public static void onAttachItem(AttachCapabilitiesEvent<ItemStack> event) {

		ItemStack stack = event.getObject();
		if(stack.getItem() instanceof IBatteryItem) {
			event.addCapability(KEY_ENERGY, new CapabilityBatteryProvider(stack));
		}
	}
}
