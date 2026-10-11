package api.hbm.fluidmk2;

import api.hbm.fluid.IFluidType;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Neighbor hopping on the fluid side. With the node graph gone, liquid travels between this machine and the cell touching it:
 * Our own machines add/subtract on the tanks directly; third-party devices go through Forge's IFluidHandler.
 *
 * Units stay our own mB, converted 1:1 across mods with no ratio maths - pressure is the caller's job to filter by tier.
 */
public final class FluidNeighbors {

	private FluidNeighbors() { }

	/** When the other end is a third-party fluid device, grab its Forge interface; our own connectors return null so the same fluid does not move twice */
	public static IFluidHandler getForeignHandler(World world, int x, int y, int z, EnumFacing dir) {
		if(world == null || dir == null) return null;
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(te == null || te instanceof IFluidConnectorMK2) return null;
		return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
	}

	/** Push mB into the third-party device, returns how much actually went in */
	public static long pushToForeign(IFluidType type, World world, int x, int y, int z, EnumFacing dir, long amount) {
		if(type == null || amount <= 0) return 0;

		IFluidHandler handler = getForeignHandler(world, x, y, z, dir);
		if(handler == null) return 0;

		Fluid fluid = type.getForgeFluid();
		if(fluid == null) return 0;

		int offer = (int) Math.min(amount, Integer.MAX_VALUE);
		if(offer <= 0) return 0;

		int accepted = handler.fill(new FluidStack(fluid, offer), true);
		return accepted <= 0 ? 0 : accepted;
	}

	/** Pull mB out of the third-party device, returns how much actually came out */
	public static long pullFromForeign(IFluidType type, World world, int x, int y, int z, EnumFacing dir, long amount) {
		if(type == null || amount <= 0) return 0;

		IFluidHandler handler = getForeignHandler(world, x, y, z, dir);
		if(handler == null) return 0;

		Fluid fluid = type.getForgeFluid();
		if(fluid == null) return 0;

		int want = (int) Math.min(amount, Integer.MAX_VALUE);
		if(want <= 0) return 0;

		FluidStack drained = handler.drain(new FluidStack(fluid, want), true);
		return drained == null ? 0 : drained.amount;
	}
}
