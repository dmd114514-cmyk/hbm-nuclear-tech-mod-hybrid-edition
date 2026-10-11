package com.hbm.blocks.gas;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.MaterialGas;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ItemBlockBase;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * The gas family's own registration unit: the shared material plus the old `blocks/gas` ten gases.
 * Registering first are the four the vent family (the fourteen BlockOutgas pieces) needs right now; a second batch adds dense radon / meltdown / soot / flammable / explosive / chlorine,
 * whose callers sit on the projectile and machine sides — once registered they drift on their own.
 */
public class ModBlocksGas implements IRegistrar {

	/** The gases' shared material: replaceable, non-blocking, but recognized by grass and leaf checks */
	public static final Material materialGas = new MaterialGas();

	public static Block gas_radon;
	public static Block gas_radon_tomb;
	public static Block gas_monoxide;
	public static Block gas_asbestos;
	public static Block gas_radon_dense;
	public static Block gas_meltdown;
	public static Block gas_coal;
	public static Block gas_flammable;
	public static Block gas_explosive;
	public static Block chlorine_gas;

	@Override
	public void preInit() {

		gas_radon = reg("gas_radon", new BlockGasRadon());
		gas_radon_tomb = reg("gas_radon_tomb", new BlockGasRadonTomb());
		gas_monoxide = reg("gas_monoxide", new BlockGasMonoxide());
		gas_asbestos = reg("gas_asbestos", new BlockGasAsbestos());
		gas_radon_dense = reg("gas_radon_dense", new BlockGasRadonDense());
		gas_meltdown = reg("gas_meltdown", new BlockGasMeltdown());
		gas_coal = reg("gas_coal", new BlockGasCoal());
		gas_flammable = reg("gas_flammable", new BlockGasFlammable());
		gas_explosive = reg("gas_explosive", new BlockGasExplosive());
		chlorine_gas = reg("chlorine_gas", new BlockGasClorine());
	}

	/**
	 * Blocks and item forms queue together. Registry names are bare; step sounds and textures aren't set here —
	 * the old four only carried a name and a creative tab, and this batch does the same (material hardness is the base constructor's business).
	 */
	private static Block reg(String name, BlockBase block) {
		return HbmRegistry.blockWithItem(block.setBlockName(name).setCreativeTab(ModBlocks.machineTab), name, ItemBlockBase::new);
	}
}
