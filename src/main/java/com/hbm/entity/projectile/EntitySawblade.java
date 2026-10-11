package com.hbm.entity.projectile;

import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.particle.helper.GibletsCreator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The sawblade: structurally the flywheel's twin (the old version copy-pasted that section and tweaked), so both are written out separately here as before,
 * no shared base extracted — the two will drift differently later, and merging would bury the old differences.
 *
 * Three differences from EntityCog:
 * - one data field only: orientation. The old version declared index 11 but never wrote it, reading it only in getMeta,
 *   so this keeps a "permanently 0" door for the render layer's texture pick (the saw has one texture; never actually used).
 *   The save writes only rot, no meta.
 * - picking it up yields ModItems.sawblade, the meta-less version.
 * - different spin: the render side's modulo period differs — a render-side affair.
 *
 * This version's conventions match the flywheel's; details recorded in EntityThrowableNT's and EntityCog's class notes
 * (processInitialInteract, EnumFacing, RayTraceResult and friends).
 * The blood-splatter effect also goes through GibletsCreator.composeEffect, packet shape identical to the flywheel's.
 */
public class EntitySawblade extends EntityThrowableInterp {

	private static final DataParameter<Integer> DW_ORIENTATION = EntityDataManager.createKey(EntitySawblade.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> DW_META = EntityDataManager.createKey(EntitySawblade.class, DataSerializers.VARINT);

	public EntitySawblade(World world) {
		super(world);
		this.setSize(1F, 1F);
	}

	public EntitySawblade(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setSize(1F, 1F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_ORIENTATION, Integer.valueOf(0));
		this.getDataManager().register(DW_META, Integer.valueOf(0));
	}

	/** The low 6 bits are the pre-stick facing; +6 means pinned to a face */
	public EntitySawblade setOrientation(int rot) {
		this.getDataManager().set(DW_ORIENTATION, Integer.valueOf(rot));
		return this;
	}

	public int getOrientation() {
		return this.getDataManager().get(DW_ORIENTATION).intValue();
	}

	/** No write door even in the old version — only the render layer reads it for texturing; the saw has one texture, so always 0 */
	public int getMeta() {
		return this.getDataManager().get(DW_META).intValue();
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {

		if(!world.isRemote) {

			if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.sawblade)))
				this.setDead();

			player.inventoryContainer.detectAndSendChanges();
		}

		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	protected void onImpact(RayTraceResult mop) {

		if(world != null && mop != null && mop.typeOfHit == RayTraceResult.Type.ENTITY && mop.entityHit != null && mop.entityHit.isEntityAlive()) {
			Entity e = mop.entityHit;
			e.attackEntityFrom(ModDamageSource.rubble, 1000);
			if(!e.isEntityAlive() && e instanceof EntityLivingBase) {
				GibletsCreator.composeEffect(world, e, 0, 5);

				world.playSound(null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.NEUTRAL, 2.0F, 0.95F + world.rand.nextFloat() * 0.2F);
			}
		}

		if(this.ticksExisted > 1 && world != null && mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {

			int orientation = this.getOrientation();

			if(orientation < 6) {

				if(new Vec3d(motionX, motionY, motionZ).lengthVector() < 0.75) {
					this.setOrientation(orientation + 6);
					orientation += 6;
				} else {
					EnumFacing side = mop.sideHit;
					this.motionX *= 1 - (Math.abs(side.getFrontOffsetX()) * 2);
					this.motionY *= 1 - (Math.abs(side.getFrontOffsetY()) * 2);
					this.motionZ *= 1 - (Math.abs(side.getFrontOffsetZ()) * 2);
					world.createExplosion(this, posX, posY, posZ, 3F, false);

					BlockPos hit = mop.getBlockPos();
					if(world.getBlockState(hit).getBlock().getExplosionResistance(this) < 50) {
						world.destroyBlock(hit, false);
					}
				}
			}

			if(orientation >= 6) {
				this.motionX = 0;
				this.motionY = 0;
				this.motionZ = 0;
				this.inGround = true;
			}
		}
	}

	@Override
	public void onUpdate() {

		if(!world.isRemote) {
			int orientation = this.getOrientation();
			if(orientation >= 6 && !this.inGround) {
				this.setOrientation(orientation - 6);
			}
		}

		super.onUpdate();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	@Override
	public double getGravityVelocity() {
		return inGround ? 0 : 0.03D;
	}

	@Override
	protected int groundDespawn() {
		return 0;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("rot", this.getOrientation());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.setOrientation(nbt.getInteger("rot"));
	}
}
