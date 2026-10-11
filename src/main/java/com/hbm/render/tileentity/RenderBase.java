package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/**
 * The TESR common skeleton: push, move to block center, switch lighting on, let the subclass draw, pop and wrap up.
 *
 * 1.12.2's entry changed from the old five-arg renderTileEntityAt to the seven-arg render (destruction stage and alpha added),
 * and the abstract door narrows back to the old shape, so per-machine ports can copy the old body verbatim.
 * Each machine's facing mapping is decided by the OBJ's factory orientation; the four yaws all differ, and those switch lines stay in the subclasses, copied per machine.
 */
public abstract class RenderBase<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

	@Override
	public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);

		renderTileEntityAt(te, partialTicks);

		GL11.glPopMatrix();
	}

	/** The old five-arg entry's name and shape */
	protected abstract void renderTileEntityAt(T te, float partialTicks);
}
