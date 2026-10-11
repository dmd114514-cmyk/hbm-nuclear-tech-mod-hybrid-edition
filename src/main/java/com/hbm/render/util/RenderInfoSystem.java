package com.hbm.render.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.hbm.registry.IRegistrar;
import com.hbm.util.LoggingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/**
 * The on-screen info overlay: whoever wants a line pinned to the player's screen (cable ranging, event notices) pushes it here,
 * and the crosshair-tier overlay event draws it all, with timeout auto-fade.
 *
 * Only two pushes are public; the intake door is {@code Proxy#displayTooltip(String, int, int)} —
 * the server side is an empty stub and the client override routes here, so senders need no side checks.
 *
 * Three shape differences from the old version:
 * <ul>
 *   <li>Moving the inbox and cleanup no longer take a ClientTickEvent. That old tick did two things: merge the inbox into
 *       messages and kick out the expired; the render entry has to do both anyway, and once per frame is timelier than once per tick.
 *       The global-tick door ({@code IHbmTickReceiver}) has all three callbacks on the server, and client work
 *       was always going to hang its own vanilla events, so even that subscription is dropped here.</li>
 *   <li>The backing rectangle: the old {@code Tessellator.instance} stateful drawing (startDrawingQuads +
 *       addVertex) is gone; this version feeds {@code BufferBuilder.begin/pos/color/endVertex} vertex by vertex.
 *       Alpha blending stays SRC_ALPHA/ONE_MINUS_SRC_ALPHA, just via GlStateManager instead of raw GL11.</li>
 *   <li>No {@code Gui.icons} rebinding after drawing: the old line was paying back 1.7's debt of tessellator binding the full atlas,
 *       while this version's glyph texture is bound by FontRenderer itself and {@code Gui.icons} is private now. The alpha line
 *       {@code color + (alpha << 24 & -0xffffff)} simplifies, value-equal, to {@code color | (alpha << 24)}.</li>
 * </ul>
 *
 * The old version read position and offset from three config keys (tier + horizontal + vertical); the config face isn't here,
 * so the old defaults land as the three writable fields below; once those keys arrive they'll be assigned from there, drawing untouched.
 */
public class RenderInfoSystem implements IRegistrar {

	/** 0=top-left 1=top-right 2=middle-right 3=middle-left (the old four tiers, default top-left) */
	public static int positionMode = 0;
	/** Horizontal offset in pixels, used alongside positionMode */
	public static int offsetHorizontal = 0;
	/** Vertical offset in pixels, used alongside positionMode */
	public static int offsetVertical = 0;

	private static int nextID = 1000;
	private static boolean drawnOnce = false;
	private static final Map<Integer, InfoEntry> inbox = new HashMap<>();
	private static final Map<Integer, InfoEntry> messages = new HashMap<>();

	@Override
	public String id() {
		return "RenderInfoSystem";
	}

	/**
	 * The overlay and its pushes only mean anything on the client, so the hook borrows the registerModels() pass — the one step of the registration unit
	 * that runs client-only, sparing the shared ClientProxy any edits.
	 */
	@Override
	public void registerModels() {
		MinecraftForge.EVENT_BUS.register(RenderInfoSystem.class);
		LoggingUtil.info("[hbm] info overlay mounted: RenderGameOverlayEvent.Pre(CROSSHAIRS) + push(text, millis[, slot number])");
	}

	/**
	 * Intake and cleanup: done first in the per-frame render entry, so the standalone client tick is gone.
	 * <br><br>
	 * Expiry uses {@code values().removeIf}, not the old "snapshot the keys, walk by index while deleting, and walk the index back one
	 * after each removal" — that dance is convoluted, deleting from the live map while stepping by a snapshot, and takes a reader half a day to confirm nothing gets skipped.
	 */
	private static void advance() {

		messages.putAll(inbox);
		inbox.clear();

		long now = System.currentTimeMillis();
		messages.values().removeIf(entry -> entry.start + entry.millis < now);
	}

	@SubscribeEvent(receiveCanceled = true)
	public static void onOverlayRender(RenderGameOverlayEvent.Pre event) {

		if(event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
			return;

		advance();

		if(messages.isEmpty())
			return;

		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution resolution = event.getResolution();

		List<InfoEntry> entries = new ArrayList<>(messages.values());
		Collections.sort(entries);

		int longest = 0;

		for(InfoEntry entry : entries) {
			int length = mc.fontRenderer.getStringWidth(entry.text);

			if(length > longest)
				longest = length;
		}

		int pX = (positionMode == 0 ? 15 : positionMode == 1 ? resolution.getScaledWidth() - longest - 15 : positionMode == 2 ? resolution.getScaledWidth() / 2 + 7 : resolution.getScaledWidth() / 2 - longest - 6) + offsetHorizontal;
		int pZ = (positionMode == 0 || positionMode == 1 ? 15 : resolution.getScaledHeight() / 2 + 7) + offsetVertical;

		int side = pX + 5 + longest;
		int bottom = entries.size() * 10 + pZ + 2;

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture2D();

		// Translucent backing block: the old average-of-four-corners lighting isn't here — the overlay was never affected by scene light
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(pX - 5, pZ - 5, 0).color(0.25F, 0.25F, 0.25F, 0.5F).endVertex();
		buffer.pos(pX - 5, bottom, 0).color(0.25F, 0.25F, 0.25F, 0.5F).endVertex();
		buffer.pos(side, bottom, 0).color(0.25F, 0.25F, 0.25F, 0.5F).endVertex();
		buffer.pos(side, pZ - 5, 0).color(0.25F, 0.25F, 0.25F, 0.5F).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();

		int off = 0;
		long now = System.currentTimeMillis();

		// One self-check is enough: the hook log only proves the bus accepted the door; reaching this line means the overlay actually runs in the frame
		if(!drawnOnce) {
			drawnOnce = true;
			LoggingUtil.info("[hbm] info overlay first frame drawn: {} lines, position tier {}, longest {} px", entries.size(), positionMode, longest);
		}

		for(InfoEntry entry : entries) {

			int elapsed = (int) (now - entry.start);
			// Linear from 510 down to 0, then clamped to [5,255]: first half fully opaque, second half fading out
			int alpha = Math.max(Math.min(510 * (entry.millis - elapsed) / entry.millis, 255), 5);

			mc.fontRenderer.drawString(entry.text, pX, pZ + off, entry.color | alpha << 24);

			off += 10;
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	/** Picks an unused slot automatically, no collisions with anyone else */
	public static void push(InfoEntry entry) {
		push(entry, nextID++);
	}

	/**
	 * Named slot: the same number keeps only the latest line (old convention: the range is big enough that a collision just means the newer line bumps the older).
	 * <br><br>
	 * Default duration is 3 seconds, matching the old single-arg constructor; {@code displayTooltip}'s three existing calls all pass a duration explicitly.
	 */
	public static void push(InfoEntry entry, int id) {
		inbox.put(id, entry);
	}

	public static void push(String text, int millis) {
		push(new InfoEntry(text, millis));
	}

	public static void push(String text, int millis, int id) {
		push(new InfoEntry(text, millis), id);
	}

	/** One line waiting to show: text, color, start time, lifetime */
	public static class InfoEntry implements Comparable<InfoEntry> {

		private final String text;
		private int color = 0xffffff;
		private final long start;
		private final int millis;

		public InfoEntry(String text) {
			this(text, 3000);
		}

		public InfoEntry(String text, int millis) {
			this.text = text;
			this.millis = millis;
			this.start = System.currentTimeMillis();
		}

		public InfoEntry withColor(int color) {
			this.color = color;
			return this;
		}

		/** Shorter lifetimes sort first, ties by arrival order (the old version only compared duration; kept as-is) */
		@Override
		public int compareTo(InfoEntry other) {
			return this.millis < other.millis ? -1 : this.millis > other.millis ? 1 : 0;
		}
	}
}
