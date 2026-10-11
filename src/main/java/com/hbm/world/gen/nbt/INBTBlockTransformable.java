package com.hbm.world.gen.nbt;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;

public interface INBTBlockTransformable {

	/**
	 * Marks a block that carries facing-type state: when spawned from a .nbt structure it must rotate with the structure,
	 * or every door and staircase ends up backwards the moment the structure turns.
	 */

	// Converts the meta stored in the nbt into the rotated meta
	public int transformMeta(int meta, int coordBaseMode);

	// Some blocks swap wholesale for another kind (a structure lamp, say, spawning in its off state)
	public default Block transformBlock(Block block) {
		return block;
	}

	/**
	 * The conversion gate for structure facing. The tables below use the old numbering (0 south / 1 west / 2 north / 3 east),
	 * while this version's StructureComponent.getCoordBaseMode() hands out an EnumFacing —
	 * **never feed it an ordinal()**: NORTH=2, SOUTH=3, WEST=4, EAST=5; off by one and the entire structure faces backwards.
	 */
	public static int coordBaseModeFromFacing(EnumFacing facing) {
		switch(facing) {
		case WEST: return 1;
		case NORTH: return 2;
		case EAST: return 3;
		default: return 0; // SOUTH, plus up/down and anything else with no horizontal facing
		}
	}


	/**
	 * These are the generic meta-per-coordBaseMode-tier algorithms (0 south / 1 west / 2 north / 3 east);
	 * each block picks one and applies it; the tier numbering follows vanilla structure's coordBaseMode.
	 */

	public static int transformMetaDeco(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		switch(coordBaseMode) {
		case 1: // west
			switch(meta) {
			case 2: return 5;
			case 3: return 4;
			case 4: return 2;
			case 5: return 3;
			}
		case 2: // north
			switch(meta) {
			case 2: return 3;
			case 3: return 2;
			case 4: return 5;
			case 5: return 4;
			}
		case 3: // east
			switch(meta) {
			case 2: return 4;
			case 3: return 5;
			case 4: return 3;
			case 5: return 2;
			}
		}
		return meta;
	}

	public static int transformMetaDecoModel(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		int rot = (meta + coordBaseMode) % 4;
		int type = (meta / 4) * 4;

		return rot | type;
	}

	public static int transformMetaStairs(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		switch(coordBaseMode) {
		case 1: // west
			if((meta & 3) < 2) // east: flip the second bit
				meta = meta ^ 2;
			else
				meta = meta ^ 3; // south: flip both bits
			break;
		case 2: // north
			meta = meta ^ 1; // flips the first bit
			break;
		case 3: // east
			if((meta & 3) < 2) //Flip both bits for E/W
				meta = meta ^ 3;
			else //Flip second bit for N/S
				meta = meta ^ 2;
			break;
		}
		return meta;
	}

	// Trapdoor meta encodes as painfully as stairs, just with bits 1 and 3 swapped
	public static int transformMetaTrapdoor(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		switch(coordBaseMode) {
		case 1: // west
			if((meta & 3) < 2)
				meta = meta ^ 3;
			else
				meta = meta ^ 2;
			break;
		case 2: // north
			meta = meta ^ 1; // flips the first bit
			break;
		case 3: // east
			if((meta & 3) < 2)
				meta = meta ^ 2;
			else
				meta = meta ^ 3;
			break;
		}
		return meta;
	}

	public static int transformMetaPillar(int meta, int coordBaseMode) {
		if(coordBaseMode == 0 || coordBaseMode == 2) return meta;
		int type = meta & 3;
		int rot = meta & 12;

		if(rot == 4) return type | 8;
		if(rot == 8) return type | 4;

		return meta;
	}

	public static int transformMetaDirectional(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		int rot = meta & 3;
		int other = meta & 12;

		switch(coordBaseMode) {
		default: //S
			break;
		case 1: //W
			rot = (rot + 1) % 4; break;
		case 2: //N
			rot ^= 2; break;
		case 3: //E
			rot = (rot + 3) % 4; break;
		}

		return other | rot;
	}

	public static int transformMetaTorch(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		switch(coordBaseMode) {
		case 1: // west
			switch(meta) {
			case 1: return 3;
			case 2: return 4;
			case 3: return 2;
			case 4: return 1;
			}
		case 2: // north
			switch(meta) {
			case 1: return 2;
			case 2: return 1;
			case 3: return 4;
			case 4: return 3;
			}
		case 3: // east
			switch(meta) {
			case 1: return 4;
			case 2: return 3;
			case 3: return 1;
			case 4: return 2;
			}
		}
		return meta;
	}

	public static int transformMetaDoor(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		if(meta == 8 || meta == 9) return meta; // the door's upper half doesn't rotate

		return transformMetaDirectional(meta, coordBaseMode);
	}

	public static int transformMetaLever(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		if(meta <= 0 || meta >= 7) { //the lever's encoding is the messiest of the lot
			switch(coordBaseMode) {
			case 1: case 3: //west / east
				meta ^= 0b111;
			}
		} else if(meta >= 5) {
			switch(coordBaseMode) {
			case 1: case 3: //west / east
				meta = (meta + 1) % 2 + 5;
			}
		} else {
			meta = transformMetaTorch(meta, coordBaseMode);
		}

		return meta;
	}

	public static int transformMetaVine(int meta, int coordBaseMode) { //vines store one bit per face, so the bits rotate one step
		int result = 0;

		for (int i = 0; i < 4; i++) {
			int bit = 1 << i;
			if ((meta & bit) != 0) {
				result |= rotateVineBit(bit, coordBaseMode);
			}
		}

		return result;
	}

	static int rotateVineBit(int bit, int coordBaseMode) {
		int index = -1;

		switch (bit) {
			case 1: index = 0; break; // south
			case 2: index = 1; break; // west
			case 4: index = 2; break; // north
			case 8: index = 3; break; // east
			default: return 0;
		}

		int rotated = index;

		switch (coordBaseMode) {
			case 1: rotated = (index + 1) % 4; break; // 90 degrees
			case 2: rotated = (index + 2) % 4; break; // 180 degrees
			case 3: rotated = (index + 3) % 4; break; // 270 degrees
			// case 0 is the original facing; vines don't move
		}

		switch (rotated) {
			case 0: return 1; // south
			case 1: return 2; // west
			case 2: return 4; // north
			case 3: return 8; // east
		}

		return 0;
	}
}
