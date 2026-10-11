package com.hbm.render.loader;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Stands in for the deleted AdvancedModelLoader: dispatches by extension to the matching loader.
 * Note this only serves TESR immediate-mode rendering; to reference an OBJ from blockstate/model JSON,
 * use Forge's own OBJLoader.INSTANCE.addDomain("hbm") — the two systems keep to their own lanes.
 */
@SideOnly(Side.CLIENT)
public final class HbmModelLoader {

	private HbmModelLoader() { }

	public static IModelCustomNamed loadModel(ResourceLocation model) {

		if(model.getResourcePath().endsWith(".hmf")) return new HbmModelObject(model);

		return new HFRWavefrontObject(model);
	}
}
