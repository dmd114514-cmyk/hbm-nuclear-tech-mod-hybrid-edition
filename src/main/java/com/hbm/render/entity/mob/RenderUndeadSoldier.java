package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityUndeadSoldier;
import com.hbm.render.model.ModelSkeletonNT;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The undead soldier's rendering: switches between the zombie and skeleton models per the synced form, texture following.
 *
 * The old version wrote both mainModel and modelBipedMain, but modelBipedMain is final in this version,
 * so only mainModel swaps — that's the one actually drawn; modelBipedMain stays for vanilla's held-item arm pose.
 */
public class RenderUndeadSoldier extends RenderBiped<EntityUndeadSoldier> {

	private static final ResourceLocation TEXTURE_ZOMBIE = new ResourceLocation("textures/entity/zombie/zombie.png");
	private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation("textures/entity/skeleton/skeleton.png");

	private static final ModelBiped MODEL_ZOMBIE = new ModelZombie();
	private static final ModelBiped MODEL_SKELETON = new ModelSkeletonNT();

	public RenderUndeadSoldier(RenderManager renderManager) {
		super(renderManager, MODEL_ZOMBIE, 0.5F);
	}

	@Override
	protected void preRenderCallback(EntityUndeadSoldier entity, float interp) {
		byte type = entity.getDataManager().get(EntityUndeadSoldier.DW_TYPE);
		if(type == EntityUndeadSoldier.TYPE_ZOMBIE) this.mainModel = MODEL_ZOMBIE;
		if(type == EntityUndeadSoldier.TYPE_SKELETON) this.mainModel = MODEL_SKELETON;
	}

	// The form is only ever 0 or 1; the old return null would bind the texture to nothing in this version, so it lands on zombie
	@Override
	protected ResourceLocation getEntityTexture(EntityUndeadSoldier entity) {
		byte type = entity.getDataManager().get(EntityUndeadSoldier.DW_TYPE);
		return type == EntityUndeadSoldier.TYPE_SKELETON ? TEXTURE_SKELETON : TEXTURE_ZOMBIE;
	}
}
