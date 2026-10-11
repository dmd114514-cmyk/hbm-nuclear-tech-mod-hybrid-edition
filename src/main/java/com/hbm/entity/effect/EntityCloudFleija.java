package com.hbm.entity.effect;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The fusion cloud: a swelling, flashing sphere, purely cosmetic, self-removing when its time is up.
 *
 * The old "max lifetime" rode raw index 16; a DataParameter here;
 * `worldObj.lastLightningBolt = 2` — that field is private now, so setLastLightningBolt(2) (the same field);
 * getBrightnessForRender/getBrightness both collapse to no-arg in this version.
 * The family's rainbow cloud takes the other path (real lightning every tick); this class only flashes the sky.
 */
public class EntityCloudFleija extends Entity {

	private static final DataParameter<Integer> DW_MAX_AGE = EntityDataManager.createKey(EntityCloudFleija.class, DataSerializers.VARINT);

	public int maxAge = 100;
	public int age;
	public float scale = 0;

	public EntityCloudFleija(World world) {
		super(world);
		this.setSize(1, 4);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
		scale = 0;
	}

	public EntityCloudFleija(World world, int maxAge) {
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
		this.world.setLastLightningBolt(2);

		if(!world.isRemote && this.age >= this.getMaxAge()) {
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
