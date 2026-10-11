package com.hbm.render.item;

import com.hbm.lib.RefStrings;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * One member of the Dyson swarm: the whole model drawn complete, slowly turning about the vertical axis.
 *
 * A different family from {@link ItemRenderSpinningPart} — those two are single pieces (Cog/Blade) of the machine OBJs, spinning fast about a horizontal axis;
 * this one uses the satellite body (renderAll, the old version reusing the {@code dyson_swarm_satellite} model and texture),
 * the axis is Y, one turn takes 7.2 s, and the direction is opposite — so it doesn't share that pose.
 */
public class ItemRenderDysonMember extends ItemRenderBase {

	private static final String MODEL = "models/dyson_swarm_satellite.obj";
	private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID, "textures/models/dyson_swarm_satellite.png");

	/** The old version wrote currentTimeMillis % 7200 * -0.05F, i.e. one turn per 7.2 s, reversed */
	private static final long PERIOD = 7200;

	@Override
	protected String modelFile() {
		return MODEL;
	}

	@Override
	protected ResourceLocation texture(ItemStack stack) {
		return TEXTURE;
	}

	@Override
	protected void renderModel(IModelCustomNamed model, ItemStack stack) {
		model.renderAll();
	}

	@Override
	protected void applyPose(ItemStack stack) {
		super.applyPose(stack);
		// The old handheld branch had no extra translation; it just turned about Y in place by the current angle
		GlStateManager.rotate(-(System.currentTimeMillis() % PERIOD) * (360F / PERIOD), 0F, 1F, 0F);
	}
}
