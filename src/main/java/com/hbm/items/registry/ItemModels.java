package com.hbm.items.registry;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.IColoredItem;
import com.hbm.items.IMultiModelItem;
import com.hbm.items.ItemEnumMulti;
import com.hbm.registry.HbmRegistry;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The one-shot registration door for item models and colors.
 *
 * 1.12.2 split "what an item looks like" into two concerns: model JSON decides which texture,
 * `ItemColors` decides what color layers over it (the replacement for getColorFromItemStack).
 *
 * **The two have different timings; don't mix them**: model registration can hang in preInit; color registration **cannot** happen there —
 * `Minecraft.itemColors` is built in `Minecraft.init()`, after mod preInit,
 * so fetching it here is guaranteed null. This class only records items awaiting colors, and the real registration goes to
 * {@link ItemColorHandler} (hung on Forge's `ColorHandlerEvent.Item`, ready only then).
 */
@SideOnly(Side.CLIENT)
public final class ItemModels {

	private static final List<Item> COLORED = new ArrayList<>();

	private ItemModels() { }

	/** For ItemColorHandler to read; client-only, and only inside the color event */
	static List<Item> colored() {
		return COLORED;
	}

	public static void register(Item item, String modelName, int meta) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(HbmRegistry.rl(modelName), "inventory"));
	}

	/** Hangs all of an item's subtype models per the rules it reports, and records the ones needing colors */
	public static void registerAll(Item item) {
		if(item == null || item.getRegistryName() == null) return;
		String name = item.getRegistryName().getResourcePath();

		if(item instanceof IMultiModelItem) {
			// The name table spans by meta index; sparse spots (materials without this shape in autogen) come out null and get skipped
			String[] names = ((IMultiModelItem) item).subtypeModels();
			for(int meta = 0; meta < names.length; meta++) {
				if(names[meta] != null) register(item, names[meta], meta);
			}
		} else if(item instanceof ItemEnumMulti) {
			// Those with their own textures hang as `<name>_<enum lowercase>`; single-texture ones (told apart by tint) all point at the same model
			ItemEnumMulti multi = (ItemEnumMulti) item;
			for(int meta = 0; meta < multi.subtypeCount(); meta++) {
				register(item, multi.usesSubtypeTextures() ? name + "_" + multi.subtypeSuffix(meta) : name, meta);
			}
		} else {
			register(item, name, 0);
		}

		if(item instanceof IColoredItem) COLORED.add(item);
	}

	/**
	 * For items whose "meta is a sparse id but the look is one texture": point 0..count-1 all at the same model.
	 * Fluid cells are this kind — meta is the fluid id, looks come from mask tinting, one model only.
	 */
	public static void registerRange(Item item, int count) {
		if(item == null || item.getRegistryName() == null) return;

		String name = item.getRegistryName().getResourcePath();
		for(int meta = 0; meta < count; meta++) register(item, name, meta);

		if(item instanceof IColoredItem) COLORED.add(item);
	}
}
