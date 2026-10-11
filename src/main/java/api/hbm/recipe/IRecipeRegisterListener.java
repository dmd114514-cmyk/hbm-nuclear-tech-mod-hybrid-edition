package api.hbm.recipe;

/**
 * Recipe-load bystander. Inside SerializableRecipe.initialize(), every handler is called back once, after reading defaults and before writing the template.
 * Recipe registration is static work scattered across handlers, so each one has to be told individually,
 * and the argument is the handler's class name (simple name) so listeners know which recipe family is up.
 *
 * The SerializableRecipe singleton itself means nothing to listeners - recipes are static, do not go mutating the instance.
 */
public interface IRecipeRegisterListener {

	public void onRecipeLoad(String recipeClassName);
}
