package com.hbm.blocks.generic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IBlockNaming;
import com.hbm.blocks.IStepTickReceiver;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

/**
 * One block, many slab materials (six concrete slabs, five concrete tiles, seven brick family).
 *
 * The old version carried the material list in constructor arguments, looks via `getIcon` reading the base block's icon by meta, and hardness/blast likewise
 * queried per meta. This version moves the list onto a proper subtype property (`variant`, values being the base block names), textures to `models/block`,
 * with blockstates listed value by value, and the item side keeping the old `<block name>.<base name>` translation keys (that lang was written long ago).
 *
 * Three traps shared with BlockEnumMulti, worth noting:
 *   1) the property can't live in an instance field — vanilla's Block constructor calls `createBlockState()`, at which point subclass fields are unassigned,
 *      so the enum class comes from the subclass as a class literal (`variantEnum()`), with the property built lazily per enum class, one shared instance;
 *   2) likewise `isDouble` reads its default inside `createBlockState()`, so **the double slab's block state also carries the half property**,
 *      listed as 2×N tiers like the single slab (double slabs never read half; the extra tiers don't affect behavior);
 *   3) double slabs get no item form (the old version had none either); they drop **two of the single slab's item** — via `getItemDropped`,
 *      with the count following vanilla BlockSlab's double = 2.
 *
 * The trampoline half (the old IStepTickReceiver and BlockSpeedy pairing) carries over.
 */
public abstract class BlockMultiSlab extends BlockSlab implements IBlockNaming, IBlockMulti, IStepTickReceiver {

	private static final Map<Class<?>, PropertyEnum<?>> VARIANTS = new ConcurrentHashMap<>();

	private final Block[] materials;
	private final boolean isDouble;
	private final Block single;

	/**
	 * @param single    the single slab (the double's drops and item form point at it; the single passes null itself)
	 * @param materials each subtype's base block, ordered exactly like the subtype enum
	 */
	public BlockMultiSlab(boolean isDouble, Block single, Material mat, Block... materials) {
		super(mat);
		this.isDouble = isDouble;
		this.single = single;
		this.materials = materials;
		this.useNeighborBrightness = true;

		IBlockState state = this.blockState.getBaseState();

		if(!isDouble)
			state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);

		this.setDefaultState(withVariant(state, 0));
	}

	/** The subclass's own subtype enum class (a class literal only; reading instance fields at construction yields null) */
	protected abstract Class<? extends Enum<?>> variantEnum();

	@SuppressWarnings({"unchecked", "rawtypes"})
	public IProperty<?> variantProperty() {

		Class<?> cls = variantEnum();
		PropertyEnum<?> property = VARIANTS.get(cls);

		if(property == null) {
			property = PropertyEnum.create("variant", (Class) cls);
			VARIANTS.put(cls, property);
		}

		return property;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private IBlockState withVariant(IBlockState state, int index) {
		return state.withProperty((IProperty) variantProperty(), (Comparable) variantEnum().getEnumConstants()[index]);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private int variantIndex(IBlockState state) {
		return ((Enum<?>) state.getValue((IProperty) variantProperty())).ordinal();
	}

	private Block materialFor(IBlockState state) {
		return materials[Math.min(variantIndex(state), materials.length - 1)];
	}

	@Override
	protected BlockStateContainer createBlockState() {
		//double slabs also carry half: this method is called inside super(), before isDouble is assigned, so the truth is unreadable
		return new BlockStateContainer(this, HALF, variantProperty());
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public IBlockState getStateFromMeta(int meta) {

		IBlockState state = withVariant(this.getDefaultState(), rectify(meta & 7));

		if(!this.isDouble())
			state = state.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);

		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {

		int meta = variantIndex(state);

		if(!this.isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP)
			meta |= 8;

		return meta;
	}

	@Override
	public String getUnlocalizedName(int meta) {
		return super.getUnlocalizedName() + "." + ((Enum<?>) variantEnum().getEnumConstants()[rectify(meta)]).name().toLowerCase(java.util.Locale.US);
	}

	@Override
	public boolean isDouble() {
		return this.isDouble;
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return variantProperty();
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
		return (Comparable<?>) variantEnum().getEnumConstants()[rectify(stack.getMetadata())];
	}

	@Override
	public int getSubCount() {
		return materials.length;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return variantIndex(state);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(this.single == null) {
			for(int i = 0; i < materials.length; i++)
				list.add(new ItemStack(this, 1, i));
		}
	}

	/** Double slabs have no item form of their own; they drop the single slab's item (the count from vanilla BlockSlab: double = 2) */
	@Override
	public Item getItemDropped(IBlockState state, java.util.Random rand, int fortune) {
		return Item.getItemFromBlock(this.single != null ? this.single : this);
	}

	/** Hardness and blast forward to the base block per query (the old approach exactly: never assume the base computes by position) */
	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
		return materialFor(state).getBlockHardness(materialFor(state).getDefaultState(), world, pos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		return materialFor(world.getBlockState(pos)).getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	public void onPlayerStep(World world, BlockPos pos, EntityPlayer player) {

		Block material = materialFor(world.getBlockState(pos));

		if(!world.isRemote || !(material instanceof BlockSpeedy))
			return;

		if(player.moveForward != 0 || player.moveStrafing != 0) {
			player.motionX *= 1.5;
			player.motionZ *= 1.5;
		}
	}
}
