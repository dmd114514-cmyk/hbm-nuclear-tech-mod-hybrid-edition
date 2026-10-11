package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.handler.CasingEjector;
import com.hbm.particle.ParticleAmatFlash;
import com.hbm.particle.ParticleDigammaSmoke;
import com.hbm.particle.ParticleExSmoke;
import com.hbm.particle.ParticleFoam;
import com.hbm.particle.ParticleFoundry;
import com.hbm.particle.ParticleHadron;
import com.hbm.particle.ParticleHaze;
import com.hbm.particle.ParticleRadiationFog;
import com.hbm.particle.ParticleRocketFlame;
import com.hbm.particle.ParticleSpark;
import com.hbm.particle.ParticleText;
import com.hbm.particle.SpentCasing;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The central home for the old `ClientProxy.effectNT`'s inline branches: one branch per nested class, registered into ParticleCreators.
 *
 * Why not one file each: these are all thin shells of "new one particle from the packet's parameters"; seven files would only make
 * "which types does the effect system know" harder to answer — now it's readable in one place.
 *
 * The move only adapts this version's shape (immutable vectors, motion* via the particle's own setMotion, maxAge via setMaxAge),
 * values matching the old version entry by entry; the old `vec.rotateYaw(360 / count)` spots treated degrees as radians (integer division),
 * copied as-is, not a typo — changing it changes the spin, which is gameplay territory.
 */
public final class EffectBranchCreators {

	private EffectBranchCreators() { }

	/*Vanilla particles' construction door: these classes' constructors are all protected, reachable only from the factory (createParticle) outside — which already registers what it builds */
	private static Particle vanilla(EnumParticleTypes type, double x, double y, double z, double a, double b, double c, int... extra) {
		return Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(type.getParticleID(), x, y, z, a, b, c, extra);
	}

	/** amat: one blast-center flash, scale from the packet */
	public static class Amat implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleAmatFlash(world, x, y, z, data.getFloat("scale")));
		}
	}

	/** debug: one floating line over the head, color and scale both in the packet */
	public static class DebugText implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

			String text = data.getString("text");
			int color = data.getInteger("color");
			float scale = data.getFloat("scale");

			ParticleText particle = new ParticleText(world, x, y, z, color, text);
			particle.multipleParticleScaleBy(scale);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	/** casing: the ejector's old road (one packet draws ejector.getAmount() shells), coexisting with casingNT's single-shell path */
	public static class CasingEject implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

			CasingEjector ejector = CasingEjector.fromId(data.getInteger("ej"));
			if(ejector == null) return;

			SpentCasing casingConfig = SpentCasing.casingMap.get(data.getString("name"));
			if(casingConfig == null) return;

			for(int i = 0; i < ejector.getAmount(); i++) {
				ejector.spawnCasing(texman, casingConfig, world, x, y, z, data.getFloat("pitch"), data.getFloat("yaw"), data.getBoolean("crouched"));
			}
		}
	}

	/** haze: one fog blob, parameters all on the particle itself */
	public static class Haze implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHaze(texman, world, x, y, z));
		}
	}

	/** radFog: radiation fog, written as an inline branch in the old ClientProxy.effectNT; the parameters sit on the particle itself here too */
	public static class RadFog implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRadiationFog(texman, world, x, y, z));
		}
	}

	/** tau: a ring of sparks bursting from a point, with a hadron perched on top */
	public static class Tau implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

			boolean small = data.getBoolean("small");
			int count = data.getByte("count");

			for(int i = 0; i < count; i++) {
				Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSpark(world, x, y, z, rand.nextGaussian() * 0.05, 0.05, rand.nextGaussian() * 0.05).makeSmall(small));
			}

			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(texman, world, x, y, z).makeSmall(small));
		}
	}

	/** exhaust: three rocket-flame poses — soyuz downward, lambda downward and enlarged, meteor scattered in place */
	public static class Exhaust implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

			String mode = data.getString("mode");
			int count = Math.max(1, data.getInteger("count"));
			double width = data.getDouble("width");

			if(new Vec3d(player.posX - x, player.posY - y, player.posZ - z).lengthVector() > 350) return;

			if("soyuz".equals(mode)) {

				for(int i = 0; i < count; i++) {
					ParticleRocketFlame fx = new ParticleRocketFlame(texman, world, x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width)
							.setMotion(0, -0.75 + rand.nextDouble() * 0.5, 0);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}

			if("lambda".equals(mode)) {

				for(int i = 0; i < count; i++) {
					ParticleRocketFlame fx = new ParticleRocketFlame(texman, world, x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width)
							.setScale(1.5F)
							.setMotion(0, -1 + rand.nextDouble() * 0.25, 0);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}

			if("meteor".equals(mode)) {

				for(int i = 0; i < count; i++) {
					ParticleRocketFlame fx = new ParticleRocketFlame(texman, world, x + rand.nextGaussian() * width, y + rand.nextGaussian() * width, z + rand.nextGaussian() * width);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}
		}
	}

	/** smoke: seven modes (cloud/radial/radialDigamma/shock/shockRand/wave/foamSplash), the last two laid out as rings */
	public static class Smoke implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

			String mode = data.getString("mode");
			int count = Math.max(1, data.getInteger("count"));

			if("cloud".equals(mode)) {

				for(int i = 0; i < count; i++) {
					double mX = rand.nextGaussian() * (1 + (count / 150));
					double mY = rand.nextGaussian() * (1 + (count / 100));
					double mZ = rand.nextGaussian() * (1 + (count / 150));

					if(rand.nextBoolean()) mY = Math.abs(mY);

					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleExSmoke(texman, world, x, y, z).setMotion(mX, mY, mZ));
				}
			}

			if("radial".equals(mode)) {

				for(int i = 0; i < count; i++) {
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleExSmoke(texman, world, x, y, z).setMotion(
							rand.nextGaussian() * (1 + (count / 50)),
							rand.nextGaussian() * (1 + (count / 50)),
							rand.nextGaussian() * (1 + (count / 50))));
				}
			}

			if("radialDigamma".equals(mode)) {

				Vec3d vec = new Vec3d(2, 0, 0).rotateYaw(rand.nextFloat() * (float) Math.PI * 2F);

				for(int i = 0; i < count; i++) {
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDigammaSmoke(texman, world, x, y, z).setMotion(vec.x, 0, vec.z));
					vec = vec.rotateYaw((float) Math.PI * 2F / (float) count);
				}
			}

			if("shock".equals(mode)) {

				double strength = data.getDouble("strength");
				Vec3d vec = new Vec3d(strength, 0, 0).rotateYaw(rand.nextInt(360));

				for(int i = 0; i < count; i++) {
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleExSmoke(texman, world, x, y, z).setMotion(vec.x, 0, vec.z));
					vec = vec.rotateYaw((float) Math.PI * 2F / (float) count);
				}
			}

			if("shockRand".equals(mode)) {

				double strength = data.getDouble("strength");
				Vec3d vec = new Vec3d(strength, 0, 0).rotateYaw(rand.nextInt(360));

				for(int i = 0; i < count; i++) {
					double r = rand.nextDouble();
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleExSmoke(texman, world, x, y, z).setMotion(vec.x * r, 0, vec.z * r));
					vec = vec.rotateYaw(360 / count);
				}
			}

			if("wave".equals(mode)) {

				double strength = data.getDouble("range");
				Vec3d vec = new Vec3d(strength, 0, 0);

				for(int i = 0; i < count; i++) {

					vec = vec.rotateYaw((float) Math.toRadians(rand.nextFloat() * 360F));

					// the old version zeroed the motion axes here, which is already a new particle's default; not written twice
					ParticleExSmoke fx = new ParticleExSmoke(texman, world, x + vec.x, y, z + vec.z);
					fx.setMaxAge(50);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);

					vec = vec.rotateYaw(360 / count);
				}
			}

			if("foamSplash".equals(mode)) {

				double strength = data.getDouble("range");
				Vec3d vec = new Vec3d(strength, 0, 0);

				for(int i = 0; i < count; i++) {

					vec = vec.rotateYaw((float) Math.toRadians(rand.nextFloat() * 360F));

					ParticleFoam fx = new ParticleFoam(texman, world, x + vec.x, y, z + vec.z);
					fx.setMaxAge(50);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);

					vec = vec.rotateYaw(360 / count);
				}
			}
		}
	}

	/** foundry: the metal stream at the casting spout. The particle joins the self-drawn channel itself; this only builds one from the packet's parameters and hands the lifecycle to vanilla */
	public static class Foundry implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

			// dir is EnumFacing.ordinal(): the old off-by-one was reconciled inside ParticleFoundry; don't touch it here
			ParticleFoundry fx = new ParticleFoundry(texman, world, x, y, z, data.getInteger("color"),
					data.getByte("dir"), data.getFloat("len"), data.getFloat("base"), data.getFloat("off"));
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	/** sweat: block debris shed all over a living thing, lifetime stretched to about 150 as before */
	public static class Sweat implements IParticleCreator {
		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

			Entity e = world.getEntityByID(data.getInteger("entity"));
			if(!(e instanceof EntityLivingBase)) return;

			// The old version sent the block as two numbers (block, meta); this version still receives those two, folding them into a state id before the factory will take it
			Block block = Block.getBlockById(data.getInteger("block"));
			int stateId = Block.getStateId(block.getStateFromMeta(data.getInteger("meta")));

			AxisAlignedBB box = e.getEntityBoundingBox();

			for(int i = 0; i < data.getInteger("count"); i++) {

				double ix = box.minX - 0.2 + (box.maxX - box.minX + 0.4) * rand.nextDouble();
				double iy = box.minY + (box.maxY - box.minY + 0.2) * rand.nextDouble();
				double iz = box.minZ - 0.2 + (box.maxZ - box.minZ + 0.4) * rand.nextDouble();

				Particle fx = vanilla(EnumParticleTypes.BLOCK_DUST, ix, iy, iz, 0, 0, 0, stateId);
				if(fx != null) fx.setMaxAge(150 + rand.nextInt(50));
			}
		}
	}

	/** vanillaExt: one mode per vanilla particle, finished uniformly by overrideAge; the mode table matches the old version line by line */
	public static class VanillaExt implements IParticleCreator {

		@Override
		@SideOnly(Side.CLIENT)
		public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

			String mode = data.getString("mode");
			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");

			Particle fx = null;

			if("flame".equals(mode))
				fx = vanilla(EnumParticleTypes.FLAME, x, y, z, mX, mY, mZ);

			if("smoke".equals(mode))
				fx = vanilla(EnumParticleTypes.SMOKE_NORMAL, x, y, z, mX, mY, mZ);

			if("volcano".equals(mode)) {
				// The old version built first then jammed motion in; this factory feeds args 4~6 into the constructor as motion — equivalent;
				// the old also had noClip=true — Particle's collision switch is protected here, unreachable outside, and no current sender carries noclip
				fx = vanilla(EnumParticleTypes.SMOKE_NORMAL, x, y, z, rand.nextGaussian() * 0.2, 2.5 + rand.nextDouble(), rand.nextGaussian() * 0.2);
				if(fx != null) {
					fx.multipleParticleScaleBy(100);
					fx.setMaxAge(200 + rand.nextInt(50));
				}
			}

			if("cloud".equals(mode)) {

				if(data.hasKey("r")) {
					// the tinted tier built at zero motion in the old version, so it sends at zero speed; the 7.5 tier jammed a private scale via reflection then, multiplied through the public one here
					float rng = rand.nextFloat() * 0.1F;
					fx = vanilla(EnumParticleTypes.CLOUD, x, y, z, 0, 0, 0);
					if(fx != null) {
						fx.setRBGColorF(data.getFloat("r") + rng, data.getFloat("g") + rng, data.getFloat("b") + rng);
						fx.multipleParticleScaleBy(7.5F);
					}
				} else {
					fx = vanilla(EnumParticleTypes.CLOUD, x, y, z, mX, mY, mZ);
				}
			}

			if("reddust".equals(mode))
				fx = vanilla(EnumParticleTypes.REDSTONE, x, y, z, mX, mY, mZ);

			if("bluedust".equals(mode))
				fx = vanilla(EnumParticleTypes.REDSTONE, x, y, z, 0.01F, 0.01F, 1F);

			if("greendust".equals(mode))
				fx = vanilla(EnumParticleTypes.REDSTONE, x, y, z, 0.01F, 0.5F, 0.1F);

			if("fireworks".equals(mode))
				fx = vanilla(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);

			if("largeexplode".equals(mode)) {

				fx = vanilla(EnumParticleTypes.EXPLOSION_LARGE, x, y, z, data.getFloat("size"), 0, 0);
				if(fx != null) {
					float r = 1.0F - rand.nextFloat() * 0.2F;
					fx.setRBGColorF(1F * r, 0.9F * r, 0.5F * r);
				}

				for(int i = 0; i < data.getByte("count"); i++) {
					Particle sec = vanilla(EnumParticleTypes.EXPLOSION_NORMAL, x, y, z, 0, 0, 0);
					if(sec != null) {
						float r2 = 1.0F - rand.nextFloat() * 0.5F;
						sec.setRBGColorF(0.5F * r2, 0.5F * r2, 0.5F * r2);
						sec.multipleParticleScaleBy(i + 1);
					}
				}
			}

			if("townaura".equals(mode)) {
				// The old version built at zero speed then setVelocity(mX,mY,mZ); the factory takes one parameter trip, so the speed slot does double duty
				fx = vanilla(EnumParticleTypes.TOWN_AURA, x, y, z, mX, mY, mZ);
				if(fx != null) {
					float color = 0.5F + rand.nextFloat() * 0.5F;
					fx.setRBGColorF(0.8F * color, 0.9F * color, 1.0F * color);
				}
			}

			if("blockdust".equals(mode)) {
				Block block = Block.getBlockById(data.getInteger("block"));
				fx = vanilla(EnumParticleTypes.BLOCK_DUST, x, y, z, mX, mY + 0.2, mZ, Block.getStateId(block.getDefaultState()));
				if(fx != null) fx.setMaxAge(10 + rand.nextInt(20));
			}

			if("colordust".equals(mode)) {
				// The old version borrowed wool's debris shape and tinted it; same road here: the state gives the shape, setRBGColorF gives the color
				fx = vanilla(EnumParticleTypes.BLOCK_DUST, x, y, z, mX, mY + 0.2, mZ, Block.getStateId(Blocks.WOOL.getDefaultState()));
				if(fx != null) {
					fx.setRBGColorF(data.getFloat("r"), data.getFloat("g"), data.getFloat("b"));
					fx.setMaxAge(10 + rand.nextInt(20));
				}
			}

			if(fx != null) {
				int overrideAge = data.getInteger("overrideAge");
				if(overrideAge > 0) fx.setMaxAge(overrideAge);
			}
		}
	}
}
