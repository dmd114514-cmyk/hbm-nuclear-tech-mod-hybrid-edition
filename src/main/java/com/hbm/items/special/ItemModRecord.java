package com.hbm.items.special;

import java.util.HashMap;
import java.util.Map;

import com.hbm.items.ItemSounds;

import net.minecraft.block.BlockJukebox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The records. The track-name-to-audio-key mapping lives in `soundFor`, the rest to vanilla's jukebox.
 * 1.12.2's ItemRecord takes a SoundEvent outright, no getRecordResource lookups — so that table
 * went from "return a path at runtime" to "resolve the event at construction".
 */
public class ItemModRecord extends ItemRecord {

	private static final Map<String, ItemModRecord> MOD_RECORDS = new HashMap<>();

	public final String recordName;

	public ItemModRecord(String name) {
		super(name, ItemSounds.resolve(soundFor(name)));
		this.recordName = name;
		MOD_RECORDS.put(name, this);
	}

	private static String soundFor(String name) {

		switch(name) {
		case "lc": return "music.recordLambdaCore";
		case "ss": return "music.recordSectorSweep";
		case "vc": return "music.recordVortalCombat";
		case "glass": return "music.transmission";
		case "gs": return "music.recordGodSpeed";
		case "gp": return "music.recordGoop";
		case "el": return "music.recordEthereal";
		default: return "music." + name;
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(world.getBlockState(pos).getBlock() != Blocks.JUKEBOX) return EnumActionResult.PASS;
		if(!(world.getBlockState(pos).getBlock() instanceof BlockJukebox)) return EnumActionResult.PASS;

		if(world.isRemote) return EnumActionResult.SUCCESS;

		ItemStack stack = player.getHeldItem(hand);
		((BlockJukebox) Blocks.JUKEBOX).insertRecord(world, pos, world.getBlockState(pos), stack);
		world.playEvent(null, 1010, pos, Item.getIdFromItem(this));
		stack.shrink(1);

		return EnumActionResult.SUCCESS;
	}

	@Override
	public String getRecordNameLocal() {
		return net.minecraft.util.text.translation.I18n.translateToLocal("item.record." + this.recordName + ".desc");
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.RARE;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return net.minecraft.util.text.translation.I18n.translateToLocal(net.minecraft.init.Items.RECORD_11.getUnlocalizedName() + ".name").trim();
	}

	public static ItemRecord getRecord(String name) {
		return MOD_RECORDS.get(name);
	}
}
