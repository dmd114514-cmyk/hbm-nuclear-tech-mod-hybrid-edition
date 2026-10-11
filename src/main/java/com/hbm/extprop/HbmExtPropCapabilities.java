package com.hbm.extprop;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.hbm.lib.RefStrings;
import com.hbm.util.LoggingUtil;

/**
 * Capability assembly for the entity extension data.
 *
 * The old version hung things via registerExtendedProperties in the entity construction event; 1.12.2 uses
 * AttachCapabilitiesEvent + a Capability registered during pre-init.
 * Player death rides PlayerEvent.Clone to carry the data over by hand, otherwise one death wipes it.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class HbmExtPropCapabilities {

	private HbmExtPropCapabilities() { }

	/** Logs once, to prove "the event is wired and really fires for players" */
	private static boolean playerAttached = false;

	/** Registers once in pre-init, before any entity exists */
	public static void register() {
		CapabilityManager.INSTANCE.register(HbmLivingProps.class, new HbmLivingStorage(), HbmLivingProps::new);
		CapabilityManager.INSTANCE.register(HbmPlayerProps.class, new HbmPlayerStorage(), HbmPlayerProps::new);
		LoggingUtil.info("[hbm] extended data capability registered: {} / {}", HbmLivingProps.KEY, HbmPlayerProps.KEY);
	}

	@SubscribeEvent
	public static void onAttachEntity(AttachCapabilitiesEvent<Entity> event) {

		Entity entity = event.getObject();
		if(!(entity instanceof EntityLivingBase)) return;

		HbmLivingProps props = new HbmLivingProps((EntityLivingBase) entity);
		event.addCapability(HbmLivingProps.KEY_RL, props);

		// A player is both living and player, so both sets attach; order matters, and the instance refs must be handed over on the spot
		if(entity instanceof EntityPlayer) {
			HbmPlayerProps playerProps = new HbmPlayerProps((EntityPlayer) entity);
			event.addCapability(HbmPlayerProps.KEY_RL, playerProps);

			if(!playerAttached) {
				playerAttached = true;
				LoggingUtil.info("[hbm] player extended data mounted: {}", HbmPlayerProps.KEY);
			}
		}
	}

	/**
	 * Moves the carried state over after a player revives.
	 *
	 * Only wasDead means "revived after dying"; coming back from the End also enters this event but the data is still there,
	 * and moving it again then would just read the same copy twice.
	 */
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {

		if(!event.isWasDeath()) return;

		EntityPlayer original = event.getOriginal();
		EntityPlayer respawned = event.getEntityPlayer();

		if(original == null || respawned == null) return;

		cloneInto(original, respawned, HbmLivingProps.CAPABILITY);
		cloneInto(original, respawned, HbmPlayerProps.CAPABILITY);
	}

	/** Moves via the data class's own serialization, so field changes never need touching here */
	private static <T extends ICapabilitySerializable<NBTTagCompound>> void cloneInto(EntityPlayer original, EntityPlayer respawned, Capability<T> capability) {

		if(capability == null) return;

		T from = original.getCapability(capability, null);
		T to = respawned.getCapability(capability, null);

		if(from == null || to == null) return;

		to.deserializeNBT(from.serializeNBT());
	}

	/** The default implementation's storage, borrowing the data class's own serialization */
	private static class HbmLivingStorage implements Capability.IStorage<HbmLivingProps> {

		@Override
		public NBTBase writeNBT(Capability<HbmLivingProps> capability, HbmLivingProps instance, EnumFacing side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<HbmLivingProps> capability, HbmLivingProps instance, EnumFacing side, NBTBase nbt) {
			if(nbt instanceof NBTTagCompound) instance.deserializeNBT((NBTTagCompound) nbt);
		}
	}

	/** Same for the player's copy */
	private static class HbmPlayerStorage implements Capability.IStorage<HbmPlayerProps> {

		@Override
		public NBTBase writeNBT(Capability<HbmPlayerProps> capability, HbmPlayerProps instance, EnumFacing side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<HbmPlayerProps> capability, HbmPlayerProps instance, EnumFacing side, NBTBase nbt) {
			if(nbt instanceof NBTTagCompound) instance.deserializeNBT((NBTTagCompound) nbt);
		}
	}
}
