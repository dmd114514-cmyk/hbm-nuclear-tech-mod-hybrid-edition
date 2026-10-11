package api.hbm.net;

/**
 * In UNINOS each node type is one INetworkProvider instance.
 * Its only job is to produce/provide a net instance of the matching type.
 *
 * NodeNet is deliberately raw here: GenNode and NodeNet are each other's generic parameter, and writing
 * F-bounded wildcards makes every link operation explode with capture errors for zero gain.
 */
@SuppressWarnings("rawtypes")
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public interface INetworkProvider<T extends NodeNet> {

	public T provideNetwork();
}
