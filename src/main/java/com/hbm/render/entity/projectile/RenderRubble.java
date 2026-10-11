package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntityRubble;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelRubble;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

/**
 * How the rubble draws: a small rolling cube model, textured by its source block — smash stone, wear stone.
 *
 * The texture lookup changed from the old approach: the old used RenderBlocks + getBlockIconFromSideAndMetadata
 * to get the icon name and stitch a path; both are gone here, replaced by asking the block model for its sprite and building the texture path
 * from the sprite's atlas name (the atlas name already carries the "blocks/" segment in 1.12.2, so one stitching step fewer than before).
 * Falls back to the old two safety nets (minecraft:stone) when nothing comes back. GL all GlStateManager.
 *
 * Honest note: the texture name format on this path was never verified in-game (rubble needs a producer before anyone sees it);
 */
public class RenderRubble extends Render<EntityRubble> {

	private static final ResourceLocation FALLBACK = new ResourceLocation(RefStrings.MODID + ":textures/models/ModelRubbleScrap.png");
	private static final ResourceLocation STONE = new ResourceLocation("minecraft", "textures/blocks/stone.png");

	private final ModelRubble model = new ModelRubble();

	public RenderRubble(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityRubble rubble, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.scale(1.0F, 1.0F, 1.0F);
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.rotate(((rubble.ticksExisted + partialTicks) % 360) * 10, 1, 1, 1);

		try {
			Block block = Block.getBlockById(rubble.getBlockId());
			IBlockState state = block.getStateFromMeta(rubble.getBlockMeta());
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);

			this.bindTexture(sprite == null ? STONE : textureOf(sprite.getIconName()));
			this.model.renderAll(0.0625F);
		} catch(Exception ex) {
			// The old version swallowed here too: a missing texture on one speck of dust isn't worth crashing the renderer
			this.bindTexture(FALLBACK);
			this.model.renderAll(0.0625F);
		}

		GlStateManager.popMatrix();
	}

	/** Atlas names look like hbm:blocks/xxx or minecraft:blocks/xxx, mapping to textures/<second half>.png */
	private static ResourceLocation textureOf(String iconName) {
		if(iconName == null || iconName.isEmpty()) return STONE;
		String[] split = iconName.split(":");
		String prefix = split.length == 2 ? split[0] : "minecraft";
		String suffix = split.length == 2 ? split[1] : iconName;
		return new ResourceLocation(prefix + ":textures/" + suffix + ".png");
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRubble rubble) {
		return FALLBACK;
	}
}
