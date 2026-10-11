package com.hbm.entity.effect;

import java.util.List;

import com.hbm.entity.projectile.EntityRubble;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The black hole: within its radius it chews blocks into rubble along rays (rubble entities come from entity/projectile),
 * drags everything toward the center and erases anything within size*1.5; an antimatter pellet or a flame pony getting sucked in detonates the hole itself.
 *
 * Shape changes on the move:
 *   · size (old raw index 16) became a DataParameter, with getSize() opened for the render layer and subclasses;
 *   · block reads/writes moved to 1.12.2's BlockPos/IBlockState (getBlockState/isAirBlock/setBlockToAir);
 *   · vectors became immutable Vec3d — the old rotateAroundY mutated in place, so the return value must be caught here;
 *   · the falling-block branch: the old version read func_145805_f()/field_145814_a for the carried block,
 *     both private and getter-less here, so fallTile comes out by reflection; when unreadable it's only erased, no rubble spat.
 */
public class EntityBlackHole extends Entity {

	public boolean breaksBlocks = true;

	protected static final DataParameter<Float> DW_SIZE = EntityDataManager.createKey(EntityBlackHole.class, DataSerializers.FLOAT);

	public EntityBlackHole(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.noClip = true;
	}

	public EntityBlackHole(World world, float size) {
		this(world);
		this.getDataManager().set(DW_SIZE, Float.valueOf(size));
	}

	public EntityBlackHole noBreak() {
		this.breaksBlocks = false;
		return this;
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(DW_SIZE, Float.valueOf(0.5F));
	}

	public float getSize() {
		return this.getDataManager().get(DW_SIZE).floatValue();
	}

	protected void setSizeValue(float size) {
		this.getDataManager().set(DW_SIZE, Float.valueOf(size));
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		float size = this.getSize();

		if(!world.isRemote && breaksBlocks) {

			for(int k = 0; k < size * 2; k++) {

				double phi = rand.nextDouble() * (Math.PI * 2);
				double costheta = rand.nextDouble() * 2 - 1;
				double theta = Math.acos(costheta);
				double x = Math.sin(theta) * Math.cos(phi);
				double y = Math.sin(theta) * Math.sin(phi);
				double z = Math.cos(theta);

				Vec3d vec = new Vec3d(x, y, z);
				int length = (int) Math.ceil(size * 15);

				for(int i = 0; i < length; i++) {

					BlockPos pos = new BlockPos((int) (this.posX + vec.x * i), (int) (this.posY + vec.y * i), (int) (this.posZ + vec.z * i));

					if(this.world.getBlockState(pos).getMaterial().isLiquid()) {
						this.world.setBlockToAir(pos);
					}

					if(!this.world.isAirBlock(pos)) {

						IBlockState state = this.world.getBlockState(pos);

						EntityRubble rubble = new EntityRubble(this.world);
						rubble.posX = pos.getX() + 0.5F;
						rubble.posY = pos.getY();
						rubble.posZ = pos.getZ() + 0.5F;
						rubble.setMetaBasedOnBlock(state.getBlock(), state.getBlock().getMetaFromState(state));

						this.world.spawnEntity(rubble);

						this.world.setBlockToAir(pos);
						break;
					}
				}
			}
		}

		double range = size * 15;

		List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(
				posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range));

		for(Entity e : entities) {

			if(e instanceof EntityPlayer && ((EntityPlayer) e).capabilities.isCreativeMode)
				continue;

			if(e instanceof EntityFallingBlock && !world.isRemote && e.ticksExisted > 1) {

				double x = e.posX;
				double y = e.posY;
				double z = e.posZ;

				IBlockState fallState = null;

				try {
					fallState = ObfuscationReflectionHelper.getPrivateValue(EntityFallingBlock.class, (EntityFallingBlock) e, "fallTile", "field_145814_a");
				} catch(Throwable t) {
					// Can't read the carried block: per the note above, erase only, no rubble
				}

				e.setDead();

				if(fallState != null) {
					EntityRubble rubble = new EntityRubble(world);
					rubble.setMetaBasedOnBlock(fallState.getBlock(), fallState.getBlock().getMetaFromState(fallState));
					rubble.setPositionAndRotation(x, y, z, 0, 0);
					rubble.motionX = e.motionX;
					rubble.motionY = e.motionY;
					rubble.motionZ = e.motionZ;
					world.spawnEntity(rubble);
				}
			}

			Vec3d vec = new Vec3d(posX - e.posX, posY - e.posY, posZ - e.posZ);

			double dist = vec.lengthVector();

			if(dist > range)
				continue;

			vec = vec.normalize();

			if(!(e instanceof EntityItem))
				vec = vec.rotateYaw((float) Math.toRadians(15));

			double speed = 0.1D;
			e.motionX += vec.x * speed;
			e.motionY += vec.y * speed * 2;
			e.motionZ += vec.z * speed;

			if(e instanceof EntityBlackHole)
				continue;

			if(dist < size * 1.5) {
				e.attackEntityFrom(ModDamageSource.blackhole, 1000);

				if(!(e instanceof EntityLivingBase))
					e.setDead();

				if(!world.isRemote && e instanceof EntityItem) {
					ItemStack stack = ((EntityItem) e).getItem();

					if(stack.getItem() == ModItems.pellet_antimatter || stack.getItem() == ModItems.flame_pony) {
						this.setDead();
						world.createExplosion(null, this.posX, this.posY, this.posZ, 5.0F, true);
						return;
					}
				}
			}
		}

		this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		this.motionX *= 0.99D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.99D;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		this.getDataManager().set(DW_SIZE, Float.valueOf(nbt.getFloat("size")));
		this.breaksBlocks = nbt.getBoolean("breaksBlocks");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setFloat("size", this.getSize());
		nbt.setBoolean("breaksBlocks", breaksBlocks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
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
}
