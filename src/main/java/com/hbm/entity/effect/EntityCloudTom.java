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
 * The moon-rock blast cloud: a rotating column with a vertically scrolling texture, drawn by RenderCloudTom; the entity itself is invisible.
 *
 * Two deltas from the other clouds, copied: expiring calls setDead outright (no age reset — the old behavior),
 * and isInRangeToRenderDist is always true (the whole sky curtain must draw; no distance culling).
 */
public class EntityCloudTom extends Entity {

	private static final DataParameter<Integer> DW_MAX_AGE = EntityDataManager.createKey(EntityCloudTom.class, DataSerializers.VARINT);

	public int maxAge = 100;
	public int age;

	public EntityCloudTom(World world) {
		super(world);
		this.setSize(1, 4);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
	}

	public EntityCloudTom(World world, int maxAge) {
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

		if(this.age >= this.getMaxAge()) {
			this.setDead();
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		age = nbt.getShort("age");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setShort("age", (short) age);
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
		return true;
	}
}
