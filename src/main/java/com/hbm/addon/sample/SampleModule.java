package com.hbm.addon.sample;

import com.hbm.registry.HbmRegistry;

import api.hbm.module.IHbmModule;
import api.hbm.module.InternalModule;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

/**
 * Sample module. Once real content ships this whole set (its four files included) gets deleted,
 * its only value right now: proving the chain module -> HbmRegistry queue -> RegistryEvent -> model -> TE is alive.
 *
 * Note it registers through {@link HbmRegistry} only; the facade sets the registry name —
 * a module calling setRegistryName itself trips Forge's set-once check.
 */
@InternalModule
public class SampleModule implements IHbmModule {

	public static SampleItem SAMPLE_ITEM;
	public static SampleBlock SAMPLE_BLOCK;

	@Override
	public String getId() {
		return "sample";
	}

	@Override
	public void preInit() {
		SAMPLE_ITEM = HbmRegistry.item(new SampleItem(), "sample_item");
		SAMPLE_BLOCK = HbmRegistry.blockWithItem(new SampleBlock(), "sample_block");
	}

	@Override
	public void init() {
		HbmRegistry.tile(SampleTileEntity.class, "sample_tile");
	}

	/** Only ever called on the client, so touching ModelLoader here is safe */
	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(SAMPLE_ITEM, 0, new ModelResourceLocation(HbmRegistry.rl("sample_item"), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(SAMPLE_BLOCK), 0, new ModelResourceLocation(HbmRegistry.rl("sample_block"), "inventory"));
	}
}
