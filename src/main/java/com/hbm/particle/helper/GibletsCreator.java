package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.main.ClientProxy;
import com.hbm.particle.ParticleGiblet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Blasts an entity into ground-covering chunks: three kinds (meat / slime / metal) picked by the packet's gibType, quantity from the entity's own width and height.
 *
 * This wasn't a table entry in the old version but one of effectNT's long if-chain; the real gap to fill on the move is its opening step,
 * vanish: the chunks are already particles, so the original must be hidden locally on the client, or vanilla keeps painting a layer over something that should be gone.
 * That ledger exists only on the client (the server door is always false), so it can only live in the creator, not on the sending side.
 * Ashing and skeletonizing already worked this way; this is the same shape.
 *
 * Numbers align with the old version entry by entry, including that 1/15-chance mult *= 10 (the old variable's own words: "blast me to the stratosphere"):
 * the occasional high lob is part of this effect's look, not a typo. Chunk count uses integer division; narrow entities (under 0.25 wide) never shed anything anyway.
 */
public class GibletsCreator implements IParticleCreator {

	/**
	 * The server-side packing door: position at the entity's mid-height, view range 150 blocks as before.
	 * countDivisor is the trim divisor each old caller hard-coded; non-positive values stay out of the packet (no cDiv means untrimmed).
	 */
	public static void composeEffect(World world, Entity toShred, int gibType, int countDivisor) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "giblets");
		data.setInteger("ent", toShred.getEntityId());
		data.setInteger("gibType", gibType);
		if(countDivisor > 0) data.setInteger("cDiv", countDivisor);

		double midY = toShred.posY + toShred.height * 0.5;
		IParticleCreator.sendPacket(world, toShred.posX, midY, toShred.posZ, 150, data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		int entityID = data.getInteger("ent");
		int gibType = data.getInteger("gibType");

		// Bookkeep before looking up the entity, same order as the old version: a failed lookup still books, so the ghost frame doesn't draw either
		ClientProxy.vanish(entityID);

		Entity entity = world.getEntityByID(entityID);
		if(entity == null) return;

		int gW = (int)(entity.width / 0.25F);
		int gH = (int)(entity.height / 0.25F);
		int count = (int)(gW * 1.5 * gH);

		if(data.hasKey("cDiv"))
			count = (int)Math.ceil(count / (double)data.getInteger("cDiv"));

		double mult = rand.nextInt(15) == 0 ? 10D : 1D;

		for(int i = 0; i < count; i++) {
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleGiblet(texman, world, x, y, z,
					rand.nextGaussian() * 0.25 * mult, rand.nextDouble() * mult, rand.nextGaussian() * 0.25 * mult, gibType));
		}
	}
}
