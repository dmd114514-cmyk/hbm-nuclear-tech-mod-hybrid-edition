package com.hbm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * Chained chat component building.
 *
 * 1.12.2 renamed the whole Chat component family to Text/Style (EnumChatFormatting becoming TextFormatting
 * is part of that same renaming), and the style accessor went from getChatStyle to getStyle.
 * The recursive recolor stays: it must cover every child already attached, not just the last segment.
 */
public class ChatBuilder {

	private TextComponentString text;
	private TextComponentBase last;

	private ChatBuilder(String text) {
		this.text = new TextComponentString(text);
		this.last = this.text;
	}

	public static ChatBuilder start(String text) {
		return new ChatBuilder(text);
	}

	public static ChatBuilder startTranslation(String text, Object... o) {
		return new ChatBuilder("").nextTranslation(text, o);
	}

	public ChatBuilder next(String text) {
		TextComponentString append = new TextComponentString(text);
		this.last.appendSibling(append);
		this.last = append;
		return this;
	}

	public ChatBuilder nextTranslation(String text, Object... o) {
		TextComponentTranslation append = new TextComponentTranslation(text, o);
		this.last.appendSibling(append);
		this.last = append;
		return this;
	}

	/** Colors only the last appended segment */
	public ChatBuilder color(TextFormatting format) {
		this.last.getStyle().setColor(format);
		return this;
	}

	/** Recursively colors every component under the root */
	public ChatBuilder colorAll(TextFormatting format) {

		List<Object> queue = new ArrayList<>();
		queue.add(text);

		ListIterator<Object> it = queue.listIterator();

		while(it.hasNext()) {
			ITextComponent component = (ITextComponent) it.next();
			Style style = component.getStyle();
			style.setColor(format);
			for(ITextComponent sibling : component.getSiblings()) queue.add(sibling);
		}
		return this;
	}

	public TextComponentString flush() {
		return this.text;
	}
}
