package com.hbm.items.weapon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.hbm.items.IMultiModelItem;
import com.hbm.items.ItemAmmoEnums.IAmmoItemEnum;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The ammunition. Subtypes are "ammo family × tier", texture names given by the enum itself (`ammo_shell`, `ammo_dkg`...),
 * which doesn't match the base's `<name>_<enum lowercase>` scheme, so model names are self-reported too.
 *
 * The tooltip section color-codes traits by prefix: PRO blue (perks), NEU yellow (neutral), CON red (costs).
 * The sort is reversed — on the same shell the positive effects come first, perks seen at a glance, costs after.
 */
public class ItemAmmo extends ItemEnumMulti implements IMultiModelItem {

	public enum AmmoItemTrait {
		CON_ACCURACY2,
		CON_DAMAGE,
		CON_HEAVY_WEAR,
		CON_LING_FIRE,
		CON_NN,
		CON_NO_DAMAGE,
		CON_NO_EXPLODE1,
		CON_NO_EXPLODE2,
		CON_NO_EXPLODE3,
		CON_NO_FIRE,
		CON_NO_MIRV,
		CON_NO_PROJECTILE,
		CON_PENETRATION,
		CON_RADIUS,
		CON_RANGE2,
		CON_SING_PROJECTILE,
		CON_SPEED,
		CON_SUPER_WEAR,
		CON_WEAR,
		NEU_40MM,
		NEU_BLANK,
		NEU_BOAT,
		NEU_BOXCAR,
		NEU_BUILDING,
		NEU_CHLOROPHYTE,
		NEU_ERASER,
		NEU_FUN,
		NEU_HEAVY_METAL,
		NEU_HOMING,
		NEU_JOLT,
		NEU_LESS_BOUNCY,
		NEU_MASKMAN_FLECHETTE,
		NEU_MASKMAN_METEORITE,
		NEU_MORE_BOUNCY,
		NEU_NO_BOUNCE,
		NEU_NO_CON,
		NEU_STARMETAL,
		NEU_TRACER,
		NEU_UHH,
		NEU_LEADBURSTER,
		NEU_WARCRIME1,
		NEU_WARCRIME2,
		PRO_ACCURATE1,
		PRO_ACCURATE2,
		PRO_BALEFIRE,
		PRO_BOMB_COUNT,
		PRO_CAUSTIC,
		PRO_CHAINSAW,
		PRO_CHLORINE,
		PRO_DAMAGE,
		PRO_DAMAGE_SLIGHT,
		PRO_EMP,
		PRO_EXPLOSIVE,
		PRO_FALLOUT,
		PRO_FIT_357,
		PRO_FLAMES,
		PRO_GRAVITY,
		PRO_HEAVY_DAMAGE,
		PRO_INCENDIARY,
		PRO_LUNATIC,
		PRO_MARAUDER,
		PRO_MINING,
		PRO_NO_GRAVITY,
		PRO_NUCLEAR,
		PRO_PENETRATION,
		PRO_PERCUSSION,
		PRO_PHOSPHORUS,
		PRO_PHOSPHORUS_SPLASH,
		PRO_FLASH,
		PRO_POISON_GAS,
		PRO_RADIUS,
		PRO_RADIUS_HIGH,
		PRO_RANGE,
		PRO_ROCKET,
		PRO_ROCKET_PROPELLED,
		PRO_SHRAPNEL,
		PRO_SPEED,
		PRO_STUNNING,
		PRO_TOXIC,
		PRO_WEAR,
		PRO_WITHERING,
		PRO_BUTTER;

		public String key = "desc.item.ammo.";

		private AmmoItemTrait() {
			key += this.toString().toLowerCase(Locale.US);
		}
	}

	private final String altName;

	public ItemAmmo(Class<? extends Enum> clazz) {
		this(clazz, "");
	}

	public ItemAmmo(Class<? extends Enum> clazz, String altName) {
		super(clazz, true, true);
		this.setCreativeTab(null);
		this.altName = altName;
	}

	@Override
	public String[] subtypeModels() {

		Enum[] enums = theEnum.getEnumConstants();
		String[] names = new String[enums.length];

		for(int i = 0; i < enums.length; i++) names[i] = ((IAmmoItemEnum) enums[i]).getInternalName();

		return names;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		super.addInformation(stack, world, list, flag);

		if(!altName.isEmpty()) list.add(TextFormatting.ITALIC + I18nUtil.resolveKey(altName));

		IAmmoItemEnum item = (IAmmoItemEnum) grab(theEnum, stack.getItemDamage());
		Set<AmmoItemTrait> ammoTraits = item.getTraits();

		if(ammoTraits.isEmpty()) return;

		ArrayList<AmmoItemTrait> sortedTraits = new ArrayList<>(ammoTraits);
		sortedTraits.sort(Comparator.reverseOrder());

		for(AmmoItemTrait trait : sortedTraits) {
			final TextFormatting color;
			switch(trait.toString().substring(0, 3)) {
			case "PRO": color = TextFormatting.BLUE; break;
			case "NEU": color = TextFormatting.YELLOW; break;
			case "CON": color = TextFormatting.RED; break;
			default: color = TextFormatting.DARK_GRAY; break;
			}
			list.add(color + I18nUtil.resolveKey(trait.key));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + ((IAmmoItemEnum) grab(theEnum, stack.getItemDamage())).getInternalName();
	}

	@Override
	public ItemEnumMulti setUnlocalizedName(String uloc) {
		this.setTextureName("hbm:" + uloc);
		// Chaining has to keep working: the parent returns ItemBase, so it must be cast back to ItemEnumMulti
		return (ItemEnumMulti) super.setUnlocalizedName(uloc);
	}
}
