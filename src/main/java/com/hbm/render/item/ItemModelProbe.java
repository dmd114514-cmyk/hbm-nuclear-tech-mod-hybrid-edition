package com.hbm.render.item;

import com.hbm.items.ModItems;
import com.hbm.items.registry.ItemModels;
import com.hbm.lib.RefStrings;
import com.hbm.registry.HbmRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The item-model registration timing fix + startup self-check (2026-10-09 P0: every item in the mod purple-black).
 *
 * The cause (probe evidence, not guesswork): `ModelLoader.setCustomModelResourceLocation` stores the "item → model location"
 * mapping in `ModelLoader.customModels`, keyed by **the `item.delegate` object itself**; what actually reaches ItemModelMesher is
 * `ModelLoader.onRegisterItems(mesher)`, called from exactly one place in the whole tree — the `RenderItem` constructor — and Forge never re-pushes when baking.
 * This tree used to hang models in preInit (`ClientProxy.preInit` → `HbmRegistry.registerModels`),
 * at which point items weren't registered yet (`getRegistryName()` reads the delegate's name, which is empty then),
 * so most items never made it into customModels (measured: 319 entries out of 1464 items, ingot_uranium not among them),
 * while the lookup happens after registration ⇒ delegates don't match ⇒ the mesher falls back to the missing model across the board.
 * Blocks were unaffected, since block looks go through blockstates JSON and never touch the mesher.
 *
 * The fix: move item model registration to **after the item registration event** — registryName/delegate are final values by then,
 * keys match lookups, and the moment still precedes baking, so Forge's own RenderItem constructor pushes them into the mesher.
 * Only the item lane is patched (block, TE and entity renderers still hang off the preInit pass); nothing else registered twice.
 *
 * Relation to the preInit pass: the 319 entries that landed there get overwritten by the same keys, no conflict; but `ItemModels.registerAll`
 * will add `IColoredItem`s to the tint list a second time, so a few tinted items log one "duplicate tint" warning.
 *
 * Self-check: once a screen is up, ask the mesher once; the control is a vanilla stick; the sample is ingot_uranium (plain shape, plain texture, no tint).
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public final class ItemModelProbe {

	private ItemModelProbe() { }

	private static boolean done;

	/** Later than RegistryHandler's registration (same priority, undefined order, so pinned to LOWEST); only now is the delegate final */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onItemsRegistered(RegistryEvent.Register<Item> event) {

		int count = 0;

		for(Item item : net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS) {
			ResourceLocation name = item.getRegistryName();
			if(name == null || !RefStrings.MODID.equals(name.getResourceDomain())) continue;
			ItemModels.registerAll(item);
			count++;
		}

		com.hbm.util.LoggingUtil.info("[hbm] item models registered (post-registration-event timing): {} items", count);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {

		if(done || event.phase != TickEvent.Phase.END) return;

		Minecraft mc = Minecraft.getMinecraft();
		if(mc == null || mc.currentScreen == null || mc.getRenderItem() == null) return; // The screen being up counts as loaded

		done = true;
		report(mc);
	}

	private static void report(Minecraft mc) {
		try {
			IBakedModel missing = mc.getRenderItem().getItemModelMesher().getModelManager().getMissingModel();

			log("mesher lookup hbm:ingot_uranium = " + describe(mc.getRenderItem().getItemModelMesher().getItemModel(new ItemStack(ModItems.ingot_uranium)), missing));
			log("mesher lookup control minecraft:stick = " + describe(mc.getRenderItem().getItemModelMesher().getItemModel(new ItemStack(net.minecraft.init.Items.STICK)), missing));

			ModelResourceLocation ours = new ModelResourceLocation(HbmRegistry.rl("ingot_uranium"), "inventory");
			log("Baked model hbm:ingot_uranium#inventory = " + describe(mc.getRenderItem().getItemModelMesher().getModelManager().getModel(ours), missing));

			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite("hbm:items/ingot_uranium");
			TextureAtlasSprite missingSprite = mc.getTextureMapBlocks().getMissingSprite();
			log("Texture hbm:items/ingot_uranium = "
					+ (sprite == null ? "null" : (sprite == missingSprite ? "missing texture (purple-black checker)" : "in the atlas " + sprite.getIconName())));

			reportNamedModels();
		} catch(Throwable t) {
			log("self-check failed on itself: " + t);
		}
	}

	/**
	 * The ISTER items (big gear / saw blade / dyson member) draw one named part of the OBJ, and a wrong part name reports **nothing at all** —
	 * the item just goes blank. Only the model's part list can check that, so it gets named here once.
	 */
	private static void reportNamedModels() {
		log("part check " + partCheck("models/machines/stirling.obj", "Cog"));
		log("part check " + partCheck("models/machines/sawmill.obj", "Blade"));
		log("part check " + partCheck("models/dyson_swarm_satellite.obj", null));
	}

	/** part = null means this item draws the whole model; only the parsed part count is reported */
	private static String partCheck(String modelFile, String part) {

		java.util.List<String> names = com.hbm.main.ResourceManager.getModel(modelFile).getPartNames();

		if(part == null) return modelFile + " resolved to " + names.size() + " parts (whole model)";

		return modelFile + " needs part " + part + " = " + (names.contains(part) ? "present" : "not there; actual roster " + names) + "; " + names.size() + " parts in total";
	}

	private static String describe(IBakedModel model, IBakedModel missing) {
		if(model == null) return "null (never baked)";
		if(model == missing) return "missing model";
		return "ok " + model.getClass().getName();
	}

	private static void log(String line) {
		com.hbm.util.LoggingUtil.info("[hbm] item look self-check: {}", line);
	}
}
