package com.hbm.inventory.material;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Wraps the materials currently hanging on a DictFrame; vanilla's too.
 */
public class NTMMaterial {

	public final int id;
	public String[] names;
	public Set<MaterialShapes> autogen = new HashSet<>();
	public Set<MatTraits> traits = new HashSet<>();
	public SmeltingBehavior smeltable = SmeltingBehavior.NOT_SMELTABLE;
	public int solidColorLight = 0xFF4A00;
	public int solidColorDark = 0x802000;
	public int moltenColor = 0xFF4A00;
	
	public NTMMaterial smeltsInto;
	public int convIn;
	public int convOut;
	
	public NTMMaterial(int id, DictFrame dict) {
		
		this.names = dict.mats;
		this.id = id;
		
		this.smeltsInto = this;
		this.convIn = 1;
		this.convOut = 1;
		
		for(String name : dict.mats) {
			Mats.matByName.put(name, this);
		}
		
		Mats.orderedList.add(this);
		Mats.matById.put(id, this);
	}
	
	public String getUnlocalizedName() {
		return "hbmmat." + this.names[0].toLowerCase(Locale.US);
	}
	
	public String getLocalizedName() {
		return I18nUtil.resolveKey(getUnlocalizedName());
	}
	
	public NTMMaterial setConversion(NTMMaterial mat, int in, int out) {
		this.smeltsInto = mat;
		this.convIn = in;
		this.convOut = out;
		return this;
	}
	
	/** The shape table for autogen */
	public NTMMaterial setAutogen(MaterialShapes... shapes) {
		for(MaterialShapes shape : shapes) this.autogen.add(shape);
		return this;
	}
	
	/** The trait table for recipe recognition */
	public NTMMaterial setTraits(MatTraits... traits) {
		for(MatTraits trait : traits) this.traits.add(trait);
		return this;
	}

	public NTMMaterial m() { this.traits.add(MatTraits.METAL); return this; }
	public NTMMaterial n() { this.traits.add(MatTraits.NONMETAL); return this; }
	
	/** Defines smelting behavior */
	public NTMMaterial smeltable(SmeltingBehavior behavior) {
		this.smeltable = behavior;
		return this;
	}
	
	public NTMMaterial setSolidColor(int colorLight, int colorDark) {
		this.solidColorLight = colorLight;
		this.solidColorDark = colorDark;
		return this;
	}
	
	public NTMMaterial setMoltenColor(int color) {
		this.moltenColor = color;
		return this;
	}
	
	public ItemStack make(Item item, int amount) {
		return new ItemStack(item, amount, this.id);
	}
	
	public ItemStack make(Item item) {
		return make(item, 1);
	}
	
	public static enum SmeltingBehavior {
		NOT_SMELTABLE,	//can't smelt, or simply shouldn't enter a furnace — diamonds, say; also smeltable things that turn into something else
		VAPORIZES,		//can't smelt because the material flies away outright
		BREAKS,			//can't smelt because the material can't take the heat
		SMELTABLE,		//most metals
		ADDITIVE		//like coal — not smeltable itself, but still crucible fodder
	}
	
	public static enum MatTraits {
		METAL,		//metal-like; the arc furnace takes these
		NONMETAL;	//non-metal: gems, non-alloy compounds and friends land here
	}
}
