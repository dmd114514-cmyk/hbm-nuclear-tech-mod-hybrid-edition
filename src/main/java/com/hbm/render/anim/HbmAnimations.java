package com.hbm.render.anim;

import com.hbm.interfaces.NotableComments;
import com.hbm.util.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

@NotableComments
public class HbmAnimations {

	// One shared animation timer for the whole client had a flaw: swap guns mid-reload and the other gun plays the reload too.
	// So each hotbar slot gets its own timing here, 8 parallel tracks per slot. Moving a gun with a playing animation to another slot cuts the animation outright.
	public static final Animation[][] hotbar = new Animation[9][8];

	/** The keyframe animation system mostly for guns; don't confuse it with animloader's model animations. */
	public static class Animation {

		// The animation slot's "name": if the held item's key doesn't match, the animation counts as canceled
		public String key;
		// When the animation started
		public long startMillis;
		// The animation bus
		public BusAnimation animation;
		// When set, the timer running out doesn't cancel the animation; it freezes on the last frame awaiting the next start
		public boolean holdLastFrame = false;

		public Animation(String key, long startMillis, BusAnimation animation) {
			this.key = key;
			this.startMillis = startMillis;
			this.animation = animation;
		}

		public Animation(String key, long startMillis, BusAnimation animation, boolean holdLastFrame) {
			this(key, startMillis, animation);
			this.holdLastFrame = holdLastFrame;
		}
	}

	public static Animation getRelevantAnim() { return getRelevantAnim(0); }
	public static Animation getRelevantAnim(int index) {

		EntityPlayer player = Minecraft.getMinecraft().player;
		int slot = player.inventory.currentItem;
		ItemStack stack = player.getHeldItemMainhand();

		if(stack.isEmpty())
			return null;

		if(slot < 0 || slot > 8) { //Abnormal hotbar with an odd slot count — probably looks wrong, but at least don't crash
			slot = Math.abs(slot) % 9;
		}

		if(hotbar[slot][index] == null)
			return null;

		if(hotbar[slot][index].key.equals(stack.getItem().getUnlocalizedName())) {
			return hotbar[slot][index];
		}

		return null;
	}

	public static double[] getRelevantTransformation(String bus) { return getRelevantTransformation(bus, 0); }
	public static double[] getRelevantTransformation(String bus, int index) {

		Animation anim = HbmAnimations.getRelevantAnim(index);

		if(anim != null) {

			BusAnimation buses = anim.animation;
			int millis = (int)(Clock.get_ms() - anim.startMillis);

			BusAnimationSequence seq = buses.getBus(bus);

			if(seq != null) {
				double[] trans = seq.getTransformation(millis);

				if(trans != null)
					return trans;
			}
		}

		return new double[] {
			0, 0, 0, // position
			0, 0, 0, // rotation
			1, 1, 1, // scale
			0, 0, 0, // offset
			0, 1, 2, // XYZ order
		};
	}

	public static void applyRelevantTransformation(String bus) { applyRelevantTransformation(bus, 0); }
	public static void applyRelevantTransformation(String bus, int index) {
		double[] transform = getRelevantTransformation(bus, index);
		int[] rot = new int[] { (int)transform[12], (int)transform[13], (int)transform[14] };

		GL11.glTranslated(transform[0], transform[1], transform[2]);
		GL11.glRotated(transform[3 + rot[0]], rot[0] == 0 ? 1 : 0, rot[0] == 1 ? 1 : 0, rot[0] == 2 ? 1 : 0);
		GL11.glRotated(transform[3 + rot[1]], rot[1] == 0 ? 1 : 0, rot[1] == 1 ? 1 : 0, rot[1] == 2 ? 1 : 0);
		GL11.glRotated(transform[3 + rot[2]], rot[2] == 0 ? 1 : 0, rot[2] == 1 ? 1 : 0, rot[2] == 2 ? 1 : 0);
		GL11.glTranslated(-transform[9], -transform[10], -transform[11]);
		GL11.glScaled(transform[6], transform[7], transform[8]);
	}

}
