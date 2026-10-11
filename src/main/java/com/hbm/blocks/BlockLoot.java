package com.hbm.blocks;

import java.util.List;

import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

/**
 * The block-drop table door.
 *
 * 1.12.2's loot tables only hang on entities and chests — the block layer was never wired vanilla-side (Block has no getLootTable,
 * and the context carries neither coordinates nor a tool), so the "drops go through tables" step has to be built here.
 * Tables still come from LootTableManager — that's how pack authors' LootTableLoadEvent edits land on us too.
 */
public class BlockLoot {

	/** Table names follow §15's shorthand (hbm:blocks/<registry name>); the loot_tables/ directory layer is added by vanilla's own table fetch */
	public static ResourceLocation table(Block block) {

		String name = block.getRegistryName().getResourcePath();

		return new ResourceLocation(RefStrings.MODID, "blocks/" + name);
	}

	/** What drops and how much is the table's call; no entries means nothing drops */
	public static void harvest(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack tool) {

		if(world.isRemote || !(world instanceof WorldServer))
			return;

		Block block = state.getBlock();

		if(!block.canHarvestBlock(world, pos, player))
			return;

		// No tool in the context, so the table can't reach silk touch; this one wires up vanilla-style by hand
		if(!tool.isEmpty() && block.canSilkHarvest(world, pos, state, player)
				&& EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {

			Block.spawnAsEntity(world, pos, new ItemStack(block));
			return;
		}

		LootContext context = new LootContext.Builder((WorldServer)world).withPlayer(player).withLuck(player.getLuck()).build();
		LootTable table = world.getLootTableManager().getLootTableFromLocation(table(block));
		List<ItemStack> drops = table.generateLootForPools(world.rand, context);

		for(ItemStack stack : drops) {

			if(stack.isEmpty())
				continue;

			Block.spawnAsEntity(world, pos, stack.copy());
		}
	}
}
