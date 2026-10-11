package com.hbm.potion;

import com.hbm.registry.IRegistrar;

/**
 * Potion registration unit. Color and icon position copy the original mod; renaming or recoloring makes old saves look wrong.
 */
public class HbmPotions implements IRegistrar {

	@Override
	public String id() {
		return "potions";
	}

	@Override
	public void preInit() {
		build();
	}

	/*
	 * Split out because the fluid toxicity traits need to build a PotionEffect from `HbmPotion.death` inside `Fluids.init()`,
	 * and registerFluids() runs before every registrar's preInit. Whoever needs a potion calls this first;
	 * repeat calls have no side effects. The real fix is dependency ordering in the registration unit — that's module-framework work.
	 */
	private static boolean built = false;

	public static void build() {
		if(built) return;
		built = true;

		HbmPotion.taint = HbmPotion.create("taint", true, 0x800080, "potion.hbm_taint", 0, 0);
		HbmPotion.radiation = HbmPotion.create("radiation", true, 0x84C128, "potion.hbm_radiation", 1, 0);
		HbmPotion.bang = HbmPotion.create("bang", true, 0x111111, "potion.hbm_bang", 3, 0);
		HbmPotion.mutation = HbmPotion.create("mutation", false, 0x800080, "potion.hbm_mutation", 2, 0);
		HbmPotion.radx = HbmPotion.create("radx", false, 0xBB4B00, "potion.hbm_radx", 5, 0);
		HbmPotion.lead = HbmPotion.create("lead", true, 0x767682, "potion.hbm_lead", 6, 0);
		HbmPotion.radaway = HbmPotion.create("radaway", false, 0xBB4B00, "potion.hbm_radaway", 7, 0);
		HbmPotion.phosphorus = HbmPotion.create("phosphorus", true, 0xFFFF00, "potion.hbm_phosphorus", 1, 1);
		HbmPotion.stability = HbmPotion.create("stability", false, 0xD0D0D0, "potion.hbm_stability", 2, 1);
		HbmPotion.potionsickness = HbmPotion.create("potionsickness", false, 0xFF8080, "potion.hbm_potionsickness", 3, 1);
		HbmPotion.death = HbmPotion.create("death", false, 1118481, "potion.hbm_death", 4, 1);
		HbmPotion.run = HbmPotion.create("run", true, 1118481, "potion.hbm_run", 14, 0);
		HbmPotion.nitan = HbmPotion.create("nitan", false, 8388736, "potion.hbm_nitan", 3, 1);
		HbmPotion.flashbang = HbmPotion.create("flashbang", false, 0xD0D0D0, "potion.hbm_flashbang", 15, 1);
		HbmPotion.slippery = HbmPotion.create("slippery", false, 0xD0D0D0, "potion.hbm_slippery", 15, 0);
	}
}
