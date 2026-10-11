package com.hbm.render.block.ct;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * One fragment: a UV rectangle on some sprite.
 *
 * The old version split this across two classes — IconCT played "a sub-rectangle of an IIcon" while CTStitchReceiver
 * gathered the 20 pieces into a cache in postStitch. This version has no IIcon interface, and sprites are no longer registered by blocks:
 * the model names them in IModel#getTextures, and once the baker stitches the atlas it hands the sprites back (see ModelCT).
 * So what's left here is "given sprite and fragment number, compute that rectangle", with the cutting rules identical to the old version line for line.
 */
public final class CTFragment {

	public final TextureAtlasSprite sprite;
	public final float minU;
	public final float maxU;
	public final float minV;
	public final float maxV;

	private CTFragment(TextureAtlasSprite sprite, float minU, float maxU, float minV, float maxV) {
		this.sprite = sprite;
		this.minU = minU;
		this.maxU = maxU;
		this.minV = minV;
		this.maxV = maxV;
	}

	/** All 20 fragments computed in one go: f cuts the base, c/j/h/v cut the connective sheet, same branching as the old fragCache */
	public static CTFragment[] build(TextureAtlasSprite base, TextureAtlasSprite connected) {

		CTFragment[] fragments = new CTFragment[CT.FRAGMENT_COUNT];

		for(int i = 0; i < fragments.length; i++) {
			fragments[i] = of(i < 4 ? base : connected, i);
		}

		return fragments;
	}

	public static CTFragment of(TextureAtlasSprite parent, int type) {

		/* f fragments take a 2×2 of the base, the other four classes a 4×4 of the connective sheet, so each cell's edge divides by its own share */
		int sub = (type < 4) ? 2 : 4;
		float lenU = (parent.getMaxU() - parent.getMinU()) / sub;
		float lenV = (parent.getMaxV() - parent.getMinV()) / sub;

		float du = parent.getMinU();
		float dv = parent.getMinV();

		// First land on the whole block (coarse position)
		if(CT.isV(type) || CT.isJ(type)) {
			du += lenU * 2;
		}
		if(CT.isH(type) || CT.isJ(type)) {
			dv += lenV * 2;
		}

		// Then land on the sub-block (fine position)
		if(CT.isR(type)) {
			du += lenU;
		}
		if(CT.isB(type)) {
			dv += lenV;
		}

		return new CTFragment(parent, du, du + lenU, dv, dv + lenV);
	}
}
