package com.hbm.lib;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyConnectorBlock;
import api.hbm.energymk2.IEnergyConnectorMK2;
import api.hbm.net.Directions;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The junk drawer. Stuffing everything in here is a bad habit, but until a better home exists, this is the default answer to "where does this go".
 * Direction constants forward to api.hbm.net.Directions, keeping both sides on the same EnumFacing instances.
 */
public final class Library {

	public static final EnumFacing POS_X = Directions.POS_X;
	public static final EnumFacing NEG_X = Directions.NEG_X;
	public static final EnumFacing POS_Y = Directions.POS_Y;
	public static final EnumFacing NEG_Y = Directions.NEG_Y;
	public static final EnumFacing POS_Z = Directions.POS_Z;
	public static final EnumFacing NEG_Z = Directions.NEG_Z;

	private Library() { }

	/** dir is "the wire's face"; it gets flipped to "the machine's face" internally before asking */
	public static boolean canConnect(IBlockAccess world, BlockPos pos, EnumFacing dir) {

		if(pos.getY() < 0 || pos.getY() > 255) return false;

		Block block = world.getBlockState(pos).getBlock();
		if(block instanceof IEnergyConnectorBlock) {
			if(((IEnergyConnectorBlock) block).canConnect(world, pos, dir.getOpposite())) return true;
		}

		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEnergyConnectorMK2) {
			if(((IEnergyConnectorMK2) te).canConnect(dir.getOpposite())) return true;
		}

		return false;
	}

	/** Charges a battery, returns the leftover power; maxPower caps the session */
	public static long chargeItemsFromTE(ItemStack stack, long power, long maxPower) {

		if(power < 0) return 0;
		if(power > maxPower) power = maxPower;
		if(stack == null || !(stack.getItem() instanceof IBatteryItem)) return power;

		IBatteryItem battery = (IBatteryItem) stack.getItem();
		long toCharge = Math.min(Math.min(power, battery.getChargeRate(stack)), battery.getMaxCharge(stack) - battery.getCharge(stack));
		if(toCharge <= 0) return power;

		battery.chargeBattery(stack, toCharge);
		return power - toCharge;
	}

	/** Discharges a battery, returns what's left after the pull */
	public static long chargeTEFromItems(ItemStack stack, long power, long maxPower) {

		if(power >= maxPower) return maxPower;
		if(stack == null || !(stack.getItem() instanceof IBatteryItem)) return power;

		IBatteryItem battery = (IBatteryItem) stack.getItem();
		long toDischarge = Math.min(Math.min(maxPower - power, battery.getDischargeRate(stack)), battery.getCharge(stack));
		if(toDischarge <= 0) return power;

		battery.dischargeBattery(stack, toDischarge);
		return power + toDischarge;
	}

	/** Smoothstep, the daily bread of transition animations */
	public static double smoothstep(double t, double edge0, double edge1) {
		t = Math.max(0.0, Math.min(1.0, (t - edge0) / (edge1 - edge0)));
		return t * t * (3.0 - 2.0 * t);
	}

	public static float smoothstep(float t, float edge0, float edge1) {
		t = Math.max(0.0F, Math.min(1.0F, (t - edge0) / (edge1 - edge0)));
		return t * t * (3.0F - 2.0F * t);
	}

	/** Ray along the player's crosshair, with the default three switches */
	public static RayTraceResult rayTrace(EntityPlayer player, double length, float interpolation) {
		return rayTrace(player, length, interpolation, false, false, false);
	}

	/**
	 * Same, but the three switches are yours: allowLiquids counts fluids as blocking,
	 * disallowNonCollidingBlocks excludes blocks without a collision box (glass, tall grass and friends) from hits,
	 * mopOnMiss returns a result at the ray's endpoint instead of null when the whole line whiffs.
	 */
	public static RayTraceResult rayTrace(EntityPlayer player, double length, float interpolation, boolean allowLiquids, boolean disallowNonCollidingBlocks, boolean mopOnMiss) {
		Vec3d start = getPosition(interpolation, player);
		Vec3d look = player.getLook(interpolation);
		Vec3d end = start.addVector(look.x * length, look.y * length, look.z * length);
		return player.world.rayTraceBlocks(start, end, allowLiquids, disallowNonCollidingBlocks, mopOnMiss);
	}

	/**
	 * The eye position. interpolation of 1 takes the current position outright, otherwise it lerps last frame to this one —
	 * skip the interpolation in a render frame and the view jitters as you turn.
	 */
	public static Vec3d getPosition(float interpolation, EntityPlayer player) {
		double rise = player.getEyeHeight() - player.getDefaultEyeHeight(); // the notch that sinks away while sneaking

		if(interpolation == 1.0F)
			return new Vec3d(player.posX, player.posY + rise, player.posZ);

		double x = player.prevPosX + (player.posX - player.prevPosX) * interpolation;
		double y = player.prevPosY + (player.posY - player.prevPosY) * interpolation + rise;
		double z = player.prevPosZ + (player.posZ - player.prevPosZ) * interpolation;
		return new Vec3d(x, y, z);
	}

	/** Whether anything stands between two points (any sight-blocking block counts) */
	public static boolean isObstructed(World world, double x, double y, double z, double a, double b, double c) {
		return world.rayTraceBlocks(new Vec3d(x, y, z), new Vec3d(a, b, c)) != null;
	}

	/** Whether anything **solid** stands between two points (collision-less blocks ignored) */
	public static boolean isObstructedOpaque(World world, double x, double y, double z, double a, double b, double c) {
		return world.rayTraceBlocks(new Vec3d(x, y, z), new Vec3d(a, b, c), false, true, false) != null;
	}
}
