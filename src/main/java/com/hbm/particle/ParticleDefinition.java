package com.hbm.particle;

public class ParticleDefinition {

	// the particle texture's grid count across/atop the whole atlas
	public int sheetWidth = 1;
	public int sheetHeight = 1;
	// color and size both lerp linearly with age
	public int startTint = 0xffffff;
	public int endTint = 0xffffff;
	public double startScale = 1.0D;
	public double endScale = 1.0D;
	// lifetime uniform-random over [minAge, maxAge]
	public int minAge = 100;
	public int maxAge = 120;

	// whether blending should be on (the renderer decides; the class only carries data)
	public boolean doesBlend = false;

	public ParticleDefinition setSize(int width, int height) {
		this.sheetWidth = width;
		this.sheetHeight = height;
		return this;
	}

	public ParticleDefinition setTint(int tint) {
		return this.setTint(tint, tint);
	}

	public ParticleDefinition setTint(int start, int end) {
		this.startTint = start;
		this.endTint = end;
		return this;
	}

	public ParticleDefinition setScale(double tint) {
		return this.setScale(tint, tint);
	}

	public ParticleDefinition setScale(double start, double end) {
		this.startScale = start;
		this.endScale = end;
		return this;
	}
}
