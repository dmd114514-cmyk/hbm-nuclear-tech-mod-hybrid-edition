package com.hbm.render.loader;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

public class HFRModelReloader implements IResourceManagerReloadListener {

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {

		for(HFRWavefrontObject obj : HFRWavefrontObject.allModels) {
			try {
				obj.destroy();
				IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(obj.resource);
				obj.loadObjModel(resource.getInputStream());
			} catch(IOException e) {
				// one model failing shouldn't sink the whole reload; keep going
			}
		}
	}
}
