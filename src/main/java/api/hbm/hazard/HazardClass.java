package api.hbm.hazard;

/**
 * The physical form of an environmental hazard, which decides what gear blocks it.
 *
 * It lives on the api layer because it is shared vocabulary for the protection system: items (IGasMask filters) and blocks both reference it,
 * and if it stayed in the content layer, api would have to import com.hbm, breaking the zero-dependency rule.
 */
public enum HazardClass {
	GAS_LUNG("hazard.gasChlorine"),            // Also irritates the eyes; a half-mask will not hold
	GAS_MONOXIDE("hazard.gasMonoxide"),        // lungs only
	GAS_INERT("hazard.gasInert"),              // Inert gas, placeholder only
	PARTICLE_COARSE("hazard.particleCoarse"),  // lungs only
	PARTICLE_FINE("hazard.particleFine"),      // lungs only
	BACTERIA("hazard.bacteria"),               // A half-mask will not hold
	GAS_BLISTERING("hazard.corrosive"),        // Corrosive, burns skin too
	SAND("hazard.sand"),                       // Blinding dust
	LIGHT("hazard.light");                     // Bright light, blinds you

	/** The matching lang key */
	public final String lang;

	HazardClass(String lang) {
		this.lang = lang;
	}
}
