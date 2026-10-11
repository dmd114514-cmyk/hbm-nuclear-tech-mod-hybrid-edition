package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.render.tileentity.RenderBobble;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

/**
 * The bobblehead's item form: the inventory cell also stands a figure, drawn with the same set as the block body (base + figure + engraved name).
 *
 * The old version hung this in the {@code ItemRenderLibrary} table, the callback calling {@code RenderBobble.instance.renderBobble(...)} directly;
 * which figure it is rides the stack's durability — this tree records the same number (the block's drop-on-harvest in {@code BlockBobble}.onBlockHarvested
 * writes {@code type.ordinal()}, and placing it back restores by the same number), so the old retrieval works here as-is, no block-side change.
 *
 * The pose takes the block tier's scale (0.25), and the reason is measurable: that OBJ's vertices span roughly ±1.2 in x/z and 0 to 2.9 in y,
 * and ×0.25 lands exactly on "one block square holding a 0.73-tall figure". The old inventory tier's translate(0,-3.5)+scale(10)
 * sat on top of vanilla's pixel-level INVENTORY pre-transform, a layer this version doesn't have — copied blindly, the figure would burst out of the cell.
 * Two things to check in-game and tune, each touching just the one number below or adding one rotation: ① the scale; ② that old pre-transform carried a 180° flip,
 * and this version doesn't take that layer — so if the cell shows you the back of the head, add {@code GlStateManager.rotate(180F, 0F, 1F, 0F)}.
 */
public class ItemRenderBobblehead extends TileEntityItemStackRenderer {

	private final RenderBobble figurine = new RenderBobble();

	/** Same scale as the block tier, see the class note */
	private static final float SCALE = 0.25F;

	@Override
	public void renderByItem(ItemStack stack) {

		// On entry the caller has the block atlas bound; it must be handed back on exit or every later item in the frame wears the wrong texture (same convention as ItemRenderBase)
		int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

		GlStateManager.pushMatrix();
		{
			GlStateManager.scale(SCALE, SCALE, SCALE);
			RenderHelper.enableStandardItemLighting();

			this.figurine.renderAsItem(type(stack));

			RenderHelper.disableStandardItemLighting();
		}
		GlStateManager.popMatrix();
		GlStateManager.bindTexture(previousTexture);
	}

	/** Durability → which figure; out-of-range and negatives fold back into the table by the same formula the block uses on restore */
	private static BobbleType type(ItemStack stack) {

		BobbleType[] all = BobbleType.values();

		return all[Math.abs(stack.getItemDamage()) % all.length];
	}
}
