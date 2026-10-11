package com.hbm.render.loader;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// 1.12.2 deleted Forge's IModelCustom; this keeps a home-grown interface with the same semantics and the same external call shape
public interface IModelCustomNamed {

	List<String> getPartNames();

	@SideOnly(Side.CLIENT)
	void renderAll();

	@SideOnly(Side.CLIENT)
	void renderPart(String partName);

	@SideOnly(Side.CLIENT)
	void renderOnly(String... groupNames);

	@SideOnly(Side.CLIENT)
	void renderAllExcept(String... excludedGroupNames);
}
