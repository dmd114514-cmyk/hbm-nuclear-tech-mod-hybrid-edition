package api.hbm.fluid;

/**
 * The minimal tank contract. The content layer's FluidTank implements it and the net layer moves fluid through these few methods,
 * without needing to know what else the tank carries.
 */
public interface IFluidTank {

	public IFluidType getTankType();

	public int getPressure();

	public int getFill();

	public int getMaxFill();

	public void setFill(int fill);
}
