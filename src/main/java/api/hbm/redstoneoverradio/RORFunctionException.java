package api.hbm.redstoneoverradio;

/** Thrown when an ROR command fails to parse or run */
public class RORFunctionException extends RuntimeException {

	public RORFunctionException(String message) {
		super(message);
	}
}
