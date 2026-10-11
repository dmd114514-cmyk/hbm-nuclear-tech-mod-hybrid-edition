package api.hbm.tweak;

/**
 * Defines the domain names available for numeric tweaks and the whitelist of fields each domain allows.
 */
public final class TweakDomains {

    public static final String WEAPON = "weapon";
    public static final String GUN = "gun";
    public static final String GRENADE = "grenade";
    public static final String ARTILLERY = "artillery";
    public static final String NUKE = "nuke";
    public static final String MOB = "mob";
    public static final String MELEE = "melee";
    public static final String TOOL = "tool";
    public static final String TURRET = "turret";
    public static final String FOOD = "food";
    public static final String GLOBAL = "global";

    private TweakDomains() {
    }

    /**
     * Checks whether a domain allows tweaking a given field; anything off the whitelist is refused.
     */
    public static boolean isLegalField(String domain, String field) {
        if (domain == null || field == null) {
            return false;
        }
        switch (domain) {
            case WEAPON:
            case MELEE:
            case TURRET:
                return "damage".equals(field);
            case GUN:
                return "damage".equals(field) || "rateOfFire".equals(field) || "capacity".equals(field);
            case GRENADE:
            case ARTILLERY:
            case NUKE:
                return "damage".equals(field) || "radius".equals(field);
            case MOB:
                return "health".equals(field) || "damage".equals(field);
            case TOOL:
                return "damage".equals(field) || "efficiency".equals(field);
            case FOOD:
                return "hunger".equals(field) || "saturation".equals(field)
                        || "heal".equals(field) || "hungerEffect".equals(field);
            case GLOBAL:
                return "damage".equals(field) || "rateOfFire".equals(field) || "capacity".equals(field)
                        || "mobHealth".equals(field) || "mobDamage".equals(field);
            default:
                return false;
        }
    }
}
