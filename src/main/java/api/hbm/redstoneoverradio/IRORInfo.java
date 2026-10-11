package api.hbm.redstoneoverradio;

/** The self-describing face of an ROR component: lists the function names it knows */
public interface IRORInfo {

	public static String PREFIX_VALUE =		"VAL:";
	public static String PREFIX_FUNCTION =	"FUN:";

	public String[] getFunctionInfo();
}
