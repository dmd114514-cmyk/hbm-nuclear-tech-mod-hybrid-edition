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
 * The EMP shock ring: a ring swelling horizontally with age (RenderEMPBlast draws it); the entity itself is just a timer.
 *
 * Only two shape changes: watcher 16 became a DataParameter, and the two brightness ports went parameterless.
 * Unlike the three clouds there's no humanoid size split here (both constructors are 1.5 square); copied as-is.
 */
public class EntityEMPBlast extends Entity {

	private static final DataParameter<Integer> DW_MAX_AGE = EntityDataManager.createKey(EntityEMPBlast.class, DataSerializers.VARINT);

	public int maxAge = 100;
	public int age;
	public float scale = 0;

	public EntityEMPBlast(World world) {
		super(world);
		this.setSize(1.5F, 1.5F);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
		scale = 0;
	}

	public EntityEMPBlast(World world, int maxAge) {
		super(world);
		this.setSize(1.5F, 1.5F);
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
