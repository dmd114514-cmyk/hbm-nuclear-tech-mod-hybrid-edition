package api.hbm.block;

/** Drill tiers and ratings; used with IDrillInteraction, old stuff */
@Deprecated
public interface IMiningDrill {

	/** Belonging era, lets IDrillInteraction adjust the yield */
	public DrillType getDrillTier();

	/** Arbitrary ratings: hand drill below 10, auto mining drill 50, laser miner 100 */
	public int getDrillRating();

	public static enum DrillType {
		PRIMITIVE,
		INDUSTRIAL,
		HITECH
	}
}
