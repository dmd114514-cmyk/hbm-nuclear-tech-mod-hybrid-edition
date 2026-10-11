package api.hbm.recipe;

import com.google.gson.JsonElement;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Recipe visibility gate. Progression mods like GameStages implement it,
 * turning whether a recipe is open to this player on this machine into a pluggable decision.
 *
 * Allow by default - with no gating mod installed nothing should change.
 */
public interface IHbmRecipeFilter {

	public boolean isRecipeAvailable(IRecipeHandler handler, JsonElement recipe, EntityPlayer player);

	/** Fallback: always allow */
	public static final IHbmRecipeFilter ALLOW_ALL = (handler, recipe, player) -> true;
}
