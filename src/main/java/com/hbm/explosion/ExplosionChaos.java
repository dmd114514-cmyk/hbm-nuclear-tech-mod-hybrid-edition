package com.hbm.explosion;

import java.util.List;
import java.util.Random;

import com.hbm.interfaces.Spaghetti;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * The two terrain/entity pranks a landing item sets off: yank every block in radius up into the air, and lift the entities in radius a bit while rolling them over.
 *
 * The old table had eleven entries; only these two have live callers in this tree - the crystal_xen branch of {@code items/special/ItemDrop} points at exactly these two,
 * and that line is still blank waiting for the items side to wire it back. The other nine (hardenVirus, igniteFlammableBlocks, igniteAllBlocks, spawnPoisonCloud,
 * spawnVolley, cluster, poison, pc, c) are called by the various bombs and missiles, which are not in this tree yet - logged as missing, no never-runs shells built here.
 *
 * Three differences here, none of them touch the numbers: block reads/writes go through {@code IBlockState} (the old place the block, then patch the meta two-step is one placement here,
 * the properties travel with the state anyway); AABBs use the constructor (the old {@code getBoundingBox} factory is gone); rounding and sqrt now use this version's
 * {@code MathHelper.floor/sqrt}. The old {@code move} ended by assigning {@code radius = (int) f} to a local nobody read again - dead weight, not carried over.
 */
@Deprecated
@Spaghetti("my eyes are bleeding")
public class ExplosionChaos {

	private static final Random random = new Random();

	/**
	 * Everything in the lower hemisphere moves up to {@code height} blocks, air left behind - the old version built floating islands with this.
	 *
	 * The test is squared distance against half the squared radius, so it is the lower hemisphere that gets lifted; the old loop bound is {@code -r..r} (exclusive of r) and stays that way.
	 * Everything is checked against {@code isBlockLoaded} first: the old {@code getBlock} just returned air on an unloaded chunk,
	 * whereas reading it directly here would generate the chunk - an explosion quietly loading somebody else's terrain.
	 */
	public static void floater(World world, int x, int y, int z, int radi, int height) {

		int r = radi;
		int r2 = r * r;
		int r22 = r2 / 2;

		for(int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;

			for(int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;

				for(int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;

					if(ZZ >= r22) continue;

					BlockPos pos = new BlockPos(X, Y, Z);
					if(!world.isBlockLoaded(pos)) continue;

					IBlockState state = world.getBlockState(pos);
					Block save = state.getBlock();

					world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

					if(save != Blocks.AIR)
						world.setBlockState(pos.up(height), state, 3);
				}
			}
		}
	}

	/**
	 * The entities in the lower hemisphere rise {@code b} blocks (the other two axes {@code a}/{@code c} were always passed 0 by the old callers, the shape stays);
	 * and living things get a name tag for good measure - Dinnerbone/Grumm flips them, jeb_ gives sheep a rainbow, straight from the old version.
	 *
	 * The distance test uses the doubled radius while the landing test {@code d9} compares against the original - the old version mixed both radii exactly like this, numbers copied verbatim.
	 */
	public static void move(World world, int x, int y, int z, int radius, int a, int b, int c) {

		double reach = radius;
		double diameter = radius * 2.0F;

		int minX = MathHelper.floor(x - reach - 1.0D);
		int maxX = MathHelper.floor(x + reach + 1.0D);
		int minY = MathHelper.floor(y - reach - 1.0D);
		int maxY = MathHelper.floor(y + reach + 1.0D);
		int minZ = MathHelper.floor(z - reach - 1.0D);
		int maxZ = MathHelper.floor(z + reach + 1.0D);

		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));

		for(int index = 0; index < list.size(); index++) {

			Entity entity = list.get(index);
			double distanceScaled = entity.getDistance(x, y, z) / diameter;

			if(distanceScaled > 1.0D) continue;

			double dX = entity.posX - x;
			double dY = entity.posY + entity.getEyeHeight() - y;
			double dZ = entity.posZ - z;

			if(entity instanceof EntityLiving && !(entity instanceof EntitySheep)) {
				if(random.nextInt(2) == 0)
					((EntityLiving) entity).setCustomNameTag("Dinnerbone");
				else
					((EntityLiving) entity).setCustomNameTag("Grumm");
			}

			if(entity instanceof EntitySheep)
				((EntityLiving) entity).setCustomNameTag("jeb_");

			double distance = MathHelper.sqrt(dX * dX + dY * dY + dZ * dZ);
			if(distance < reach)
				entity.setPosition(entity.posX + a, entity.posY + b, entity.posZ + c);
		}
	}
}
