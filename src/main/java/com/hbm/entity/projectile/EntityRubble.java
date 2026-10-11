package com.hbm.entity.projectile;

import com.hbm.lib.ModDamageSource;
import com.hbm.sound.SoundRefs;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * Building rubble: a flying speck from a shattered block, hurting what it hits (rubble damage);
 * past two ticks it lands and shatters — one debris sound, plus a "this block broke" broadcast so nearby clients draw particles.
 *
 * Two shape changes:
 *   · the two watchers became DataParameters, with getters for the render layer (rendering shouldn't know watcher ids);
 *   · the old version sent debris particles via its own ParticleBurstPacket (the commented-out playAuxSFX(2001,…) in the old source
 *     was its prototype); this version calls vanilla's playEvent(2001, pos, block state) directly — the server broadcasts automatically,
 *     no custom packet kept for it.
 */
public class EntityRubble extends EntityThrowableNT {

	private static final DataParameter<Integer> DW_BLOCK = EntityDataManager.createKey(EntityRubble.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> DW_META = EntityDataManager.createKey(EntityRubble.class, DataSerializers.VARINT);

	public EntityRubble(World world) {
		super(world);
	}

	public EntityRubble(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_BLOCK, Integer.valueOf(0));
		this.getDataManager().register(DW_META, Integer.valueOf(0));
	}

	@Override
	protected void onImpact(RayTraceResult mop) {

		if(mop.entityHit != null) {
			mop.entityHit.attackEntityFrom(ModDamageSource.rubble, 15);
		}

		if(this.ticksExisted > 2) {
			this.setDead();

			this.world.playSound(null, this.posX, this.posY, this.posZ,
					SoundRefs.resolve(new ResourceLocation("hbm:block.debris")), SoundCategory.BLOCKS, 1.5F, 1.0F);

			if(!this.world.isRemote) {
				Block block = Block.getBlockById(this.getBlockId());
				BlockPos pos = new BlockPos(MathHelper.floor(this.posX), (int) this.posY, MathHelper.floor(this.posZ));
				this.world.playEvent(2001, pos, Block.getStateId(block.getStateFromMeta(this.getBlockMeta())));
			}
		}
	}

	@Override
	protected float getAirDrag() {
		return 1F;
	}

	public void setMetaBasedOnBlock(Block b, int i) {
		this.getDataManager().set(DW_BLOCK, Integer.valueOf(Block.getIdFromBlock(b)));
		this.getDataManager().set(DW_META, Integer.valueOf(i));
	}

	public int getBlockId() {
		return this.getDataManager().get(DW_BLOCK).intValue();
	}

	public int getBlockMeta() {
		return this.getDataManager().get(DW_META).intValue();
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.getDataManager().set(DW_BLOCK, Integer.valueOf(nbt.getInteger("block")));
		this.getDataManager().set(DW_META, Integer.valueOf(nbt.getInteger("meta")));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("block", this.getBlockId());
		nbt.setInteger("meta", this.getBlockMeta());
	}
}
