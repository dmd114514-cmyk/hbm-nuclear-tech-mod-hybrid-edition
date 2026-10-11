package api.hbm.tile;

/**
 * Tile entities that only need on/off and want it synced to the client right away (rail switches and friends).
 *
 * The packet references no concrete content class, same reason as {@link ISirenSync}: the packet lives in the core layer, content TEs in the content layer,
 * and having the lower layer lean on the upper one simply will not compile. Content implements this interface; the packet side only knows the interface.
 */
public interface ISwitchSync {

	/** Writes the switch bit into the client-side copy - for display and local checks only, do not touch world state here */
	public void setSwitched(boolean switched);
}
