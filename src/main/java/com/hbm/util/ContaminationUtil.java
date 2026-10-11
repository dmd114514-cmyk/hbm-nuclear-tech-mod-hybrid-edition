package com.hbm.util;

import com.hbm.config.RadiationConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.HazmatRegistry;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.potion.HbmPotion;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.entity.IRadiationImmune;
import api.hbm.hazard.HazardClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The unified entry for the three invisible goods-and-bads: radiation, neutron, digamma.
 *
 * A thin wrapper doing three things: deciding whether this dose should be blocked (hazmat, Faraday, stabilizers, creative, first 10 seconds after join),
 * accumulating what gets through onto {@link HbmLivingProps}, and turning Geiger/dosimeter readings into human speech.
 *
 * Ambient radiation (the chunk system) never enters here; that side only converts "how much RAD is around" into one contaminate call,
 * and the reverse path doesn't exist, so the two never double-book.
 */
public class ContaminationUtil {

	/** How much this gear blocks: Resistance closer to 1 is safer, summing all but the worst of the four pieces */
	public static float calculateRadiationMod(EntityLivingBase entity) {

		if(entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;

			float koeff = 10.0F;
			return (float) Math.pow(koeff, -HazmatRegistry.getResistance(player));
		}

		return 1;
	}

	public static float getRads(Entity e) {

		if(!(e instanceof EntityLivingBase))
			return 0.0F;

		if(isRadImmune(e))
			return 0.0F;

		EntityLivingBase entity = (EntityLivingBase)e;

		return HbmLivingProps.getRadiation(entity);
	}

	public static boolean isRadImmune(Entity e) {

		if(e instanceof EntityLivingBase && ((EntityLivingBase)e).isPotionActive(HbmPotion.mutation))
			return true;

		// our own mobs plus any mod declaring immunity implement this port and done
		if(e instanceof IRadiationImmune) return true;

		if(e instanceof EntityMooshroom) return true;
		if(e instanceof EntityZombie) return true;
		if(e instanceof EntitySkeleton) return true;

		if("cyano.lootable.entities.EntityLootableBody".equals(e.getClass().getName())) return true;

		return false;
	}

	/// asbestos ///
	public static void applyAsbestos(Entity e, int i) {

		if(!(e instanceof EntityLivingBase))
			return;

		if(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode)
			return;

		if(e instanceof EntityPlayer && e.ticksExisted < 200)
			return;

		EntityLivingBase entity = (EntityLivingBase)e;

		if(ArmorRegistry.hasAllProtection(entity, 3, HazardClass.PARTICLE_FINE))
			ArmorUtil.damageGasMaskFilter(entity, i);
		else
			HbmLivingProps.incrementAsbestos(entity, i);
	}

	/// DIGAMMA ///
	public static void applyDigammaData(Entity e, float f) {

		if(!(e instanceof EntityLivingBase))
			return;

		// Ducks and ocelots are immune to digamma. The duck matches by class name, taking effect once EntityDuck lands
		if(isOfClass(e, "com.hbm.entity.mob.EntityDuck") || e instanceof EntityOcelot)
			return;

		if(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode)
			return;

		if(e instanceof EntityPlayer && e.ticksExisted < 200)
			return;

		EntityLivingBase entity = (EntityLivingBase)e;

		if(entity.isPotionActive(HbmPotion.stability))
			return;

		if(!(entity instanceof EntityPlayer && ArmorUtil.checkForDigamma((EntityPlayer) entity)))
			HbmLivingProps.incrementDigamma(entity, f);
	}

	public static void applyDigammaDirect(Entity e, float f) {

		if(!(e instanceof EntityLivingBase))
			return;

		if(e instanceof IRadiationImmune)
			return;

		if(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode)
			return;

		EntityLivingBase entity = (EntityLivingBase)e;
		HbmLivingProps.incrementDigamma(entity, f);
	}

	public static float getDigamma(Entity e) {

		if(!(e instanceof EntityLivingBase))
			return 0.0F;

		EntityLivingBase entity = (EntityLivingBase)e;
		return HbmLivingProps.getDigamma(entity);
	}

	public static void printGeigerData(EntityPlayer player) {

		World world = player.getEntityWorld();

		double eRad = ((int)(HbmLivingProps.getRadiation(player) * 10)) / 10D;

		double neut = ((int)(HbmLivingProps.getNeutronActivation(player) * 10)) / 10D;

		double rads = ((int)(ChunkRadiationManager.proxy.getRadiation(world, (int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ)) * 10)) / 10D;
		double env = ((int)(HbmLivingProps.getRadBuf(player) * 10D)) / 10D;

		double res = ((int)(10000D - ContaminationUtil.calculateRadiationMod(player) * 10000D)) / 100D;
		double resKoeff = ((int)(HazmatRegistry.getResistance(player) * 100D)) / 100D;

		String chunkPrefix = getPreffixFromRad(rads);
		String envPrefix = getPreffixFromRad(env);
		String radPrefix = "";
		String resPrefix = "" + TextFormatting.WHITE;

		if(eRad < 200)
			radPrefix += TextFormatting.GREEN;
		else if(eRad < 400)
			radPrefix += TextFormatting.YELLOW;
		else if(eRad < 600)
			radPrefix += TextFormatting.GOLD;
		else if(eRad < 800)
			radPrefix += TextFormatting.RED;
		else if(eRad < 1000)
			radPrefix += TextFormatting.DARK_RED;
		else
			radPrefix += TextFormatting.DARK_GRAY;

		if(resKoeff > 0)
			resPrefix += TextFormatting.GREEN;

		//localization plus "the server must be able to send it too" made this section filthy, but it's the kind of dirty that runs
		player.sendMessage(new TextComponentString("===== ☢ ").appendSibling(new TextComponentTranslation("geiger.title")).appendSibling(new TextComponentString(" ☢ =====")).setStyle(new Style().setColor(TextFormatting.GOLD)));
		player.sendMessage(new TextComponentTranslation("geiger.chunkRad").appendSibling(new TextComponentString(" " + chunkPrefix + rads + " RAD/s")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
		player.sendMessage(new TextComponentTranslation("geiger.envRad").appendSibling(new TextComponentString(" " + envPrefix + env + " RAD/s")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
		player.sendMessage(new TextComponentTranslation("geiger.playerRad").appendSibling(new TextComponentString(" " + radPrefix + eRad + " RAD")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
		player.sendMessage(new TextComponentTranslation("geiger.playerAct").appendSibling(new TextComponentString(" " + envPrefix + neut + " RAD/s")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
		player.sendMessage(new TextComponentTranslation("geiger.playerRes").appendSibling(new TextComponentString(" " + resPrefix + res + "% (" + resKoeff + ")")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
	}

	public static void printDosimeterData(EntityPlayer player) {

		double env = ((int)(HbmLivingProps.getRadBuf(player) * 10D)) / 10D;
		boolean limit = false;

		if(env > 3.6D) {
			env = 3.6D;
			limit = true;
		}

		String envPrefix = getPreffixFromRad(env);

		player.sendMessage(new TextComponentString("===== ☢ ").appendSibling(new TextComponentTranslation("geiger.title.dosimeter")).appendSibling(new TextComponentString(" ☢ =====")).setStyle(new Style().setColor(TextFormatting.GOLD)));
		player.sendMessage(new TextComponentTranslation("geiger.envRad").appendSibling(new TextComponentString(" " + envPrefix + (limit ? ">" : "") + env + " RAD/s")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
	}

	public static String getPreffixFromRad(double rads) {

		String chunkPrefix = "";

		if(rads == 0)
			chunkPrefix += TextFormatting.GREEN;
		else if(rads < 1)
			chunkPrefix += TextFormatting.YELLOW;
		else if(rads < 10)
			chunkPrefix += TextFormatting.GOLD;
		else if(rads < 100)
			chunkPrefix += TextFormatting.RED;
		else if(rads < 1000)
			chunkPrefix += TextFormatting.DARK_RED;
		else
			chunkPrefix += TextFormatting.DARK_GRAY;

		return chunkPrefix;
	}

	public static void printDiagnosticData(EntityPlayer player) {

		double digamma = ((int)(HbmLivingProps.getDigamma(player) * 100)) / 100D;
		double halflife = ((int)((1D - Math.pow(0.5, digamma)) * 10000)) / 100D;

		player.sendMessage(new TextComponentString("===== Ϝ ").appendSibling(new TextComponentTranslation("digamma.title")).appendSibling(new TextComponentString(" Ϝ =====")).setStyle(new Style().setColor(TextFormatting.DARK_PURPLE)));
		player.sendMessage(new TextComponentTranslation("digamma.playerDigamma").appendSibling(new TextComponentString(TextFormatting.RED + " " + digamma + " DRX")).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));
		player.sendMessage(new TextComponentTranslation("digamma.playerHealth").appendSibling(new TextComponentString(TextFormatting.RED + " " + halflife + "%")).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));
		player.sendMessage(new TextComponentTranslation("digamma.playerRes").appendSibling(new TextComponentString(TextFormatting.BLUE + " " + "N/A")).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));
	}

	public static enum HazardType {
		RADIATION,
		NEUTRON,
		DIGAMMA
	}

	public static enum ContaminationType {
		FARADAY,			//blockable by metal armor
		HAZMAT,				//blockable by hazmat
		HAZMAT2,			//blockable by heavy hazmat
		DIGAMMA,			//blockable by FAU/DNS or stabilizers
		DIGAMMA2,			//blockable by robes
		CREATIVE,			//blocked in creative; counts for hazmat bonuses but ignores radiation resistance
		RAD_BYPASS,			//same as CREATIVE, but skips the resistance conversion
		NONE				// does not block
	}

	/*
	 * This "one function runs everything" shape isn't great, and the cont type names read wrong easily.
	 * The better shape is ArmorRegistry for protection + HbmLivingProps for bookkeeping, each on its own side —
	 * regular radiation is fine through here; prefer those two elsewhere.
	 */
	public static boolean contaminate(EntityLivingBase entity, HazardType hazard, ContaminationType cont, float amount) {

		if(hazard == HazardType.RADIATION) {
			float radEnv = HbmLivingProps.getRadEnv(entity);
			HbmLivingProps.setRadEnv(entity, radEnv + amount);
		}

		if(hazard == HazardType.NEUTRON && !RadiationConfig.disableNeutron) {
			float radEnv = HbmLivingProps.getRadEnv(entity);
			HbmLivingProps.setRadEnv(entity, radEnv + amount*10);
		}

		if(entity instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer)entity;

			switch(cont) {
			case FARADAY:			if(ArmorUtil.checkForFaraday(player))	return false; break;
			case HAZMAT:			if(ArmorUtil.checkForHazmat(player))	return false; break;
			case HAZMAT2:			if(ArmorUtil.checkForHaz2(player))		return false; break;
			case DIGAMMA:			if(ArmorUtil.checkForDigamma(player))	return false; if(ArmorUtil.checkForDigamma2(player))	return false; break;
			case DIGAMMA2:			if(ArmorUtil.checkForDigamma2(player))	return false; break;
			}

			if(player.capabilities.isCreativeMode && cont != ContaminationType.NONE && cont != ContaminationType.DIGAMMA2)
				return false;

			if(player.ticksExisted < 200)
				return false;
		}

		if(hazard == HazardType.RADIATION && isRadImmune(entity))
			return false;

		switch(hazard) {
		case RADIATION: HbmLivingProps.incrementRadiation(entity, amount * (cont == ContaminationType.RAD_BYPASS ? 1 : calculateRadiationMod(entity))); break;
		case NEUTRON: HbmLivingProps.incrementNeutronActivation(entity, amount * (cont == ContaminationType.RAD_BYPASS ? 1 : calculateRadiationMod(entity))); break;
		case DIGAMMA: HbmLivingProps.incrementDigamma(entity, amount); break;
		}

		return true;
	}

	private static boolean isOfClass(Object o, String className) {

		for(Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
			if(c.getName().equals(className))
				return true;
		}

		return false;
	}
}
