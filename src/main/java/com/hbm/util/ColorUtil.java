package com.hbm.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Color conversion and "pull a representative color out of a texture".
 *
 * The color-extraction half had to reroute in 1.12.2: the old version read atlas pixels through IIcon, and with the atlas gone
 * it reads the raw png out of the resource pack by item registry name — these methods only feed GUI color swatches anyway,
 * a one-shot cost, never on the hot path.
 */
public class ColorUtil {

	private ColorUtil() { }

	/** The item texture's raw image. From 1.12.2 the atlas exposes no pixels, so the pack's png is read */
	@SideOnly(Side.CLIENT)
	public static BufferedImage getImageFromStack(ItemStack stack) throws IOException {
		ResourceLocation name = stack.getItem().getRegistryName();
		if(name == null) throw new IOException("Item has no registry name; no texture to fetch");

		ResourceLocation texture = new ResourceLocation(name.getResourceDomain(),
				"textures/items/" + name.getResourcePath() + ".png");
		IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(texture);
		return ImageIO.read(resource.getInputStream());
	}

	@SideOnly(Side.CLIENT)
	public static int getAverageColorFromStack(ItemStack stack) {

		try {
			BufferedImage tex = getImageFromStack(stack);

			int r = 0, g = 0, b = 0, pixels = 0;

			for(int i = 0; i < tex.getWidth(); i++) {
				for(int j = 0; j < tex.getHeight(); j++) {
					Color pixel = new Color(tex.getRGB(i, j));
					// only opaque pixels count, or transparent edges gray the color out
					if(pixel.getAlpha() == 255) {
						r += pixel.getRed();
						g += pixel.getGreen();
						b += pixel.getBlue();
						pixels++;
					}
				}
			}

			if(pixels == 0) return 0xFFFFFF;
			return ((r / pixels) << 16) | ((g / pixels) << 8) | (b / pixels);

		} catch(Exception ex) {
			return 0xFFFFFF;
		}
	}

	@SideOnly(Side.CLIENT)
	public static int getMedianBrightnessColorFromStack(ItemStack stack) {

		try {
			BufferedImage tex = getImageFromStack(stack);

			HashMap<Integer, Color> brightMap = new HashMap<>();
			List<Integer> brightnesses = new ArrayList<>();

			for(int i = 0; i < tex.getWidth(); i++) {
				for(int j = 0; j < tex.getHeight(); j++) {
					Color pixel = new Color(tex.getRGB(i, j));
					int brightness = pixel.getRed() * pixel.getRed() + pixel.getGreen() * pixel.getGreen() + pixel.getBlue() * pixel.getBlue();
					brightnesses.add(brightness);
					brightMap.put(brightness, pixel); // brightnesses collide, but specific hues don't matter anyway
				}
			}

			Collections.sort(brightnesses);
			return brightMap.get(brightnesses.get(brightnesses.size() / 2)).getRGB();

		} catch(Exception ex) {
			return 0xFFFFFF;
		}
	}

	public static int color(int r, int g, int b) {
		return ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
	}

	public static int ir(int color) { return (color & 0xff0000) >> 16; }
	public static int ig(int color) { return (color & 0x00ff00) >> 8; }
	public static int ib(int color) { return color & 0x0000ff; }

	public static float fr(int color) { return ir(color) / 255F; }
	public static float fg(int color) { return ig(color) / 255F; }
	public static float fb(int color) { return ib(color) / 255F; }

	/** "Vivid enough": too dark or near-gray is rejected; used to pick hazard-label colors */
	public static boolean isColorColorful(int hex) {
		Color color = new Color(hex);
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
		return hsb[1] > 0.25 && hsb[2] > 0.25;
	}

	/** Drags the brightest channel up to limit, the rest following proportionally — a uniform brighten */
	public static int amplifyColor(int hex, int limit) {
		Color color = new Color(hex);
		int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
		int max = Math.max(Math.max(1, r), Math.max(g, b));
		return new Color(r * limit / max, g * limit / max, b * limit / max).getRGB();
	}

	public static int amplifyColor(int hex) {
		return amplifyColor(hex, 255);
	}

	/** Pulls toward white proportionally; percent=1 guarantees pure white */
	public static int lightenColor(int hex, double percent) {
		Color color = new Color(hex);
		int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
		return new Color(
				(int) (r + (255 - r) * percent),
				(int) (g + (255 - g) * percent),
				(int) (b + (255 - b) * percent)).getRGB();
	}

	/** Converts to HSB and takes the brightness component */
	public static double getColorBrightness(int hex) {
		Color color = new Color(hex);
		return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3])[2];
	}

	public static final HashMap<String, Integer> nameToColor = new HashMap<String, Integer>() {{
		put("black", 1973019);
		put("red", 11743532);
		put("green", 3887386);
		put("brown", 5320730);
		put("blue", 2437522);
		put("purple", 8073150);
		put("cyan", 2651799);
		put("silver", 11250603);
		put("gray", 4408131);
		put("pink", 14188952);
		put("lime", 4312372);
		put("yellow", 14602026);
		put("light_blue", 6719955);
		put("magenta", 12801229);
		put("orange", 15435844);
		put("white", 15790320);
	}};

	/** The color value for a dyeable item; white when it isn't a dye */
	public static int getColorFromDye(ItemStack stack) {
		if(stack == null || stack.isEmpty() || stack.getItem() != net.minecraft.init.Items.DYE) return 0xFFFFFF;
		return EnumDyeColor.byDyeDamage(stack.getMetadata()).getColorValue();
	}
}
