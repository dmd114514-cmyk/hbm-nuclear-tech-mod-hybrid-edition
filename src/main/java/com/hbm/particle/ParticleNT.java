package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The old EntityFX became Particle in 1.12.2 and no longer extends Entity —
 * Entity members like rotationPitch/setDead are gone; subclasses supply their own where needed.
 */
@SideOnly(Side.CLIENT)
public class ParticleNT extends Particle {

	protected final ParticleDefinition definition;

	public ParticleNT(World world, double x, double y, double z, ParticleDefinition definition) {
		super(world, x, y, z);
		this.definition = definition;
	}

	public ParticleNT(World world, double x, double y, double z, double moX, double moY, double moZ, ParticleDefinition definition) {
		super(world, x, y, z, moX, moY, moZ);
		this.definition = definition;
	}
}
