package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.config.ServerConfig;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockAllocatorWater;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ModItems;
import com.hbm.main.NTMSounds;
import com.hbm.sound.SoundRefs;
import com.hbm.tileentity.bomb.TileEntityLandmine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The five landmines: a defuser reclaims them; stepping on one (or losing power, or support) detonates it.
 *
 * Three triggers: a powered neighbor, lost support below, or a player mining it. All three pass the `safeMode` gate — during detonation this
 * cell must stay held, or the support check sets it off a second time.
 *
 * The five differ only in the `Kind` table (trigger radius for the TE, yield and bounding box for this side).
 * The explosion settles with this version's existing vanillant parts; the old mine damage rode the cross entity processor family (flat damage + distance falloff +
 * armor piercing), neither class in the tree yet ⇒ this family hangs a temporary damage-form-only processor (`MineDamageProcessor`, same falloff,
 * the five tier values reading `ServerConfig.MINE_{AP,HE,SHRAP,NAVAL,NUKE}_DAMAGE`), swapping wholesale once the cross family lands.
 * Two more things outside this family's reach: armor piercing and explosion damage classification (the old `attackEntityFromNT` + `BulletConfig`), plus
 * the old cross-family node-distance sampling for range — range currently rides the standard processor's spherical check.
 */
public class BlockLandmine extends BlockContainerBase implements IBomb {

	/**
	 * The five mines' tier table. range/height are the TE's trigger radius (hung on the block in the old version, read live by the TE),
	 * size is the blast yield, bounds the body's box.
	 */
	public enum Kind {

		AP(1.5, 1.0, 3F, box(5, 0, 5, 11, 1, 11)),
		HE(2.0, 5.0, 4F, box(4, 0, 4, 12, 2, 12)),
		SHRAP(1.5, 1.0, 3F, box(5, 0, 5, 11, 1, 11)),
		//the naval mine has no entry in the old box table, falling back to a full cell; copied
		NAVAL(2.5, 1.0, 25F, new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)),
		FAT(2.5, 1.0, 10F, box(5, 0, 4, 11, 6, 12));

		public final double range;
		public final double height;
		public final float size;
		public final AxisAlignedBB bounds;

		Kind(double range, double height, float size, AxisAlignedBB bounds) {
			this.range = range;
			this.height = height;
			this.size = size;
			this.bounds = bounds;
		}

		/** The old hand-written 1/16-block bounding boxes */
		private static AxisAlignedBB box(double x1, double y1, double z1, double x2, double y2, double z2) {
			double f = 0.0625;
			return new AxisAlignedBB(x1 * f, y1 * f, z1 * f, x2 * f, y2 * f, z2 * f);
		}
	}

	/** The master switch held during detonation; see the class note */
	public static boolean safeMode = false;

	public final Kind kind;

	public BlockLandmine(Kind kind) {
		super(Material.IRON);
		this.kind = kind;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityLandmine();
	}

	/** The old {@code getRenderType()=-1} + {@code renderAsNormalBlock=false} "block body undrawn, all rendering to the entity" set */
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return this.kind.bounds.offset(pos);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return this.kind.bounds.offset(pos);
	}

	/** The mine doesn't drop its item form: only a defuser brings it back intact (the drop face takes §15's empty-table route) */
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return hasSupport(world, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos neighborPos) {

		if(world.isBlockIndirectlyGettingPowered(pos) > 0) {
			explode(world, pos);
		}

		if(!hasSupport(world, pos)) {
			if(!safeMode) {
				explode(world, pos);
			} else {
				world.setBlockToAir(pos);
			}
		}
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
		if(!safeMode) {
			explode(world, pos);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		ItemStack held = player.getHeldItem(hand);

		//the defuser item isn't registered yet (item line's lease); with the field null this comparison simply fails — wiring itself once it lands
		if(!held.isEmpty() && held.getItem() == ModItems.defuser) {

			safeMode = true;
			dismantle(world, pos);
			safeMode = false;

			return true;
		}

		return false;
	}

	/** Support: solid below, or the vanilla fence/gate/wall class of perches the old version honored */
	private boolean hasSupport(World world, BlockPos pos) {

		BlockPos below = pos.down();
		IBlockState state = world.getBlockState(below);

		if(state.isSideSolid(world, below, EnumFacing.UP)) return true;

		Block block = state.getBlock();
		return block instanceof BlockFence || block instanceof BlockFenceGate || block instanceof BlockWall;
	}

	private boolean isWaterAbove(World world, BlockPos pos) {

		BlockPos above = pos.up();

		for(int xo = -1; xo <= 1; xo++) { for(int zo = -1; zo <= 1; zo++) {
			Block block = world.getBlockState(above.add(xo, 0, zo)).getBlock();
			if(block == Blocks.WATER || block == Blocks.FLOWING_WATER) return true;
		}}

		return false;
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos) {

		if(world.isRemote) return BombReturnCode.DETONATED;

		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5;

		safeMode = true;
		world.destroyBlock(pos, false);
		safeMode = false;

		switch(this.kind) {

		case AP:
			//the old AP set no block allocation/settlement: hurts people, breaks no blocks
			new ExplosionVNT(world, x, y, z, this.kind.size)
					.setEntityProcessor(new MineDamageProcessor(ServerConfig.MINE_AP_DAMAGE))
					.setPlayerProcessor(new PlayerProcessorStandard())
					.setSFX(new ExplosionEffectWeapon(5, 1F, 0.5F))
					.explode();
			break;

		case HE:
			new ExplosionVNT(world, x, y, z, this.kind.size)
					.setBlockAllocator(new BlockAllocatorStandard())
					.setBlockProcessor(new BlockProcessorStandard())
					.setEntityProcessor(new MineDamageProcessor(ServerConfig.MINE_HE_DAMAGE))
					.setPlayerProcessor(new PlayerProcessorStandard())
					.setSFX(new ExplosionEffectWeapon(15, 3.5F, 1.25F))
					.explode();
			break;

		case SHRAP:
			new ExplosionVNT(world, x, y, z, this.kind.size)
					.setEntityProcessor(new MineDamageProcessor(ServerConfig.MINE_SHRAP_DAMAGE))
					.setPlayerProcessor(new PlayerProcessorStandard())
					.setSFX(new ExplosionEffectWeapon(5, 1F, 0.5F))
					.explode();
			//pending: the shrapnel shower and shrapnel entities live in ExplosionLarge (spawnShrapnelShower / spawnShrapnels), not in the tree yet
			break;

		case NAVAL:
			//the old tier's blast center wrote x+5/y+5/z+5, not the same set as the x+0.5 used for particles in the same branch; copied and noted, reconciled with the visuals when wired
			new ExplosionVNT(world, pos.getX() + 5, pos.getY() + 5, pos.getZ() + 5, this.kind.size)
					.setBlockAllocator(new BlockAllocatorWater(32))
					.setBlockProcessor(new BlockProcessorStandard())
					.setEntityProcessor(new MineDamageProcessor(ServerConfig.MINE_NAVAL_DAMAGE))
					.setPlayerProcessor(new PlayerProcessorStandard())
					.setSFX(new ExplosionEffectWeapon(10, 1F, 0.5F))
					.explode();
			//pending: splash/rubble/particles likewise live in ExplosionLarge (spawnParticlesRadial / spawnRubble / spawnFoam)
			break;

		case FAT:
			new ExplosionVNT(world, x, y, z, this.kind.size)
					.setBlockAllocator(new BlockAllocatorStandard(64))
					.setBlockProcessor(new BlockProcessorStandard())
					.setEntityProcessor(new MineDamageProcessor(ServerConfig.MINE_NUKE_DAMAGE).withRangeMod(1.5F))
					.setPlayerProcessor(new PlayerProcessorStandard())
					.explode();

			spawnRadBall(world, pos, 1.5F);
			world.playSound(null, x, y, z, SoundRefs.resolve(new ResourceLocation(NTMSounds.GUN_MINI_NUKE_EXPLOSION)), SoundCategory.BLOCKS, 25.0F, 0.9F);
			//two pendings: the muke fireball particle isn't among this version's effectNT table's 19 entries (presentation line's lease);
			//  event reporting goes through SatelliteDetector.reportEvent (saveddata/satellites, the key line's lease); not hung this batch.
			break;
		}

		return BombReturnCode.DETONATED;
	}

	/**
	 * Centered on the blast, spreading radiation dose over the ±2 chunks, fading with distance.
	 * The old ring formula hung in the ballistic factory (sedna); the mine chain merely called it — that whole side isn't ported, so it's computed inline for now
	 */
	private void spawnRadBall(World world, BlockPos pos, float mult) {

		for(int i = -2; i <= 2; i++) { for(int j = -2; j <= 2; j++) {

			int manhattan = Math.abs(i) + Math.abs(j);
			if(manhattan < 4)
				ChunkRadiationManager.proxy.incrementRad(world,
						pos.getX() + i * 16, pos.getY(), pos.getZ() + j * 16,
						50F / (manhattan + 1) * mult);
		}}
	}
}
