package com.hbm.blocks;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

/**
 * Subtyped blocks. 1.7.10 stuffed subtypes into the ItemStack's meta and drew via IIcon[];
 * 1.12.2 uses a proper PropertyEnum, while the meta semantics, translation-key suffix and texture names all stay the same,
 * so the old lang and textures reuse directly — only the blockstates/models JSON is new.
 *
 * The property must **not** live in an instance field: 1.12.2's Block constructor calls createBlockState(), at which point
 * the subclass's instance fields aren't assigned yet (super() runs first), and storing it in a field feeds BlockStateContainer a
 * null property, blowing up validateProperty at load. So subclasses only report the enum class (a class literal is safe
 * inside super()); the property is built lazily per enum class here, one shared global instance.
 */
public abstract class BlockEnumMulti<E extends Enum<E> & IEnumMulti> extends BlockMulti {

	public final boolean multiName;
	protected final boolean multiTexture;

	private static final Map<Class<?>, PropertyEnum<?>> VARIANTS = new ConcurrentHashMap<>();

	protected BlockEnumMulti(Material mat, boolean multiName, boolean multiTexture) {
		super(mat);
		this.multiName = multiName;
		this.multiTexture = multiTexture;
	}

	/**
	 * Subclasses return their own subtype enum class. It gets called by createBlockState() inside super()'s chain,
	 * when instance fields aren't assigned yet, so only a class literal may come back — no instance state reads.
	 */
	public abstract Class<E> getEnumClass();

	/**
	 * The property in a block state must be one globally unique instance: the container and the meta round-trips must see the same object,
	 * or withProperty/getValue throw because the property isn't the same object.
	 */
	@SuppressWarnings("unchecked")
	public PropertyEnum<E> getVariant() {
		Class<E> enumClass = getEnumClass();
		PropertyEnum<?> variant = VARIANTS.get(enumClass);
		if(variant == null) {
			variant = PropertyEnum.create("variant", enumClass);
			VARIANTS.put(enumClass, variant);
		}
		return (PropertyEnum<E>) variant;
	}

	private E[] consts() {
		return getEnumClass().getEnumConstants();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, getVariant());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		E[] consts = consts();
		return getDefaultState().withProperty(getVariant(), consts[Math.abs(meta) % consts.length]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(getVariant()).ordinal();
	}

	@Override
	public int getSubCount() {
		return consts().length;
	}

	/** The translation-key suffix keeps the lowercase enum name, so the old tile.<base>.<variant>.name lang works as-is */
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if(!this.multiName) return super.getUnlocalizedName(stack);
		return super.getUnlocalizedName(stack) + "." + consts()[Math.abs(stack.getMetadata()) % getSubCount()].name().toLowerCase(Locale.US);
	}
}
