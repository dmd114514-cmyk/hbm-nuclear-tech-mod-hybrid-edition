package com.hbm.blocks.bomb;

import com.hbm.items.ItemBlockBase;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.tileentity.bomb.TileEntityLandmine;

import net.minecraft.block.Block;

/**
 * The bomb family's block registration unit: five mines plus their shared fuze TE.
 */
public class ModBlocksBomb implements IRegistrar {

	public static Block mine_ap;
	public static Block mine_he;
	public static Block mine_shrap;
	public static Block mine_fat;
	public static Block mine_naval;

	@Override
	public void preInit() {

		//the five share one TE class (the old TileMappings name tileentity_landmine carries over)
		HbmRegistry.tile(TileEntityLandmine.class, "tileentity_landmine");

		//five mines: hardness all 1.0, tiers differing in BlockLandmine.Kind's table; drops pressed to "nothing drops"
		//only a defuser brings one back intact (that item is the item line's lease, unregistered), so no loot table on this side.
		mine_ap = HbmRegistry.blockWithItem(new BlockLandmine(BlockLandmine.Kind.AP).setBlockTextureName(RefStrings.MODID + ":mine_ap").setBlockName("mine_ap").setCreativeTab(ModItems.nukeTab).setHardness(1.0F), "mine_ap", ItemBlockBase::new);
		mine_he = HbmRegistry.blockWithItem(new BlockLandmine(BlockLandmine.Kind.HE).setBlockTextureName(RefStrings.MODID + ":mine_he").setBlockName("mine_he").setCreativeTab(ModItems.nukeTab).setHardness(1.0F), "mine_he", ItemBlockBase::new);
		mine_shrap = HbmRegistry.blockWithItem(new BlockLandmine(BlockLandmine.Kind.SHRAP).setBlockTextureName(RefStrings.MODID + ":mine_shrap").setBlockName("mine_shrap").setCreativeTab(ModItems.nukeTab).setHardness(1.0F), "mine_shrap", ItemBlockBase::new);
		mine_fat = HbmRegistry.blockWithItem(new BlockLandmine(BlockLandmine.Kind.FAT).setBlockTextureName(RefStrings.MODID + ":mine_fat").setBlockName("mine_fat").setCreativeTab(ModItems.nukeTab).setHardness(1.0F), "mine_fat", ItemBlockBase::new);
		mine_naval = HbmRegistry.blockWithItem(new BlockLandmine(BlockLandmine.Kind.NAVAL).setBlockTextureName(RefStrings.MODID + ":mine_naval").setBlockName("mine_naval").setCreativeTab(ModItems.nukeTab).setHardness(1.0F), "mine_naval", ItemBlockBase::new);
	}
}
