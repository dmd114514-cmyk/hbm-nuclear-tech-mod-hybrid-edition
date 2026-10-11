package com.hbm.entity.mob;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

/**
 * The block spider: a mob carrying a block on its back, that block deciding its health. On the old table it's called
 * {@code entity_taintcrawler}; the name stays, don't change it.
 *
 * Production isn't here: this mob is summoned by the pollution/taint branch (emitted from {@code EntityCreeperTainted} and taint spread in the old version,
 * world and block side), and {@code makeBlock(Block, int)} is the door for that side — it asks on landing
 * "which block are you right now". Until that side lands, this mob only spawns via commands or other callers, hence renderer and registration first.
 *
 * Four deltas:
 * - the two carried numbers (block id and metadata) rode raw dataWatcher slots 12/13; both became
 *   {@code DataParameter}s (VARINT) with registration-ordered slots; the renderer no longer pokes the slots,
 *   reading the two getters below. Defaults as before: block id 1 (stone at that old moment), metadata 0.
 * - health comes from the block's blast resistance: the old {@code block.getExplosionResistance(null)}; that door's parameter here
 *   is "the exploder", and the old call passed null anyway ⇒ null as-is, not an omission.
 * - the old constructor's {@code getNavigator().setAvoidsWater(true)} has no counterpart (decompile-verified: this version's
 *   PathNavigate has only setSpeed/setPath, PathNavigateGround only setCanSwim/setAvoidSun and door toggles).
 *   Dropping it changes nothing: ground navigation's {@code canSwim} defaults off here, and staying dry is the default;
 *   anyone adding "can swim" later goes through {@code PathNavigateGround.setCanSwim(true)}, not a same-named setter.
 * - {@code isAIEnabled()} is gone entirely (AI always on); the old targeting task was {@code (this, EntityPlayer.class, 0, true)},
 *   that four-arg shape is gone, leaving {@code (creature, class, boolean)} where the last bit is the old
 *   {@code alwaysClose=true} (the old 0 was default priority) ⇒ true here, same as the RAD beast's.
 */
public class EntityBlockSpider extends EntityMob {

	/** The carried block: old dataWatcher slot 12 */
	private static final DataParameter<Integer> DW_BLOCK = EntityDataManager.<Integer>createKey(EntityBlockSpider.class, DataSerializers.VARINT);

	/** That block's metadata: old dataWatcher slot 13 */
	private static final DataParameter<Integer> DW_META = EntityDataManager.<Integer>createKey(EntityBlockSpider.class, DataSerializers.VARINT);

	public EntityBlockSpider(World world) {
		super(world);

		this.setSize(0.95F, 1.25F);
		this.tasks.addTask(1, new EntityAIWander(this, 0.5D));
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_BLOCK, Integer.valueOf(Block.getIdFromBlock(Blocks.STONE)));
		this.getDataManager().register(DW_META, Integer.valueOf(0));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
	}

	/**
	 * Swaps the carried block, adjusting health to its blast resistance (floor 1 — never insta-death at 0).
	 * The caller is the pollution side (see the class note); this side only provides the door.
	 */
	public void makeBlock(Block block, int meta) {

		this.getDataManager().set(DW_BLOCK, Integer.valueOf(Block.getIdFromBlock(block)));
		this.getDataManager().set(DW_META, Integer.valueOf(meta));

		double health = Math.max(1D, block.getExplosionResistance((Entity) null));

		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.setHealth(this.getMaxHealth());
	}

	/** The renderer poses the carried block off these two */
	public Block getBlockType() {
		return Block.getBlockById(((Integer) this.getDataManager().get(DW_BLOCK)).intValue());
	}

	public int getBlockMeta() {
		return ((Integer) this.getDataManager().get(DW_META)).intValue();
	}
}
