package api.hbm.redstoneoverradio;

/** ROR components that report values by name; reading must never have side effects */
public interface IRORValueProvider extends IRORInfo {

	public String provideRORValue(String name);
}
