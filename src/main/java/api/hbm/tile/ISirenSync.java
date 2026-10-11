package api.hbm.tile;

/**
 * Tile entities that need a looped sound toggled by a network command (sirens, that kind of thing).
 *
 * It is a tiny interface instead of the packet referencing concrete TE classes because the packet lives in the core layer,
 * the machine TEs in the content layer, and a packet importing concrete content makes the lower layer lean on the upper,
 * and that class is not even in yet - referencing it would just add two compile errors.
 * As it stands: content implements this interface, the packet side only knows the interface.
 */
public interface ISirenSync {

	/** Switch track and start/stop by active; id is the track/tune number */
	public void setSirenTrack(int id, boolean active);
}
