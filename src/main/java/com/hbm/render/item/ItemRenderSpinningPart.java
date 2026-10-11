package com.hbm.render.item;

import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Self-spinning item parts (big gear, saw blade, that family): the model is one piece of the machine OBJs, and it spins the whole time it's on screen.
 *
 * The old version gave these two callback sets — {@code renderInventory} with the spin, {@code renderCommonWithStack} just posing;
 * this version's caller gives no "GUI or in hand" context (see the {@link ItemRenderBase} class note), so they merge into one:
 * pose takes the base generic values plus the old held-branch translate(0,0,-0.875), and the spin is on in both.
 * A stopped gear in your hand looks like it was never installed; that matters more than "don't spin in the inventory".
 */
public class ItemRenderSpinningPart extends ItemRenderBase {

	/** The base scale is 0.25 and the model coords already sit at the ±1.5 tier, so the old 1.375 spin-axis offset carries over directly */
	private static final float SPIN_PIVOT_Y = 1.375F;

	private final String model;
	private final String part;
	/** Tier by meta, past the last one draw the last texture — the old if(meta==0) two-way pick degenerates into the same thing */
	private final ResourceLocation[] textures;
	/** Milliseconds per revolution (the old version wrote expressions like currentTimeMillis % 3600 * 0.1F; gear 3.6 s, saw blade 1.8 s) */
	private final long period;

	public ItemRenderSpinningPart(String model, String part, long period, ResourceLocation... textures) {
		this.model = model;
		this.part = part;
		this.period = period;
		this.textures = textures;
	}

	@Override
	protected String modelFile() {
		return model;
	}

	@Override
	protected ResourceLocation texture(ItemStack stack) {

		int index = stack.getItemDamage();

		if(index < 0) index = 0;
		if(index >= textures.length) index = textures.length - 1;

		return textures[index];
	}

	@Override
	protected void renderModel(IModelCustomNamed model, ItemStack stack) {
		model.renderPart(part);
	}

	@Override
	protected void applyPose(ItemStack stack) {
		super.applyPose(stack);
		GlStateManager.translate(0F, 0F, -0.875F);
		// The old three strokes: lift to the axis → turn about Z by the current angle → set back down
		GlStateManager.translate(0F, SPIN_PIVOT_Y, 0F);
		GlStateManager.rotate(System.currentTimeMillis() % this.period * (360F / this.period), 0F, 0F, 1F);
		GlStateManager.translate(0F, -SPIN_PIVOT_Y, 0F);
	}
}
