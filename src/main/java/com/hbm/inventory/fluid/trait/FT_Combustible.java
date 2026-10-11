package com.hbm.inventory.fluid.trait;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.util.text.TextFormatting;

public class FT_Combustible extends FluidTrait {

	protected FuelGrade fuelGrade;
	protected long combustionEnergy;

	public FT_Combustible() { }

	public FT_Combustible(FuelGrade grade, long energy) {
		this.fuelGrade = grade;
		this.combustionEnergy = energy;
	}

	@Override
	public void addInfo(List<String> info) {
		super.addInfo(info);

		info.add(TextFormatting.GOLD + "[" + I18nUtil.resolveKey("hbmfluid.trait.combustible") + "]");

		if(combustionEnergy > 0) {
			info.add(TextFormatting.GOLD + I18nUtil.resolveKey("hbmfluid.trait.provides") + " " + TextFormatting.RED + "" + BobMathUtil.getShortNumber(combustionEnergy) + "HE " + TextFormatting.GOLD + I18nUtil.resolveKey("hbmfluid.trait.perBucket"));
			info.add(TextFormatting.GOLD + I18nUtil.resolveKey("hbmfluid.trait.fuelGrade") + ": " + TextFormatting.RED + this.fuelGrade.getLocalizedName());
		}
	}

	public long getCombustionEnergy() {
		return this.combustionEnergy;
	}

	public FuelGrade getGrade() {
		return this.fuelGrade;
	}

	public static enum FuelGrade {
		LOW("low"),			//heavy oil, industrial oil					< steam engines, industrial generators
		MEDIUM("medium"),	//diesel oil							< diesel generators
		HIGH("high"),		//diesel, gasoline						< high-pressure engines
		AERO("aviation"),	//kerosene and other light aviation fuels				< turbofans
		GAS("gaseous");		//natural gas, petroleum gas, syngas and friends		< gas turbines

		private String grade;

		private FuelGrade(String grade) {
			this.grade = grade;
		}

		public String getLocalizedName() {
			return I18nUtil.resolveKey("hbmfluid.trait.fuel." + this.grade);
		}
	}

	@Override
	public void serializeJSON(JsonWriter writer) throws IOException {
		writer.name("energy").value(combustionEnergy);
		writer.name("grade").value(fuelGrade.name());
	}

	@Override
	public void deserializeJSON(JsonObject obj) {
		this.combustionEnergy = obj.get("energy").getAsLong();
		this.fuelGrade = FuelGrade.valueOf(obj.get("grade").getAsString());
	}
}
