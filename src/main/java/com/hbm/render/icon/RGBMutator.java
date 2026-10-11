package com.hbm.render.icon;

import java.awt.image.BufferedImage;

/** The pixel-rewrite hook at texture load: one mutator is one recoloring trick */
public interface RGBMutator {

	public void mutate(BufferedImage image, int frame, int frameCount);
}
