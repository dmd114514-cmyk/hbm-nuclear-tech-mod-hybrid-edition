package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.IModelCustomNamed;
import com.hbm.render.loader.S_GroupObject;
import com.hbm.render.util.ObjUtil;
import com.hbm.render.util.SpriteUtil;
import com.hbm.util.LoggingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The bedrock for drawing OBJ in-world.
 *
 * 1.7.10's custom render type (getRenderType returning a registered id, with RenderBlocks dispatching to each family's
 * renderWorld) is entirely gone this version; yet rails and multiblock bodies need to stand in the world as multi-cell odd shapes
 * the JSON cube/cross vocabulary can't express. So vertices are written by hand, and "fetch model, bind block atlas, open buffer, draw once"
 * collects into one place; subclasses only state three things: which model to draw, how to pose this cell, which quads to append.
 *
 * Posing follows BlockDummyable's meta convention: the true body is 10+facing ordinal (12..15, horizontal four only),
 * and placeholder cells are smaller and never drawn — the old if(meta &lt; 12) return at the top of every renderWorld meant exactly this.
 * The centering half-block (the old addTranslation(x + 0.5, y, z + 0.5) every family had) is stacked on uniformly by this class;
 * the subclass Pose carries only each family's own offset relative to the true body's center, never recomputing that half.
 *
 * Shading goes through ObjUtil's face normals (the old shadow parameter), so GL lighting is switched off here;
 * the old models' noSmooth only affected the vertex-normal render path, and ObjUtil uses face normals, so it doesn't matter.
 */
@SideOnly(Side.CLIENT)
public abstract class TileEntityObjRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

	/** The model origin's translation relative to the true body's center (in blocks), plus one radian about Y */
	public static final class Pose {

		public final double x;
		public final double y;
		public final double z;
		public final float yaw;

		public Pose(double x, double y, double z, float yaw) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
		}
	}

	private boolean announced;

	/** Like models/blocks/rail_standard_switch.obj, handed to ResourceManager for lazy parse and caching.
	 *  The tile class comes in because one tile class often serves both sides of a switch pair, and the model name only separates at the block level */
	protected abstract String modelPath(T tile);

	/** Returning null means this cell draws nothing */
	protected abstract Pose pose(T tile);

	/** Appends quads into an already-begun buffer, via one or more drawPart/drawWhole */
	protected abstract void draw(T tile, HFRWavefrontObject model, Pose pose);

	/** Set true when the body spans several cells: this version's RenderChunk culls by the TE's render box, and a one-cell box cuts the body off */
	protected boolean isGlobal() {
		return false;
	}

	@Override
	public final boolean isGlobalRenderer(T tile) {
		return this.isGlobal();
	}

	@Override
	public void render(T tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		Pose pose = this.pose(tile);
		if(pose == null) return;

		IModelCustomNamed loaded = ResourceManager.getModel(this.modelPath(tile));
		if(!(loaded instanceof HFRWavefrontObject)) return;	// the hmf crowd has its own drawing channel; it doesn't get force-drawn here

		HFRWavefrontObject model = (HFRWavefrontObject) loaded;

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		GlStateManager.pushMatrix();

		// Camera translation is already done by vanilla at this point; x/y/z are this cell's corner relative to the camera — don't stack world coordinates on again
		GlStateManager.translate(x + 0.5D + pose.x, y + pose.y, z + 0.5D + pose.z);
		GlStateManager.disableLighting();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		this.draw(tile, model, pose);

		Tessellator.getInstance().draw();

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		this.announce(model, this.modelPath(tile), pose);
	}

	/** Draws the whole model; iconName like hbm:blocks/rail_standard_straight */
	protected void drawWhole(HFRWavefrontObject model, String iconName, Pose pose) {
		ObjUtil.renderWithIcon(model, this.sprite(iconName), Tessellator.getInstance().getBuffer(), pose.yaw, true);
	}

	/** Draws one part; the part name is the obj's o line; a mismatch and ObjUtil silently skips */
	protected void drawPart(HFRWavefrontObject model, String partName, String iconName, Pose pose) {
		ObjUtil.renderPartWithIcon(model, partName, this.sprite(iconName), Tessellator.getInstance().getBuffer(), pose.yaw, true);
	}

	protected TextureAtlasSprite sprite(String iconName) {
		return SpriteUtil.getSprite(iconName);
	}

	/** The first real draw logs the face count: "drawn but invisible" is the hardest thing to debug, so leave a self-check trace */
	private void announce(HFRWavefrontObject model, String modelPath, Pose pose) {

		if(this.announced) return;

		this.announced = true;

		int faces = 0;

		for(S_GroupObject group : model.groupObjects) faces += group.faces.size();

		LoggingUtil.info("[hbm] block OBJ first frame drawn: model {} faces {} pose x={} y={} z={} yaw={} global={}",
				modelPath, faces, pose.x, pose.y, pose.z, pose.yaw, this.isGlobal());
	}

	/** The true body's meta converted to facing; not a true body, return null */
	public static EnumFacing coreFacing(int meta) {

		if(meta < BlockDummyable.CORE_MIN) return null;

		return BlockDummyable.facingForMeta(meta - BlockDummyable.offset);
	}

	/**
	 * The true body's meta converts straight to the pose; placeholder cells get null.
	 *
	 * The offsets are one table shared by the whole family — the old four addTranslation lines: meta 12 half a block toward +X, 13 toward -X, 14 toward -Z, 15 toward +Z;
	 * the yaw is where families differ (each model is authored in its own factory orientation; copy the old lines in per family).
	 */
	public static Pose corePose(int meta, float yaw) {

		EnumFacing dir = coreFacing(meta);
		if(dir == null) return null;

		double x = 0D;
		double z = 0D;

		if(dir == EnumFacing.NORTH) x = 0.5D;
		else if(dir == EnumFacing.SOUTH) x = -0.5D;
		else if(dir == EnumFacing.WEST) z = -0.5D;
		else if(dir == EnumFacing.EAST) z = 0.5D;

		return new Pose(x, 0D, z, yaw);
	}
}
