package com.hbm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.hbm.config.ServerConfig;
import com.hbm.items.ModItems;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

/**
 * The damage-dealing master entry.
 *
 * Vanilla's "armor first, then enchantments" can't express two things this mod needs: how much penetration a hit carries,
 * and which armor layer blocks which damage type. So the damage math is taken over and run here,
 * with penetration passed to the two events via {@link DamageResistanceHandler}'s static pair.
 *
 * Default is the SEDNA path (self-computed); flipping DAMAGE_COMPATIBILITY_MODE in config switches to "let vanilla compute,
 * we only handle knockback and i-frames" — an escape hatch for mobs that override attackEntityFrom.
 */
public class EntityDamageUtil {

	/*Damage from a chest-mounted V1 gets special handling; the armor mod system hasn't landed, so this honestly reports none */
	public static boolean wasAttackedByV1(DamageSource source) {

		if(source instanceof EntityDamageSource) {
			Entity attacker = ((EntityDamageSource) source).getTrueSource();

			if(attacker instanceof EntityPlayer) {
				// TODO once com.hbm.handler.ArmorModHandler lands, read whether the chestplate's mods include v1
			}
		}

		return false;
	}

	/** The ugly trick: if the first hit doesn't land, hit again with "the previous damage" to pierce i-frames */
	@Deprecated
	public static boolean attackEntityFromIgnoreIFrame(Entity victim, DamageSource src, float damage) {

		if(!victim.attackEntityFrom(src, damage)) {

			if(victim instanceof EntityLivingBase) {
				EntityLivingBase living = (EntityLivingBase) victim;

				if(living.hurtResistantTime > living.maxHurtResistantTime / 2.0F) {
					damage += lastDamage(living);
				}
			}
			return victim.attackEntityFrom(src, damage);
		} else {
			return true;
		}
	}

	/** The new damage math; only this door */
	public static boolean attackEntityFromNT(EntityLivingBase living, DamageSource source, float amount, boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDT, float pierce) {
		if(living instanceof EntityPlayerMP && source.getTrueSource() instanceof EntityPlayer) {
			EntityPlayerMP playerMP = (EntityPlayerMP) living;
			EntityPlayer attacker = (EntityPlayer) source.getTrueSource();
			if(!playerMP.canAttackPlayer(attacker)) return false; //PVE rules and scoreboard friendly-fire both live here
		}
		DamageResistanceHandler.setup(pierceDT, pierce);
		boolean ret = attackEntityFromNTInternal(living, source, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier);
		DamageResistanceHandler.reset();
		return ret;
	}

	private static boolean attackEntityFromNTInternal(EntityLivingBase living, DamageSource source, float amount, boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier) {
		boolean superCompatibility = ServerConfig.DAMAGE_COMPATIBILITY_MODE;
		return superCompatibility
				? attackEntitySuperCompatibility(living, source, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier)
				: attackEntitySEDNAPatch(living, source, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier);
	}

	/**
	 * Lets vanilla compute; we only re-apply knockback and i-frames this mod's way.
	 * The cost: DR penetration doesn't apply to vanilla armor.
	 */
	private static boolean attackEntitySuperCompatibility(EntityLivingBase living, DamageSource source, float amount, boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier) {
		//disable iframes
		if(ignoreIFrame) { setLastDamage(living, 0F); living.hurtResistantTime = 0; }
		//cache last velocity
		// All three lines reading motionX is a typo carried verbatim from the old version (each should read its own axis); in compatibility mode it
		double motionX = living.motionX;
		double motionY = living.motionX;
		double motionZ = living.motionX;
		//bam!
		boolean ret = living.attackEntityFrom(source, amount);
		//restore last velocity
		living.motionX = motionX;
		living.motionY = motionY;
		living.motionZ = motionZ;
		//apply own knockback
		Entity entity = source.getTrueSource();
		if(entity != null) {
			double deltaX = entity.posX - living.posX;
			double deltaZ;

			for(deltaZ = entity.posZ - living.posZ; deltaX * deltaX + deltaZ * deltaZ < 1.0E-4D; deltaZ = (Math.random() - Math.random()) * 0.01D) {
				deltaX = (Math.random() - Math.random()) * 0.01D;
			}

			living.attackedAtYaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - living.rotationYaw;
			if(knockbackMultiplier > 0) knockBack(living, entity, amount, deltaX, deltaZ, knockbackMultiplier);
		}
		return ret;
	}

	/**
	 * Reimplements vanilla's damage flow (only down to Entity; subclass overrides are bypassed by nature).
	 *
	 * Three unavoidable deltas from the old version: 1.12.2 dropped prevHealth/entityAge (death messages
	 * now come from CombatTracker), and "is immune" is asked per damage source, no longer one global flag.
	 */
	private static boolean attackEntitySEDNAPatch(EntityLivingBase living, DamageSource source, float amount, boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier) {
		living.attackEntityFrom(source, 0F);
		if(ignoreIFrame) setLastDamage(living, 0F);
		if(ForgeHooks.onLivingAttack(living, source, amount) && allowSpecialCancel) return false;
		if(living.isEntityInvulnerable(source)) return false;
		if(living.getEntityWorld().isRemote) return false;
		if(living instanceof EntityPlayer && ((EntityPlayer) living).capabilities.disableDamage && !source.canHarmInCreative()) return false;

		if(living.getHealth() <= 0.0F) return false;
		if(source.isFireDamage() && living.isPotionActive(MobEffects.FIRE_RESISTANCE)) return false;

		living.limbSwingAmount = 1.5F;
		boolean didAttackRegister = true;

		if(living.hurtResistantTime > living.maxHurtResistantTime / 2.0F && !ignoreIFrame) {
			if(amount <= lastDamage(living)) { return false; }
			damageEntityNT(living, source, amount - lastDamage(living));
			setLastDamage(living, amount);
			didAttackRegister = false;
		} else {
			setLastDamage(living, amount);
			living.hurtResistantTime = living.maxHurtResistantTime;
			damageEntityNT(living, source, amount);
			living.hurtTime = living.maxHurtTime = 10;
		}

		living.attackedAtYaw = 0.0F;
		Entity entity = source.getTrueSource();

		if(entity != null) {
			if(entity instanceof EntityLivingBase) {
				living.setRevengeTarget((EntityLivingBase) entity);
			}

			if(entity instanceof EntityPlayer) {
				setRecentlyHit(living, 100);
				setAttackingPlayer(living, (EntityPlayer) entity);
			} else if(entity instanceof EntityTameable) {
				EntityTameable entitywolf = (EntityTameable) entity;

				if(entitywolf.isTamed()) {
					setRecentlyHit(living, 100);
					setAttackingPlayer(living, null);
				}
			}
		}

		if(didAttackRegister) {
			living.getEntityWorld().setEntityState(living, (byte) 2);

			if(source != DamageSource.DROWN) setBeenAttacked(living);

			if(entity != null) {
				double deltaX = entity.posX - living.posX;
				double deltaZ;

				for(deltaZ = entity.posZ - living.posZ; deltaX * deltaX + deltaZ * deltaZ < 1.0E-4D; deltaZ = (Math.random() - Math.random()) * 0.01D) {
					deltaX = (Math.random() - Math.random()) * 0.01D;
				}

				living.attackedAtYaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - living.rotationYaw;
				if(knockbackMultiplier > 0) knockBack(living, entity, amount, deltaX, deltaZ, knockbackMultiplier);
			} else {
				living.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
			}
		}

		if(living.getHealth() <= 0.0F) {
			playHurtSound(living, getDeathSound(living), amount);
			living.onDeath(source);
		} else {
			playHurtSound(living, getHurtSound(living, source), amount);
		}

		return true;
	}

	private static void playHurtSound(EntityLivingBase living, SoundEvent sound, float amount) {
		if(sound == null) return;
		living.playSound(sound, getSoundVolume(living), getSoundPitch(living));
	}

	public static void knockBack(EntityLivingBase living, Entity attacker, float damage, double motionX, double motionZ, double multiplier) {
		if(living.getRNG().nextDouble() >= living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()) {
			living.isAirBorne = true;
			double horizontal = Math.sqrt(motionX * motionX + motionZ * motionZ);
			double magnitude = 0.4D * multiplier;
			living.motionX /= 2.0D;
			living.motionY /= 2.0D;
			living.motionZ /= 2.0D;
			living.motionX -= motionX / horizontal * magnitude;
			living.motionY += magnitude;
			living.motionZ -= motionZ / horizontal * magnitude;

			if(living.motionY > 0.2D) {
				living.motionY = 0.2D * multiplier;
			}
		}
	}

	public static void damageEntityNT(EntityLivingBase living, DamageSource source, float amount) {
		if(!living.isEntityInvulnerable(source)) {
			amount = ForgeHooks.onLivingHurt(living, source, amount);
			if(amount <= 0) return;

			amount = applyArmorCalculationsNT(living, source, amount);
			amount = applyPotionDamageCalculations(living, source, amount);

			float originalAmount = amount;
			amount = Math.max(amount - living.getAbsorptionAmount(), 0.0F);
			living.setAbsorptionAmount(living.getAbsorptionAmount() - (originalAmount - amount));

			if(amount != 0.0F) {
				float health = living.getHealth();
				living.setHealth(health - amount);
				// The old version passed "health before the hit" as the second CombatTracker argument; 1.12.2 wants
				// (raw amount of this hit, amount after armor), which is the "x damage" in death messages
				living.getCombatTracker().trackDamage(source, originalAmount, amount);
				living.setAbsorptionAmount(living.getAbsorptionAmount() - amount);
			}
		}
	}

	public static float applyArmorCalculationsNT(EntityLivingBase living, DamageSource source, float amount) {
		if(!source.isUnblockable()) {
			float i = 25F - (living.getTotalArmorValue() * (1 - DamageResistanceHandler.currentPDR));
			float armor = amount * i;
			damageArmorNT(living, amount);
			amount = armor / 25.0F;
		}

		return amount;
	}

	/** Armor durability is the equipment layer's own job; only a hook remains here */
	/** Resistance potions and enchantments both compute as "a fraction of 25 removed", and stack */
	public static float applyPotionDamageCalculations(EntityLivingBase living, DamageSource source, float amount) {

		if(source.isDamageAbsolute()) return amount;

		if(living.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
			int reduction = (living.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
			amount = amount * (25 - reduction) / 25.0F;
		}

		if(amount <= 0.0F) return 0.0F;

		// The old version asked "all worn equipment"; 1.12.2's equivalent is the armor list's iterable view
		int j = EnchantmentHelper.getEnchantmentModifierDamage(living.getArmorInventoryList(), source);
		j = Math.min(j, 20);

		if(j > 0) {
			amount = amount * (25 - j) / 25.0F;
		}

		return amount;
	}

	public static void damageArmorNT(EntityLivingBase living, float amount) { }

	/** Vanilla's own, kept for damage sources not yet migrated */
	@Deprecated
	public static boolean attackEntityFromNT(EntityLivingBase living, DamageSource source, float amount) {

		if(ForgeHooks.onLivingAttack(living, source, amount))
			return false;
		if(living.isEntityInvulnerable(source)) {
			return false;
		} else if(living.getEntityWorld().isRemote) {
			return false;
		} else {
			if(living.getHealth() <= 0.0F) {
				return false;
			} else if(source.isFireDamage() && living.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
				return false;
			} else {
				ItemStack helmet = living.getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.HEAD);
				if((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && helmet != null) {
					helmet.damageItem((int) (amount * 4.0F + living.getRNG().nextFloat() * amount * 2.0F), living);
					amount *= 0.75F;
				}

				living.limbSwingAmount = 1.5F;
				boolean flag = true;

				if((float) living.hurtResistantTime > (float) living.maxHurtResistantTime / 2.0F) {
					if(amount <= lastDamage(living)) {
						return false;
					}

					damageEntityNT(living, source, amount - lastDamage(living));
					setLastDamage(living, amount);
					flag = false;
				} else {
					setLastDamage(living, amount);
					living.hurtResistantTime = living.maxHurtResistantTime;
					damageEntityNT(living, source, amount);
					living.hurtTime = living.maxHurtTime = 10;
				}

				living.attackedAtYaw = 0.0F;
				Entity entity = source.getTrueSource();

				if(entity != null) {
					if(entity instanceof EntityLivingBase) {
						living.setRevengeTarget((EntityLivingBase) entity);
					}

					if(entity instanceof EntityPlayer) {
						setRecentlyHit(living, 100);
						setAttackingPlayer(living, (EntityPlayer) entity);
					} else if(entity instanceof EntityTameable) {
						EntityTameable entitywolf = (EntityTameable) entity;

						if(entitywolf.isTamed()) {
							setRecentlyHit(living, 100);
							setAttackingPlayer(living, null);
						}
					}
				}

				if(flag) {
					living.getEntityWorld().setEntityState(living, (byte) 2);

					if(source != DamageSource.DROWN) {
						setBeenAttacked(living);
					}

					if(entity != null) {
						double d1 = entity.posX - living.posX;
						double d0;

						for(d0 = entity.posZ - living.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
							d1 = (Math.random() - Math.random()) * 0.01D;
						}

						living.attackedAtYaw = (float) (Math.atan2(d0, d1) * 180.0D / Math.PI) - living.rotationYaw;
						living.knockBack(entity, amount, d1, d0);
					} else {
						living.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
					}
				}

				if(living.getHealth() <= 0.0F) {
					playHurtSound(living, getDeathSound(living), amount);
					living.onDeath(source);
				} else {
					playHurtSound(living, getHurtSound(living, source), amount);
				}

				return true;
			}
		}
	}

	/*
	 * The four sound getters below are protected, unreachable outside the package; vanilla plays them itself, and bypassing that to compute damage
	 * means asking for them via reflection. The returned SoundEvent may be null (some entities define none); callers must check.
	 */

	public static SoundEvent getDeathSound(EntityLivingBase living) {
		SoundEvent s = callProtected(living, SoundEvent.class, "getDeathSound", "func_184610_bc");
		return s != null ? s : DEATH_FALLBACK;
	}

	public static SoundEvent getHurtSound(EntityLivingBase living, DamageSource source) {
		SoundEvent s = callProtected(living, SoundEvent.class, "getHurtSound", "func_184607_bt", source);
		return s != null ? s : HURT_FALLBACK;
	}

	public static float getSoundVolume(EntityLivingBase living) {
		Float f = callProtected(living, Float.class, "getSoundVolume", "func_70599_aP");
		return f == null ? 1F : f.floatValue();
	}

	public static float getSoundPitch(EntityLivingBase living) {
		Float f = callProtected(living, Float.class, "getSoundPitch", "func_184630_bo");
		return f == null ? 1F : f.floatValue();
	}

	private static final SoundEvent DEATH_FALLBACK = sound("entity.generic.death");
	private static final SoundEvent HURT_FALLBACK = sound("entity.generic.hurt");

	private static SoundEvent sound(String name) {
		return SoundEvent.REGISTRY.getObject(new ResourceLocation(name));
	}

	/** Finds the protected method by both MCP and SRG names; falls back to the caller's default when absent */
	private static <T> T callProtected(EntityLivingBase living, Class<T> type, String mcp, String srg, Object... args) {

		for(Class<?> c = living.getClass(); c != null; c = c.getSuperclass()) {
			for(Method m : c.getDeclaredMethods()) {

				if((m.getName().equals(mcp) || m.getName().equals(srg)) && m.getParameterTypes().length == args.length) {
					try {
						m.setAccessible(true);
						return type.cast(m.invoke(living, args));
					} catch(Exception ex) {
						return null;
					}
				}
			}
		}

		return null;
	}

	/** Whether to sync the velocity change after being hit — vanilla's own single line */
	public static void setBeenAttacked(EntityLivingBase living) {
		living.velocityChanged = living.getRNG().nextDouble() >= living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
	}

	/*
	 * 1.12.2 made the i-frame fields protected (public before), and this whole system exists to bypass vanilla's
	 * "one hit per tick" rule — without reading them you can't know "how much the last hit dealt".
	 * Reflection rather than an AccessTransformer: this mod doesn't touch build-time class rewriting; the cost is a slightly slower read in a few spots.
	 */

	private static float lastDamage(EntityLivingBase living) {
		return readFloat(living, "lastDamage", "field_70738_a");
	}

	private static void setLastDamage(EntityLivingBase living, float value) {
		writeField(living, "lastDamage", "field_70738_a", value);
	}

	private static void setRecentlyHit(EntityLivingBase living, int value) {
		writeField(living, "recentlyHit", "field_70724_aO", value);
	}

	private static void setAttackingPlayer(EntityLivingBase living, EntityPlayer player) {
		writeField(living, "attackingPlayer", "field_70725_aJ", player);
	}

	private static float readFloat(EntityLivingBase living, String mcp, String srg) {
		try {
			Field f = field(living.getClass(), mcp, srg);
			f.setAccessible(true);
			return f.getFloat(living);
		} catch(Exception ex) {
			return 0F;
		}
	}

	private static void writeField(EntityLivingBase living, String mcp, String srg, Object value) {
		try {
			Field f = field(living.getClass(), mcp, srg);
			f.setAccessible(true);
			f.set(living, value);
		} catch(Exception ex) {
			// On failure, treat it as unset: worst case this hit still obeys vanilla's i-frames — never break the whole damage flow over it
		}
	}

	private static Field field(Class<?> clazz, String mcp, String srg) throws NoSuchFieldException {

		for(Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			try {
				return c.getDeclaredField(mcp);
			} catch(NoSuchFieldException ignored) { }
			try {
				return c.getDeclaredField(srg);
			} catch(NoSuchFieldException ignored) { }
		}

		throw new NoSuchFieldException(mcp);
	}

	/** The old "who is the crosshair on" implementation; vanilla's misses things in multiplayer */
	public static RayTraceResult getMouseOver(EntityPlayer attacker, double reach) {
		return getMouseOver(attacker, reach, 0D);
	}

	@SuppressWarnings("unchecked")
	public static RayTraceResult getMouseOver(EntityPlayer attacker, double reach, double threshold) {

		World world = attacker.getEntityWorld();
		RayTraceResult objectMouseOver = null;
		Entity pointedEntity = null;

		objectMouseOver = rayTrace(attacker, reach, 1F);

		Vec3d pos = getPosition(attacker);
		Vec3d look = attacker.getLook(1F);
		Vec3d end = pos.addVector(look.x * reach, look.y * reach, look.z * reach);
		Vec3d hitvec = null;
		double grace = 1.0D;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(attacker, attacker.getEntityBoundingBox().expand(look.x * reach, look.y * reach, look.z * reach).expand(grace, grace, grace));

		double closest = reach;

		for(int i = 0; i < list.size(); ++i) {
			Entity entity = list.get(i);

			if(entity.canBeCollidedWith() && entity.isEntityAlive()) {

				double borderSize = entity.getCollisionBorderSize() + threshold;
				AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
				RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(pos, end);

				if(axisalignedbb.contains(pos)) {
					if(0.0D <= closest) {
						pointedEntity = entity;
						hitvec = movingobjectposition == null ? pos : movingobjectposition.hitVec;
						closest = 0.0D;
					}

				} else if(movingobjectposition != null) {
					double dist = pos.distanceTo(movingobjectposition.hitVec);

					if(dist < closest || closest == 0.0D) {
						if(entity == attacker.getRidingEntity() && !entity.canRiderInteract()) {
							if(closest == 0.0D) {
								pointedEntity = entity;
								hitvec = movingobjectposition.hitVec;
							}
						} else {
							pointedEntity = entity;
							hitvec = movingobjectposition.hitVec;
							closest = dist;
						}
					}
				}
			}
		}

		if(pointedEntity != null && (closest < reach || objectMouseOver == null)) {
			objectMouseOver = new RayTraceResult(pointedEntity, hitvec);
		}

		return objectMouseOver;
	}

	public static RayTraceResult rayTrace(EntityPlayer player, double dist, float interp) {
		Vec3d pos = getPosition(player);
		Vec3d look = player.getLook(interp);
		Vec3d end = pos.addVector(look.x * dist, look.y * dist, look.z * dist);
		return player.getEntityWorld().rayTraceBlocks(pos, end, false, false, true);
	}

	/**
	 * The eye position. While sneaking, vanilla's yOffset already includes that fraction,
	 * and adding eye height again would measure from above the head — kept per the old version's "crooked" check.
	 */
	public static Vec3d getPosition(EntityPlayer player) {
		boolean client = player.getYOffset() != 0;
		return new Vec3d(player.posX, player.posY + (client ? 0 : player.getEyeHeight()), player.posZ);
	}
}
