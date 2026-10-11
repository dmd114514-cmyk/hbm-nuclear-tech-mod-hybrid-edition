package api.hbm.tile;

/** Implemented by things that need dropping out of the network when their chunk is not loaded. Node links are unaffected. */
public interface ILoadedTile {

	public boolean isLoaded();
}
