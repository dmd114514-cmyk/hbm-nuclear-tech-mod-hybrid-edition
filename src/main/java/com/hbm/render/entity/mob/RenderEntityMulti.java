package com.hbm.render.entity.mob;

import java.util.Locale;

import com.hbm.entity.mob.IEntityEnumMulti;
import com.hbm.lib.RefStrings;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

/**
 * A shared renderer for multi-form mobs: the constructor lays out a column of textures by enum constant, and drawing picks the one matching the entity's getEnum().
 *
 * Texture path rule as before: textures/entity/<enum class lowercase>_<enum name lowercase>.png.
 * Only two shape changes: the constructor wants a RenderManager (RenderLiving's fixed slot),
 * and the generic is declared EntityLiving here (the old one was a bare RenderLiving + runtime casts);
 * the form enum is still read live off IEntityEnumMulti — whoever uses it implements that port.
 */
public class RenderEntityMulti extends RenderLiving<EntityLiving> {

	private final ResourceLocation[] textures;

	@SuppressWarnings("rawtypes")
	public RenderEntityMulti(RenderManager renderManager, ModelBase model, Class<? extends Enum> theEnum, float shadowSize) {
		super(renderManager, model, shadowSize);

		Enum[] order = theEnum.getEnumConstants();
		textures = new ResourceLocation[order.length];

		for(int i = 0; i < order.length; i++) {
			textures[i] = new ResourceLocation(RefStrings.MODID, "textures/entity/"
					+ theEnum.getSimpleName().toLowerCase(Locale.US) + "_" + order[i].name().toLowerCase(Locale.US) + ".png");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ResourceLocation getEntityTexture(EntityLiving entity) {
		Enum entityEnum = ((IEntityEnumMulti) entity).getEnum();
		return textures[entityEnum.ordinal()];
	}
}
