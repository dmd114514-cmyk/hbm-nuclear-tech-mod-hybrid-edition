package com.hbm.blocks.rail;

import java.util.ArrayList;
import java.util.List;

import api.hbm.net.Tuple.Pair;

import com.hbm.blocks.BlockDummyable;
import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ParticleUtil;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The rail waypoint system's base: one rail structure can carry several chains (RailDef), each chain a string of coordinate points.
 * When a train latches on it finds the nearest segment across all chains, then walks this tick's travel down that chain cell by cell,
 * handing the leftover travel to info so the next rail continues it.
 */
public abstract class BlockRailWaypointSystem extends BlockDummyable implements IRailNTM {

	public List<RailDef> railDefs = new ArrayList();

	public BlockRailWaypointSystem(Material mat) {
		super(mat);
	}

	/** Whether a train can go from 'from' to 'to' (along def's chain); switches intercept right here */
	public boolean canCross(World world, int x, int y, int z, Vec3d from, Vec3d to, RailDef def) {
		return true;
	}

	/** The rail itself renders via a JSON model; the placeholder cells point at an empty model in the blockstate */
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
	}

	@Override
	public Vec3d getSnappingPos(World world, int x, int y, int z, double trainX, double trainY, double trainZ) {
		return snapAndMove(world, x, y, z, trainX, trainY, trainZ, 0, 0, 0, 0, new RailContext());
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info, MoveContext context) {
		return snapAndMove(world, x, y, z, trainX, trainY, trainZ, motionX, motionY, motionZ, speed, info);
	}

	/** Converts the node table into world coordinates, finds the nearest segment, then walks the chain for this tick */
	public Vec3d snapAndMove(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info) {

		BlockPos core = this.findCore(world, new BlockPos(x, y, z));
		if(core == null) return new Vec3d(trainX, trainY, trainZ);

		int cX = core.getX();
		int cY = core.getY();
		int cZ = core.getZ();
		int meta = metaAt(world, core);
		double moveAngle = Math.atan2(motionX, motionZ) * 180D / Math.PI + 90;
		Vec3d trainPos = new Vec3d(trainX, trainY, trainZ);
		Vec3d train = new Vec3d(trainX, trainY, trainZ);
		Vec3d corePos = new Vec3d(cX + 0.5, cY, cZ + 0.5);

		List<List<Pair<Vec3d[], RailDef>>> links = new ArrayList();

		for(RailDef def : railDefs) {

			List<Pair<Vec3d[], RailDef>> linkList = new ArrayList();
			links.add(linkList);

			for(int i = 0; i < def.nodes.size() - 1; i++) {

				Vec3d vec1 = getPositionFromNode(world, x, y, z, corePos, def.nodes.get(i), meta);
				Vec3d vec2 = getPositionFromNode(world, x, y, z, corePos, def.nodes.get(i + 1), meta);
				ParticleUtil.spawnDroneLine(world, vec1.x, vec1.y, vec1.z, vec2.x - vec1.x, vec2.y - vec1.y, vec2.z - vec1.z, 0xff0000);
				linkList.add(new Pair<Vec3d[], RailDef>(new Vec3d[] {vec1, vec2}, def));
			}
		}

		Pair<Vec3d[], RailDef> closest = null;
		Vec3d startingPos = null;
		/** The nearest chain */
		List<Pair<Vec3d[], RailDef>> cDef = null;
		double angularDiff = 0;
		double linkAngle = 0;
		double dist = Double.MAX_VALUE;
		/** The direction along the chain; false means walking it backwards */
		boolean d = true;

		for(List<Pair<Vec3d[], RailDef>> chain : links) {
			for(Pair<Vec3d[], RailDef> link : chain) {

				Vec3d[] array = link.getKey();
				Vec3d point = getClosestPointOnLink(array[0], array[1], train);

				if(point != null) {

					Vec3d delta = point.subtract(train);
					double length = delta.lengthVector();

					if(!canCross(world, cX, cY, cZ, trainPos, point, link.getValue())) continue;

					linkAngle = EntityRailCarBase.generateYaw(array[1], array[0]);
					angularDiff = BobMathUtil.angularDifference(linkAngle, -moveAngle);
					if(angularDiff < -180) { angularDiff += 180; linkAngle += 180; d = false; }
					if(angularDiff > 0) { angularDiff -= 180; linkAngle -= 180; d = false; }

					if(length < dist) {
						closest = link;
						startingPos = point;
						cDef = chain;
						dist = length;
					}
				}
			}
		}

		if(closest == null)
			return new Vec3d(trainX, trainY, trainZ);

		double distRemaining = speed;
		boolean engaged = false;
		Vec3d currentPos = startingPos;

		for(int i = d ? 0 : cDef.size() - 1; d ? (i < cDef.size()) : (i >= 0); i += d ? 1 : -1) {

			Pair<Vec3d[], RailDef> link = cDef.get(i);
			Vec3d[] array = link.getKey();

			if(!engaged) {
				if(link == closest) {
					engaged = true;
				} else {
					continue;
				}
			}

			Vec3d nextNode = array[d ? 1 : 0];
			Vec3d delta = nextNode.subtract(currentPos);

			if(!canCross(world, cX, cY, cZ, currentPos, nextNode, link.getValue())) break;

			double len = delta.lengthVector();

			if(len >= distRemaining) {
				info.overshoot = 0;
				double newYaw = EntityRailCarBase.generateYaw(nextNode, currentPos);
				if(Math.abs(BobMathUtil.angularDifference(newYaw, moveAngle)) < 45) info.yaw = (float) newYaw;
				else info.yaw = (float) moveAngle;
				delta = delta.normalize();
				return new Vec3d(currentPos.x - delta.x * distRemaining / len, currentPos.y - delta.y * distRemaining / len, currentPos.z - delta.z * distRemaining / len);
			}

			distRemaining -= len;
			currentPos = nextNode;
		}

		info.overshoot = distRemaining;
		info.pos = new BlockPos(currentPos.x, currentPos.y, currentPos.z);

		return currentPos;
	}

	/** The nearest point on this segment; outside either end it falls back to that endpoint */
	public Vec3d getClosestPointOnLink(Vec3d pointA, Vec3d pointB, Vec3d pointP) {

		double apX = pointP.x - pointA.x, apZ = pointP.z - pointA.z;
		double abX = pointB.x - pointA.x, abZ = pointB.z - pointA.z;

		double magAB = abX * abX + abZ * abZ;
		double dotProd = apX * abX + apZ * abZ;
		double dist = dotProd / magAB;

		if(dist < 0) return pointA;
		if(dist > 1) return pointB;

		return new Vec3d(pointA.x + abX * dist, pointA.y + (pointB.y - pointA.y) * dist, pointA.z + abZ * dist);
	}

	/** Nodes are written relative to the core, so they rotate by the core's facing before placement */
	public Vec3d getPositionFromNode(World world, int x, int y, int z, Vec3d core, Vec3d node, int meta) {

		float rotation = 0;
		if(meta == 12) rotation = 90F / 180F * (float) Math.PI;
		if(meta == 14) rotation = 180F / 180F * (float) Math.PI;
		if(meta == 13) rotation = 270F / 180F * (float) Math.PI;

		Vec3d copy = new Vec3d(node.x, node.y, node.z).rotateYaw(rotation);

		return core.addVector(copy.x, copy.y, copy.z);
	}

	/** One chain: the name feeds switch checks; nodes are coordinates relative to the core's center */
	public class RailDef {

		String name;
		public List<Vec3d> nodes = new ArrayList();

		public RailDef(String name) {
			this.name = name;
		}
	}
}
