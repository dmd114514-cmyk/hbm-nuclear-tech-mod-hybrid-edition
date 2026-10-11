package com.hbm.render.tileentity;

import com.hbm.blocks.machine.BlockMassStorage;
import com.hbm.render.util.RenderDecoItem;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;
import com.hbm.util.LoggingUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

/**
 * The mass storage's front face: the currently stored item, a stock line beneath, and a fill-level color bar at the bottom.
 *
 * Positions align with the old version value for value; only the results of the old "GUI space + 180° about X + negative Z scale" conversion are written directly as face-center coordinates:
 *   · item: half a block square, horizontally centered, its center 0.594 blocks above the bottom (the old GUI's 2.5..10.5 cell folded back);
 *   · digits: one line 8 pixels tall, top edge 0.3125 blocks above the bottom;
 *   · color bar: 0.125..0.156 above the bottom, horizontally from 0.125 out to 0.875.
 *
 * The two things swapped this version are not look differences:
 *   · facing source: the old version crammed subtype and facing into one meta (facing = meta/4); this version keeps two separate properties,
 *     so this reads facing from the block state instead of guessing meta; the property only takes the four horizontal directions, so up and down never matter.
 *   · brightness: the old version, to light up the GUI flat plane, hacked the global lightmap texture coordinates (that change leaked into later frames);
 *     this version walks the model path, the item model carrying full-brightness vertex light, and the global state stays untouched.
 *
 * The stored kind and amount are already in the TE's sync packet (serialize wrote the stack and the cell item); this only reads, never computes.
 */
@SideOnly(Side.CLIENT)
public class RenderMassStorage extends TileEntitySpecialRenderer<TileEntityMassStorage> {

	/** The product of the old two-level scale (1/16)*(4/16): one 8-pixel-tall line of text mapped to 1/8 of a block */
	private static final float TEXT_UNIT = 1F / 64F;

	private static final int TEXT_COLOR = 0x00FF00;

	/** All vertical positions below are computed as "face center = origin, Y up": 0.594 above the bottom = face center +0.094, and so on */
	private static final float ITEM_Y = 0.094F;
	private static final float TEXT_TOP_Y = -0.1875F;
	private static final float BAR_BOTTOM_Y = -0.375F;
	private static final float BAR_TOP_Y = -0.344F;

	private boolean announced = false;

	@Override
	public void render(TileEntityMassStorage te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		ItemStack type = te.getType();
		if(type == null || type.isEmpty()) return;

		World world = te.getWorld();
		IBlockState state = world.getBlockState(te.getPos());
		if(!(state.getBlock() instanceof BlockMassStorage)) return;

		EnumFacing facing = state.getValue(BlockMassStorage.FACING);

		// Turning about Y by this angle lands local +Z exactly on the facing: (0,0,1) rotated becomes (sin, 0, cos) = that face's offset
		float yaw = (float) Math.toDegrees(Math.atan2(facing.getFrontOffsetX(), facing.getFrontOffsetZ()));

		GlStateManager.pushMatrix();
		{
			// Move to the face center, then nudge outward half a centimeter: staying on the face fights the block's own front texture for depth
			GlStateManager.translate(x + 0.5D + facing.getFrontOffsetX() * 0.5D, y + 0.5D, z + 0.5D + facing.getFrontOffsetZ() * 0.5D);
			GlStateManager.rotate(yaw, 0F, 1F, 0F);
			GlStateManager.translate(0F, 0F, 0.005F);

			// From here down, local coordinates are: origin = face center, +X = right as seen from outside, +Y = up, +Z = outward
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, ITEM_Y, 0F);
			RenderDecoItem.drawFlat(type, world, 0.5F);
			GlStateManager.popMatrix();

			drawCount(te);
			drawBar(te);
		}
		GlStateManager.popMatrix();

		if(!this.announced) {
			this.announced = true;
			LoggingUtil.info("[hbm] storage front item first frame drawn: facing {} stock {} capacity {}", facing.getName(), te.getStockpile(), te.getCapacity());
		}
	}

	private void drawCount(TileEntityMassStorage te) {

		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		String text = getTextForCount(te.getStockpile(), font.getUnicodeFlag());
		int width = font.getStringWidth(text);

		GlStateManager.pushMatrix();
		{
			// The font draws "larger y = lower on screen", so Y folds to negative for the text to stand upright
			GlStateManager.translate(0F, TEXT_TOP_Y, 0F);
			GlStateManager.scale(TEXT_UNIT, -TEXT_UNIT, TEXT_UNIT);
			GlStateManager.disableLighting();

			// The shadow lands bottom-right first, then the main text presses over it
			font.drawString(text, 1F - width / 2F, 1F, (TEXT_COLOR & 16579836) >> 2 | TEXT_COLOR & -16777216, false);
			font.drawString(text, -width / 2F, 0F, TEXT_COLOR, false);

			GlStateManager.enableLighting();
		}
		GlStateManager.popMatrix();
	}

	/** Empty to full: green to red */
	private void drawBar(TileEntityMassStorage te) {

		float fraction = (float) Math.min(1D, (double) te.getStockpile() / (double) Math.max(1, te.getCapacity()));

		float left = -0.375F;
		float right = left + fraction * 0.75F;

		GlStateManager.pushMatrix();
		{
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.color(1F - fraction, fraction, 0F, 1F);

			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
			buffer.pos(left, BAR_BOTTOM_Y, 0D).endVertex();
			buffer.pos(right, BAR_BOTTOM_Y, 0D).endVertex();
			buffer.pos(right, BAR_TOP_Y, 0D).endVertex();
			buffer.pos(left, BAR_TOP_Y, 0D).endVertex();
			Tessellator.getInstance().draw();

			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
		}
		GlStateManager.popMatrix();
	}

	/** Big numbers fold to K/M, same criterion as the old version: whether to carry early depends on the unicode font being on */
	private static String getTextForCount(int stockpile, boolean isUnicode) {

		if(stockpile >= 100000000 || (stockpile >= 1000000 && isUnicode)) return String.format("%.0fM", stockpile / 1000000F);
		if(stockpile >= 1000000) return String.format("%.1fM", stockpile / 1000000F);
		if(stockpile >= 100000 || (stockpile >= 10000 && isUnicode)) return String.format("%.0fK", stockpile / 1000F);
		if(stockpile >= 10000) return String.format("%.1fK", stockpile / 1000F);

		return String.valueOf(stockpile);
	}
}
