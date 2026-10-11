package api.hbm.fluid;

import java.util.Locale;

import api.hbm.net.INetworkProvider;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * The minimal fluid type contract. The content layer's FluidType implements it, so api.hbm.fluidmk2 does not have to import
 * com.hbm.inventory.fluid in reverse - exactly the back-dependency the architecture wants cut.
 *
 * It only declares the three things the net layer really needs: name, colour, and the fluid's own node provider.
 */
public interface IFluidType {

	public String getName();

	/** Colour used for pipe/particle rendering */
	public int getColor();

	/** One net per fluid, so the provider travels with the type */
	public INetworkProvider<?> getNetworkProvider();

	/**
	 * The matching Forge fluid (registry name per the 16 convention: hbm_ prefix, all lowercase).
	 * Return null when there is none - it means the fluid never made it into Forge's registry, so the cross-mod side stays out of it.
	 */
	public default Fluid getForgeFluid() {
		String name = getName();
		return name == null ? null : FluidRegistry.getFluid("hbm_" + name.toLowerCase(Locale.US));
	}
}
