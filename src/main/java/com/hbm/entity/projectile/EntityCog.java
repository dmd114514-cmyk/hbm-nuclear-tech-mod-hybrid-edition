package com.hbm.entity.projectile;

import com.hbm.lib.ModDamageSource;
import com.hbm.items.ModItems;
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
 * The flywheel: thrown, it bites into walls and shreds what it hits for 1000 points of rubble damage, spinning to a stop on a face until picked up.
 *
 * Orientation lives in the data watcher, not just rotationYaw: it must distinguish "still flying" from "stuck", and once stuck
 * the render layer poses it by the face it bit. Both ride DataParameters here (raw ids 10/11 in the old
 * dataWatcher; ids are auto-assigned by createKey now — no hand-written ordinals).
 *
 * Two conventions this version:
 * - the hit block face: the old `ForgeDirection.getOrientation(mop.sideHit)`; this version's RayTraceResult
 *   sideHit is already an EnumFacing, taken directly; offsetX/Y/Z become getFrontOffsetX/Y/Z.
 * - pickup: the old `interactFirst(EntityPlayer)` has no same-named door here;
 *   on bare Entity it's `processInitialInteract(EntityPlayer, EnumHand)`, returning false as before (not blocking other interactions).
 *
 * The blood-splatter effect sends type=giblets, both packing and sending collected in particle/helper/GibletsCreator's composeEffect
 * (the old version hand-built NBT and sent it directly; this tree's sibling effects — ashing, skeletonizing — all go through the creator's packing door, so same shape).
 * Damage, explosions and block breaking are unaffected.
 */
public class EntityCog extends EntityThrowableInterp {

	private static final DataParameter<Integer> DW_ORIENTATION = EntityDataManager.createKey(EntityCog.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> DW_META = EntityDataManager.createKey(EntityCog.class, DataSerializers.VARINT);

	public EntityCog(World world) {
		super(world);
		this.setSize(1F, 1F);
	}

	public EntityCog(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setSize(1F, 1F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_ORIENTATION, Integer.valueOf(0));
		this.getDataManager().register(DW_META, Integer.valueOf(0));
	}

	/** The low 6 bits are the still-spinning facing; +6 means stuck still */
	public EntityCog setOrientation(int rot) {
		this.getDataManager().set(DW_ORIENTATION, Integer.valueOf(rot));
		return this;
	}

	public EntityCog setMeta(int meta) {
		this.getDataManager().set(DW_META, Integer.valueOf(meta));
		return this;
	}

	public int getOrientation() {
		return this.getDataManager().get(DW_ORIENTATION).intValue();
	}

	public int getMeta() {
		return this.getDataManager().get(DW_META).intValue();
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {

		if(!world.isRemote) {

			if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.gear_large, 1, this.getMeta())))
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
		nbt.setInteger("meta", this.getMeta());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.setOrientation(nbt.getInteger("rot"));
		this.setMeta(nbt.getInteger("meta"));
	}
}
