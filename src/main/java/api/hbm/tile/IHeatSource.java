package api.hbm.tile;

/** Heat source: reports how much heat it holds, and anyone drawing heat takes it from here */
public interface IHeatSource {

	public int getHeatStored();

	/** Draw heat. Clamping to non-negative is the implementer's job - the interface does not cover for it */
	public void useUpHeat(int heat);
}
