package com.hbm.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Items with description text. Descriptions ride the lang's `<name>.desc`, line-breaking on '$' themselves.
 * Also hides an easter egg: the `undefined` item renders same-prefixed things as strings of garbled names.
 */
public class ItemCustomLore extends ItemBase {

	protected EnumRarity rarity;
	protected boolean hasEffect = false;

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		String key = this.getUnlocalizedName() + ".desc";

		// April fools easter egg: that day swaps in the P11-version description
		if(ModItems.polaroidID == 11) {
			String p11 = this.getUnlocalizedName() + ".desc.P11";

			if(!p11.equals(I18nUtil.resolveKey(p11))) {
				key = p11;
			}
		}

		String loc = I18nUtil.resolveKey(key);

		if(!key.equals(loc)) {
			for(String s : loc.split("\\$")) {
				list.add(s);
			}
		}

		if(this == ModItems.undefined) {

			try {
				if(world != null && world.rand.nextInt(10) == 0) {
					list.add(TextFormatting.DARK_RED + "UNDEFINED");
				} else {
					Random rand = new Random(System.currentTimeMillis() / 500);

					if(setSize == 0) setSize = Item.REGISTRY.getKeys().size();

					int r = rand.nextInt(setSize);
					Item item = Item.getItemById(r);

					if(item != null) {
						list.add(new ItemStack(item).getDisplayName());
					} else {
						list.add(TextFormatting.RED + "ERROR #" + r);
					}
				}
			} catch(Exception ex) {
				list.add(TextFormatting.DARK_RED + "UNDEFINED");
			}
		}
	}

	static int setSize = 0;

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return this.rarity != null ? rarity : super.getRarity(stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return hasEffect;
	}

	public ItemCustomLore setRarity(EnumRarity rarity) {
		this.rarity = rarity;
		return this;
	}

	public ItemCustomLore setEffect() {
		this.hasEffect = true;
		return this;
	}

	@Override
	public ItemCustomLore setUnlocalizedName(String uloc) {
		this.setTextureName(RefStrings.MODID + ':' + uloc);
		super.setUnlocalizedName(uloc);
		return this;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		if(stack.getItem() != ModItems.undefined || stack.getItemDamage() != 99) {
			return I18nUtil.resolveKey(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
		}

		return name.getResult();
	}

	public static String[] names = new String[] {
		"THE DEFAULT", "NEXT ONE", "ANOTHER ONE", "NON-STANDARD NAME", "AMBIGUOUS TITLE", "SHORT"
	};

	public static Random rand = new Random();
	public static int currentIndex = 0;
	public static ScramblingName name = new ScramblingName(names[0]);

	public static void updateSystem() {
		name.updateTick(names);
	}

	/*The thing that scrambles item names character by character; a pure client-side visual gag */
	public static class ScramblingName {

		public String previous;
		public String next;
		public String[] previousFrags;
		public String[] nextFrags;
		public String[] frags;
		public int[] mask;
		public int age = 0;

		public ScramblingName(String init) {
			previous = next = init;
			frags = init.split("");
			mask = new int[frags.length];
			previousFrags = chop(previous, frags.length);
			nextFrags = chop(next, frags.length);
		}

		public String getResult() {
			return String.join("", frags);
		}

		public void updateTick(String[] nextNames) {
			age++;
			try {
				if(age % 200 == 0) nextName(nextNames);
				if(age % 5 == 0) scramble();
			} catch(Exception ex) { }
		}

		public void nextName(String[] nextNames) {
			if(nextNames.length < 2) return;

			this.previous = this.next;

			String initial = next;
			while(initial.equals(next)) {
				next = nextNames[rand.nextInt(nextNames.length)];
			}

			int length = Math.min(previous.length(), next.length());
			this.previousFrags = chop(previous, length);
			this.frags = chop(previous, length);
			this.nextFrags = chop(next, length);
			mask = new int[length];
		}

		public void scramble() {

			List<Integer> indices = new ArrayList<>();

			for(int i = 0; i < mask.length; i++) {
				int m = mask[i];
				if(m == 0) indices.add(i);
				if(m > 0 && m <= 5) mask[i]++;
				if(m > 5) frags[i] = nextFrags[i];
			}

			if(!indices.isEmpty()) {
				int toSwitch = indices.get(rand.nextInt(indices.size()));
				mask[toSwitch] = 1;
				frags[toSwitch] = TextFormatting.OBFUSCATED + previousFrags[toSwitch] + TextFormatting.RESET;
			}
		}

		public String[] chop(String name, int parts) {
			if(parts == name.length()) return name.split("");

			double index = 0;
			double incrementPerStep = (double) name.length() / (double) parts;
			List<String> slices = new ArrayList<>();

			for(int i = 0; i < parts; i++) {
				int end = (i == parts - 1) ? name.length() : (int) (index + incrementPerStep);
				slices.add(name.substring((int) index, end));
				index += incrementPerStep;
			}

			return slices.toArray(new String[parts]);
		}
	}
}
