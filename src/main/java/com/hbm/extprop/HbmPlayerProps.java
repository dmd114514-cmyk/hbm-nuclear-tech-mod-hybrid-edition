package com.hbm.extprop;

import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.main.ServerProxy;
import com.hbm.network.NetworkHandler;
import com.hbm.network.packet.PlayerInformPacket;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemModShield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

/**
 * The extension layer on players: shields, stamina/sprint, toggle states, plus a few timers that need remembering across rounds.
 *
 * In 1.7.10 this hung off IExtendedEntityProperties; that interface is gone in 1.12.2, hence a capability,
 * shaped like this package's HbmLivingProps. Inner tag names copied verbatim from the old version ("shield"/"maxShield"/
 * "nitan"/"enableBackpack"...), so old saves never leave you guessing "moved or lost".
 *
 * Fields stay public on purpose: every old call site reads and writes directly (props.shield += ...),
 * and a getter/setter layer would mean rewriting all of them.
 *
 * Didn't come along with this batch:
 *   · the ByteBuf serialize/deserialize — that was the old com.hbm.packet sync channel; this version's counterpart is
 *     com.hbm.network; write it that way when player state actually needs syncing.
 *   · the old nitanHealth field: computed from nitanCount at construction, when nitanCount was always 0, and nobody read it anyway.
 *
 * The keybind half (the keysPressed bits) is deliberately **not saved** in this data: it records "which key is held this beat",
 * the client pushes a packet to overwrite it on every change (see {@code KeybindPacket}), and a reconnect should mean released keys anyway —
 * persisting it would resurrect a key nobody is holding.
 */
public class HbmPlayerProps implements ICapabilitySerializable<NBTTagCompound> {

	public static final String KEY = "NTM_EXT_PLAYER";
	public static final ResourceLocation KEY_RL = new ResourceLocation(RefStrings.MODID, KEY);

	/** The shield cap; infusion stops stacking once it reaches here */
	public static final float shieldCap = 100;
	/** Cooldown after one dash (ticks) */
	public static final int dashCooldownLength = 5;
	/** Minimum spacing between that metallic block-sound (ticks) */
	public static final int plinkCooldownLength = 10;

	/** Forge assigns it via reflection, so it can't be final */
	@CapabilityInject(HbmPlayerProps.class)
	public static Capability<HbmPlayerProps> CAPABILITY = null;

	private EntityPlayer player;

	/* Toggles, flipped by the keybind bits in setKeyPressed below */
	public boolean enableHUD = true;
	public boolean enableBackpack = true;
	public boolean enableMagnet = true;

	/** One bit per {@link EnumKeybind}, recording "held right now"; not saved, see the class doc */
	private boolean[] keysPressed = new boolean[EnumKeybind.values().length];

	/* Sprint and stamina: only the timers need to survive across rounds, so these three go through accessors */
	private int dashCooldown = 0;
	private int totalDashCount = 0;
	private int stamina = 0;

	/** Block-sound cooldown, decremented by each armor piece */
	public int plinkCooldown = 0;

	/* Shield */
	public float shield = 0;
	public float maxShield = 0;
	/** ticksExisted of the last hit; the shield waits 60 ticks past it */
	public int lastDamage = 0;
	/** Lifetime count of nitro-type drinks; 3 means overdose */
	public int nitanCount = 0;

	/** Lantern Town reputation; fixing lanterns adds, breaking them subtracts */
	public int reputation;

	/** Ladder flag: what holds you up when a multiblock doubles as a ladder */
	public boolean isOnLadder = false;

	/** The hand pulling the pin: ticks holding right-click live here, not in the item's NBT */
	public int grenadeDeployment;

	/** First landing after a cross-dimension trip gets a tip; never repeated after that */
	public boolean hasWarped = false;
	public int lastDimension = 0;

	/** The masked man's entrance timer */
	public int maskManTimer = 0;

	public HbmPlayerProps() { }

	public HbmPlayerProps(EntityPlayer player) {
		this.player = player;
	}

	public void setEntity(EntityPlayer player) {
		this.player = player;
	}

	/// accessors ///

	/** Returns null when absent, and call sites null-check first — harder to fool anyone than stuffing in a fake data object */
	public static HbmPlayerProps getData(EntityPlayer player) {
		if(player == null) return null;
		if(CAPABILITY == null) return null;
		return player.getCapability(CAPABILITY, null);
	}

	/// keybinds ///

	public boolean getKeyPressed(EnumKeybind key) {
		return keysPressed[key.ordinal()];
	}

	/** Jetpack-style thrust check: the pack's master toggle on, and the jump key currently held */
	public boolean isJetpackActive() {
		return this.enableBackpack && getKeyPressed(EnumKeybind.JETPACK);
	}

	/** The magnet only reads its one toggle; there's no "held" tier for it */
	public boolean isMagnetActive() {
		return this.enableMagnet;
	}

	/**
	 * Records a keybind bit's new state, and on the "just pressed" beat flips the matching toggle in passing.
	 * <br><br>
	 * Toggles flip server-side only: the client calling this is local bookkeeping (it compares against the synced state to decide whether to send),
	 * and both sides flipping would cancel out. Same split as the old version.
	 */
	public void setKeyPressed(EnumKeybind key, boolean pressed) {

		if(!getKeyPressed(key) && pressed && isServerSide()) {

			if(key == EnumKeybind.TOGGLE_JETPACK) {
				this.enableBackpack = !this.enableBackpack;
				inform(this.enableBackpack ? TextFormatting.GREEN + "Jetpack ON" : TextFormatting.RED + "Jetpack OFF", ServerProxy.ID_JETPACK);
			}

			if(key == EnumKeybind.TOGGLE_MAGNET) {
				this.enableMagnet = !this.enableMagnet;
				inform(this.enableMagnet ? TextFormatting.GREEN + "Magnet ON" : TextFormatting.RED + "Magnet OFF", ServerProxy.ID_MAGNET);
			}

			if(key == EnumKeybind.TOGGLE_HEAD) {
				this.enableHUD = !this.enableHUD;
				inform(this.enableHUD ? TextFormatting.GREEN + "HUD ON" : TextFormatting.RED + "HUD OFF", ServerProxy.ID_HUD);
			}

			/* Pressing this one while on a tram opens its cargo bay: x carries the entity id, the last leg of the GUI dispatch chain looks the provider up by it */
			if(key == EnumKeybind.TRAIN) {
				Entity ridden = player.getRidingEntity();
				if(ridden instanceof EntityRailCarBase && ridden instanceof IGUIProvider)
					player.openGui(MainRegistry.instance, 0, player.world, ridden.getEntityId(), 0, 0);
			}
		}

		keysPressed[key.ordinal()] = pressed;
	}

	/** The toggle's one-line tip goes to the toggler only and vanishes after 1s; same ids replace each other */
	private void inform(String message, int id) {
		if(!(player instanceof EntityPlayerMP)) return;
		NetworkHandler.sendTo(new PlayerInformPacket(message, id, 1000), (EntityPlayerMP) player);
	}

	/** This data may also be held by the client's copy of the player, which must never flip any toggles */
	private boolean isServerSide() {
		return player != null && player.world != null && !player.world.isRemote;
	}

	/// sprint and stamina ///

	public void setDashCooldown(int cooldown) { this.dashCooldown = cooldown; }
	public int getDashCooldown() { return this.dashCooldown; }

	public void setStamina(int stamina) { this.stamina = stamina; }
	public int getStamina() { return this.stamina; }

	public void setDashCount(int count) { this.totalDashCount = count; }
	public int getDashCount() { return this.totalDashCount; }

	/// shield ///

	/**
	 * Cap = your own maxShield plus whatever the chestpiece's insert mod (kevlar slot) grants.
	 *
	 * Inserts only count `ItemModShield`; other mods contribute nothing, and pryMods' empty-slot convention is EMPTY,
	 * so emptiness checks always go through isEmpty().
	 */
	public float getEffectiveMaxShield() {

		float max = this.maxShield;

		if(player == null)
			return max;

		ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if(chest == null || chest.isEmpty())
			return max;

		ItemStack[] mods = ArmorModHandler.pryMods(chest);
		ItemStack kevlar = mods[ArmorModHandler.kevlar];

		if(kevlar != null && !kevlar.isEmpty() && kevlar.getItem() instanceof ItemModShield)
			max += ((ItemModShield) kevlar.getItem()).shield;

		return max;
	}

	/** The unified exit for block sounds: stays silent while the cooldown runs, so it doesn't chain into a wall of noise */
	public static void plink(EntityPlayer player, SoundEvent sound, float volume, float pitch) {
		HbmPlayerProps props = getData(player);
		if(props == null)
			return;

		if(props.plinkCooldown <= 0) {
			player.world.playSound(null, player.posX, player.posY, player.posZ, sound, SoundCategory.PLAYERS, volume, pitch);
			props.plinkCooldown = plinkCooldownLength;
		}
	}

	/// Capability ///

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return CAPABILITY != null && capability == CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(CAPABILITY != null && capability == CAPABILITY) return CAPABILITY.cast(this);
		return null;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound props = new NBTTagCompound();

		props.setFloat("shield", shield);
		props.setFloat("maxShield", maxShield);
		// The old version wrote setFloat but read getInteger, i.e. every read came back 0; both sides now agree on an integer
		props.setInteger("nitan", nitanCount);
		props.setBoolean("enableBackpack", enableBackpack);
		props.setBoolean("enableMagnet", enableMagnet);
		props.setBoolean("enableHUD", enableHUD);
		props.setInteger("reputation", reputation);
		props.setBoolean("isOnLadder", isOnLadder);
		props.setBoolean("hasWarped", hasWarped);
		props.setInteger("lastDimension", lastDimension);
		props.setInteger("maskManTimer", maskManTimer);

		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag(KEY, props);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if(nbt == null) return;

		NBTBase base = nbt.getTag(KEY);
		if(!(base instanceof NBTTagCompound)) return;
		NBTTagCompound props = (NBTTagCompound) base;

		shield = props.getFloat("shield");
		maxShield = props.getFloat("maxShield");
		nitanCount = props.getInteger("nitan");
		enableBackpack = props.getBoolean("enableBackpack");
		enableMagnet = props.getBoolean("enableMagnet");
		enableHUD = props.getBoolean("enableHUD");
		reputation = props.getInteger("reputation");
		isOnLadder = props.getBoolean("isOnLadder");
		hasWarped = props.getBoolean("hasWarped");
		lastDimension = props.getInteger("lastDimension");
		maskManTimer = props.getInteger("maskManTimer");
	}
}
