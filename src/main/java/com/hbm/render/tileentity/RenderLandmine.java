package com.hbm.render.tileentity;

import com.hbm.blocks.bomb.BlockLandmine;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import com.hbm.util.LoggingUtil;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

/**
 * How the landmine body draws: five kinds sharing one tile class, the one standing on the ground drawn here.
 *
 * Three conventions against the old version:
 *   · identifying "which mine" reads the block's own {@link BlockLandmine.Kind} (this tree's five write their differences on that table),
 *     while the old version compared block identity against static fields like {@code ModBlocks.mine_ap} — a comparison that ignores inheritance and isn't worth inviting back.
 *   · the model goes through ResourceManager.getModel lazily, paths and texture names held in this class (the old batch of
 *     ResourceManager.mine_* static fields is gone; reading disk at class init is not wanted this version).
 *   · heightmap reads: the old {@code World.getHeightValue(x, z)} is just {@code World.getHeight(x, z)} here, same criterion.
 *
 * The three texture rules stay as the old version wrote them: surface two cells above the mine's head means buried (stone texture), otherwise look at the biome —
 * snowy, dry-hot (temperature ≥ 1.5 and rainfall ≤ 0.1), grass for the rest. The frag mine borrows the AP mine's geometry as before, swapping only its own texture.
 *
 * One line still missing on the block side: the old {@code Landmine.getRenderType()} returned -1 (block body undrawn, all handed here),
 * while this version's {@link BlockLandmine} doesn't declare INVISIBLE yet ⇒ during the binding window you'll see block model and mine body stacked,
 */
@SideOnly(Side.CLIENT)
public class RenderLandmine extends TileEntitySpecialRenderer<TileEntityLandmine> {

	private static final String MODEL_AP = "models/bombs/ap_mine.obj";
	private static final String MODEL_MARELET = "models/bombs/marelet.obj";
	private static final String MODEL_FAT = "models/bombs/mine_fat.obj";
	private static final String MODEL_NAVAL = "models/bombs/naval_mine.obj";

	private static final ResourceLocation TEX_AP_STONE = bombTex("mine_ap_stone.png");
	private static final ResourceLocation TEX_AP_SNOW = bombTex("mine_ap_snow.png");
	private static final ResourceLocation TEX_AP_DESERT = bombTex("mine_ap_desert.png");
	private static final ResourceLocation TEX_AP_GRASS = bombTex("mine_ap_grass.png");
	private static final ResourceLocation TEX_MARELET = bombTex("mine_marelet.png");
	private static final ResourceLocation TEX_SHRAPNEL = bombTex("mine_shrapnel.png");
	private static final ResourceLocation TEX_FAT = bombTex("mine_fat.png");
	private static final ResourceLocation TEX_NAVAL = new ResourceLocation(RefStrings.MODID, "textures/models/NMine.png");

	private boolean announced = false;

	private static ResourceLocation bombTex(String name) {
		return new ResourceLocation(RefStrings.MODID, "textures/models/bombs/" + name);
	}

	@Override
	public void render(TileEntityLandmine te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		World world = te.getWorld();
		BlockPos pos = te.getPos();
		Block block = world.getBlockState(pos).getBlock();

		if(!(block instanceof BlockLandmine)) return;

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(x + 0.5D, y, z + 0.5D);
			GlStateManager.enableLighting();
			GlStateManager.disableCull();
			GlStateManager.enableRescaleNormal();

			// The whole family's models were baked facing "south"; squaring up takes a half turn
			GlStateManager.rotate(180F, 0F, 1F, 0F);

			draw((BlockLandmine) block, world, pos);

			GlStateManager.enableCull();
		}
		GlStateManager.popMatrix();

		if(!this.announced) {
			this.announced = true;
			LoggingUtil.info("[hbm] landmine body first frame drawn: {} at {}", ((BlockLandmine) block).kind.name(), pos);
		}
	}

	private void draw(BlockLandmine mine, World world, BlockPos pos) {

		switch(mine.kind) {
		case AP: {
			GlStateManager.scale(0.375D, 0.375D, 0.375D);
			GlStateManager.translate(0D, -0.0625D * 3.5D, 0D);

			this.bindTexture(pickApTexture(world, pos));
			ResourceManager.getModel(MODEL_AP).renderAll();
			break;
		}
		case HE: {
			GlStateManager.rotate(-90F, 0F, 1F, 0F);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			this.bindTexture(TEX_MARELET);
			ResourceManager.getModel(MODEL_MARELET).renderAll();
			GlStateManager.shadeModel(GL11.GL_FLAT);
			break;
		}
		case SHRAP: {
			GlStateManager.enableRescaleNormal();
			GlStateManager.scale(0.375D, 0.375D, 0.375D);
			GlStateManager.translate(0D, -0.0625D * 3.5D, 0D);
			this.bindTexture(TEX_SHRAPNEL);
			// Same geometry as the AP mine — the old version borrowed it just like this; copied
			ResourceManager.getModel(MODEL_AP).renderAll();
			break;
		}
		case FAT: {
			GlStateManager.scale(0.25D, 0.25D, 0.25D);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			this.bindTexture(TEX_FAT);
			ResourceManager.getModel(MODEL_FAT).renderAll();
			GlStateManager.shadeModel(GL11.GL_FLAT);
			break;
		}
		case NAVAL: {
			GlStateManager.translate(0D, 0.5D, 0D);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			this.bindTexture(TEX_NAVAL);
			ResourceManager.getModel(MODEL_NAVAL).renderAll();
			GlStateManager.shadeModel(GL11.GL_FLAT);
			break;
		}
		}
	}

	/** Buried deep = the underground stone look; otherwise the biome picks among snowy, dry-hot and grass */
	private static ResourceLocation pickApTexture(World world, BlockPos pos) {

		if(world.getHeight(pos.getX(), pos.getZ()) > pos.getY() + 2) return TEX_AP_STONE;

		Biome biome = world.getBiome(pos);

		if(biome.getEnableSnow()) return TEX_AP_SNOW;
		if(biome.getTemperature(pos) >= 1.5F && biome.getRainfall() <= 0.1F) return TEX_AP_DESERT;

		return TEX_AP_GRASS;
	}
}
