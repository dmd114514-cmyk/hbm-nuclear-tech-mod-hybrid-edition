package com.hbm.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.ThreeInts;
import com.hbm.interfaces.ICopiable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.util.Clock;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

/**
 * The fake-multiblock base: one machine has a single real body (with a tile entity), surrounded entirely by "placeholder cells".
 * Placeholders point back at the core via their own meta, so they don't save, don't hold contents, and leave nothing behind when broken.
 *
 * The meta's three tiers, facing numbering and mirror math all live in this class; subclasses may only call
 * the {@link #getMetaForCore}/{@link #getStateFromMeta} layer — no hand-computed offsets.
 */
public abstract class BlockDummyable extends BlockMetaBase implements ICustomBlockHighlight, ICopiable, INBTBlockTransformable {

	/// BLOCK METADATA ///

	// 0-5  placeholders: store the facing the core points at them; reversed, it's the way back
	// 6-11 same, plus an extra flag (placeholders needing extra handling, like pipe ports)
	// 12-15 cores: own facing + offset; only the four horizontal facings get in
	public static final int offset = 10;
	public static final int extra = 6;
	/** The core tier's lower bound; the smallest horizontal facing is NORTH=2, hence starting at 12 — matching the old 176 subclasses' meta >= 12 */
	public static final int CORE_MIN = offset + EnumFacing.NORTH.ordinal();

	/*
	 * An integer set before placement, intercepted in createNewTileEntity. The same meta can also produce different tiles,
	 * a back door for "one block, several machines" structures. Ugly, but a proper parameter would touch the whole chunk-write chain.
	 */
	public static int overrideTileMeta = 0;

	public static void setOverride(int i) {
		overrideTileMeta = i;
	}

	public static void resetOverride() {
		overrideTileMeta = 0;
	}

	public BlockDummyable(Material mat) {
		super(mat);
	}

	/** meta → facing; null when out of range. The old version returned ForgeDirection.UNKNOWN here; this version has no such value */
	public static EnumFacing facingForMeta(int meta) {
		return meta >= 0 && meta < EnumFacing.VALUES.length ? EnumFacing.VALUES[meta] : null;
	}

	/** Is this the core tier */
	public static boolean isCore(int meta) {
		return meta >= CORE_MIN;
	}

	/** An extra-flagged placeholder (static so the conversions above can share the same criterion) */
	public static boolean hasExtra(int meta) {
		return meta >= extra && meta < CORE_MIN;
	}

	protected static int metaAt(IBlockAccess world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getBlock().getMetaFromState(state);
	}

	/* The meta carrier and its round-trips live in BlockMetaBase; this class only owns the three-tier meanings and facing numbering */
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block cause, BlockPos causePos) {
		super.neighborChanged(state, world, pos, cause, causePos);

		// The master gate sits in MultiblockHandlerXR; borrowed here, not rebuilt: while a batch lays, nobody tears down anyone
		if(MultiblockHandlerXR.safeRem) return;

		if(!internalPlayers.isEmpty()) {
			boolean anyStillInside = false;
			for(EntityPlayer player : internalPlayers) {
				if(isPlayerInside(world, player)) {
					anyStillInside = true;
					break;
				}
			}

			if(anyStillInside) {
				world.scheduleBlockUpdate(pos, this, 1, 0);
			} else {
				internalPlayers.clear();
			}
		}

		destroyIfOrphan(world, pos);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		super.updateTick(world, pos, state, rand);

		if(!internalPlayers.isEmpty()) {
			boolean anyStillInside = false;
			for(EntityPlayer player : internalPlayers) {
				if(isPlayerInside(world, player)) {
					anyStillInside = true;
					break;
				}
			}

			if(anyStillInside) {
				world.scheduleBlockUpdate(pos, this, 1, 0);
			} else {
				internalPlayers.clear();
			}
		}
	}

	private void destroyIfOrphan(World world, BlockPos pos) {
		if(world.isRemote) return;

		int metadata = metaAt(world, pos);

		// flagged ones lose the flag first
		if(hasExtra(metadata))
			metadata -= extra;

		EnumFacing dir = facingForMeta(metadata);

		// still out of range after stripping means it's a core; cores get no orphan check (the old version managed the same via UNKNOWN's zero offset)
		if(dir == null) return;

		BlockPos anchor = pos.offset(dir.getOpposite());
		Block b = world.getBlockState(anchor).getBlock();

		// Guards against multiblocks on chunk borders being wrongly deleted. In theory chunk crossings could breed ghost placeholders,
		// but that beats "the whole machine vanishes when a chunk unloads" — no benefit to the player and not reachable by accident.
		if(b != this && world.isAreaLoaded(pos, 1)) {
			if(isLegacyMonoblock(world, pos)) {
				fixLegacyMonoblock(world, pos);
			} else {
				world.setBlockToAir(pos);
			}
		}
	}

	// Overridden when a single block upgrades into a fake multiblock: returning true stops this cell being deleted as an orphan and promotes it to core in place
	protected boolean isLegacyMonoblock(World world, BlockPos pos) {
		return false;
	}

	protected void fixLegacyMonoblock(World world, BlockPos pos) {
		world.setBlockState(pos, getStateFromMeta(offset + metaAt(world, pos)), 3);
	}

	public BlockPos findCore(IBlockAccess world, BlockPos pos) {
		positions.clear();
		return findCoreRec(world, pos);
	}

	List<ThreeInts> positions = new ArrayList<>();

	public BlockPos findCoreRec(IBlockAccess world, BlockPos pos) {

		int metadata = metaAt(world, pos);

		if(isCore(metadata))
			return pos;

		if(hasExtra(metadata))
			metadata -= extra;

		EnumFacing dir = facingForMeta(metadata);

		if(dir == null)
			return null;

		BlockPos anchor = pos.offset(dir.getOpposite());

		if(world.getBlockState(anchor).getBlock() != this) {
			return null;
		}

		ThreeInts probe = new ThreeInts(pos.getX(), pos.getY(), pos.getZ());

		if(positions.contains(probe))
			return null;

		positions.add(probe);

		return findCoreRec(world, anchor);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getStateFromMeta(facing.ordinal());
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {

		if(!(player instanceof EntityPlayer))
			return;

		EnumFacing placedSide = facingForMeta(metaAt(world, pos));

		// Leave a gap for the placeholder chain: this cell is about to become a core or placeholder; clear it first so the new block doesn't tear down neighbors on landing
		MultiblockHandlerXR.safeRem = true;
		world.setBlockToAir(pos);
		MultiblockHandlerXR.safeRem = false;

		EntityPlayer pl = (EntityPlayer) player;

		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		// the player's facing, used to shove the machine out in front
		EnumFacing facingDir = EnumFacing.NORTH;

		if(placedSide == EnumFacing.UP || placedSide == EnumFacing.DOWN) {
			if(i == 0) facingDir = EnumFacing.NORTH;
			if(i == 1) facingDir = EnumFacing.EAST;
			if(i == 2) facingDir = EnumFacing.SOUTH;
			if(i == 3) facingDir = EnumFacing.WEST;
		} else {
			facingDir = placedSide;
		}

		// the whole machine's final facing
		EnumFacing dir = getDirModified(facingDir);

		int o = -getOffset();

		int ox = pos.getX() + facingDir.getFrontOffsetX() * o;
		int oy = pos.getY() + getHeightOffset();
		int oz = pos.getZ() + facingDir.getFrontOffsetZ() * o;

		// wall-mounted and stacked placements offset differently; re-checked against the rotated dimensions
		if(placedSide == EnumFacing.DOWN) {
			int[] dim = getDimensions();
			oy -= dim[0] + dim[1];
		} else if(placedSide != EnumFacing.UP) {
			int[] rotDim = MultiblockHandlerXR.rotate(getDimensions(), dir);
			if(rotDim != null) {
				ox = pos.getX() + placedSide.getFrontOffsetX() * rotDim[placedSide.getOpposite().ordinal()];
				oz = pos.getZ() + placedSide.getFrontOffsetZ() * rotDim[placedSide.getOpposite().ordinal()];
			}
		}

		if(!checkRequirement(world, ox - dir.getFrontOffsetX() * o, oy, oz - dir.getFrontOffsetZ() * o, dir, o)) {

			if(!pl.capabilities.isCreativeMode) {
				ItemStack stack = pl.inventory.mainInventory.get(pl.inventory.currentItem);
				Item item = Item.getItemFromBlock(this);

				if(stack.isEmpty()) {
					pl.inventory.mainInventory.set(pl.inventory.currentItem, new ItemStack(this));
				} else {
					if(stack.getItem() != item || stack.getCount() == stack.getMaxStackSize()) {
						pl.inventory.addItemStackToInventory(new ItemStack(this));
					} else {
						pl.getHeldItemMainhand().grow(1);
					}
				}
			}

			return;
		}

		BlockPos origin = new BlockPos(ox, oy, oz);

		if(!world.isRemote) {
			// computing them separately matters: the machine's facing and the final meta aren't always equivalent
			int meta = getMetaForCore(world, origin, pl, dir.ordinal() + offset);

			world.setBlockState(origin, getStateFromMeta(meta), 3);
			IPersistentNBT.restoreData(world, origin, itemStack);
			fillSpace(world, ox - dir.getFrontOffsetX() * o, oy, oz - dir.getFrontOffsetZ() * o, dir, o);
		}

		// if the player stands inside the freshly laid structure, remember it — breaking waits until they step out
		if(isPlayerInside(world, pl)) internalPlayers.add(pl);

		world.scheduleBlockUpdate(pos, this, 1, 0);
		world.scheduleBlockUpdate(pos, this, 2, 0);

		super.onBlockPlacedBy(world, pos, state, player, itemStack);
	}

	private boolean isPlayerInside(World world, EntityPlayer player) {
		BlockPos feet = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY + 0.1D), MathHelper.floor(player.posZ));
		return world.getBlockState(feet).getBlock() == this || world.getBlockState(feet.up()).getBlock() == this;
	}

	/**
	 * A notch finer than {@link #getDirModified}, but the result must land in the core tier.
	 * Grabbing an extra tier is possible but drags in core-lookup recursion and tile numbering — not worth it.
	 */
	protected int getMetaForCore(World world, BlockPos pos, EntityPlayer player, int original) {
		return original;
	}

	/** As if the player turned to place; limits or corrects the placeable orientations */
	protected EnumFacing getDirModified(EnumFacing dir) {
		return dir;
	}

	protected boolean checkRequirement(World world, int x, int y, int z, EnumFacing dir, int o) {
		return MultiblockHandlerXR.checkSpace(world, x + dir.getFrontOffsetX() * o, y + dir.getFrontOffsetY() * o, z + dir.getFrontOffsetZ() * o, getDimensions(), x, y, z, dir);
	}

	protected void fillSpace(World world, int x, int y, int z, EnumFacing dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x + dir.getFrontOffsetX() * o, y + dir.getFrontOffsetY() * o, z + dir.getFrontOffsetZ() * o, getDimensions(), this, dir);
	}

	/** Promotes a plain placeholder to the extra-flagged kind */
	public void makeExtra(World world, BlockPos pos) {

		if(world.getBlockState(pos).getBlock() != this)
			return;

		int meta = metaAt(world, pos);

		if(meta > 5)
			return;

		MultiblockHandlerXR.safeRem = true;
		world.setBlockState(pos, getStateFromMeta(meta + extra), 3);
		MultiblockHandlerXR.safeRem = false;
	}

	public void removeExtra(World world, BlockPos pos) {

		if(world.getBlockState(pos).getBlock() != this)
			return;

		int meta = metaAt(world, pos);

		if(!hasExtra(meta))
			return;

		MultiblockHandlerXR.safeRem = true;
		world.setBlockState(pos, getStateFromMeta(meta - extra), 3);
		MultiblockHandlerXR.safeRem = false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		int i = getMetaFromState(state);

		if(isCore(i)) { } else if(!MultiblockHandlerXR.safeRem) {

			if(hasExtra(i)) i -= extra;

			EnumFacing d = facingForMeta(i);

			if(d != null) {
				BlockPos anchor = pos.offset(d.getOpposite());
				if(world.getBlockState(anchor).getBlock() == this)
					world.setBlockToAir(anchor);
			}
		}

		TileEntity te = world.getTileEntity(pos);

		if(te instanceof ISidedInventory) {

			ISidedInventory sidedinv = (ISidedInventory) te;

			for(int i1 = 0; i1 < sidedinv.getSizeInventory(); ++i1) {
				ItemStack itemstack = sidedinv.getStackInSlot(i1);

				if(!itemstack.isEmpty()) {
					float f = world.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.getCount() > 0) {
						int j1 = world.rand.nextInt(21) + 10;

						if(j1 > itemstack.getCount()) {
							j1 = itemstack.getCount();
						}

						ItemStack dropped = new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage());

						if(itemstack.hasTagCompound()) {
							dropped.setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
						}

						itemstack.shrink(j1);

						EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, dropped);

						float f3 = 0.05F;
						entityitem.motionX = world.rand.nextGaussian() * f3;
						entityitem.motionY = world.rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = world.rand.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}
			}

			world.markBlockRangeForRenderUpdate(pos, pos);
		}

		super.breakBlock(world, pos, state);
	}

	/** Looks go entirely to the tile entity; the block body takes no render layer */
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) { return false; }

	/**
	 * @returns six numbers: how many placeholder cells to lay along each of the core's six directions, ordered U D N S W E
	 */
	public abstract int[] getDimensions();

	public abstract int getOffset();

	public int getHeightOffset() {
		return 0;
	}

	protected boolean standardOpenBehavior(World world, BlockPos pos, EntityPlayer player, int id) {

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			BlockPos core = findCore(world, pos);

			if(core == null)
				return false;

			player.openGui(MainRegistry.instance, id, world, core.getX(), core.getY(), core.getZ());
			return true;
		} else {
			return true;
		}
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			// Drops "machine body + the data in its tile entity"; 1.12.2 block drops don't read tables at all (see docs §17) — a mechanism call
			this.dropBlockAsItem(world, pos, state, 0);
			harvesters.set(null);
		}
	}

	/*
	 * By here both the block and its tile entity are gone, so this override only records the "block mined" stat;
	 * the drop belongs to the one above — don't drop again here.
	 */
	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this), 1);
		player.addExhaustion(0.025F);
	}

	public boolean useDetailedHitbox() {
		return !bounding.isEmpty();
	}

	public List<AxisAlignedBB> bounding = new ArrayList<>();

	// Players currently standing inside this block family
	private List<EntityPlayer> internalPlayers = new ArrayList<>();

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {
		if(!internalPlayers.isEmpty() && internalPlayers.contains(entity))
			return;

		if(!this.useDetailedHitbox()) {
			super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity, isActualState);
			return;
		}

		BlockPos core = this.findCore(world, pos);

		if(core == null)
			return;

		EnumFacing rot = facingForMeta(metaAt(world, core) - offset);

		for(AxisAlignedBB aabb : this.bounding) {
			AxisAlignedBB boxlet = getAABBRotationOffset(aabb, core.getX() + 0.5, core.getY(), core.getZ() + 0.5, rot);

			if(entityBox.intersects(boxlet)) {
				collidingBoxes.add(boxlet);
			}
		}
	}

	/*The whole machine's selection box rotates with this too, so the up/down cases give the full block rather than a half offset */
	public static AxisAlignedBB getAABBRotationOffset(AxisAlignedBB aabb, double x, double y, double z, EnumFacing dir) {

		AxisAlignedBB newBox = null;

		if(dir == EnumFacing.NORTH) newBox = new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
		if(dir == EnumFacing.EAST) newBox = new AxisAlignedBB(-aabb.maxZ, aabb.minY, aabb.minX, -aabb.minZ, aabb.maxY, aabb.maxX);
		if(dir == EnumFacing.SOUTH) newBox = new AxisAlignedBB(-aabb.maxX, aabb.minY, -aabb.maxZ, -aabb.minX, aabb.maxY, -aabb.minZ);
		if(dir == EnumFacing.WEST) newBox = new AxisAlignedBB(aabb.minZ, aabb.minY, -aabb.maxX, aabb.maxZ, aabb.maxY, -aabb.minX);

		if(newBox != null) {
			// This version's AABB is immutable; offset returns a new box — the old in-place edit silently lost the offset without catching the return
			return newBox.offset(x, y, z);
		}

		return new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).offset(x + 0.5, y + 0.5, z + 0.5);
	}

	// don't touch the xyz parameters, or the interaction-distance check bites
	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d startVec, Vec3d endVec) {
		if(!this.useDetailedHitbox()) {
			return super.collisionRayTrace(state, world, pos, startVec, endVec);
		}

		BlockPos core = this.findCore(world, pos);

		if(core == null)
			return super.collisionRayTrace(state, world, pos, startVec, endVec);

		EnumFacing rot = facingForMeta(metaAt(world, core) - offset);

		for(AxisAlignedBB aabb : this.bounding) {
			AxisAlignedBB boxlet = getAABBRotationOffset(aabb, core.getX() + 0.5, core.getY(), core.getZ() + 0.5, rot);

			RayTraceResult intercept = boxlet.calculateIntercept(startVec, endVec);
			if(intercept != null) {
				return new RayTraceResult(RayTraceResult.Type.BLOCK, intercept.hitVec, intercept.sideHit, pos);
			}
		}

		return null;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		if(!this.useDetailedHitbox()) {
			return FULL_BLOCK_AABB;
		}
		// item collision only behaves when maxY isn't 1; this number was found by trial, don't revert it
		return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.999F, 1.0F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldDrawHighlight(World world, BlockPos pos) {
		return !this.bounding.isEmpty();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event, World world, BlockPos pos) {

		BlockPos core = this.findCore(world, pos);
		if(core == null) return;

		EntityPlayer player = event.getPlayer();
		float interp = event.getPartialTicks();
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) interp;
		double exp = 0.002F;

		EnumFacing rot = facingForMeta(metaAt(world, core) - offset);

		ICustomBlockHighlight.setup();
		for(AxisAlignedBB aabb : this.bounding) RenderGlobal.drawSelectionBoundingBox(getAABBRotationOffset(aabb.expand(exp, exp, exp), 0, 0, 0, rot).offset(core.getX() - dX + 0.5, core.getY() - dY, core.getZ() - dZ + 0.5), 0.0F, 0.0F, 0.0F, 0.4F);
		ICustomBlockHighlight.cleanup();
	}

	@Override
	public NBTTagCompound getSettings(World world, BlockPos pos) {
		BlockPos core = findCore(world, pos);
		if(core == null) return null;
		TileEntity tile = world.getTileEntity(core);
		if(tile instanceof ICopiable)
			return ((ICopiable) tile).getSettings(world, core);
		else
			return null;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, BlockPos pos) {
		BlockPos core = findCore(world, pos);
		if(core == null) return;
		TileEntity tile = world.getTileEntity(core);
		if(tile instanceof ICopiable)
			((ICopiable) tile).pasteSettings(nbt, index, world, player, core);
	}

	@Override
	public String[] infoForDisplay(World world, BlockPos pos) {
		BlockPos core = findCore(world, pos);
		if(core == null) return null;
		TileEntity tile = world.getTileEntity(core);
		if(tile instanceof ICopiable)
			return ((ICopiable) tile).infoForDisplay(world, core);
		return null;
	}

	/*The meta's facing component: subtract the tier's base and there it is; wrong numbers give null */
	private static EnumFacing facingOf(int meta) {
		if(isCore(meta)) return facingForMeta(meta - offset);
		if(hasExtra(meta)) return facingForMeta(meta - extra);
		return facingForMeta(meta);
	}

	/*Keep the tier, swap just the facing bits */
	private static int turnMeta(int meta, java.util.function.UnaryOperator<EnumFacing> turn) {
		EnumFacing facing = facingOf(meta);
		if(facing == null) return meta;
		EnumFacing turned = turn.apply(facing);
		return turned == null ? meta : meta - facing.ordinal() + turned.ordinal();
	}

	/**
	 * Vanilla structure blocks (a 1.10 addition the old version never fed) rotate the whole template — cell positions and facing together,
	 * so the placeholders' "pointing at the core" numbering stays self-consistent after one rotation; none of the three tiers needs special care.
	 * The conversion doesn't copy `transformMeta`'s tier numbers; vanilla's own `Rotation`/`Mirror` implementations are used,
	 * with the self-check confirming who maps to which tier — no guessing clockwise vs counterclockwise here.
	 */
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return getStateFromMeta(turnMeta(getMetaFromState(state), rot::rotate));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return getStateFromMeta(turnMeta(getMetaFromState(state), mirror::mirror));
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		boolean isOffset = isCore(meta); // crowding them causes trouble, so core and extra each step back to the placeholder tier before rotating
		boolean isExtra = !isOffset && hasExtra(meta);

		if(isOffset) {
			meta -= offset;
		} else if(isExtra) {
			meta -= extra;
		}

		meta = INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);

		if(isOffset) {
			meta += offset;
		} else if(isExtra) {
			meta += extra;
		}

		return meta;
	}

	public int[][] getAllDimensions() {
		return new int[][] { getDimensions() };
	}

	public double[][] getAABBExtras() {
		return new double[0][0];
	}

	/*The placement-preview brush: no "set one color for a batch of vertices" anymore — color travels with each vertex */
	private static final class LinePen {

		private final BufferBuilder buffer;
		private int r, g, b;

		LinePen(BufferBuilder buffer) {
			this.buffer = buffer;
		}

		void rgb(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		void v(double x, double y, double z) {
			buffer.pos(x, y, z).color(r, g, b, 255).endVertex();
		}
	}

	@SideOnly(Side.CLIENT)
	public void drawPlacementHighlight(EntityPlayer player, float interp) {
		RayTraceResult mop = player.rayTrace(5.0D, interp);

		if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
			double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
			double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
			double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) interp;

			int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

			EnumFacing placedSide = mop.sideHit;
			BlockPos clicked = mop.getBlockPos().offset(placedSide);

			// the player's facing, used to shove the machine out in front
			EnumFacing facing = EnumFacing.NORTH;

			if(placedSide == EnumFacing.UP || placedSide == EnumFacing.DOWN) {
				if(i == 0) facing = EnumFacing.NORTH;
				if(i == 1) facing = EnumFacing.EAST;
				if(i == 2) facing = EnumFacing.SOUTH;
				if(i == 3) facing = EnumFacing.WEST;
			} else {
				facing = placedSide;
			}

			// the whole machine's final facing
			EnumFacing dir = getDirModified(facing);

			int o = -getOffset();

			int originX = clicked.getX() + facing.getFrontOffsetX() * o;
			int originY = clicked.getY() + getHeightOffset();
			int originZ = clicked.getZ() + facing.getFrontOffsetZ() * o;

			// wall-mounted and stacked placements offset differently; re-checked against the rotated dimensions
			if(placedSide == EnumFacing.DOWN) {
				int[] dim = getDimensions();
				originY -= dim[0] + dim[1];
			} else if(placedSide != EnumFacing.UP) {
				int[] rotDim = MultiblockHandlerXR.rotate(getDimensions(), dir);
				if(rotDim != null) {
					originX = clicked.getX() + placedSide.getFrontOffsetX() * rotDim[placedSide.getOpposite().ordinal()];
					originZ = clicked.getZ() + placedSide.getFrontOffsetZ() * rotDim[placedSide.getOpposite().ordinal()];
				}
			}

			boolean canPlace = checkRequirement(player.world, originX - dir.getFrontOffsetX() * o, originY, originZ - dir.getFrontOffsetZ() * o, dir, o);

			BufferBuilder buffer = Tessellator.getInstance().getBuffer();

			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			GL11.glLineWidth(2.0F);
			GL11.glDepthMask(false);
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

			double timer = (Clock.get_ms() % (1000D * Math.PI)) / 250D;
			double sine = Math.sin(timer);
			int color = (int) (255 * (sine * 0.25 + 0.75));

			LinePen pen = new LinePen(buffer);

			if(canPlace) {
				pen.rgb(0, color, 0);
			} else {
				pen.rgb(color, 0, 0);
			}

			// Spreads each layer's dimensions into cells to light up. XL multiblocks must override getAllDimensions,
			// or the shape draws wrong.
			List<BlockPos> blocks = new java.util.ArrayList<>();
			Set<BlockPos> set = new java.util.HashSet<>();

			for(int[] dims : getAllDimensions()) {
				// Some structures place with an offset; the ones that don't get a run of zeros appended
				int offFwd = dims.length > 6 ? dims[6] : 0;
				int offUp = dims.length > 7 ? dims[7] : 0;
				int offLat = dims.length > 8 ? dims[8] : 0;
				int worldOffX;
				int worldOffY;
				int worldOffZ;

				worldOffY = offUp;
				worldOffX = facing.getFrontOffsetX() * offFwd + facing.rotateY().getFrontOffsetX() * offLat;
				worldOffZ = facing.getFrontOffsetZ() * offFwd + facing.rotateY().getFrontOffsetZ() * offLat;

				int[] rot = MultiblockHandlerXR.rotate(dims, facing);
				for(int bx = -rot[4] + worldOffX; bx <= rot[5] + worldOffX; bx++) {
					for(int by = -rot[1] + worldOffY; by <= rot[0] + worldOffY; by++) {
						for(int bz = -rot[2] + worldOffZ; bz <= rot[3] + worldOffZ; bz++) {
							BlockPos bp = new BlockPos(MathHelper.floor(originX) + bx, MathHelper.floor(originY) + by, MathHelper.floor(originZ) + bz);
							blocks.add(bp);
							set.add(bp);
						}
					}

				}
			}
			// walk the neighbors, drawing lines only on the sides missing one, and the box assembles itself (this trick learned from Mellow)
			for(BlockPos pos : blocks) {
				boolean px = set.contains(pos.add(1, 0, 0));
				boolean nx = set.contains(pos.add(-1, 0, 0));
				boolean ppy = set.contains(pos.add(0, 1, 0));
				boolean ny = set.contains(pos.add(0, -1, 0));
				boolean ppz = set.contains(pos.add(0, 0, 1));
				boolean nz = set.contains(pos.add(0, 0, -1));

				double minX = pos.getX() - dX;
				double maxX = pos.getX() + 1 - dX;
				double minY = pos.getY() - dY;
				double maxY = pos.getY() + 1 - dY;
				double minZ = pos.getZ() - dZ;
				double maxZ = pos.getZ() + 1 - dZ;

				if(!ppy) {
					if(!nx) {
						pen.v(minX, maxY, minZ);
						pen.v(minX, maxY, maxZ);
					}
					if(!ppz) {
						pen.v(minX, maxY, maxZ);
						pen.v(maxX, maxY, maxZ);
					}
					if(!px) {
						pen.v(maxX, maxY, maxZ);
						pen.v(maxX, maxY, minZ);
					}
					if(!nz) {
						pen.v(maxX, maxY, minZ);
						pen.v(minX, maxY, minZ);
					}
				}
				if(!ny) {
					if(!nx) {
						pen.v(minX, minY, minZ);
						pen.v(minX, minY, maxZ);
					}
					if(!ppz) {
						pen.v(minX, minY, maxZ);
						pen.v(maxX, minY, maxZ);
					}
					if(!px) {
						pen.v(maxX, minY, maxZ);
						pen.v(maxX, minY, minZ);
					}
					if(!nz) {
						pen.v(maxX, minY, minZ);
						pen.v(minX, minY, minZ);
					}
				}
				if(!nz) {
					if(!nx) {
						pen.v(minX, minY, minZ);
						pen.v(minX, maxY, minZ);
					}
					if(!ppy) {
						pen.v(minX, maxY, minZ);
						pen.v(maxX, maxY, minZ);
					}
					if(!px) {
						pen.v(maxX, maxY, minZ);
						pen.v(maxX, minY, minZ);
					}
					if(!ny) {
						pen.v(maxX, minY, minZ);
						pen.v(minX, minY, minZ);
					}
				}
				if(!ppz) {
					if(!nx) {
						pen.v(minX, minY, maxZ);
						pen.v(minX, maxY, maxZ);
					}
					if(!ppy) {
						pen.v(minX, maxY, maxZ);
						pen.v(maxX, maxY, maxZ);
					}
					if(!px) {
						pen.v(maxX, maxY, maxZ);
						pen.v(maxX, minY, maxZ);
					}
					if(!ny) {
						pen.v(maxX, minY, maxZ);
						pen.v(minX, minY, maxZ);
					}
				}
				if(!nx) {
					if(!nz) {
						pen.v(minX, minY, minZ);
						pen.v(minX, maxY, minZ);
					}
					if(!ppy) {
						pen.v(minX, maxY, minZ);
						pen.v(minX, maxY, maxZ);
					}
					if(!ppz) {
						pen.v(minX, maxY, maxZ);
						pen.v(minX, minY, maxZ);
					}
					if(!ny) {
						pen.v(minX, minY, maxZ);
						pen.v(minX, minY, minZ);
					}
				}
				if(!px) {
					if(!nz) {
						pen.v(maxX, minY, minZ);
						pen.v(maxX, maxY, minZ);
					}
					if(!ppy) {
						pen.v(maxX, maxY, minZ);
						pen.v(maxX, maxY, maxZ);
					}
					if(!ppz) {
						pen.v(maxX, maxY, maxZ);
						pen.v(maxX, minY, maxZ);
					}
					if(!ny) {
						pen.v(maxX, minY, maxZ);
						pen.v(maxX, minY, minZ);
					}
				}
			}

			pen.rgb(0, 0, color);

			// the extra boxes hung on the outside
			for(double[] extra : this.getAABBExtras()) {
				EnumFacing rot = facing.rotateY();
				double cX = MathHelper.floor(originX) - dX + 0.5;
				double cY = MathHelper.floor(originY) - dY;
				double cZ = MathHelper.floor(originZ) - dZ + 0.5;

				double upr = extra[0];
				double lwr = extra[1];
				double fwd = extra[2];
				double bwd = extra[3];
				double lft = extra[4];
				double rgt = extra[5];

				double x0 = cX + fwd * facing.getFrontOffsetX() + lft * rot.getFrontOffsetX();
				double x1 = cX + bwd * facing.getFrontOffsetX() + rgt * rot.getFrontOffsetX();
				double y0 = cY + lwr;
				double y1 = cY + upr;
				double z0 = cZ + fwd * facing.getFrontOffsetZ() + lft * rot.getFrontOffsetZ();
				double z1 = cZ + bwd * facing.getFrontOffsetZ() + rgt * rot.getFrontOffsetZ();

				pen.v(x0, y0, z0); pen.v(x0, y0, z1);
				pen.v(x1, y0, z0); pen.v(x1, y0, z1);
				pen.v(x0, y0, z0); pen.v(x1, y0, z0);
				pen.v(x0, y0, z1); pen.v(x1, y0, z1);

				pen.v(x0, y1, z0); pen.v(x0, y1, z1);
				pen.v(x1, y1, z0); pen.v(x1, y1, z1);
				pen.v(x0, y1, z0); pen.v(x1, y1, z0);
				pen.v(x0, y1, z1); pen.v(x1, y1, z1);

				pen.v(x0, y0, z0); pen.v(x0, y1, z0);
				pen.v(x1, y0, z0); pen.v(x1, y1, z0);
				pen.v(x0, y0, z1); pen.v(x0, y1, z1);
				pen.v(x1, y0, z1); pen.v(x1, y1, z1);
			}

			Tessellator.getInstance().draw();
			buffer.setTranslation(0, 0, 0);

			GL11.glDepthMask(true);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
	}

	/**
	 * The facing conventions' self-check: core/placeholder/extra numbering, core lookups, mirrored hitbox facing,
	 * rotated structure meta — none of it touches saves or the world; one fake block layer verifies it all.
	 * The placement-time offset math needs a real world; tested alongside the first machine.
	 */
	public static String selfCheck() {

		StringBuilder sb = new StringBuilder();
		int bad = 0;

		BlockDummyable probe = new BlockDummyable(Material.ROCK) {
			@Override public int[] getDimensions() { return new int[] { 0, 0, 1, 1, 1, 1 }; }
			@Override public int getOffset() { return 0; }
			@Override public TileEntity createNewTileEntity(World world, int meta) { return null; }
		};

		BlockPos core = new BlockPos(10, 10, 10);
		FakeAccess fake = new FakeAccess(probe);

		for(EnumFacing dir : new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST }) {

			// A placeholder's meta stores the direction the core points at it; the extra tier adds 6 on top
			int coreMeta = dir.ordinal() + offset;
			int dummyMeta = dir.ordinal();
			BlockPos dummy = core.offset(dir);
			EnumFacing side = dir.rotateY();
			BlockPos stub = core.offset(side);

			fake.put(core, coreMeta);
			fake.put(dummy, dummyMeta);
			fake.put(stub, side.ordinal() + extra);

			EnumFacing turned = facingForMeta(probe.transformMeta(coreMeta, 2) - offset);
			EnumFacing expect = oppositeInPair(dir);

			BlockPos found = probe.findCore(fake.access(), dummy);
			BlockPos extraFound = probe.findCore(fake.access(), stub);

			boolean rangesOk = coreMeta >= CORE_MIN && coreMeta <= 15 && dummyMeta + extra >= extra && dummyMeta + extra < CORE_MIN;
			String rotFail = rotMismatch(probe, coreMeta);

			if(!rangesOk || core.equals(found) == false || core.equals(extraFound) == false || turned != expect || !isMirrorCorrect(dir) || !rotFail.isEmpty()) bad++;

			sb.append("facing ").append(dir.getName())
					.append(": core meta=").append(coreMeta)
					.append(" placeholder meta=").append(dummyMeta)
					.append(" marker block meta=").append(side.ordinal() + extra)
					.append(" back to core=").append(core.equals(found) ? "OK" : "FAIL")
					.append(" marker back to core=").append(core.equals(extraFound) ? "OK" : "FAIL")
					.append(" mirror hitbox=").append(isMirrorCorrect(dir) ? "OK" : "FAIL")
					.append(" structure rotated to north=").append(turned == expect ? "OK" : "FAIL")
					.append(" rotation hook=").append(rotFail.isEmpty() ? "OK" : "FAIL " + rotFail)
					.append("\n");

			fake.clear();
		}

		String mirFail = mirrorMismatch(probe);
		if(!mirFail.isEmpty()) bad++;
		sb.append("mirror hook: ").append(mirFail.isEmpty() ? "OK" : "FAIL " + mirFail).append("\n");

		sb.append("self-test verdict: ").append(bad == 0 ? "facing convention passes all four directions" : bad + " mismatches");
		return sb.toString();
	}

	private static int rot(BlockDummyable probe, int meta, Rotation r) {
		return probe.getMetaFromState(probe.withRotation(probe.getStateFromMeta(meta), r));
	}

	private static int mir(BlockDummyable probe, int meta, Mirror m) {
		return probe.getMetaFromState(probe.withMirror(probe.getStateFromMeta(meta), m));
	}

	/**
	 * Vanilla `Rotation`'s three tiers should map one-to-one onto the old `transformMeta`'s 1/2/3.
	 * The mapping isn't guessed: the old `NBTStructure` drove mode 1 via `dir.getRotation(UP)` (= this version's rotateY = clockwise),
	 * mode 3 via `getRotation(DOWN)` (=rotateYCCW, counterclockwise) and mode 2 by inversion; this table's 2→5/2→4 matches.
	 * An empty string means all correct; otherwise the first mismatch comes back as "who expected=? got=?".
	 */
	private static String rotMismatch(BlockDummyable probe, int coreMeta) {
		if(rot(probe, coreMeta, Rotation.NONE) != coreMeta) return "NONE expected=" + coreMeta + " got=" + rot(probe, coreMeta, Rotation.NONE);
		return compare("CW90", rot(probe, coreMeta, Rotation.CLOCKWISE_90), probe.transformMeta(coreMeta, 1),
				compare("CW180", rot(probe, coreMeta, Rotation.CLOCKWISE_180), probe.transformMeta(coreMeta, 2),
						compare("CCW90", rot(probe, coreMeta, Rotation.COUNTERCLOCKWISE_90), probe.transformMeta(coreMeta, 3), "")));
	}

	private static String compare(String what, int got, int expect, String tail) {
		if(got == expect) return tail;
		return what + " expected=" + expect + " got=" + got + (tail.isEmpty() ? "" : "; " + tail);
	}

	/**
	 * Mirror's three invariants: each mirror applied twice restores; each mirror touches exactly one axis pair; the two mirrors touch different pairs.
	 * **Not** trusting names about who flips east-west vs north-south — measured, this version's `LEFT_RIGHT` flips north-south, the name exactly counter to intuition,
	 * so only structural properties are asserted; the implementation hands facing straight to vanilla's `Mirror.mirror` anyway and can't go wrong from it.
	 */
	private static String mirrorMismatch(BlockDummyable probe) {
		StringBuilder sb = new StringBuilder();

		int north = EnumFacing.NORTH.ordinal() + offset;
		int west = EnumFacing.WEST.ordinal() + offset;
		boolean nsByLr = facingOf(mir(probe, north, Mirror.LEFT_RIGHT)) != EnumFacing.NORTH;
		boolean weByLr = facingOf(mir(probe, west, Mirror.LEFT_RIGHT)) != EnumFacing.WEST;
		boolean nsByFb = facingOf(mir(probe, north, Mirror.FRONT_BACK)) != EnumFacing.NORTH;
		boolean weByFb = facingOf(mir(probe, west, Mirror.FRONT_BACK)) != EnumFacing.WEST;

		if(nsByLr == weByLr) sb.append("LEFT_RIGHT moved two pairs of axes, or none;");
		if(nsByFb == weByFb) sb.append("FRONT_BACK moved two pairs of axes, or none;");
		if(nsByLr == nsByFb) sb.append("both mirrors moved the same pair of axes;");
		if(mir(probe, mir(probe, north, Mirror.LEFT_RIGHT), Mirror.LEFT_RIGHT) != north) sb.append("LEFT_RIGHT flipped twice and didn't come back;");
		if(mir(probe, mir(probe, west, Mirror.FRONT_BACK), Mirror.FRONT_BACK) != west) sb.append("FRONT_BACK flipped twice and didn't come back;");

		return sb.toString();
	}

	/*Which horizontal facing each becomes when the structure turns from its facing to "north" */
	private static EnumFacing oppositeInPair(EnumFacing dir) {
		if(dir == EnumFacing.NORTH) return EnumFacing.SOUTH;
		if(dir == EnumFacing.SOUTH) return EnumFacing.NORTH;
		if(dir == EnumFacing.WEST) return EnumFacing.EAST;
		return EnumFacing.WEST;
	}

	/**
	 * Take a pole pointing north; after rotating to each facing it must land on the right axis.
	 * The center is deliberately non-zero (8,3,-6) so "rotated right but offset lost" shows up too.
	 */
	private static boolean isMirrorCorrect(EnumFacing dir) {
		AxisAlignedBB nose = new AxisAlignedBB(-0.5, 0, -1.5, 0.5, 1, -0.5);
		AxisAlignedBB turned = getAABBRotationOffset(nose, 8, 3, -6, dir);

		boolean parked = turned.minY == 3.0 && turned.minX > 6.0 && turned.maxX < 10.0 && turned.minZ > -8.0 && turned.maxZ < -4.0;
		if(!parked) return false;

		switch(dir) {
		case NORTH: return turned.maxZ < -6.0;
		case SOUTH: return turned.minZ > -6.0;
		case WEST: return turned.maxX < 8.0;
		case EAST: return turned.minX > 8.0;
		default: return false;
		}
	}

	/*The self-check's fake world: only this block's cells are filled; everything else counts as air */
	private static final class FakeAccess {

		private final Map<BlockPos, IBlockState> map = new java.util.HashMap<>();
		private final BlockDummyable block;
		private final IBlockAccess view = new IBlockAccess() {
			@Override public TileEntity getTileEntity(BlockPos pos) { return null; }
			@Override public int getCombinedLight(BlockPos pos, int light) { return 15; }
			@Override public IBlockState getBlockState(BlockPos pos) { return map.getOrDefault(pos, net.minecraft.init.Blocks.AIR.getDefaultState()); }
			@Override public boolean isAirBlock(BlockPos pos) { return !map.containsKey(pos); }
			@Override public Biome getBiome(BlockPos pos) { return net.minecraft.init.Biomes.PLAINS; }
			@Override public int getStrongPower(BlockPos pos, EnumFacing dir) { return 0; }
			@Override public WorldType getWorldType() { return WorldType.DEFAULT; }
			@Override public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean defaultState) { return false; }
		};

		FakeAccess(BlockDummyable block) {
			this.block = block;
		}

		IBlockAccess access() {
			return view;
		}

		void put(BlockPos pos, int meta) {
			map.put(pos, block.getStateFromMeta(meta));
		}

		void clear() {
			map.clear();
		}
	}
}
