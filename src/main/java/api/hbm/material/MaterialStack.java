package api.hbm.material;

/**
 * Material cost: one material plus an amount.
 *
 * The material type itself comes from the content layer (NTMMaterial in this version), hence the generics here -
 * the api layer does not need to know the content layer's material class, and the content layer keeps its strongly typed fields.
 */
public class MaterialStack<M> {

	//final, as a guard against accidental writes
	public final M material;
	public int amount;

	public MaterialStack(M material, int amount) {
		this.material = material;
		this.amount = amount;
	}

	public MaterialStack<M> copy() {
		return new MaterialStack<M>(material, amount);
	}
}
