package angelo.projects.snf.tools;

public class Pair<K, V> {
	K first;
	V second;
	
	public Pair(K f, V s) {
		this.first = f;
		this.second = s;
	}

	public K getFirst() {
		return first;
	}

	public void setFirst(K first) {
		this.first = first;
	}

	public V getSecond() {
		return second;
	}

	public void setSecond(V second) {
		this.second = second;
	}
	
	
}
