package api.hbm.block;

/** The side of a reactor core that takes neutrons */
@Deprecated
public interface IPileNeutronReceiver {

	public void receiveNeutrons(int n);
}
