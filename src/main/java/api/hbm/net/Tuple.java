package api.hbm.net;

/**
 * Generic multi-value container. It used to live in com.hbm.util while api.hbm interfaces referenced it back, forming
 * an api -> com.hbm -> api cycle; it now sits in the dependency-free api.hbm.net, usable by anyone without owing anyone.
 *
 * Fields stay public: reading them directly on a hot path saves a call over a getter. Do not change that.
 */
public class Tuple {

	/** The most used pair; key/value naming follows the old code, and changing it would touch tens of thousands of call sites */
	public static class Pair<X, Y> {

		public X key;
		public Y value;

		public Pair(X x, Y y) {
			this.key = x;
			this.value = y;
		}

		public X getKey() {
			return this.key;
		}

		public Y getValue() {
			return this.value;
		}

		@Override
		public int hashCode() {
			final int prime = 27644437;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null) return false;
			if(getClass() != obj.getClass()) return false;
			Pair<?, ?> other = (Pair<?, ?>) obj;
			if(key == null) { if(other.key != null) return false; } else if(!key.equals(other.key)) return false;
			if(value == null) { if(other.value != null) return false; } else if(!value.equals(other.value)) return false;
			return true;
		}
	}

	public static class Triplet<X, Y, Z> {

		public X x;
		public Y y;
		public Z z;

		public Triplet(X x, Y y, Z z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public X getX() { return this.x; }
		public Y getY() { return this.y; }
		public Z getZ() { return this.z; }

		@Override
		public int hashCode() {
			final int prime = 27644437;
			int result = 1;
			result = prime * result + ((x == null) ? 0 : x.hashCode());
			result = prime * result + ((y == null) ? 0 : y.hashCode());
			result = prime * result + ((z == null) ? 0 : z.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null) return false;
			if(getClass() != obj.getClass()) return false;
			Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) obj;
			if(x == null) { if(other.x != null) return false; } else if(!x.equals(other.x)) return false;
			if(y == null) { if(other.y != null) return false; } else if(!y.equals(other.y)) return false;
			if(z == null) { if(other.z != null) return false; } else if(!z.equals(other.z)) return false;
			return true;
		}
	}

	public static class Quartet<W, X, Y, Z> {

		public W w;
		public X x;
		public Y y;
		public Z z;

		public Quartet(W w, X x, Y y, Z z) {
			this.w = w;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public W getW() { return this.w; }
		public X getX() { return this.x; }
		public Y getY() { return this.y; }
		public Z getZ() { return this.z; }

		/** Mutate in place; made for the reuse-one-instance-as-a-lookup-key case, saving an allocation per clone */
		public void mangle(W w, X x, Y y, Z z) {
			this.w = w;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public Quartet<W, X, Y, Z> clone() {
			return new Quartet<>(w, x, y, z);
		}

		@Override
		public int hashCode() {
			final int prime = 27644437;
			int result = 1;
			result = prime * result + ((w == null) ? 0 : w.hashCode());
			result = prime * result + ((x == null) ? 0 : x.hashCode());
			result = prime * result + ((y == null) ? 0 : y.hashCode());
			result = prime * result + ((z == null) ? 0 : z.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null) return false;
			if(getClass() != obj.getClass()) return false;
			Quartet<?, ?, ?, ?> other = (Quartet<?, ?, ?, ?>) obj;
			if(w == null) { if(other.w != null) return false; } else if(!w.equals(other.w)) return false;
			if(x == null) { if(other.x != null) return false; } else if(!x.equals(other.x)) return false;
			if(y == null) { if(other.y != null) return false; } else if(!y.equals(other.y)) return false;
			if(z == null) { if(other.z != null) return false; } else if(!z.equals(other.z)) return false;
			return true;
		}
	}

	public static class Quintet<V, W, X, Y, Z> {

		public V v;
		public W w;
		public X x;
		public Y y;
		public Z z;

		public Quintet(V v, W w, X x, Y y, Z z) {
			this.v = v;
			this.w = w;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public V getV() { return this.v; }
		public W getW() { return this.w; }
		public X getX() { return this.x; }
		public Y getY() { return this.y; }
		public Z getZ() { return this.z; }
	}
}
