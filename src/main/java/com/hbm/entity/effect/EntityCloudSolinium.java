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
 * The solinium cloud: a cyan swelling sphere, blasting a bolt skyward every tick.
 *
 * Shape changes as the fusion cloud (watcher → DataParameter, the two brightness ports parameterless).
 * The lightning line fires on both sides as before (the client spawns its own EntityLightningBolt),
 * with this version's new "effects only" switch given false — the old fire-starting real bolt.
 *
 * The registration name drops the old collision per this version's rules: the old one **shared** "entity_cloud_rainbow" with the rainbow cloud
 * (1.7 didn't dedupe; this registry throws), renamed entity_cloud_solinium here.
 */
public class EntityCloudSolinium extends Entity {

	private static final DataParameter<Integer> DW_MAX_AGE = EntityDataManager.createKey(EntityCloudSolinium.class, DataSerializers.VARINT);

	public int maxAge = 100;
	public int age;
	public float scale = 0;

	public EntityCloudSolinium(World world) {
		super(world);
		this.setSize(1, 4);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
		scale = 0;
	}

	public EntityCloudSolinium(World world, int maxAge) {
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
}
