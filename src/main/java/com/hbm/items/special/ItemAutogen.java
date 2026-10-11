package com.hbm.items.special;

import java.util.HashMap;
import java.util.Locale;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.IMultiModelItem;
import com.hbm.items.ItemBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.item.ItemStack;

/**
 * The "one shape, one subtype per material" items — ingots, dusts, plates, wires, pipes all ride it.
 *
 * The old version minted icons at runtime: remapping one grayscale base into an independent sprite per material color.
 * 1.12.2 does the same (the base carries its own shading; an ItemColors flat tint would flatten it, so no tint shortcut),
 * with registration moved from `registerIcons` to `TextureStitchEvent.Pre`, landed by {@link ItemAutogenSprites}.
 *
 * meta is the **material id**, not an enum ordinal; so per-meta model names spread by the material table, sparse spots left null.
 */
public class ItemAutogen extends ItemBase implements IMultiModelItem {

	protected MaterialShapes shape;

	private final HashMap<NTMMaterial, String> textureOverrides = new HashMap<>();
	private String overrideUnlocalizedName = null;

	public ItemAutogen(MaterialShapes shape) {
		this.setHasSubtypes(true);
		this.shape = shape;
	}

	/** Assigns an existing texture to specific materials, skipping the remap */
	public ItemAutogen aot(NTMMaterial mat, String tex) {
		textureOverrides.put(mat, tex);
		return this;
	}

	/** Swaps the translation-key stem (called oun in the old code) */
	public ItemAutogen oun(String overrideUnlocalizedName) {
		this.overrideUnlocalizedName = overrideUnlocalizedName;
		return this;
	}

	public MaterialShapes getShape() {
		return shape;
	}

	/** Whether the material belongs to this shape and registered for autogen */
	public boolean autogens(NTMMaterial mat) {
		return mat != null && shape != null && mat.autogen.contains(shape);
	}

	public String getOverrideTexture(NTMMaterial mat) {
		return textureOverrides.get(mat);
	}

	/** Subtype model names indexed by material id; non-member materials stay null (skipped at registration) */
	@Override
	public String[] subtypeModels() {

		int max = 0;
		for(NTMMaterial mat : Mats.orderedList) if(mat.id > max) max = mat.id;

		String[] names = new String[max + 1];

		for(NTMMaterial mat : Mats.orderedList) {
			if(autogens(mat)) names[mat.id] = modelName(this, mat);
		}

		return names;
	}

	/**
	 * The model file name: `<item registry name>_<material id>`.
	 *
	 * Deliberately the **id**, not the material name — the id is offline-computable (`Mats` builds it as `_VS/_AS/_ES + NN`),
	 * while the name would have to be dug out of another class's DictFrame constants. Models are static assets that must exist at build time,
	 * hence the offline-computable choice.
	 */
	public static String modelName(ItemAutogen item, NTMMaterial mat) {
		return item.getRegistryName().getResourcePath() + "_" + mat.id;
	}

	/**
	 * The atlas sprite name for a meta.
	 * Overridden textures use the real texture; otherwise it's a remap sprite — named `<item name>-<material id>`,
	 * that dash being the placeholder-suffix separator, cut at load, with the base still `<item name>.png`.
	 */
	public static String spriteName(ItemAutogen item, NTMMaterial mat) {

		String override = item.getOverrideTexture(mat);
		if(override != null) return "hbm:items/" + override;

		return "hbm:items/" + item.getRegistryName().getResourcePath() + "-" + mat.id;
	}

	/*The remap sprite's base image when no texture override exists */
	public static String baseTexture(ItemAutogen item) {
		return "hbm:items/" + item.getRegistryName().getResourcePath();
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return overrideUnlocalizedName != null ? "item." + overrideUnlocalizedName : super.getUnlocalizedName(stack);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		NTMMaterial mat = Mats.matById.get(stack.getItemDamage());
		if(mat == null) return "UNDEFINED";

		String matName = I18nUtil.resolveKey(mat.getUnlocalizedName());
		return I18nUtil.resolveKey(this.getUnlocalizedNameInefficiently(stack) + ".name", matName);
	}
}
