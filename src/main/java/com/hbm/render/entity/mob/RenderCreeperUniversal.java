package com.hbm.render.entity.mob;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * The shared look for the creeper family: skeleton, swell scaling and charge overlay all vanilla; only the body texture swaps per member.
 *
 * Six family members share it because the old version was already one universal renderer plus two texture parameters (body/overlay). One convenient change here:
 * the old charge overlay was multi-pass ({@code shouldRenderPass} pass 1 binding the overlay texture, scrolling UVs, additive blending,
 * pass 2 resetting); both hooks are gone in this version — and this version's vanilla {@code RenderCreeper} already carries
 * a {@code LayerCreeperCharge} doing exactly that (same {@code textures/entity/creeper/creeper_armor.png},
 * same UV scroll and ONE/ONE blend). So inheriting it comes free, and the overlay constructor argument is redundant for "overlay = vanilla's" members — only the body texture stays.
 * this version keeps only the body texture.
 *
 * A note for whoever picks this up: any member whose overlay isn't vanilla's (the taint creeper uses hbm's own {@code creeper_armor_taint.png})
 * can't swap the inherited layer; it needs its own {@code LayerRenderer} added via {@code addLayer}.
 */
public class RenderCreeperUniversal extends RenderCreeper {

	private final ResourceLocation bodyTexture;
	private float swellMod = 1.0F;

	public RenderCreeperUniversal(RenderManager renderManager, ResourceLocation bodyTexture) {
		super(renderManager);
		this.bodyTexture = bodyTexture;
	}

	public RenderCreeperUniversal setSwellMod(float mod) {
		this.swellMod = mod;
		return this;
	}

	/**
	 * The swell scaling: vanilla algorithm, with one extra swellMod multiplied in before scaling — the radiation creeper is meant to puff up bigger,
	 * that's what the old version used the multiplier for; everyone else is 1.
	 */
	@Override
	protected void preRenderCallback(EntityCreeper creeper, float interp) {
		float swell = creeper.getCreeperFlashIntensity(interp);
		float flash = 1.0F + MathHelper.sin(swell * 100.0F) * swell * 0.01F;

		if(swell < 0.0F) {
			swell = 0.0F;
		}

		if(swell > 1.0F) {
			swell = 1.0F;
		}

		swell *= swell;
		swell *= swell;
		swell *= swell;
		swell *= this.swellMod;

		float scaleHorizontal = (1.0F + swell * 0.4F) * flash;
		float scaleVertical = (1.0F + swell * 0.1F) / flash;

		GlStateManager.scale(scaleHorizontal, scaleVertical, scaleHorizontal);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCreeper creeper) {
		return this.bodyTexture;
	}
}
