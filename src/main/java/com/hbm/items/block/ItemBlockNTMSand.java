package com.hbm.items.block;

import com.hbm.items.IMultiModelItem;

import net.minecraft.block.Block;

/** sand_mix's block item: five subtypes each with their own texture, model names listed by meta */
public class ItemBlockNTMSand extends ItemBlockMeta implements IMultiModelItem {

	public ItemBlockNTMSand(Block block) {
		super(block);
	}

	/** Meta order = EnumSandType enum order */
	@Override
	public String[] subtypeModels() {
		return new String[] {"sand_mix_boron", "sand_mix_lead", "sand_mix_uranium", "sand_mix_polonium", "sand_mix_quartz"};
	}
}
