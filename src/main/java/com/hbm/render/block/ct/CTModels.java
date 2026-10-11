package com.hbm.render.block.ct;

import com.hbm.registry.IRegistrar;

/**
 * The registration unit for the connected-texture model loader.
 *
 * Custom loaders must reach ModelLoaderRegistry before model baking, and registerModels() is exactly the pass that runs in client preInit
 * (same timing as OBJLoader claiming its domain). A separate IRegistrar exists so the shared ClientProxy stays untouched;
 * the subscribing line lives in registry/HbmModules.
 *
 * Only one call stays here; the registration body and its self-check line both live in ModelCT.registerLoader() — the reason is in that method note:
 * registration units get loaded on the server too, and a client type in a method body crashes the server start.
 */
public class CTModels implements IRegistrar {

	@Override
	public String id() {
		return "CTModels";
	}

	@Override
	public void registerModels() {
		ModelCT.registerLoader();
	}
}
