package com.hbm.entity.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The rainbow cloud: a purely cosmetic sphere, swelling while blasting lightning skyward.
 *
 * Two shape changes:
 *   · watcher 16 became a DataParameter;
 *   · the old getBrightnessForRender(float)/getBrightness(float) are parameterless in this version,
 *     so they collapse to no-arg here (return value unchanged: full bright).
 * One faithful copy: the lightning strike in onUpdate fires on both sides (the old version wrote it that way) — the client also spawns its own
 * EntityLightningBolt; don't make it server-only — lightning renders client-side, and removing that leaves no visible bolt.
 * This version's EntityLightningBolt constructor gained an "effects only" switch; false is the old fire-starting real bolt.
 */
public class EntityCloudFleijaRainbow extends Entity {

	private static final DataParameter<Integer> DW_MAX_AGE = EntityDataManager.createKey(EntityCloudFleijaRainbow.class, DataSerializers.VARINT);

	public int maxAge = 100;
	public int age;
	public float scale = 0;

	public EntityCloudFleijaRainbow(World world) {
		super(world);
		this.setSize(1, 4);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
		scale = 0;
	}

	public EntityCloudFleijaRainbow(World world, int maxAge) {
		super(world);
		this.setSize(20, 40);
		this.isImmuneToFire = true;
		this.setMaxAge(maxAge);
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(DW_MAX_AGE, Integer.valueOf(0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender() {
		return 15728880;
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@Override
	public void onUpdate() {

		this.age++;
		this.world.spawnEntity(new EntityLightningBolt(this.world, this.posX, this.posY + 200, this.posZ, false));

		if(this.age >= this.getMaxAge()) {
			this.age = 0;
			this.setDead();
		}

		this.scale++;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		age = nbt.getShort("age");
		scale = nbt.getShort("scale");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setShort("age", (short) age);
		nbt.setShort("scale", (short) scale);
	}

	public void setMaxAge(int i) {
		this.getDataManager().set(DW_MAX_AGE, Integer.valueOf(i));
	}

	public int getMaxAge() {
		return this.getDataManager().get(DW_MAX_AGE).intValue();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
	}
}
