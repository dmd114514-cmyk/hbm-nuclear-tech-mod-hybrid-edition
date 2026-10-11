package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCassette extends ItemBase {

	//track names and sound paths stay verbatim; change them and old records stop being recognized
	public enum TrackType {

		NULL(				" ", 						null,												SoundType.SOUND,	0,			0),
		HATCH(				"Hatch Siren", 				new ResourceLocation("hbm:alarm.hatch"),			SoundType.LOOP,		3358839,	250),
		ATUOPILOT(			"Autopilot Disconnected", 	new ResourceLocation("hbm:alarm.autopilot"),		SoundType.LOOP,		11908533,	50),
		AMS_SIREN(			"AMS Siren", 				new ResourceLocation("hbm:alarm.amsSiren"),			SoundType.LOOP,		15055698,	50),
		BLAST_DOOR(			"Blast Door Alarm", 		new ResourceLocation("hbm:alarm.blastDoorAlarm"),	SoundType.LOOP,		11665408,	50),
		APC_LOOP(			"APC Siren", 				new ResourceLocation("hbm:alarm.apcLoop"),			SoundType.LOOP,		3565216,	50),
		KLAXON(				"Klaxon", 					new ResourceLocation("hbm:alarm.klaxon"),			SoundType.LOOP,		8421504,	50),
		KLAXON_A(			"Vault Door Alarm",			new ResourceLocation("hbm:alarm.foKlaxonA"),		SoundType.LOOP,		0x8c810b,	50),
		KLAXON_B(			"Security Alert", 			new ResourceLocation("hbm:alarm.foKlaxonB"),		SoundType.LOOP,		0x76818e,	50),
		SIREN(				"Standard Siren", 			new ResourceLocation("hbm:alarm.regularSiren"),		SoundType.LOOP,		6684672,	100),
		CLASSIC(			"Classic Siren", 			new ResourceLocation("hbm:alarm.classic"),			SoundType.LOOP,		0xc0cfe8,	100),
		BANK_ALARM(			"Bank Alarm", 				new ResourceLocation("hbm:alarm.bankAlarm"),		SoundType.LOOP,		3572962,	100),
		BEEP_SIREN(			"Beep Siren", 				new ResourceLocation("hbm:alarm.beepSiren"),		SoundType.LOOP,		13882323,	100),
		CONTAINER_ALARM(	"Container Alarm", 			new ResourceLocation("hbm:alarm.containerAlarm"),	SoundType.LOOP,		14727839,	100),
		SWEEP_SIREN(		"Sweep Siren", 				new ResourceLocation("hbm:alarm.sweepSiren"),		SoundType.LOOP,		15592026,	500),
		STRIDER_SIREN(		"Missile Silo Siren", 		new ResourceLocation("hbm:alarm.striderSiren"),		SoundType.LOOP,		11250586,	500),
		AIR_RAID(			"Air Raid Siren", 			new ResourceLocation("hbm:alarm.airRaid"),			SoundType.LOOP,		0xDF3795,	500),
		NOSTROMO_SIREN(		"Nostromo Self Destruct",	new ResourceLocation("hbm:alarm.nostromoSiren"),	SoundType.LOOP,		0x5dd800,	100),
		EAS_ALARM(			"EAS Alarm Screech",		new ResourceLocation("hbm:alarm.easAlarm"),			SoundType.LOOP,		0xb3a8c1,	50),
		APC_PASS(			"APC Pass", 				new ResourceLocation("hbm:alarm.apcPass"),			SoundType.PASS,		3422163,	50),
		RAZORTRAIN(			"Razortrain Horn", 			new ResourceLocation("hbm:alarm.razortrainHorn"),	SoundType.SOUND,	7819501,	250),
		DISEMBODIED(		"Ducc",						new ResourceLocation("hbm:alarm.ducc"),				SoundType.LOOP,		0xb3a8c1,	50),
		SUICIDE(			"Xbox Live",				new ResourceLocation("hbm:alarm.mama"),				SoundType.LOOP,		0xb3a8c1,	70);

		//the track name shown in the GUI
		private String title;
		//the sound location
		private ResourceLocation location;
		//looping, single pass, or play-once only
		private SoundType type;
		//the cassette shell color
		private int color;
		//audible range
		private int volume;

		private TrackType(String name, ResourceLocation loc, SoundType sound, int msa, int intensity) {
			title = name;
			location = loc;
			type = sound;
			color = msa;
			volume = intensity;
		}

		public String getTrackTitle() {
			return title;
		}

		public ResourceLocation getSoundLocation() {
			return location;
		}

		public SoundType getType() {
			return type;
		}

		public int getColor() {
			return color;
		}

		public int getVolume() {
			return volume;
		}

		public static TrackType getEnum(int i) {
			if(i < TrackType.values().length)
				return TrackType.values()[i];
			else
				return TrackType.NULL;
		}
	};

	public enum SoundType {
		LOOP,
		PASS,
		SOUND;
	};

	public ItemCassette() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
		//1.12.2 requires judging the tab yourself, or every tab gets stuffed
		if(!this.isInCreativeTab(tab)) return;

		//starting from 1, skipping the "empty" NULL
		for(int i = 1; i < TrackType.values().length; ++i) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		TrackType track = TrackType.getEnum(stack.getMetadata());
		list.add("Siren sound cassette:");
		list.add("   Name: " + track.getTrackTitle());
		list.add("   Type: " + track.getType().name());
		list.add("   Volume: " + track.getVolume());
	}

	public static TrackType getType(ItemStack stack) {
		if(!stack.isEmpty() && stack.getItem() instanceof ItemCassette)
			return TrackType.getEnum(stack.getMetadata());
		else
			return TrackType.NULL;
	}

	/**
	 * For the client's item color handler.
	 * 1.12.2 deleted Item.getColorFromItemStack; colors register through ItemColors;
	 * the overlay layer is tintIndex 1, using the track's own color.
	 */
	@SideOnly(Side.CLIENT)
	public static int getOverlayColor(ItemStack stack, int tintIndex) {
		if(tintIndex != 1) return 0xFFFFFF;
		int color = TrackType.getEnum(stack.getMetadata()).getColor();
		return color < 0 ? 0xFFFFFF : color;
	}
}
