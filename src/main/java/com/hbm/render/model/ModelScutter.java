package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * The scutterfish's shape: a fish assembled from named parts, fins and tail wobbling out of phase with the swim.
 *
 * The model still loads through the named-parts path (the .obj carries o group names); the only entry is ResourceManager.getModel,
 * parsing on first actual use, not at class init — the old ResourceManager.scutterfish-style
 * static field parsed the whole OBJ the moment the class loaded; this version is lazy. After a resource reload ResourceManager
 * clears its cache, so this fetches fresh every frame instead of caching it in a static field.
 *
 * GL calls all go through GlStateManager. The old fin sway used glRotated (double precision); this version only has the float
 * rotate, but the angles are tens of degrees at most — the truncation is invisible; sway values still compute in double then narrow, old-style.
 */
public class ModelScutter extends ModelBase {

	private static final String MODEL = "models/mobs/scutterfish.obj";

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		IModelCustomNamed scutterfish = ResourceManager.getModel(MODEL);

		GlStateManager.pushMatrix();
		{
			GlStateManager.rotate(180.0F, 0, 0, 1);
			GlStateManager.translate(0, -1.5F, 0);

			// One wave delayed 0.2 phases nose to tail; the four staggered segments are the slither of swimming.
			// The limbSwing here is the accumulated swing the render layer interpolated by partialTicks before passing in
			double cy0 = Math.sin(limbSwing % (Math.PI * 2));
			double cy1 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.2);
			double cy2 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.4);
			double cy3 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.6);

			scutterfish.renderPart("body");

			// head
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate(headPitch, 1, 0, 0);
				GlStateManager.rotate(netHeadYaw, 0, 1, 0);
				scutterfish.renderPart("head");
			}
			GlStateManager.popMatrix();

			// Three fins per side, left and right in anti-phase
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate((float) (cy0 * 20), 0, 0, 1);
				scutterfish.renderPart("leftfin1");
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate((float) (cy0 * -20), 0, 0, 1);
				scutterfish.renderPart("rightfin1");
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate((float) (cy1 * 20), 0, 0, 1);
				scutterfish.renderPart("leftfin2");
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate((float) (cy1 * -20), 0, 0, 1);
				scutterfish.renderPart("rightfin2");
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate((float) (cy2 * 20), 0, 0, 1);
				scutterfish.renderPart("leftfin3");
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate((float) (cy2 * -20), 0, 0, 1);
				scutterfish.renderPart("rightfin3");
			}
			GlStateManager.popMatrix();

			// Tail: the meaty segment and the thin one each lag another beat
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate((float) (cy1 * 10), 0, 0, 1);
				GlStateManager.rotate((float) (cy2 * -5), 1, 0, 0);
				scutterfish.renderPart("tailmeat");
				GlStateManager.rotate((float) (cy3 * -5), 1, 0, 0);
				scutterfish.renderPart("tailthin");
			}
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}
}
