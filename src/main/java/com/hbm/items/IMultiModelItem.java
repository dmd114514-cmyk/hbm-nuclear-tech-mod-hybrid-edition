package com.hbm.items;

/**
 * For when subtype model names can't be derived.
 *
 * Most subtyped items' model names derive from the enum (`<name>_<enum lowercase>`), but a few never line up —
 * like starmetal's four hand-named star textures, or a whole family sharing one base told apart only by tint.
 * Those items report their own model names; the registrar hangs them as listed.
 */
public interface IMultiModelItem {

	/** The model name per meta; length must cover this item's subtype count */
	String[] subtypeModels();
}
