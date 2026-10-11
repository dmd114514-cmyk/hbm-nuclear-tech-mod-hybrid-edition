package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityPigeon;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelPigeon;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * The pigeon's look: the body texture plus {@link ModelPigeon}; the only extra move is interpolating the "landing wobble" pair
 * and handing it to the render base as an overall rotation — the old algorithm ({@code (sin(fallTime)+1)*dest}) is unchanged, word for word.
 *
 * Two deltas:
 * - the old one was a bare {@code RenderLiving} with two {@code getEntityTexture}/{@code handleRotationFloat} overloads, each
 *   "take an Entity first, forward to the EntityPigeon one". This version pins the generic to {@code EntityPigeon},
 *   leaving only the entity-taking one; the forwarding shell goes.
 * - the constructor wants a {@code RenderManager} (the old one grabbed a static field itself), so {@code ModelPigeon} and the 0.3F shadow
 *   keep their old values, with the manager passed by the registry.
 * - {@code MathHelper} lives under {@code net.minecraft.util.math} in this version.
 */
public class RenderPigeon extends RenderLiving<EntityPigeon> {

	private static final ResourceLocation PIGEON = new ResourceLocation(RefStrings.MODID, "textures/entity/pigeon.png");

	public RenderPigeon(RenderManager renderManager, ModelPigeon model) {
		super(renderManager, model, 0.3F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPigeon entity) {
		return PIGEON;
	}

	@Override
	protected float handleRotationFloat(EntityPigeon entity, float partialTicks) {
		float f1 = entity.prevFallTime + (entity.fallTime - entity.prevFallTime) * partialTicks;
		float f2 = entity.prevDest + (entity.dest - entity.prevDest) * partialTicks;
		return (MathHelper.sin(f1) + 1.0F) * f2;
	}
}
