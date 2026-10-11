package com.hbm.util;

import java.util.List;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.material.MaterialStack;

import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The common pour flow: one hitscan downward, find the receiver, fill it, splash whatever won't fit.
 *
 * Target lookup doesn't live here — that's {@link ICrucibleAcceptor#pouringTarget}'s static method;
 * the old getPouringTarget already moved up onto the interface, so no second copy.
 */
public class CrucibleUtil {

	/**
	 * Standard pouring, casting a hitscan straight down at the given coordinates with the given range. Returns the leftover material, just like ICrucibleAcceptor's pour.
	 * The method directly modifies the original stack, so be careful and make a copy beforehand if you don't want that.
	 * Pass a single-cell Vec3d array in order to get the impact position of the stream.
	 */
	public static MaterialStack<NTMMaterial> pourSingleStack(World world, double x, double y, double z, double range, boolean safe, MaterialStack<NTMMaterial> stack, int quanta, Vec3d[] impactPosHolder) {

		RayTraceResult[] mopHolder = new RayTraceResult[1];
		ICrucibleAcceptor acc = ICrucibleAcceptor.pouringTarget(world, new Vec3d(x, y, z), new Vec3d(x, y - range, z), mopHolder);
		RayTraceResult mop = mopHolder[0];

		if(acc == null) {
			spill(mop, safe, stack, quanta, impactPosHolder);
			return stack;
		}

		MaterialStack<NTMMaterial> ret = tryPourStack(world, acc, mop, stack, impactPosHolder);

		if(ret != null) {
			return ret;
		}

		spill(mop, safe, stack, quanta, impactPosHolder);
		return stack;
	}

	/**
	 * Standard pouring, casting a hitscan straight down at the given coordinates with the given range. Returns the materialStack that has been removed.
	 * The method doesn't make copies of the MaterialStacks in the list, so the materials being subtracted or outright removed will apply to the original list.
	 * Pass a single-cell Vec3d array in order to get the impact position of the stream.
	 */
	public static MaterialStack<NTMMaterial> pourFullStack(World world, double x, double y, double z, double range, boolean safe, List<MaterialStack<NTMMaterial>> stacks, int quanta, Vec3d[] impactPosHolder) {

		if(stacks.isEmpty()) return null;

		RayTraceResult[] mopHolder = new RayTraceResult[1];
		ICrucibleAcceptor acc = ICrucibleAcceptor.pouringTarget(world, new Vec3d(x, y, z), new Vec3d(x, y - range, z), mopHolder);
		RayTraceResult mop = mopHolder[0];

		if(acc == null) {
			return spill(mop, safe, stacks, quanta, impactPosHolder);
		}

		for(MaterialStack<NTMMaterial> stack : stacks) {
			if(stack.material == null) continue;

			int amountToPour = Math.min(stack.amount, quanta);
			MaterialStack<NTMMaterial> toPour = new MaterialStack<NTMMaterial>(stack.material, amountToPour);
			MaterialStack<NTMMaterial> left = tryPourStack(world, acc, mop, toPour, impactPosHolder);

			if(left != null) {
				stack.amount -= (amountToPour - left.amount);
				return new MaterialStack<NTMMaterial>(stack.material, stack.amount - left.amount);
			}
		}

		return spill(mop, safe, stacks, quanta, impactPosHolder);
	}

	/**
	 * Tries to pour the stack onto the supplied crucible acceptor instance. Also features our friend the impact holder, which will be filled with the stream's impact position.
	 * Returns whatever is left of the stack when successful or null when unsuccessful (potential spillage).
	 */
	public static MaterialStack<NTMMaterial> tryPourStack(World world, ICrucibleAcceptor acc, RayTraceResult mop, MaterialStack<NTMMaterial> stack, Vec3d[] impactPosHolder) {
		Vec3d hit = mop.hitVec;

		if(stack.material.smeltable != SmeltingBehavior.SMELTABLE) {
			return null;
		}

		//the old version fed mop.sideHit's int into ForgeDirection.getOrientation; sideHit is an EnumFacing already in this version
		if(acc.canAcceptPartialPour(world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), hit.x, hit.y, hit.z, mop.sideHit, stack)) {
			MaterialStack<?> left = acc.pour(world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), hit.x, hit.y, hit.z, mop.sideHit, stack);

			//a receiver taking everything returns null, equivalent to "0 left"; this normalizes into one fresh instance rather than following the receiver's type
			if(left == null) {
				return new MaterialStack<NTMMaterial>(stack.material, 0);
			}

			if(impactPosHolder != null) {
				impactPosHolder[0] = hit;
			}

			return new MaterialStack<NTMMaterial>(stack.material, left.amount);
		}

		return null;
	}

	/**
	 * Regular spillage routine but accepts a stack list instead of a stack. simply uses the first available stack from the list. Assumes list is not empty.
	 */
	public static MaterialStack<NTMMaterial> spill(RayTraceResult mop, boolean safe, List<MaterialStack<NTMMaterial>> stacks, int quanta, Vec3d[] impactPos) {
		//simply use the first available material
		MaterialStack<NTMMaterial> top = stacks.get(0);
		MaterialStack<NTMMaterial> ret = spill(mop, safe, top, quanta, impactPos);
		//remove all stacks with no content
		stacks.removeIf(o -> o.amount <= 0);

		return ret;
	}

	/**
	 * The routine used for then there is no valid crucible acceptor found. Will NOP with safe mode on. Returns the MaterialStack that was lost.
	 */
	public static MaterialStack<NTMMaterial> spill(RayTraceResult mop, boolean safe, MaterialStack<NTMMaterial> stack, int quanta, Vec3d[] impactPos) {

		//do nothing if safe mode is on
		if(safe) {
			return null;
		}

		MaterialStack<NTMMaterial> toWaste = new MaterialStack<NTMMaterial>(stack.material, Math.min(stack.amount, quanta));
		stack.amount -= toWaste.amount;

		//if there is an impact holder, set the impact coordinates
		if(impactPos != null && mop != null) {
			impactPos[0] = mop.hitVec;
		}

		return toWaste;
	}
}
