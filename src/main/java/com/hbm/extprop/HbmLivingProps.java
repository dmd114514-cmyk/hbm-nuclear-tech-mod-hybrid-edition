package com.hbm.extprop;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import com.hbm.config.RadiationConfig;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;
import com.hbm.config.ServerConfig;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.ServerProxy;
import com.hbm.network.NetworkHandler;
import com.hbm.network.packet.PlayerInformPacket;
import com.hbm.util.ChatBuilder;

/**
 * Radiation/contamination/breath state carried on living beings.
 *
 * In 1.7.10 this hung off IExtendedEntityProperties; that interface is gone in 1.12.2, hence a capability.
 * NBT tag names and structure **copied verbatim from the old version** ("HbmLivingProps" outer + hfr_* inner + cont_N),
 * so reading old saves during development never leaves you guessing "moved house or lost data".
 *
 * No atmosphere field here: it only matters inside the tick-behavior layer and ships with that layer.
 */
public class HbmLivingProps implements ICapabilitySerializable<NBTTagCompound> {

	public static final String KEY = "NTM_EXT_LIVING";
	public static final ResourceLocation KEY_RL = new ResourceLocation(RefStrings.MODID, KEY);

	/** Fields the behavior layer needs, parked here across the two-part delivery so the structure doesn't get touched twice */
	public static final int MAX_ASBESTOS = 60 * 60 * 20;
	public static final int MAX_BLACKLUNG = 2 * 60 * 60 * 20;

	/** Forge assigns it via reflection, so it can't be final */
	@CapabilityInject(HbmLivingProps.class)
	public static Capability<HbmLivingProps> CAPABILITY = null;

	private EntityLivingBase entity;

	private float radiation;
	private float digamma;
	private int asbestos;
	private int blacklung;
	private float radEnv;
	private float radBuf;
	private int bombTimer;
	private int contagion;
	private int oil;
	private float activation;
	private int oxygen = 100;
	public int fire;
	public int phosphorus;
	public int balefire;
	public int blackFire;
	private boolean gravity = false;

	private final List<ContaminationEffect> contamination = new ArrayList<>();

	public HbmLivingProps() { }

	public HbmLivingProps(EntityLivingBase entity) {
		this.entity = entity;
	}

	public void setEntity(EntityLivingBase entity) {
		this.entity = entity;
	}

	/// accessors ///

	/** Returns null when absent, and call sites null-check first — harder to fool anyone than stuffing in a fake data object */
	public static HbmLivingProps getData(EntityLivingBase entity) {
		if(entity == null) return null;
		if(CAPABILITY == null) return null;
		return entity.getCapability(CAPABILITY, null);
	}

	/// state read/write ///

	public float getRad() { return radiation; }
	public void setRad(float radiation) { this.radiation = radiation; }
	public void incrementRad(float amount) { this.radiation += amount; }

	public float getDigamma() { return digamma; }
	public void setDigamma(float digamma) { this.digamma = digamma; }
	public void incrementDigamma(float amount) { this.digamma += amount; }

	public float getRadEnv() { return radEnv; }
	public void setRadEnv(float radEnv) { this.radEnv = radEnv; }

	public float getRadBuf() { return radBuf; }
	public void setRadBuf(float radBuf) { this.radBuf = radBuf; }

	public int getAsbestos() { return asbestos; }
	public void setAsbestos(int asbestos) { this.asbestos = asbestos; }

	public int getBlackLung() { return blacklung; }
	public void setBlackLung(int blacklung) { this.blacklung = blacklung; }

	public int getBombTimer() { return bombTimer; }
	public void setBombTimer(int bombTimer) { this.bombTimer = bombTimer; }

	public int getContagion() { return contagion; }
	public void setContagion(int contagion) { this.contagion = contagion; }

	public int getOil() { return oil; }
	public void setOil(int oil) { this.oil = oil; }

	public float getActivation() { return activation; }
	public void setActivation(float activation) { this.activation = activation; }

	public int getOxygen() { return oxygen; }
	public void setOxygen(int oxygen) { this.oxygen = oxygen; }

	public boolean hasGravity() { return gravity; }
	public void setGravity(boolean gravity) { this.gravity = gravity; }

	public List<ContaminationEffect> getContamination() { return contamination; }

	public void addContamination(ContaminationEffect effect) { contamination.add(effect); }

	/// static convenience forms (old call shape, so each site doesn't getData + null-check first) ///

	public static float getRadiation(EntityLivingBase entity) {
		if(!RadiationConfig.enableContamination)
			return 0;

		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getRad();
	}

	/** Adds and clamps to 0~2500: past 2500 dying doesn't get any deader, extra digits just make the gauge flicker */
	public static void incrementRadiation(EntityLivingBase entity, float amount) {
		if(!RadiationConfig.enableContamination)
			return;

		HbmLivingProps props = getData(entity);
		if(props == null)
			return;

		float radiation = props.getRad() + amount;
		if(radiation > 2500)
			radiation = 2500;
		if(radiation < 0)
			radiation = 0;

		props.setRad(radiation);
	}

	public static void setRadiation(EntityLivingBase entity, float rad) {
		if(!RadiationConfig.enableContamination)
			return;

		HbmLivingProps props = getData(entity);
		if(props != null) props.setRad(rad);
	}

	public static void setNeutronActivation(EntityLivingBase entity, float rad) {
		if(RadiationConfig.disableNeutron)
			return;

		HbmLivingProps props = getData(entity);
		if(props != null) props.setActivation(rad);
	}

	public static void incrementNeutronActivation(EntityLivingBase entity, float rad) {
		if(RadiationConfig.disableNeutron)
			return;

		HbmLivingProps props = getData(entity);
		if(props == null)
			return;

		float neutrons = props.getActivation() + rad;
		if(neutrons < 0)
			neutrons = 0;

		props.setActivation(neutrons);
	}

	public static void setRadEnv(EntityLivingBase entity, float rad) {
		HbmLivingProps props = getData(entity);
		if(props != null) props.setRadEnv(rad);
	}

	public static void setRadBuf(EntityLivingBase entity, float rad) {
		HbmLivingProps props = getData(entity);
		if(props != null) props.setRadBuf(rad);
	}

	/** Asbestosis: hitting MAX_ASBESTOS kills once, then resets and starts over */
	public static void setAsbestos(EntityLivingBase entity, int asbestos) {
		if(RadiationConfig.disableAsbestos)
			return;

		HbmLivingProps props = getData(entity);
		if(props == null)
			return;

		props.setAsbestos(asbestos);

		if(asbestos >= MAX_ASBESTOS) {
			props.setAsbestos(0);
			entity.attackEntityFrom(ModDamageSource.asbestos, 1000);
		}
	}

	public static void incrementAsbestos(EntityLivingBase entity, int asbestos) {
		if(RadiationConfig.disableAsbestos)
			return;

		setAsbestos(entity, getAsbestos(entity) + asbestos);
		informGasHazard(entity, "info.asbestos");
	}

	/** Black lung, same rule as asbestos, just takes far longer to fill */
	public static void setBlackLung(EntityLivingBase entity, int blacklung) {
		if(RadiationConfig.disableCoal)
			return;

		HbmLivingProps props = getData(entity);
		if(props == null)
			return;

		props.setBlackLung(blacklung);

		if(blacklung >= MAX_BLACKLUNG) {
			props.setBlackLung(0);
			entity.attackEntityFrom(ModDamageSource.blacklung, 1000);
		}
	}

	public static void incrementBlackLung(EntityLivingBase entity, int blacklung) {
		if(RadiationConfig.disableCoal)
			return;

		setBlackLung(entity, getBlackLung(entity) + blacklung);
		informGasHazard(entity, "info.coaldust");
	}

	/** Digamma doesn't touch current HP, it cuts the cap: the modifier is type 2 (multiplicative), so 1.0 means halved. */
	public static void setDigamma(EntityLivingBase entity, float digamma) {

		if(entity.getEntityWorld().isRemote)
			return;

		HbmLivingProps props = getData(entity);
		if(props == null)
			return;

		props.setDigamma(digamma);

		float healthMod = (float)Math.pow(0.5, props.getDigamma()) - 1F;

		IAttributeInstance attribute = entity.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
		attribute.removeModifier(DIGAMMA_UUID);
		attribute.applyModifier(new AttributeModifier(DIGAMMA_UUID, "digamma", healthMod, 2));

		if(entity.getHealth() > entity.getMaxHealth() && entity.getMaxHealth() > 0) {
			entity.setHealth(entity.getMaxHealth());
		}

		if((entity.getMaxHealth() <= 0 || props.getDigamma() >= 10.0F) && entity.isEntityAlive()) {
			entity.setAbsorptionAmount(0);
			entity.attackEntityFrom(ModDamageSource.digamma, 500F);
			entity.setHealth(0);
			entity.onDeath(ModDamageSource.digamma);

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "sweat");
			data.setInteger("count", 50);
			data.setInteger("block", Block.getIdFromBlock(Blocks.SOUL_SAND));
			data.setInteger("entity", entity.getEntityId());
			AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, 0, 0, 0),
					new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 50));
		}
	}

	public static void incrementDigamma(EntityLivingBase entity, float digamma) {

		// Ducks are immune to digamma. Matched by class name; takes effect automatically once EntityDuck lands
		if(isOfClass(entity, "com.hbm.entity.mob.EntityDuck"))
			digamma = 0.0F;

		HbmLivingProps props = getData(entity);
		if(props == null)
			return;

		float dRad = props.getDigamma() + digamma;
		if(dRad > 10)
			dRad = 10;
		if(dRad < 0)
			dRad = 0;

		setDigamma(entity, dRad);
	}

	/** The attribute modifier UUID matches old saves; swapping it strands the old bonus on every old save forever */
	private static final UUID DIGAMMA_UUID = UUID.fromString("2a3d8aec-5ab9-4218-9b8b-ca812bdf378b");

	private static boolean isOfClass(Object o, String className) {

		for(Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
			if(c.getName().equals(className))
				return true;
		}

		return false;
	}

	/** Tips go to real players only; NPCs don't need "you inhaled dust" */
	private static void informGasHazard(EntityLivingBase entity, String translationKey) {
		if(!(entity instanceof EntityPlayerMP))
			return;

		NetworkHandler.sendTo(new PlayerInformPacket(ChatBuilder.start("").nextTranslation(translationKey)
				.color(TextFormatting.RED).flush(), ServerProxy.ID_GAS_HAZARD, 3000), (EntityPlayerMP) entity);
	}

	public static void setTimer(EntityLivingBase entity, int bombTimer) {
		HbmLivingProps props = getData(entity);
		if(props != null) props.setBombTimer(bombTimer);
	}

	public static void setContagion(EntityLivingBase entity, int contagion) {
		HbmLivingProps props = getData(entity);
		if(props != null) props.setContagion(contagion);
	}

	public static void addCont(EntityLivingBase entity, ContaminationEffect cont) {
		HbmLivingProps props = getData(entity);
		if(props != null) props.addContamination(cont);
	}

	public static float getNeutronActivation(EntityLivingBase entity) {
		if(RadiationConfig.disableNeutron)
			return 0;

		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getActivation();
	}

	public static float getRadEnv(EntityLivingBase entity) {
		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getRadEnv();
	}

	public static float getRadBuf(EntityLivingBase entity) {
		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getRadBuf();
	}

	public static float getDigamma(EntityLivingBase entity) {
		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getDigamma();
	}

	public static int getAsbestos(EntityLivingBase entity) {
		if(RadiationConfig.disableAsbestos)
			return 0;

		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getAsbestos();
	}

	public static int getBlackLung(EntityLivingBase entity) {
		if(RadiationConfig.disableCoal)
			return 0;

		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getBlackLung();
	}

	public static int getTimer(EntityLivingBase entity) {
		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getBombTimer();
	}

	public static int getContagion(EntityLivingBase entity) {
		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getContagion();
	}

	public static int getOxy(EntityLivingBase entity) {
		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getOxygen();
	}

	public static int getOil(EntityLivingBase entity) {
		HbmLivingProps props = getData(entity);
		return props == null ? 0 : props.getOil();
	}

	public static void setOil(EntityLivingBase entity, int oil) {
		HbmLivingProps props = getData(entity);
		if(props != null) props.setOil(oil);
	}

	public static List<ContaminationEffect> getCont(EntityLivingBase entity) {
		HbmLivingProps props = getData(entity);
		return props == null ? new ArrayList<>() : props.getContamination();
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

		props.setFloat("hfr_radiation", radiation);
		props.setFloat("hfr_digamma", digamma);
		props.setInteger("hfr_asbestos", asbestos);
		props.setInteger("hfr_bomb", bombTimer);
		// With MKU off, nothing is saved or cleared — flipping settings shouldn't wipe infection values players already carry
		if(ServerConfig.ENABLE_MKU) props.setInteger("hfr_contagion", contagion);
		props.setInteger("hfr_blacklung", blacklung);
		props.setInteger("hfr_oil", oil);
		props.setInteger("hfr_oxygen", oxygen);
		props.setFloat("hfr_activation", activation);
		props.setBoolean("hfr_gravity", gravity);
		props.setInteger("hfr_fire", fire);
		props.setInteger("hfr_phosphorus", phosphorus);
		props.setInteger("hfr_balefire", balefire);
		props.setInteger("hfr_blackfire", blackFire);

		props.setInteger("hfr_cont_count", contamination.size());
		for(int i = 0; i < contamination.size(); i++) contamination.get(i).save(props, i);

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

		radiation = props.getFloat("hfr_radiation");
		digamma = props.getFloat("hfr_digamma");
		asbestos = props.getInteger("hfr_asbestos");
		bombTimer = props.getInteger("hfr_bomb");
		if(ServerConfig.ENABLE_MKU) contagion = props.getInteger("hfr_contagion");
		blacklung = props.getInteger("hfr_blacklung");
		oil = props.getInteger("hfr_oil");
		activation = props.getFloat("hfr_activation");
		oxygen = props.getInteger("hfr_oxygen");
		gravity = props.getBoolean("hfr_gravity");
		fire = props.getInteger("hfr_fire");
		phosphorus = props.getInteger("hfr_phosphorus");
		balefire = props.getInteger("hfr_balefire");
		blackFire = props.getInteger("hfr_blackfire");

		contamination.clear();
		int count = props.getInteger("hfr_cont_count");
		for(int i = 0; i < count; i++) contamination.add(ContaminationEffect.load(props, i));
	}

	/**
	 * How one infection decays: remaining strength shrinks linearly with "time left",
	 * so stacking the same contaminant longer just weakens it, rather than vanishing all at once on expiry.
	 */
	public static class ContaminationEffect {

		public float maxRad;
		public int maxTime;
		public int time;
		public boolean ignoreArmor;

		public ContaminationEffect(float rad, int time, boolean ignoreArmor) {
			this.maxRad = rad;
			this.maxTime = this.time = time;
			this.ignoreArmor = ignoreArmor;
		}

		public float getRad() {
			if(maxTime <= 0) return 0;
			return maxRad * ((float) time / (float) maxTime);
		}

		public void serialize(ByteBuf buf) {
			buf.writeFloat(this.maxRad);
			buf.writeInt(this.maxTime);
			buf.writeInt(this.time);
			buf.writeBoolean(ignoreArmor);
		}

		public static ContaminationEffect deserialize(ByteBuf buf) {
			float maxRad = buf.readFloat();
			int maxTime = buf.readInt();
			int time = buf.readInt();
			boolean ignoreArmor = buf.readBoolean();
			ContaminationEffect effect = new ContaminationEffect(maxRad, maxTime, ignoreArmor);
			effect.time = time;
			return effect;
		}

		public void save(NBTTagCompound nbt, int index) {
			NBTTagCompound me = new NBTTagCompound();
			me.setFloat("maxRad", this.maxRad);
			me.setInteger("maxTime", this.maxTime);
			me.setInteger("time", this.time);
			me.setBoolean("ignoreArmor", ignoreArmor);
			nbt.setTag("cont_" + index, me);
		}

		public static ContaminationEffect load(NBTTagCompound nbt, int index) {
			NBTBase base = nbt.getTag("cont_" + index);
			if(!(base instanceof NBTTagCompound)) return new ContaminationEffect(0, 1, false);
			NBTTagCompound me = (NBTTagCompound) base;

			ContaminationEffect effect = new ContaminationEffect(me.getFloat("maxRad"), me.getInteger("maxTime"), me.getBoolean("ignoreArmor"));
			effect.time = me.getInteger("time");
			return effect;
		}
	}
}
