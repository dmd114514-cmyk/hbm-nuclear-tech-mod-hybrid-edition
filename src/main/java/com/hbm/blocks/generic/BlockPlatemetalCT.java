package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockEnums;
import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * The platemetal panels (the old {@code BlockEnumMultiCT}'s only user; fifteen tiers: base plus fourteen colors).
 *
 * Subtypes ride the base {@link BlockEnumMulti}'s proper {@code variant} property, the tier table being
 * {@link BlockEnums.PlatemetalType}; textures and translation keys both follow the "base name.enum lowercase" family style
 * (the old {@code getTextureMultiName} and the lang's {@code tile.platemetal.<tier>.name} are exactly this shape,
 * and this tree's assets and lang are in place accordingly).
 *
 * Looks ride connected textures, the block-side three steps matching {@link BlockDecoCT}: the port, the context-carrying container,
 * and world coordinates hung on the state; the switch is in the blockstates — all fifteen tiers' models read
 * {@code hbm:ct/blocks/platemetal.<tier>}, with the loader fetching {@code hbm:blocks/platemetal.<tier>}
 * plus the same-named {@code _ct} image. The property tier is unaffected: CT's context rides unlisted properties, never the blockstate JSON.
 *
 * The asset sides use two naming conventions, each with provenance: textures and connect maps keep the old dotted names ({@code platemetal.<tier>.png}
 * and {@code platemetal.<tier>_ct.png}, 15 pairs complete here), while model files follow this tree's underscore convention for subtyped blocks
 * ({@code models/block/platemetal_<tier>.json}, like the {@code sand_mix} and concrete slab families) —
 * the fifteen {@code models/item/} files parent to their block models; the item side's per-meta icons still need that small class, so
 * currently only meta 0 has an icon, the same gap as this tree's slab family.
 *
 * {@code canConnect} isn't overridden: the old line was {@code this == block}, matching this version's interface default.
 *
 * ⚠ One skipped and logged: the old chain's {@code setStepSound(ModSoundTypes.platemetal)} — that custom sound family wants
 * {@code ModSoundType} (swappable step/dig/break sounds plus per-type pitch) and a {@code MainRegistry} roster entry,
 * neither in this tree; per "no shells without callers" this batch uses vanilla {@code SoundType.METAL} (the old tier's step sound differed from vanilla's anyway),
 * and the line reconnects once the sound-type side lands.
 */
public class BlockPlatemetalCT extends BlockEnumMulti<BlockEnums.PlatemetalType> implements IBlockCT {

	public BlockPlatemetalCT(Material mat, boolean multiName, boolean multiTexture) {
		super(mat, multiName, multiTexture);
	}

	/** The base needs the property table inside super()'s chain, so only a class literal comes back — no instance state reads */
	@Override
	public Class<BlockEnums.PlatemetalType> getEnumClass() {
		return BlockEnums.PlatemetalType.class;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return CTContext.container(this, super.createBlockState());
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return CTContext.attach(world, pos, state);
	}
}
