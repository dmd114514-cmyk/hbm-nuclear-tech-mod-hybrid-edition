package com.hbm.inventory.fluid.trait;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.util.text.TextFormatting;

public class FT_Flammable extends FluidTrait {

	/** Heat released per 1000mB, generally 1:1 with HE */
	private long energy;

	public FT_Flammable() { }

	public FT_Flammable(long energy) {
		this.energy = energy;
	}

	public long getHeatEnergy() {
		return this.energy;
	}

	@Override
	public void addInfo(List<String> info) {
		super.addInfo(info);

		info.add(TextFormatting.YELLOW + "[" + I18nUtil.resolveKey("hbmfluid.trait.flammable") + "]");

		if(energy > 0)
			info.add(TextFormatting.YELLOW + I18nUtil.resolveKey("hbmfluid.trait.provides") + " " + TextFormatting.RED + "" + BobMathUtil.getShortNumber(energy) + "TU " + TextFormatting.YELLOW + I18nUtil.resolveKey("hbmfluid.trait.perBucket"));
	}

	@Override
	public void serializeJSON(JsonWriter writer) throws IOException {
		writer.name("energy").value(energy);
	}

	@Override
	public void deserializeJSON(JsonObject obj) {
		this.energy = obj.get("energy").getAsLong();
	}
}
