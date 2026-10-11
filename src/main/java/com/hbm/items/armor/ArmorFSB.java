package com.hbm.items.armor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.HazmatRegistry;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.NotableComments;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import api.hbm.hazard.HazardClass;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ShadyUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.net.Tuple.Pair;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The "full set counts" armor base class.
 *
 * One piece alone has no effects; all four together trigger the set bonuses (jump/fall/footstep sounds, potion effects, radiation resistance,
 * hazmat classes...) — that's what FSB stands for. The check's entry is {@link #hasFSBArmor}.
 *
 * The 1.12.2 conversions, noted here so every subclass doesn't re-step on them:
 * · `armorType` went from the "0 head 1 chest 2 legs 3 feet" int to the `EntityEquipmentSlot` enum,
 *   with the constructor still taking the old ordinal, converted once by {@link ModArmor#slotOf(int)};
 * · `inventory.armorInventory` is a `NonNullList` ordered feet/legs/chest/head (the old code read in the same order,
 *   so indices don't move — but use `get(i)` rather than `[i]`);
 * · the old `Vec3` was mutable with `createVectorHelper`; this version's `Vec3d` has three final fields;
 * · sounds went from "strings as keys" to `SoundEvent`, unified through {@link ItemSounds};
 * · `ItemArmor.getArmorModel` gains a `ModelBiped _default` argument here — the head-piece models
 *   (the M65 mask family) live in the render layer; this class doesn't touch them.
 */
@NotableComments
public class ArmorFSB extends ItemArmor implements IArmorDisableModel {

	private String texture = "";
	private ResourceLocation overlay = null;
	public List<PotionEffect> effects = new ArrayList<PotionEffect>();
	public boolean noHelmet = false;
	public boolean vats = false;
	public boolean thermal = false;
	public boolean geigerSound = false;
	public boolean customGeiger = false;
	public boolean hardLanding = false;
	public int dashCount = 0;
	public int stepSize = 0;
	public String step;
	public String jump;
	public String fall;
	public boolean canSeal;
	public double radResist = 0;

	public ArmorFSB(ArmorMaterial material, int slot, String texture) {
		super(material, 0, ModArmor.slotOf(slot));
		this.texture = texture;
	}

	public ArmorFSB addEffect(PotionEffect effect) {
		effects.add(effect);
		return this;
	}

	public ArmorFSB setNoHelmet(boolean noHelmet) {
		this.noHelmet = noHelmet;
		return this;
	}

	public ArmorFSB enableVATS(boolean vats) {
		this.vats = vats;
		return this;
	}

	public ArmorFSB enableThermalSight(boolean thermal) {
		this.thermal = thermal;
		return this;
	}

	public ArmorFSB setHasGeigerSound(boolean geiger) {
		this.geigerSound = geiger;
		return this;
	}

	public ArmorFSB setHasCustomGeiger(boolean geiger) {
		this.customGeiger = geiger;
		return this;
	}

	public ArmorFSB setHasHardLanding(boolean hardLanding) {
		this.hardLanding = hardLanding;
		return this;
	}

	public ArmorFSB setDashCount(int dashCount) {
		this.dashCount = dashCount;
		return this;
	}

	public ArmorFSB setStepSize(int stepSize) {
		this.stepSize = stepSize;
		return this;
	}

	public ArmorFSB setStep(String step) {
		this.step = step;
		return this;
	}

	public ArmorFSB setJump(String jump) {
		this.jump = jump;
		return this;
	}

	public ArmorFSB setFall(String fall) {
		this.fall = fall;
		return this;
	}

	public ArmorFSB setOverlay(String path) {
		this.overlay = new ResourceLocation(path);
		return this;
	}

	public ArmorFSB setSealed(boolean canSeal) {
		this.canSeal = canSeal;
		return this;
	}

	public ArmorFSB setHazardClass(HazardClass... classes) {
		ArmorUtil.external.add(new Pair(this, classes));
		return this;
	}

	public ArmorFSB setRadResist(double fullSet) {
		this.radResist = fullSet;
		if(fullSet > 0) {
			double mult = armorType == EntityEquipmentSlot.HEAD ? HazmatRegistry.helmet :
				armorType == EntityEquipmentSlot.CHEST ? HazmatRegistry.chest :
				armorType == EntityEquipmentSlot.LEGS ? HazmatRegistry.legs : HazmatRegistry.boots;
			HazmatRegistry.external.add(new Pair(this, fullSet * mult));
		}
		return this;
	}

	public ArmorFSB cloneStats(ArmorFSB original) {

		// the lists stop changing once built, so referencing the same instance beats a deep copy
		this.effects = original.effects;
		this.noHelmet = original.noHelmet;
		this.vats = original.vats;
		this.thermal = original.thermal;
		this.geigerSound = original.geigerSound;
		this.customGeiger = original.customGeiger;
		this.hardLanding = original.hardLanding;
		this.dashCount = original.dashCount;
		this.stepSize = original.stepSize;
		this.step = original.step;
		this.jump = original.jump;
		this.fall = original.fall;
		this.canSeal = original.canSeal;
		this.setRadResist(original.radResist);
		// the mask overlay layer isn't copied — that's helmet-exclusive
		return this;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String layer) {
		return texture;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		List<String> toAdd = new ArrayList<>();

		if(canSeal) {
			list.add(TextFormatting.BLUE + "" + I18nUtil.format("armor.canSeal"));
		}

		if(!effects.isEmpty()) {
			List<String> potionList = new ArrayList<>();
			for(PotionEffect effect : effects) {
				potionList.add(I18nUtil.resolveKey(effect.getEffectName()));
			}

			toAdd.add(TextFormatting.AQUA + String.join(", ", potionList));
		}

		if(geigerSound) toAdd.add(TextFormatting.GOLD + "  " + I18nUtil.resolveKey("armor.geigerSound"));
		if(customGeiger) toAdd.add(TextFormatting.GOLD + "  " + I18nUtil.resolveKey("armor.geigerHUD"));
		if(vats) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.vats"));
		if(thermal) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.thermal"));
		if(hardLanding) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.hardLanding"));
		if(stepSize != 0) toAdd.add(TextFormatting.BLUE + "  " + I18nUtil.resolveKey("armor.stepSize", stepSize));
		if(dashCount > 0) toAdd.add(TextFormatting.AQUA + "  " + I18nUtil.resolveKey("armor.dash", dashCount));

		if(!toAdd.isEmpty()) {
			list.add(TextFormatting.GOLD + I18nUtil.resolveKey("armor.fullSetBonus"));
			list.addAll(toAdd);
		}
	}

	public static boolean hasFSBArmor(EntityPlayer player) {

		ItemStack plate = player.inventory.armorInventory.get(2);

		if(plate != null && !plate.isEmpty() && plate.getItem() instanceof ArmorFSB) {

			ArmorFSB chestplate = (ArmorFSB) plate.getItem();
			boolean noHelmet = chestplate.noHelmet;

			for(int i = 0; i < (noHelmet ? 3 : 4); i++) {

				ItemStack armor = player.inventory.armorInventory.get(i);

				if(armor == null || armor.isEmpty() || !(armor.getItem() instanceof ArmorFSB)) return false;
				if(((ArmorFSB) armor.getItem()).getArmorMaterial() != chestplate.getArmorMaterial()) return false;
				if(!((ArmorFSB) armor.getItem()).isArmorEnabled(armor)) return false;
			}
			return true;
		}
		return false;
	}

	public static boolean hasFSBArmorIgnoreCharge(EntityPlayer player) {

		ItemStack plate = player.inventory.armorInventory.get(2);

		if(plate != null && !plate.isEmpty() && plate.getItem() instanceof ArmorFSB) {

			ArmorFSB chestplate = (ArmorFSB) plate.getItem();
			boolean noHelmet = chestplate.noHelmet;

			for(int i = 0; i < (noHelmet ? 3 : 4); i++) {

				ItemStack armor = player.inventory.armorInventory.get(i);

				if(armor == null || armor.isEmpty() || !(armor.getItem() instanceof ArmorFSB)) return false;
				if(((ArmorFSB) armor.getItem()).getArmorMaterial() != chestplate.getArmorMaterial()) return false;
			}
			return true;
		}
		return false;
	}

	public void handleTick(TickEvent.PlayerTickEvent event) {

		EntityPlayer player = event.player;
		boolean step = true;

		if(player.getUniqueID().toString().equals(ShadyUtil.the_NCR) || player.getUniqueID().toString().equals(ShadyUtil.Barnaby99_x)) {
			step = false;

			if(player.world.isRemote && player.onGround) {
				steppy(player, "hbm:step.powered");
			}
		}

		if(ArmorFSB.hasFSBArmor(player)) {

			ItemStack plate = player.inventory.armorInventory.get(2);
			ArmorFSB chestplate = (ArmorFSB) plate.getItem();

			if(!chestplate.effects.isEmpty()) {
				for(PotionEffect i : chestplate.effects) {
					player.addPotionEffect(new PotionEffect(i.getPotion(), i.getDuration(), i.getAmplifier(), true, true));
				}
			}

			if(step == true && chestplate.step != null && player.world.isRemote && player.onGround) {
				steppy(player, chestplate.step);
			}
		}
	}

	/*What the old reflection read was these two "how far the last stride carried" fields — still private here, names unchanged too */
	public static void steppy(EntityPlayer player, String sound) {

		try {
			Field nextStepDistance = ReflectionHelper.findField(Entity.class, "nextStepDistance", "field_70150_b");
			Field distanceWalkedOnStepModified = ReflectionHelper.findField(Entity.class, "distanceWalkedOnStepModified", "field_82151_R");

			if(player.getEntityData().getFloat("hfr_nextStepDistance") == 0) {
				player.getEntityData().setFloat("hfr_nextStepDistance", nextStepDistance.getFloat(player));
			}

			int px = MathHelper.floor(player.posX);
			int py = MathHelper.floor(player.posY - 0.2D - (double) player.getYOffset());
			int pz = MathHelper.floor(player.posZ);
			IBlockState state = player.world.getBlockState(new BlockPos(px, py, pz));

			if(state.getMaterial() != Material.AIR && player.getEntityData().getFloat("hfr_nextStepDistance") <= distanceWalkedOnStepModified.getFloat(player))
				player.playSound(sound(sound), 0.25F, 1.0F);

			player.getEntityData().setFloat("hfr_nextStepDistance", nextStepDistance.getFloat(player));

		} catch(Exception x) {
		}
	}

	public void handleJump(EntityPlayer player) {

		if(ArmorFSB.hasFSBArmor(player)) {

			ArmorFSB chestplate = (ArmorFSB) player.inventory.armorInventory.get(2).getItem();

			if(chestplate.jump != null)
				player.playSound(sound(chestplate.jump), 0.5F, 1.0F);
		}
	}

	public void handleFall(EntityPlayer player, float fallDistance) {

		if(ArmorFSB.hasFSBArmor(player)) {

			ArmorFSB chestplate = (ArmorFSB) player.inventory.armorInventory.get(2).getItem();

			if(chestplate.hardLanding && fallDistance > 10) {

				List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow(3, 0, 3));

				for(Entity e : entities) {

					if(e instanceof EntityItem)
						continue;

					Vec3d vec = new Vec3d(player.posX - e.posX, 0, player.posZ - e.posZ);

					if(vec.lengthVector() < 3) {

						double intensity = 3 - vec.lengthVector();
						e.motionX += vec.x * intensity * -2;
						e.motionY += 0.1D * intensity;
						e.motionZ += vec.z * intensity * -2;

						e.attackEntityFrom(DamageSource.causePlayerDamage(player).setDamageBypassesArmor(), (float) (intensity * 10));
					}
				}
			}

			if(chestplate.fall != null)
				player.playSound(sound(chestplate.fall), 0.5F, 1.0F);
		}
	}

	@Override
	public void onArmorTick(World world, EntityPlayer entity, ItemStack stack) {

		if(this.armorType != EntityEquipmentSlot.CHEST) return;
		if(!hasFSBArmor(entity) || !this.geigerSound) return;
		if(entity.inventory.hasItemStack(new ItemStack(ModItems.geiger_counter)) || entity.inventory.hasItemStack(new ItemStack(ModItems.dosimeter))) return;

		if(world.getTotalWorldTime() % 5 == 0) {

			// The armor's dosimeter reads the dose inside the armor, so it's folded by the protection ratio first
			float mod = ContaminationUtil.calculateRadiationMod(entity);
			float x = HbmLivingProps.getRadBuf(entity) * mod;

			if(x > 1E-5) {
				if(world.rand.nextFloat() > x) return;

				List<Integer> list = new ArrayList<Integer>();

				if(x < 1) list.add(0);
				if(x < 5) list.add(0);
				if(x < 10) list.add(1);
				if(x > 5 && x < 15) list.add(2);
				if(x > 10 && x < 20) list.add(3);
				if(x > 15 && x < 25) list.add(4);
				if(x > 20 && x < 30) list.add(5);
				if(x > 25) list.add(6);

				int r = list.get(world.rand.nextInt(list.size()));

				if(r > 0) world.playSound(null, entity.posX, entity.posY, entity.posZ, sound("hbm:item.geiger" + r), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	public static int check(World world, int x, int y, int z) {
		int rads = (int) Math.ceil(ChunkRadiationManager.proxy.getRadiation(world, x, y, z));
		return rads;
	}

	// The old version kept a row of empty shells for "things that can't be done without event hooks"
	@SideOnly(Side.CLIENT) public void handleOverlay(RenderGameOverlayEvent.Pre event, EntityPlayer player) { }

	public boolean isArmorEnabled(ItemStack stack) { return true; }

	@SideOnly(Side.CLIENT)
	public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float partialTicks, boolean hasScreen, int mouseX, int mouseY) {

		if(overlay == null)
			return;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		Minecraft.getMinecraft().getTextureManager().bindTexture(overlay);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0.0D, (double) resolution.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
		buffer.pos((double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
		buffer.pos((double) resolution.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
		buffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private HashSet<EnumPlayerPart> hidden = new HashSet<EnumPlayerPart>();
	private boolean needsFullSet = false;

	public ArmorFSB hides(EnumPlayerPart... parts) {
		Collections.addAll(hidden, parts);
		return this;
	}

	public ArmorFSB setFullSetForHide() {
		needsFullSet = true;
		return this;
	}

	@Override
	public boolean disablesPart(EntityPlayer player, ItemStack stack, EnumPlayerPart part) {
		return hidden.contains(part) && (!needsFullSet || hasFSBArmorIgnoreCharge(player));
	}

	public void handleAttack(LivingAttackEvent event) { }
	public void handleHurt(LivingHurtEvent event) { }

	public static void setupRenderInv() {
		GL11.glTranslated(0, -1.5, 0);
		GL11.glScaled(3.25, 3.25, 3.25);
		GL11.glRotated(180, 1, 0, 0);
		GL11.glRotated(-135, 0, 1, 0);
		GL11.glRotated(-20, 1, 0, 0);
	}

	public static void setupRenderNonInv() {
		GL11.glRotated(180, 1, 0, 0);
		GL11.glScaled(0.75, 0.75, 0.75);
		GL11.glRotated(-90, 0, 1, 0);
	}

	/*The old "bind texture per slot + renderPart" shared drawing bound to `IModelCustom`, an interface deleted in this version */
	private static SoundEvent sound(String name) {
		return ItemSounds.resolve(name);
	}
}
