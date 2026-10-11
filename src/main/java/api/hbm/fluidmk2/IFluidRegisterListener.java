package api.hbm.fluidmk2;

/**
 * Bystander at fluid registry init.
 * Fluids are not Forge registry objects, so the content layer has to run registration explicitly once; that is when this gets called back,
 * letting third parties (or this mod's own optional packages) hang things off the same set of fluids.
 */
public interface IFluidRegisterListener {

	public void onFluidsLoad();
}
