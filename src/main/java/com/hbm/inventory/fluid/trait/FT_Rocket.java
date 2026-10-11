package com.hbm.inventory.fluid.trait;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.util.BobMathUtil;

import net.minecraft.util.text.TextFormatting;

public class FT_Rocket extends FluidTrait {

	/**
	 * Currently only for transport platforms; likely to grow.
	 * ISP decides how long it can burn, thrust how much it can carry.
	 * Ascent burns gravity losses, so a low TWR wastes dV for nothing.
	 */

	//fuel specific impulse, i.e. "how long it burns"
	private int isp;

	//fuel thrust, i.e. "how much it carries"
	private long thrust;

	public FT_Rocket(int isp, long twr) {
		this.isp = isp;
		this.thrust = twr;
	}

	public int getISP() {
		return this.isp;
	}

	public long getThrust() {
		return this.thrust;
	}

	@Override
	public void addInfo(List<String> info) {
		super.addInfo(info);

		info.add(TextFormatting.LIGHT_PURPLE + "[Rocket Grade]");

		if(isp > 0)
			info.add(TextFormatting.YELLOW + "Provides " + TextFormatting.RED + "" + BobMathUtil.getShortNumber(isp) + " ISP " + TextFormatting.YELLOW + "per bucket");

		info.add(TextFormatting.RED + "[Thrust power]");

		if(thrust > 0)
			info.add(TextFormatting.YELLOW + "Provides " + TextFormatting.RED + "" + BobMathUtil.getShortNumber(thrust) + " N " + TextFormatting.YELLOW + "of thrust per bucket");
	}

	@Override
	public void serializeJSON(JsonWriter writer) throws IOException {
		writer.name("isp").value(isp);
		writer.name("thrust").value(thrust);
	}

	@Override
	public void deserializeJSON(JsonObject obj) {
		this.isp = obj.get("isp").getAsInt();
		this.thrust = obj.get("thrust").getAsLong();
	}

}
