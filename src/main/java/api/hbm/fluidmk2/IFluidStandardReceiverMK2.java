package api.hbm.fluidmk2;

import api.hbm.fluid.IFluidTank;
import api.hbm.fluid.IFluidType;

/**
 * Generic IFluidReceiverMK2: hand over the input tanks and this class does the demand maths and the injection split.
 * Good enough for the vast majority of machines.
 */
public interface IFluidStandardReceiverMK2 extends IFluidReceiverMK2 {

	public IFluidTank[] getReceivingTanks();

	@Override
	public default long getDemand(IFluidType type, int pressure) {
		long amount = 0;
		for(IFluidTank tank : getReceivingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) amount += (tank.getMaxFill() - tank.getFill());
		}
		return amount;
	}

	@Override
	public default long transferFluid(IFluidType type, int pressure, long amount) {
		int tanks = 0;
		for(IFluidTank tank : getReceivingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) tanks++;
		}

		// Spread one even round first, so multiple tanks of the same kind do not all get poured into the first slot
		if(tanks > 1) {
			int firstRound = (int) Math.floor((double) amount / (double) tanks);
			for(IFluidTank tank : getReceivingTanks()) {
				if(tank.getTankType() == type && tank.getPressure() == pressure) {
					int toAdd = Math.min(firstRound, tank.getMaxFill() - tank.getFill());
					tank.setFill(tank.getFill() + toAdd);
					amount -= toAdd;
				}
			}
		}

		// Then split what is left and top each one up
		if(amount > 0) for(IFluidTank tank : getReceivingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) {
				int toAdd = (int) Math.min(amount, tank.getMaxFill() - tank.getFill());
				tank.setFill(tank.getFill() + toAdd);
				amount -= toAdd;
			}
		}
		return amount;
	}

	@Override
	public default int[] getReceivingPressureRange(IFluidType type) {
		int lowest = HIGHEST_VALID_PRESSURE;
		int highest = 0;

		for(IFluidTank tank : getReceivingTanks()) {
			if(tank.getTankType() == type) {
				if(tank.getPressure() < lowest) lowest = tank.getPressure();
				if(tank.getPressure() > highest) highest = tank.getPressure();
			}
		}

		return lowest <= highest ? new int[] { lowest, highest } : DEFAULT_PRESSURE_RANGE;
	}

	@Override
	public default long getReceiverSpeed(IFluidType type, int pressure) {
		return 1_000_000_000;
	}
}
