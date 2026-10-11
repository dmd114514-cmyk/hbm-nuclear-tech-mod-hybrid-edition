package com.hbm.items.food;

import com.hbm.tweak.HbmTweaks;

import api.hbm.tweak.TweakDomains;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

/**
 * The food base. Beyond being the common parent, its real job is **handing hunger/saturation to the runtime tweak table** —
 * CraftTweaker's `mods.hbm.tweaks.set("food", "<registry name>", "hunger", 8)` takes effect immediately.
 *
 * Why the table reads sit on two getters rather than inside `onItemUseFinish`: this version's `ItemFood`
 * `healAmount`/`saturationModifier` are **private final fields**, and eating settles via
 * `getFoodStats().addStats(this, stack)` calling back `getHealAmount(stack)`/`getSaturationModifier(stack)`
 * for values — meaning these two getters **are only ever called during eating**, so overriding them equals
 * "reading the table at settlement time", without touching private fields or copying the whole `onItemUseFinish`.
 * `heal` (direct healing) and `hungerEffect` (hunger potion level) have no getters, so subclasses fetch them in
 * `onFoodEaten` via {@link #tweakFood} — a path likewise only walked from eating settlement.
 *
 * When the table lacks this domain/registry-name/field combo, vanilla behavior wins ({@link #tweakFood} returns the base value),
 * so foods with no heal component or hunger effect don't change by a single byte.
 */
public class ItemFoodBase extends ItemFood {

	public ItemFoodBase(int hunger, float saturation, boolean isDogFood) {
		super(hunger, saturation, isDogFood);
	}

	public ItemFoodBase(int hunger, boolean isDogFood) {
		super(hunger, isDogFood);
	}

	@Override
	public int getHealAmount(ItemStack stack) {
		return Math.max(0, Math.round(tweakFood(stack, "hunger", super.getHealAmount(stack))));
	}

	@Override
	public float getSaturationModifier(ItemStack stack) {
		return Math.max(0F, tweakFood(stack, "saturation", super.getSaturationModifier(stack)));
	}

	/**
	 * · **unoverridden → base value**: covered by `resolve(domain,target,field,base)`'s 4th argument, which also handles the
	 * · **`set` → absolute value**: also `resolve`, returning the table's current value;
	 * · **`multiply` → multiplier**: via `factor` (resolve walking the default 1.0), then multiplied onto the base.
	 *
	 * Why `getSource` gets a word in: the table stores only **a float**, and multiplier 3.0 vs absolute 3.0 are
	 * indistinguishable — only "who wrote this entry" separates them, so the branch must consult `getSource`.
	 *
	 * target uses **the item's own registry name**, matching contract §14.2's "target always takes the registry name path".
	 */
	public static float tweakFood(ItemStack stack, String field, float base) {

		if(stack == null || stack.getItem() == null || stack.getItem().getRegistryName() == null)
			return base;

		String target = stack.getItem().getRegistryName().getResourcePath();

		if(HbmTweaks.SOURCE_MULTIPLY.equals(sourceOf(target, field)))
			return base * HbmTweaks.factor(TweakDomains.FOOD, target, field);

		return HbmTweaks.resolve(TweakDomains.FOOD, target, field, base);
	}

	/*When this target isn't overridden, the same domain's `*` entry also counts (`resolve`/`factor` both fall back to it), so the source follows */
	private static String sourceOf(String target, String field) {

		String source = HbmTweaks.getSource(TweakDomains.FOOD, target, field);

		if(!HbmTweaks.SOURCE_DEFAULT.equals(source))
			return source;

		return HbmTweaks.getSource(TweakDomains.FOOD, HbmTweaks.GLOBAL_TARGET, field);
	}

	/**
	 * Self-test: the three-state semantics + "illegal fields don't apply" + "`getHealAmount` really reads the table".
	 * Runs only with `GeneralConfig.enableDebugMode` on, clearing the table afterward (so the self-test doesn't change the game).
	 * Prints only, never throws — a debug switch mustn't take down the session.
	 */
	public static void selfCheck(ItemStack food) {

		if(food == null || food.isEmpty() || !(food.getItem() instanceof ItemFood)) return;

		ItemFoodBase item = (ItemFoodBase) food.getItem();
		String target = item.getRegistryName() == null ? "?" : item.getRegistryName().getResourcePath();
		StringBuilder sb = new StringBuilder("[hbm] food tweak self-test (").append(target).append("): ");

		HbmTweaks.reset(TweakDomains.FOOD);

		float base = tweakFood(food, "heal", 2F);
		sb.append("default=").append(base).append(base == 2F ? " OK" : " FAIL");

		HbmTweaks.set(TweakDomains.FOOD, target, "heal", 7F);
		float set = tweakFood(food, "heal", 2F);
		sb.append(" | set=7→").append(set).append(set == 7F ? " OK" : " FAIL");

		HbmTweaks.multiply(TweakDomains.FOOD, target, "heal", 3F);
		float mul = tweakFood(food, "heal", 2F);
		sb.append(" | multiply×3→").append(mul).append(mul == 6F ? " OK" : " FAIL");

		// the domain's whitelist lacks radius; writing it should be refused and shouldn't touch other fields
		HbmTweaks.set(TweakDomains.FOOD, target, "radius", 99F);
		float afterIllegal = tweakFood(food, "heal", 2F);
		sb.append(" | after illegal field still=").append(afterIllegal).append(afterIllegal == 6F ? " OK" : " FAIL");

		// really walks the getHealAmount eating path
		HbmTweaks.reset(TweakDomains.FOOD);
		HbmTweaks.set(TweakDomains.FOOD, target, "hunger", 9F);
		int heal = item.getHealAmount(food);
		sb.append(" | hunger set=9→getHealAmount=").append(heal).append(heal == 9 ? " OK" : " FAIL");

		// writes only the same-domain `*` entry: it should apply even when this target isn't named (resolve's fallback + the source following it down)
		HbmTweaks.reset(TweakDomains.FOOD);
		HbmTweaks.set(TweakDomains.FOOD, HbmTweaks.GLOBAL_TARGET, "heal", 5F);
		float star = tweakFood(food, "heal", 2F);
		sb.append(" | same-domain* set=5→").append(star).append(star == 5F ? " OK" : " FAIL");

		HbmTweaks.reset(TweakDomains.FOOD);
		HbmTweaks.multiply(TweakDomains.FOOD, HbmTweaks.GLOBAL_TARGET, "heal", 3F);
		float starMul = tweakFood(food, "heal", 2F);
		sb.append(" | same-domain* ×3→").append(starMul).append(starMul == 6F ? " OK" : " FAIL");

		HbmTweaks.reset(TweakDomains.FOOD);
		sb.append(" | table cleared");

		System.out.println(sb);
	}
}
