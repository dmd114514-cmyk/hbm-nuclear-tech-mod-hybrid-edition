package com.hbm.hazard;

import java.util.ArrayList;
import java.util.List;

import com.hbm.config.ServerConfig;
import com.hbm.hazard.type.HazardTypeNeutron;
import com.hbm.interfaces.IHbmTickReceiver;
import com.hbm.main.ModEventHandler;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Runtime entry point for hazard ticking: runs which thing on whom is glowing, on a rhythm.
 * <br><br>
 * Three tracks, each on its own rhythm, same as the old version:
 * <ul>
 * <li>dropped items use {@code ServerConfig.ITEM_HAZARD_DROP_TICKRATE} as a modulo and sweep the world's EntityItems;</li>
 * <li>online players get their inventory settled every tick, through the global rhythm's player callback;</li>
 * <li>other living things (mobs carrying radioactive material) only have the five equipment slots, settled every tick off vanilla's living event -
 * the global rhythm has no per-living-thing slot, and forcing one means walking entities yourself, which costs more than the event.</li>
 * </ul>
 * All three callbacks run server-side only: dose is save data, and a client-side copy would just fight the server's.
 */
public class HazardTickHandler implements IHbmTickReceiver {

	private static final HazardTickHandler INSTANCE = new HazardTickHandler();

	private HazardTickHandler() { }

	/**
	 * Hooks into the global rhythm list and hands itself to the event bus.
	 * <br><br>
	 * The bus step is an explicit registration rather than {@code @Mod.EventBusSubscriber}: that annotation only takes static methods,
	 * and this class's living callback is an instance method - with the annotation it would silently not attach.
	 */
	public static void register() {
		ModEventHandler.addTickReceiver(INSTANCE);
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	@Override
	public String tickName() {
		return "hazard";
	}

	/** Dropped items: if a dose can be read off it, it radiates - a uranium block sitting on the ground still shines on you */
	@Override
	public void onWorldTick(World world) {

		int tickrate = Math.max(1, ServerConfig.ITEM_HAZARD_DROP_TICKRATE);
		if(world.getTotalWorldTime() % tickrate != 0)
			return;

		// The tick can kill an entity and vanilla yanks it out of loadedEntityList right there, so iterate over a copy
		List<Entity> loaded = new ArrayList<Entity>(world.loadedEntityList);

		for(Entity e : loaded) {
			if(e instanceof EntityItem)
				HazardSystem.updateDroppedItem((EntityItem) e);
		}
	}

	/** Players: top up the leftover neutron activation dose first, then settle the whole inventory */
	@Override
	public void onPlayerTick(EntityPlayer player) {

		for(ItemStack stack : player.inventory.mainInventory) {
			// Empty slots are EMPTY here, not null; without a guard you would run a lookup over an empty stack
			if(stack.isEmpty() || !stack.hasTagCompound())
				continue;
			/*
			 * Activation leftover: the thing itself no longer radiates, but the dose neutrons knocked into its NBT is still there,
			 * and the old version keeps charging the player 1/20 of it right here. Judging that it no longer radiates needs a live reading,
			 * not a look at whether a RADIATION tag exists - readings were never stored keyed by hazard name.
			 */
			if(HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.RADIATION) == 0) {
				float activation = stack.getTagCompound().getFloat(HazardTypeNeutron.NEUTRON_KEY);
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.CREATIVE, activation / 20);
			}
		}

		HazardSystem.updatePlayerInventory(player);
	}

	/** Non-player living things: only equipment and the held items; a player's whole inventory is onPlayerTick's business */
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {

		EntityLivingBase entity = event.getEntityLiving();
		if(entity.world.isRemote || entity instanceof EntityPlayer)
			return;

		HazardSystem.updateLivingInventory(entity);
	}
}
