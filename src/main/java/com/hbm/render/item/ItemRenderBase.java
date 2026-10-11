package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * The base for dynamic item-form rendering.
 *
 * The old class was the deleted {@code IItemRenderer} callback set (handleRenderType / shouldUseRenderHelper /
 * renderItem(ItemRenderType,…)); this version becomes {@link TileEntityItemStackRenderer}: once an item carries an ISTER,
 * vanilla skips the baked model entirely and hands GL straight to {@link #renderByItem(ItemStack)} (the door vanilla's shield uses).
 *
 * The entry state is verified (javap {@code RenderItem.renderItem(ItemStack, IBakedModel)}): the caller does
 * pushMatrix → translate(-0.5,-0.5,-0.5) → color(1,1,1,1) → enableRescaleNormal, and only pops on exit,
 * with no scaling or rotation anywhere between; the model JSON's display transforms are **inert** on this branch.
 * The two direct consequences are written into applyPose and the wrap-up below.
 *
 * The cost, stated plainly: the old version split GUI / held / grounded poses by ItemRenderType, and this version's caller gives no such context,
 * so the base can only offer one compromise pose (subclasses may override applyPose). That pose needs an in-game look before tuning —
 * don't treat it as already cross-checked against the original.
 */
public abstract class ItemRenderBase extends TileEntityItemStackRenderer {

	@Override
	public void renderByItem(ItemStack stack) {

		// On entry the caller has the block atlas bound; we bind our own texture and must hand it back on exit, or every later item in the frame wears the wrong one
		int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

		GlStateManager.pushMatrix();
		applyPose(stack);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		IModelCustomNamed model = ResourceManager.getModel(modelFile());
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture(stack));
		renderModel(model, stack);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
		GlStateManager.bindTexture(previousTexture);
	}

	/** The OBJ to draw, like "models/machines/battery.obj", through ResourceManager's on-demand parse + cache */
	protected abstract String modelFile();

	/** Picks the texture by stack: a large crowd of old item renderers swapped textures exactly here, which is also why it can't be expressed in static JSON */
	protected abstract ResourceLocation texture(ItemStack stack);

	protected abstract void renderModel(IModelCustomNamed model, ItemStack stack);

	/**
	 * Default pose = the old version's three non-INVENTORY strokes (translate(0.5,0.25,0) → scale(0.25) → rotate(90,Y)),
	 * minus the half-block offset the caller already applied, to avoid a double translation: (0.5,0.25,0) − (−0.5,−0.5,−0.5).
	 */
	protected void applyPose(ItemStack stack) {
		GlStateManager.translate(1.0F, 0.75F, 0.5F);
		GlStateManager.scale(0.25F, 0.25F, 0.25F);
		GlStateManager.rotate(90F, 0F, 1F, 0F);
	}
}
