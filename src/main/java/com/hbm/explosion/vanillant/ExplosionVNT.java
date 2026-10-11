package com.hbm.explosion.vanillant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;
import com.hbm.explosion.vanillant.interfaces.IBlockProcessor;
import com.hbm.explosion.vanillant.interfaces.IEntityProcessor;
import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;
import com.hbm.explosion.vanillant.interfaces.IPlayerProcessor;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.CustomDamageHandlerAmat;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectAmat;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

/** Splits an explosion into pluggable parts: block allocation, entity settlement, block settlement, presentation, each swappable */
public class ExplosionVNT {

	private IBlockAllocator blockAllocator;
	private IEntityProcessor entityProcessor;
	private IBlockProcessor blockProcessor;
	private IPlayerProcessor playerProcessor;
	//presentation usually stacks several (sound and particles arrive separately), hence an array
	private IExplosionSFX[] sfx;

	public World world;
	public double posX;
	public double posY;
	public double posZ;
	public float size;
	public Entity exploder;

	private Map<EntityPlayer, Vec3d> compatPlayers = new HashMap<EntityPlayer, Vec3d>();
	public Explosion compat;

	public ExplosionVNT(World world, double x, double y, double z, float size) {
		this(world, x, y, z, size, null);
	}

	public ExplosionVNT(World world, double x, double y, double z, float size, Entity exploder) {
		this.world = world;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.size = size;
		this.exploder = exploder;

		//1.12.2's Explosion fields are all private and fixed at construction; the only way is feeding values at construction + overriding the knockback map exit to get the player map out
		this.compat = new Explosion(world, exploder, x, y, z, size, false, true) {

			@Override
			public Map<EntityPlayer, Vec3d> getPlayerKnockbackMap() {
				return ExplosionVNT.this.compatPlayers;
			}
		};
	}

	public void explode() {

		boolean processBlocks = blockAllocator != null && blockProcessor != null;
		boolean processEntities = entityProcessor != null && playerProcessor != null;

		HashSet<BlockPos> affectedBlocks = null;
		HashMap<EntityPlayer, Vec3d> affectedPlayers = null;

		//distribution
		if(processBlocks) affectedBlocks = blockAllocator.allocate(this, world, posX, posY, posZ, size);
		if(processBlocks) this.compat.getAffectedBlockPositions().addAll(affectedBlocks);
		if(processEntities) affectedPlayers = entityProcessor.process(this, world, posX, posY, posZ, size);
		//The affected-entity list already travels with the Detonate event anyway; this copy exists for the knockback map
		if(processEntities) this.compatPlayers.putAll(affectedPlayers);

		//server settlement
		if(processBlocks) blockProcessor.process(this, world, posX, posY, posZ, affectedBlocks);
		if(processEntities) playerProcessor.process(this, world, posX, posY, posZ, affectedPlayers);

		if(sfx != null) {
			for(IExplosionSFX fx : sfx) {
				fx.doEffect(this, world, posX, posY, posZ, size);
			}
		}
	}

	public ExplosionVNT setBlockAllocator(IBlockAllocator blockAllocator) {
		this.blockAllocator = blockAllocator;
		return this;
	}
	public ExplosionVNT setEntityProcessor(IEntityProcessor entityProcessor) {
		this.entityProcessor = entityProcessor;
		return this;
	}
	public ExplosionVNT setBlockProcessor(IBlockProcessor blockProcessor) {
		this.blockProcessor = blockProcessor;
		return this;
	}
	public ExplosionVNT setPlayerProcessor(IPlayerProcessor playerProcessor) {
		this.playerProcessor = playerProcessor;
		return this;
	}
	public ExplosionVNT setSFX(IExplosionSFX... sfx) {
		this.sfx = sfx;
		return this;
	}

	public ExplosionVNT makeStandard() {
		this.setBlockAllocator(new BlockAllocatorStandard());
		this.setBlockProcessor(new BlockProcessorStandard());
		this.setEntityProcessor(new EntityProcessorStandard());
		this.setPlayerProcessor(new PlayerProcessorStandard());
		this.setSFX(new ExplosionEffectStandard());
		return this;
	}

	public ExplosionVNT makeAmat() {
		this.setBlockAllocator(new BlockAllocatorStandard(this.size < 15 ? 16 : 32));
		this.setBlockProcessor(new BlockProcessorStandard()
				.setNoDrop());
		this.setEntityProcessor(new EntityProcessorStandard()
				.withRangeMod(2F)
				.withDamageMod(new CustomDamageHandlerAmat(50F)));
		this.setPlayerProcessor(new PlayerProcessorStandard());
		this.setSFX(new ExplosionEffectAmat());
		return this;
	}
}
