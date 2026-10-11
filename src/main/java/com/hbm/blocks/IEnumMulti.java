package com.hbm.blocks;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

/**
 * The common interface for block/item subtype enums.
 * 1.12.2's PropertyEnum wants enums implementing IStringSerializable, but writing getName() twenty-plus times
 * is tedious; a default implementation here: the serialized name is just the lowercase enum name.
 * So each enum only needs the single word implements IEnumMulti.
 */
public interface IEnumMulti extends IStringSerializable {

	@Override
	default String getName() {
		return ((Enum<?>) this).name().toLowerCase(Locale.US);
	}
}
