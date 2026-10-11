package com.hbm.render.item;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The registry for dynamic item rendering.
 *
 * The old table lived in ClientProxy (a several-hundred-line registerItemRenderer) where everyone shoved a line in;
 * registration now collects into this one class, and uses {@code Item.setTileEntityItemStackRenderer} —
 * a public setter, so **the content side's item classes never need touching**; exposing the item fields is enough.
 *
 * The timing picks ModelRegistryEvent: items are all registered by then and the client hasn't started rendering.
 * The table assembles inside the event, never reading {@code ModItems.xxx} in a static block — those fields are assigned in preInit,
 * and a class loading one step early would stuff nulls in. The wrap-up logs the count, so one client run confirms how many got hung.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class HbmItemRenderers {

	private static final List<IItemRendererProvider> providers = new ArrayList<IItemRendererProvider>();

	private HbmItemRenderers() { }

	/** The old batch hung paths and textures on ResourceManager's static fields (gone this version); whoever uses them writes the path on their own side */
	private static final String MODEL_STIRLING = "models/machines/stirling.obj";
	private static final String MODEL_SAWMILL = "models/machines/sawmill.obj";

	private static final ResourceLocation TEX_STIRLING = machineTex("stirling.png");
	private static final ResourceLocation TEX_STIRLING_STEEL = machineTex("stirling_steel.png");
	private static final ResourceLocation TEX_SAWMILL = machineTex("sawmill.png");

	private static ResourceLocation machineTex(String name) {
		return new ResourceLocation(RefStrings.MODID, "textures/models/machines/" + name);
	}

	/** A block renderer registering its own item form along the way */
	public static void provide(IItemRendererProvider provider) {
		providers.add(provider);
	}

	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {

		int count = 0;

		count += assign(ModItems.battery_pack, new ItemRenderBatteryPack());

		count += assign(ModItems.gear_large, new ItemRenderSpinningPart(MODEL_STIRLING, "Cog", 3600, TEX_STIRLING, TEX_STIRLING_STEEL));
		count += assign(ModItems.sawblade, new ItemRenderSpinningPart(MODEL_SAWMILL, "Blade", 1800, TEX_SAWMILL));
		count += assign(ModItems.swarm_member, new ItemRenderDysonMember());

		// Bobblehead: the block body is INVISIBLE and all TESR's, so the item cell must stand the same figure (durability is which one)
		count += assign(Item.getItemFromBlock(ModBlocks.bobblehead), new ItemRenderBobblehead());

		for(IItemRendererProvider provider : providers) {
			for(Item item : provider.getItemsForRenderer()) {
				count += assign(item, provider.getRenderer());
			}
		}

		MainRegistry.logger.info("[hbm] dynamic item renderers registered: {}", count);
	}

	private static int assign(Item item, TileEntityItemStackRenderer renderer) {

		if(item == null || renderer == null) return 0;

		item.setTileEntityItemStackRenderer(renderer);
		return 1;
	}
}
