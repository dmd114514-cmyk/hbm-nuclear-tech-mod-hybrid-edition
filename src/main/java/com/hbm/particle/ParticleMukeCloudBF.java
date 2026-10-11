package com.hbm.particle;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The muke cloud BF tier: its only difference is the strip texture (explosion_bf).
 *
 * The parent's physics, paging and lifetime tiers all carry over; this overrides only the getSprite door — exactly the old shape
 * (a 23-line shell swapping textures via getTexture()), structure untouched.
 */
@SideOnly(Side.CLIENT)
public class ParticleMukeCloudBF extends ParticleMukeCloud {

	public ParticleMukeCloudBF(TextureManager texman, World world, double x, double y, double z, double mx, double my, double mz) {
		super(texman, world, x, y, z, mx, my, mz);
	}

	@Override
	protected TextureAtlasSprite getSprite() {
		return ParticleSprites.sprite("explosion_bf");
	}
}
