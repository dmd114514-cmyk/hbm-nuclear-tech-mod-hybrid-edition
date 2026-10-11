package com.hbm.entity.effect;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.particle.helper.FlameCreator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Lingering ground fire: an area boxed by width/height; the server sets fire to living beings inside each tick, the client only spawns flames.
 *
 * Three shape changes:
 *   · three watchers became DataParameters (the old ones read/wrote raw indices 10/11/12);
 *   · the old per-tick `yOffset = 0` line goes — Entity has no such field in this version;
 *   · the old writeToNBT override to an empty body is unneeded: that port was replaced by writeEntityToNBT/writeToNBTOptional,
 *     which are empty bodies / return false anyway.
 * One faithful copy: the black-fire line is a dangling `if(...) if(...) ... else ...` meaning "below 200 raise it to 200,
 * otherwise add 5"; keep the original binding when adding braces, don't shuffle it.
 */
public class EntityFireLingering extends Entity {

	public static int TYPE_DIESEL = 0;
	public static int TYPE_BALEFIRE = 1;
	public static int TYPE_PHOSPHORUS = 2;
	public static int TYPE_OXY = 3;
	public static int TYPE_BLACK = 4;

	private static final DataParameter<Integer> DW_TYPE = EntityDataManager.createKey(EntityFireLingering.class, DataSerializers.VARINT);
	private static final DataParameter<Float> DW_WIDTH = EntityDataManager.createKey(EntityFireLingering.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> DW_HEIGHT = EntityDataManager.createKey(EntityFireLingering.class, DataSerializers.FLOAT);

	public int maxAge = 150;

	public EntityFireLingering(World world) {
		super(world);
	}

	public EntityFireLingering setArea(float width, float height) {
		this.getDataManager().set(DW_WIDTH, Float.valueOf(width));
		this.getDataManager().set(DW_HEIGHT, Float.valueOf(height));
		return this;
	}

	public EntityFireLingering setDuration(int duration) {
		this.maxAge = duration;
		return this;
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(DW_TYPE, Integer.valueOf(0));
		this.getDataManager().register(DW_WIDTH, Float.valueOf(0F));
		this.getDataManager().register(DW_HEIGHT, Float.valueOf(0F));
	}

	public EntityFireLingering setType(int type) {
		this.getDataManager().set(DW_TYPE, Integer.valueOf(type));
		return this;
	}

	public int getType() {
		return this.getDataManager().get(DW_TYPE).intValue();
	}

	@Override
	public void onEntityUpdate() {

		float width = this.getDataManager().get(DW_WIDTH).floatValue();
		float height = this.getDataManager().get(DW_HEIGHT).floatValue();

		this.setSize(width, height);
		this.setPosition(this.posX, this.posY, this.posZ);

		if(!world.isRemote) {

			if(this.ticksExisted >= maxAge) {
				this.setDead();
			}

			List<Entity> affected = world.getEntitiesWithinAABBExcludingEntity(this,
					new AxisAlignedBB(posX - width / 2, posY, posZ - width / 2, posX + width / 2, posY + height, posZ + width / 2));

			for(Entity e : affected) {
				if(e instanceof EntityLivingBase) {
					EntityLivingBase living = (EntityLivingBase) e;
					HbmLivingProps props = HbmLivingProps.getData(living);
					if(this.getType() == TYPE_DIESEL) if(props.fire < 60) props.fire = 60;
					if(this.getType() == TYPE_PHOSPHORUS) if(props.fire < 300) props.fire = 300;
					if(this.getType() == TYPE_BALEFIRE) if(props.balefire < 100) props.balefire = 100;
					if(this.getType() == TYPE_BLACK) { if(props.blackFire < 200) props.blackFire = 200; else props.blackFire += 5; }
				} else {
					e.setFire(4);
				}
			}
		} else {

			for(int i = 0; i < (width >= 5 ? 2 : 1); i++) {
				double x = posX - width / 2 + rand.nextDouble() * width;
				double z = posZ - width / 2 + rand.nextDouble() * width;

				Vec3d up = new Vec3d(x, posY + height, z);
				Vec3d down = new Vec3d(x, posY - height, z);
				RayTraceResult mop = world.rayTraceBlocks(up, down, false, true, true);
				if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) down = mop.hitVec;
				if(this.getType() == TYPE_DIESEL) FlameCreator.composeEffectClient(world, x, down.y, z, FlameCreator.META_FIRE);
				if(this.getType() == TYPE_PHOSPHORUS) FlameCreator.composeEffectClient(world, x, down.y, z, FlameCreator.META_FIRE);
				if(this.getType() == TYPE_BALEFIRE) FlameCreator.composeEffectClient(world, x, down.y, z, FlameCreator.META_BALEFIRE);
				if(this.getType() == TYPE_BLACK) FlameCreator.composeEffectClient(world, x, down.y, z, FlameCreator.META_BLACK);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderOnFire() {
		return false;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) { }

	@Override
	public boolean writeToNBTOptional(NBTTagCompound nbt) {
		return false;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		this.setDead();
	}
}
