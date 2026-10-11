package com.hbm.blocks;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Blocks drawing their own selection box take this road, skipping vanilla's one-box-per-block outline.
 * The box is the block's own math, so a whole multiblock lights up together when selected.
 */
public interface ICustomBlockHighlight {

	@SideOnly(Side.CLIENT) boolean shouldDrawHighlight(World world, BlockPos pos);

	@SideOnly(Side.CLIENT) void drawHighlight(DrawBlockHighlightEvent event, World world, BlockPos pos);

	/** The scene before the outline: blending on, textures off, no depth writes */
	@SideOnly(Side.CLIENT)
	static void setup() {
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(false);
	}

	@SideOnly(Side.CLIENT)
	static void cleanup() {
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
