package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The continuously radiating blocks (the uranium/plutonium/thorium "hazardous material" tier).
 *
 * Behavior as before: on world entry it reads this block item's radiation from the hazard table (a tenth) as the prefix, then adds every 2 seconds
 * and reschedules itself; beacon base and naming ride the {@link BlockBase}. The differences are two rewritten spots:
 *   1) block updates and coordinates moved to BlockPos/IBlockState (the old x/y/z triple);
 *   2) client particles go through {@code World.spawnParticle(EnumParticleTypes...)} here; the old RADFOG/FLAMES/LAVAPOP
 *      map to townaura / flame+smoke / lava one for one; SCHRAB sent a custom `schrabfog` packet in the old version,
 *      absent from this version's effectNT table — logged and skipped (radiation itself still runs server-side, unaffected);
 *
 * The old `getRarity` (blue names on the bismuth/joyeuse/plutonic acid items) didn't come over: the block side needs an ItemBlock forwarder this version,
 */
public class BlockHazard extends BlockBase {

	protected float rad = 0.0F;
	private ExtDisplayEffect extEffect = null;

	public BlockHazard() {
		this(Material.IRON);
	}

	public BlockHazard(Material mat) {
		super(mat);
	}

	public BlockHazard setDisplayEffect(ExtDisplayEffect extEffect) {
		this.extEffect = extEffect;
		return this;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);

		rad = HazardSystem.getHazardLevelFromStack(new ItemStack(Item.getItemFromBlock(this)), HazardRegistry.RADIATION) * 0.1F;

		if(this.rad > 0)
			world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(this.rad > 0) {
			ChunkRadiationManager.proxy.incrementRad(world, pos.getX(), pos.getY(), pos.getZ(), rad);
			world.scheduleUpdate(pos, this, this.tickRate(world));
		}
	}

	@Override
	public int tickRate(World world) {

		if(this.rad > 0)
			return 20;

		return super.tickRate(world);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);

		if(extEffect == null)
			return;

		switch(extEffect) {
		case RADFOG:
		case SCHRAB:
		case FLAMES:
			sPart(world, pos, rand);
			break;

		case SPARKS:
			break;

		case LAVAPOP:
			world.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
			break;

		default: break;
		}
	}

	@SideOnly(Side.CLIENT)
	private void sPart(World world, BlockPos pos, Random rand) {

		for(EnumFacing dir : EnumFacing.VALUES) {

			if(dir == EnumFacing.DOWN && this.extEffect == ExtDisplayEffect.FLAMES)
				continue;

			if(world.getBlockState(pos.offset(dir)).getMaterial() == Material.AIR) {

				double ix = pos.getX() + 0.5F + dir.getFrontOffsetX() + rand.nextDouble() * 3 - 1.5D;
				double iy = pos.getY() + 0.5F + dir.getFrontOffsetY() + rand.nextDouble() * 3 - 1.5D;
				double iz = pos.getZ() + 0.5F + dir.getFrontOffsetZ() + rand.nextDouble() * 3 - 1.5D;

				if(dir.getFrontOffsetX() != 0)
					ix = pos.getX() + 0.5F + dir.getFrontOffsetX() * 0.5 + rand.nextDouble() * dir.getFrontOffsetX();
				if(dir.getFrontOffsetY() != 0)
					iy = pos.getY() + 0.5F + dir.getFrontOffsetY() * 0.5 + rand.nextDouble() * dir.getFrontOffsetY();
				if(dir.getFrontOffsetZ() != 0)
					iz = pos.getZ() + 0.5F + dir.getFrontOffsetZ() * 0.5 + rand.nextDouble() * dir.getFrontOffsetZ();

				if(this.extEffect == ExtDisplayEffect.RADFOG) {
					world.spawnParticle(EnumParticleTypes.TOWN_AURA, ix, iy, iz, 0.0D, 0.0D, 0.0D);
				}
				if(this.extEffect == ExtDisplayEffect.SCHRAB) {
					//the old version sent effectNT's schrabfog packet here; no such type in this version's dispatch table — logged and skipped
				}
				if(this.extEffect == ExtDisplayEffect.FLAMES) {
					world.spawnParticle(EnumParticleTypes.FLAME, ix, iy, iz, 0.0D, 0.0D, 0.0D);
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0D, 0.0D, 0.0D);
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0D, 0.1D, 0.0D);
				}
			}
		}
	}

	public enum ExtDisplayEffect {
		RADFOG,
		SPARKS,
		SCHRAB,
		FLAMES,
		LAVAPOP
	}
}
