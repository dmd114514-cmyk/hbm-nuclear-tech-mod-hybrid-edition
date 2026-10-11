package com.hbm.explosion;

import java.util.List;
import java.util.Random;

import com.hbm.entity.projectile.EntityRubble;
import com.hbm.main.MainRegistry;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;
import com.hbm.util.ParticleUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * The parts bin for large-yield explosions: smoke clouds, shock rings, foam, flying rubble, shrapnel rain, drilling chain-detonations.
 * The old version's 32 call sites wanted exactly these static entries.
 *
 * This round leaves out <code>spawnShrapnels</code> / <code>spawnTracers</code> / <code>spawnShrapnelShower</code>
 * plus the three <code>@Deprecated</code> <code>explode</code> / <code>explodeFire</code> —
 * they all sit on <code>EntityShrapnel</code>, and that entity can't ship yet: its <code>onImpact</code>
 * needs <code>ExplosionNT.ExAttrib</code> (no <code>ExplosionNT</code> anywhere in this version; the parallel explosion we have is
 * the <code>com.hbm.explosion.vanillant</code> set) and four unregistered blocks from <code>ModBlocks</code>.
 * The damage source <code>ModDamageSource.shrapnel</code> is already in, so that's no gate.
 * Once the shrapnel half lands, the six members above can be added in the old shapes directly; writing them now would just be hollow shells that never spawn shrapnel.
 *
 * Only two deltas this version, both shape-level: packet dispatch moved to <code>AsyncPacketSender</code> (the counterpart of the old <code>PacketThreading</code>),
 * vectors swapped to <code>Vec3d</code> (1.12.2 deleted <code>net.minecraft.util.Vec3</code>).
 * Every value matches the old version line for line.
 */
public class ExplosionLarge {

	static Random rand = new Random();

	/** Broadcast radius; the old smoke entries all used 250 blocks */
	private static final int RANGE = 250;

	/**
	 * Smoke cloud: <code>smoke</code>'s <code>cloud</code> mode.
	 *
	 * @deprecated already marked so in the old version, kept only as a floor for callers still doing "strength into particle count"; new calls go to
	 *             {@link #spawnParticlesRadial} or {@link #spawnShock}.
	 */
	@Deprecated
	public static void spawnParticles(World world, double x, double y, double z, int count) {

		NBTTagCompound data = smokeData("cloud", count);
		emitSmoke(world, data, x, y, z);
	}

	/** Smoke cloud, each axis scatters independently ({@code cloud} has a special vertical bias, this one doesn't) */
	public static void spawnParticlesRadial(World world, double x, double y, double z, int count) {

		NBTTagCompound data = smokeData("radial", count);
		emitSmoke(world, data, x, y, z);
	}

	/** Firefighting foam splatter. Note this version, like the old: the particle side reads {@code range} from the packet while only {@code count} gets filled here */
	public static void spawnFoam(World world, double x, double y, double z, int count) {

		NBTTagCompound data = smokeData("foamSplash", count);
		emitSmoke(world, data, x, y, z);
	}

	/** Shock ring: a ring of smoke shoved flat along the ground; strength is the ring's starting radius */
	public static void spawnShock(World world, double x, double y, double z, int count, double strength) {

		NBTTagCompound data = smokeData("shock", count);
		data.setDouble("strength", strength);
		// The old version lifted the particle half a block while the broadcast point stayed at the original height; at radius 250 half a block is nothing, so the broadcast point follows the particle now
		emitSmoke(world, data, x, y + 0.5, z);
	}

	/** A ring of flame. The rotation count uses 360 divided by an integer as radians — that's the old version verbatim, not a typo */
	public static void spawnBurst(World world, double x, double y, double z, int count, double strength) {

		Vec3d vec = new Vec3d(strength, 0, 0).rotateYaw(rand.nextInt(360));

		for(int i = 0; i < count; i++) {
			ParticleUtil.spawnGasFlame(world, x, y, z, vec.x, 0.0, vec.z);

			vec = vec.rotateYaw(360 / count);
		}
	}

	/** Shattered block chunks, tossed high and far by count; stone is the fixed material */
	public static void spawnRubble(World world, double x, double y, double z, int count) {

		// In the old version count=0 made rand.nextInt(0) throw; this version guards degenerate input — strength/10 coming out 0 is a common value
		if(count <= 0) return;

		for(int i = 0; i < count; i++) {
			EntityRubble rubble = new EntityRubble(world);
			rubble.posX = x;
			rubble.posY = y;
			rubble.posZ = z;
			rubble.motionY = 0.75						* (1 + ((count + rand.nextInt(count * 5))) / 25);
			rubble.motionX = rand.nextGaussian() * 0.75	* (1 + (count / 50));
			rubble.motionZ = rand.nextGaussian() * 0.75	* (1 + (count / 50));
			rubble.setMetaBasedOnBlock(Blocks.STONE, 0);
			world.spawnEntity(rubble);
		}
	}

	/** Parts scattered when a missile gets shredded: thrown out at roughly half the original stack, rares at a separate one-in-ten chance */
	public static void spawnMissileDebris(World world, double x, double y, double z, double motionX, double motionY, double motionZ, double deviation, List<ItemStack> debris, ItemStack rareDrop) {

		if(debris != null) {
			for(int i = 0; i < debris.size(); i++) {
				ItemStack stack = debris.get(i);
				if(stack == null || stack.isEmpty()) continue;

				int k = rand.nextInt(stack.getCount() + 1);
				for(int j = 0; j < k; j++) {
					EntityItem item = new EntityItem(world, x, y, z, stack.copy());
					item.motionX = (motionX + rand.nextGaussian() * deviation) * 0.85;
					item.motionY = (motionY + rand.nextGaussian() * deviation) * 0.85;
					item.motionZ = (motionZ + rand.nextGaussian() * deviation) * 0.85;
					item.posX = item.posX + item.motionX * 2;
					item.posY = item.posY + item.motionY * 2;
					item.posZ = item.posZ + item.motionZ * 2;

					world.spawnEntity(item);
				}
			}
		}

		if(rareDrop != null && !rareDrop.isEmpty() && rand.nextInt(10) == 0) {
			EntityItem item = new EntityItem(world, x, y, z, rareDrop.copy());
			item.motionX = motionX + rand.nextGaussian() * deviation * 0.1;
			item.motionY = motionY + rand.nextGaussian() * deviation * 0.1;
			item.motionZ = motionZ + rand.nextGaussian() * deviation * 0.1;

			world.spawnEntity(item);
		}
	}

	/** Fires every other block along one direction, for "bunker buster" style chain detonations */
	public static void buster(World world, double x, double y, double z, Vec3d vector, float strength, float depth) {

		Vec3d dir = vector.normalize();

		for(int i = 0; i < depth; i += 2) {
			world.createExplosion(null, x + dir.x * i, y + dir.y * i, z + dir.z * i, strength, true);
		}
	}

	/**
	 * Shoots a ray from the blast center in a random direction; the first "breakable" block it hits gets removed and tossed as rubble.
	 * Blocks with blast resistance above 70 count as unbreakable and skip the ray entirely.
	 *
	 * Server-side hands only; rubble is an entity, clients see it through entity sync.
	 */
	public static void jolt(World world, double posX, double posY, double posZ, double strength, int count, double vel) {

		for(int j = 0; j < count; j++) {

			double phi = rand.nextDouble() * (Math.PI * 2);
			double costheta = rand.nextDouble() * 2 - 1;
			double theta = Math.acos(costheta);
			double x = Math.sin(theta) * Math.cos(phi);
			double y = Math.sin(theta) * Math.sin(phi);
			double z = Math.cos(theta);

			Vec3d vec = new Vec3d(x, y, z);

			for(int i = 0; i < strength; i++) {
				double x0 = posX + (vec.x * i);
				double y0 = posY + (vec.y * i);
				double z0 = posZ + (vec.z * i);
				BlockPos pos = new BlockPos((int) x0, (int) y0, (int) z0);

				if(world.isRemote) continue;

				IBlockState state = world.getBlockState(pos);

				if(state.getMaterial().isLiquid()) {
					world.setBlockState(pos, Blocks.AIR.getDefaultState());
				}

				// The liquid was just smoothed over — re-fetch before judging, or that cell gets treated as solid and tossed as rubble; the old lines re-checked too
				state = world.getBlockState(pos);
				if(state.getBlock() == Blocks.AIR) continue;

				if(state.getBlock().getExplosionResistance((Entity) null) > 70) continue;

				EntityRubble rubble = new EntityRubble(world);
				rubble.posX = x0 + 0.5F;
				rubble.posY = y0 + 0.5F;
				rubble.posZ = z0 + 0.5F;
				rubble.setMetaBasedOnBlock(state.getBlock(), state.getBlock().getMetaFromState(state));

				Vec3d vec4 = new Vec3d(posX - rubble.posX, posY - rubble.posY, posZ - rubble.posZ).normalize();

				rubble.motionX = vec4.x * vel;
				rubble.motionY = vec4.y * vel;
				rubble.motionZ = vec4.z * vel;

				world.spawnEntity(rubble);

				world.setBlockState(pos, Blocks.AIR.getDefaultState());
				break;
			}
		}
	}

	/** Strength into smoke cloud count: saturates around 865, decays fast at low yields */
	public static int cloudFunction(int i) {
		return (int)(850 * (1 - Math.pow(Math.E, -i/15)) + 15);
	}

	/** Strength into rubble count */
	public static int rubbleFunction(int i) {
		return i/10;
	}

	/** Strength into shrapnel count; the shrapnel half isn't in yet, so this hangs on the old formula for callers */
	public static int shrapnelFunction(int i) {
		return i/3;
	}

	private static NBTTagCompound smokeData(String mode, int count) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "smoke");
		data.setString("mode", mode);
		data.setInteger("count", count);
		return data;
	}

	/**
	 * Same contract as {@code ParticleUtil}: the client side renders locally, only the server broadcasts;
	 * enqueuing takes a read-only snapshot, so data and coordinates are filled in here already and TargetPoint carries just the dimension id and a few doubles.
	 */
	private static void emitSmoke(World world, NBTTagCompound data, double x, double y, double z) {

		if(world.isRemote) {
			data.setDouble("posX", x);
			data.setDouble("posY", y);
			data.setDouble("posZ", z);
			MainRegistry.proxy.effectNT(data);
			return;
		}

		AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.getDimension(), x, y, z, RANGE));
	}
}
