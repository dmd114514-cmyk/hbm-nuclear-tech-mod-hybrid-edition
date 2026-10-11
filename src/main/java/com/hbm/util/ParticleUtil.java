package com.hbm.util;

import java.util.Random;

import com.hbm.main.MainRegistry;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * A few "draw here / send to nearby clients" effect exits.
 *
 * Each does one thing: stuff parameters into NBT; the client side hands off to effectNT, the server side sends a ranged packet.
 * Meaning these methods **only know type strings**; whether anything draws depends on the creator table having that type registered.
 * Of these six, all but debugdrone are registered; unregistered types get one deduplicated WARN from effectNT, no more silence.
 */
public final class ParticleUtil {

	private ParticleUtil() { }

	/** Gas flame */
	public static void spawnGasFlame(World world, double x, double y, double z, double mX, double mY, double mZ) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "gasfire");
		data.setDouble("mX", mX);
		data.setDouble("mY", mY);
		data.setDouble("mZ", mZ);

		emit(world, data, x, y, z);
	}

	/** A flame pillar on a one-in-twelve roll (the old "what in the actual fuck" comment was about how blunt this dice roll reads) */
	public static void spawnJesusFlame(World world, double x, double y, double z) {

		if(world.rand.nextInt(12) != 0)
			return;

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "duodec");

		emit(world, data, x, y, z);
	}

	/**
	 * The drone link line: passes the target point as "velocity", drawing the line between two points.
	 *
	 * TODO this effect can't draw yet: its creator needs the drone content pieces — the item side is one unassigned declaration, the block side 0 hits,
	 * so the missing piece is booked on the content side, not the particle layer. The method stays; callers keep calling; packets keep flowing.
	 */
	public static void spawnDroneLine(World world, double x, double y, double z, double x0, double y0, double z0, int color) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "debugdrone");
		data.setDouble("mX", x0);
		data.setDouble("mY", y0);
		data.setDouble("mZ", z0);
		data.setInteger("color", color);

		emit(world, data, x, y, z);
	}

	/*The tuning flame, likewise one in twelve */
	public static void spawnTuneFlame(World world, double x, double y, double z) {

		if(world.rand.nextInt(12) != 0)
			return;

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "duoewe");

		emit(world, data, x, y, z);
	}

	/*The dust flame, fixed 4× scale */
	public static void spawnDustFlame(World world, double x, double y, double z, double mX, double mY, double mZ) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "duststorm");
		data.setDouble("mX", mX);
		data.setDouble("mY", mY);
		data.setDouble("mZ", mZ);
		data.setFloat("scale", 4F);

		emit(world, data, x, y, z);
	}

	/** The flare: scale rides the parameters */
	public static void spawnFlare(World world, double x, double y, double z, double mX, double mY, double mZ, float scale) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "flare");
		data.setDouble("mX", mX);
		data.setDouble("mY", mY);
		data.setDouble("mZ", mZ);
		data.setFloat("scale", scale);

		emit(world, data, x, y, z);
	}

	/**
	 * Radiation fog: coordinates only; color and lifetime live in the client-side creator.
	 * The old version called `proxy.effectNT` straight from the server tick — fine in 1.7.10 where the single-player server shares the client thread,
	 * but this version's integrated server is its own thread, so everything goes through {@link #emit}: client draws locally, server broadcasts.
	 */
	public static void spawnRadFog(World world, double x, double y, double z) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "radFog");

		emit(world, data, x, y, z);
	}

	/**
	 * The shared step: coordinates go only into the local copy; remote copies carry their own.
	 * Packets ride the async facade — the contract is **queued means read-only snapshot**, so data and coordinates are fully filled here,
	 * and TargetPoint carries just the dimension id and a few doubles, never BlockPos or entity references.
	 */
	private static void emit(World world, NBTTagCompound data, double x, double y, double z) {

		if(world.isRemote) {
			data.setDouble("posX", x);
			data.setDouble("posY", y);
			data.setDouble("posZ", z);
			MainRegistry.proxy.effectNT(data);
			return;
		}

		AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.getDimension(), x, y, z, 150));
	}
}
