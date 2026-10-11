package api.hbm.redstoneoverradio;

import java.util.Locale;

/** ROR components that take commands and change their own state (controllers/programmers and such) */
public interface IRORInteractive extends IRORInfo {

	public static String NAME_SEPARATOR = "!";
	public static String PARAM_SEPARATOR = ":";

	public static String EX_NULL = "Exception: Null Command";
	public static String EX_NAME = "Exception: Multiple Name Separators";
	public static String EX_FORMAT = "Exception: Parameter in Invalid Format";

	/** Run one command; nobody uses the return value right now */
	public String runRORFunction(String name, String[] params);

	/** Pull the command name out of a full command string */
	public static String getCommand(String input) {
		if(input == null || input.isEmpty()) throw new RORFunctionException(EX_NULL);
		String[] parts = input.split(NAME_SEPARATOR);
		if(parts.length <= 0 || parts.length > 2) throw new RORFunctionException(EX_NAME);
		if(parts[0].isEmpty()) throw new RORFunctionException(EX_NULL);
		return parts[0].toLowerCase(Locale.US);
	}

	/** Pull the argument list out of a full command string */
	public static String[] getParams(String input) {
		if(input == null || input.isEmpty()) throw new RORFunctionException(EX_NULL);
		String[] parts = input.split(NAME_SEPARATOR);
		if(parts.length <= 0 || parts.length > 2) throw new RORFunctionException(EX_NAME);
		if(parts.length == 1) return new String[0];
		String paramList = parts[1];
		String[] params = paramList.split(PARAM_SEPARATOR);
		return params;
	}

	public static int parseInt(String val) {
		return parseInt(val, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public static int parseInt(String val, int min, int max) {
		int result = 0;
		try { result = Integer.parseInt(val); } catch(Exception x) {
			try { result = (int) Math.round(Double.parseDouble(val)); } catch(Exception y) { throw new RORFunctionException(EX_FORMAT); }
		}
		if(result < min || result > max) throw new RORFunctionException(EX_FORMAT);
		return result;
	}
}
