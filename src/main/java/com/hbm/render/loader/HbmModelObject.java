package com.hbm.render.loader;

import java.io.InputStream;

import com.hbm.util.Clock;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * HMF is, bluntly, an OBJ with a time-scrolled V coordinate (flames, plasma and friends); early on it got its own dedicated parser.
 * This just extends the OBJ renderer and hangs the scroll amount on, saving six hundred lines of duplicate code.
 */
@SideOnly(Side.CLIENT)
public class HbmModelObject extends HFRWavefrontObject {

	public HbmModelObject(ResourceLocation resource) {
		super(resource);
	}

	public HbmModelObject(String fileName, InputStream inputStream) {
		super(fileName, inputStream);
	}

	@Override
	protected float uvAnim() {
		return (float) ((Clock.get_ms() % HmfController.modoloMod) / HmfController.quotientMod);
	}

	public String getType() {
		return "hmf";
	}
}
