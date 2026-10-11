package com.hbm.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * The registration facade. All content queues here, then RegistryHandler pours it into the registries inside the matching RegistryEvents.
 *
 * Why not let content subscribe to RegistryEvents itself? Because then every subdomain has to mind its own registry names,
 * casing, chunk item pairing and all that busywork, and gets it wrong eventually. Centralized here, it can't go wrong.
 */
public final class HbmRegistry {

	public static final String MODID = "hbm";

	private static final List<IRegistrar> REGISTRARS = new ArrayList<>();
	private static final List<Block> BLOCKS = new ArrayList<>();
	private static final List<Item> ITEMS = new ArrayList<>();
	private static final List<SoundEvent> SOUNDS = new ArrayList<>();
	private static final List<EntityEntry> ENTITIES = new ArrayList<>();
	private static final List<net.minecraft.potion.Potion> POTIONS = new ArrayList<>();

	private HbmRegistry() { }

	/** Uniform lowercase registry names; kills the "hbm:MyThing" case mess waiting to happen */
	public static ResourceLocation rl(String name) {
		return new ResourceLocation(MODID, name.toLowerCase(Locale.ROOT));
	}

	/*
	 * Registration unit management
	 */

	public static void registrar(IRegistrar registrar) {
		REGISTRARS.add(registrar);
	}

	public static List<IRegistrar> registrars() {
		return REGISTRARS;
	}

	/**
	 * Prints the assembly roster.
	 *
	 * The "declared but never hung into the assembly point" class of defect doesn't fail the build, doesn't crash startup — you only find it by actually checking the registry;
	 * turn the roster into one log line and it becomes an artifact checkable on every startup, instead of something someone has to remember.
	 */
	public static void logAssembly() {
		StringBuilder sb = new StringBuilder();
		for(IRegistrar registrar : REGISTRARS) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(registrar.id());
		}
		com.hbm.util.LoggingUtil.info("[hbm] {} registration units mounted at the assembly point: {}", REGISTRARS.size(), sb);
	}

	public static void preInitRegistrars() {
		for(IRegistrar registrar : REGISTRARS) registrar.preInit();
	}

	/**
	 * The init point for fluids/materials and other non-Forge registry objects. The only one in the project; don't call it anywhere else.
	 */
	public static void registerFluids() {
		for(IRegistrar registrar : REGISTRARS) registrar.registerFluids();
	}

	public static void initRegistrars() {
		for(IRegistrar registrar : REGISTRARS) registrar.init();
	}

	public static void postInitRegistrars() {
		for(IRegistrar registrar : REGISTRARS) registrar.postInit();
	}

	/** Client-only: model registration */
	public static void registerModels() {
		for(IRegistrar registrar : REGISTRARS) registrar.registerModels();

		// Block items get their own pass: it walks the item list captured while queuing (at this point neither the Forge registry nor
		// the Block→Item mapping is filled — measured, both routes come back empty at this timing),
		// picking out ItemBlocks to hang models on; multi-model reporters (IMultiModelItem) only get consumers through this pass.
		// This is the single entry for block-item models — don't open a second one on the registrar side, or the same item gets hung twice.
		int itemBlocks = 0;
		for(Item item : ITEMS) {
			if(!(item instanceof net.minecraft.item.ItemBlock)) continue;
			com.hbm.items.registry.ItemModels.registerAll(item);
			itemBlocks++;
		}
		com.hbm.util.LoggingUtil.info("[hbm] block item models mounted: {} (queued blocks {} / queued items {})", itemBlocks, BLOCKS.size(), ITEMS.size());
	}

	/*
	 * The queued registration facade (call during preInit)
	 */

	public static <T extends Block> T block(T block, String name) {
		block.setRegistryName(rl(name));
		BLOCKS.add(block);
		return block;
	}

	/** For custom ItemBlocks — subtyped / multi-named ItemBlocks all come in through here */
	@FunctionalInterface
	public interface ItemBlockFactory {

		ItemBlock create(Block block);
	}

	/** Nearly every block wants a matching ItemBlock; handled in passing so none slip through */
	public static <T extends Block> T blockWithItem(T block, String name) {
		return blockWithItem(block, name, ItemBlock::new);
	}

	public static <T extends Block> T blockWithItem(T block, String name, ItemBlockFactory factory) {
		block(block, name);
		ItemBlock itemBlock = factory.create(block);
		// The vanilla ItemBlock constructor copies the block's registry name, and Forge forbids setting it twice (it throws
		// "existing registry name"), so this only fills the gap it hasn't set yet.
		if(itemBlock.getRegistryName() == null) itemBlock.setRegistryName(block.getRegistryName());
		ITEMS.add(itemBlock);
		return block;
	}

	public static <T extends Item> T item(T item, String name) {
		item.setRegistryName(rl(name));
		ITEMS.add(item);
		return item;
	}

	public static SoundEvent sound(String name) {
		ResourceLocation location = soundLocation(name);
		SoundEvent event = new SoundEvent(location);
		event.setRegistryName(location);
		SOUNDS.add(event);
		return event;
	}

	/**
	 * Fetches a registered sound; hands back a placeholder when it's not there yet.
	 *
	 * Keys in sounds.json get auto-registered as SoundEvents by Forge, but that may land later than a content object's
	 * "stash the sound in a static field" moment, so this can't just null-check: the placeholder holds the spot,
	 * and by actual playback the name resolves to the registered version.
	 */
	public static SoundEvent soundRef(String name) {
		ResourceLocation location = soundLocation(name);
		SoundEvent event = SoundEvent.REGISTRY.getObject(location);
		return event != null ? event : new SoundEvent(location);
	}

	/**
	 * Sound name → registry key: namespaced forms (the {@code NTMSounds} constants are "hbm:xxx") parse as whole strings,
	 * bare ones get this mod's domain. Both paths fold the name to lowercase — the lookup table is lowercase to begin with.
	 *
	 * This step can't be skipped: handing "hbm:xxx" straight to rl() yields the malformed hbm:hbm:xxx key,
	 * which neither throws nor ever resolves — exactly the shape of "the sound just never plays".
	 */
	private static ResourceLocation soundLocation(String name) {
		return name.indexOf(':') >= 0 ? new ResourceLocation(name) : rl(name);
	}

	public static <T extends Entity> EntityEntryBuilder<T> entity(Class<T> clazz, String name, int id) {
		return EntityEntryBuilder.<T>create()
				.entity(clazz)
				.id(rl(name), id)
				.name(MODID + "." + name.toLowerCase(Locale.ROOT));
	}

	/** Drop the result of EntityEntryBuilder.build() in here */
	public static void entity(EntityEntry entry) {
		ENTITIES.add(entry);
	}

	/** TEs have no RegistryEvent; register directly; names use the ResourceLocation form */
	public static void tile(Class<? extends TileEntity> clazz, String name) {
		GameRegistry.registerTileEntity(clazz, rl(name));
	}

	public static <T extends net.minecraft.potion.Potion> T potion(T potion, String name) {
		potion.setRegistryName(rl(name));
		POTIONS.add(potion);
		return potion;
	}

	/*
	 * Called by RegistryHandler inside the RegistryEvents
	 */

	public static void flushBlocks(IForgeRegistry<Block> registry) {
		registry.registerAll(BLOCKS.toArray(new Block[0]));
		com.hbm.util.LoggingUtil.info("[hbm] registered blocks: {}", BLOCKS.size());
	}

	public static void flushItems(IForgeRegistry<Item> registry) {
		registry.registerAll(ITEMS.toArray(new Item[0]));
		com.hbm.util.LoggingUtil.info("[hbm] registered items: {}", ITEMS.size());
	}

	public static void flushSounds(IForgeRegistry<SoundEvent> registry) {
		registry.registerAll(SOUNDS.toArray(new SoundEvent[0]));
		com.hbm.util.LoggingUtil.info("[hbm] registered sounds: {}", SOUNDS.size());
	}

	public static void flushEntities(IForgeRegistry<EntityEntry> registry) {
		registry.registerAll(ENTITIES.toArray(new EntityEntry[0]));
		com.hbm.util.LoggingUtil.info("[hbm] registered entities: {}", ENTITIES.size());
	}

	public static void flushPotions(IForgeRegistry<net.minecraft.potion.Potion> registry) {
		registry.registerAll(POTIONS.toArray(new net.minecraft.potion.Potion[0]));
		com.hbm.util.LoggingUtil.info("[hbm] registered potions: {}", POTIONS.size());
	}
}
