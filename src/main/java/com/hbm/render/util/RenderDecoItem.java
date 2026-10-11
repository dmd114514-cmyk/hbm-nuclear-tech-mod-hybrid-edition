package com.hbm.render.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * In-world "item as decoration": draws an ItemStack flat onto the face the caller has currently posed (the old RenderDecoItem's job).
 *
 * This version changes shape rather than copying the old class: 1.7.10's RenderItem could be subclassed (the old one leaned on overriding getMiniBlockCount/shouldBob
 * plus a hand-written GUI flat path to assemble that face), but 1.12.2's RenderItem is a Minecraft-held singleton and the GUI path moved to
 * the model route — subclassing it would just invite the old paradigm back. Only "drawing" needs exposing now; model parsing, texture binding and enchantment glint
 * all go through the engine's in-world path.
 *
 * The sizing convention comes from bytecode: renderItem(ItemStack, IBakedModel) opens with translate(-0.5,-0.5,-0.5) before drawing the model,
 * which means item model vertices already live in the 0..1 unit cube ⇒ to draw it any size, just scale by that much.
 * Camera transforms (TransformType) are deliberately not applied — that step multiplies another model-owned scale into the current matrix, and then the caller no longer controls the size.
 *
 * Output orientation: front toward local +Z, X is right as seen from outside, Y up — the caller just poses the face (template: RenderMassStorage).
 * If in-game it looks mirrored left-right, this +Z assumption is backwards — change the FACE_FLIP below only, never each user's pose.
 */
@SideOnly(Side.CLIENT)
public final class RenderDecoItem {

	/** Item face orientation: 0 = model front as-is (toward local +Z), 180 = flipped over */
	private static final float FACE_FLIP = 0F;

	private RenderDecoItem() { }

	/**
	 * @param world only so per-position models (compasses, clocks, that sort) resolve right; ordinary items never use it
	 * @param sizeInBlocks edge length after squaring, in blocks (0.5 is half a block square)
	 */
	public static void drawFlat(ItemStack stack, World world, float sizeInBlocks) {

		if(stack == null || stack.isEmpty()) return;

		Minecraft mc = Minecraft.getMinecraft();
		RenderItem renderItem = mc.getRenderItem();
		TextureManager texman = mc.getTextureManager();

		GlStateManager.pushMatrix();
		{
			GlStateManager.scale(sizeInBlocks, sizeInBlocks, sizeInBlocks);
			if(FACE_FLIP != 0F) GlStateManager.rotate(FACE_FLIP, 0F, 1F, 0F);

			// This path doesn't bind its own texture — items and blocks are stitched into the same atlas anyway
			texman.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			IBakedModel model = renderItem.getItemModelWithOverrides(stack, world, null);
			renderItem.renderItem(stack, model);

			// Bind the old texture back when done: TESRs draw in a chain, and leaving our binding behind bites the next renderer
			texman.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}
		GlStateManager.popMatrix();
	}
}
