package org.antlr.v4.runtime.misc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/** A limited map (many unsupported operations) that lets me use
 *  varying hashCode/equals.
 */
public class FlexibleHashMap<K,V> implements EquivalenceMap<K,V> {
	public static final int INITAL_CAPACITY = 16; // must be power of 2
	public static final int INITAL_BUCKET_CAPACITY = 8;
	public static final double LOAD_FACTOR = 0.75;

	public class Entry {
		K key;
		V value;

		public Entry(K key, V value) { this.key = key; this.value = value; }

		@Override
		public String toString() {
			return key.toString()+":"+value.toString();
		}
	}

	protected LinkedList<Entry>[] buckets;

	/** How many elements in set */
	protected int n = 0;

	protected int threshold = (int)(INITAL_CAPACITY * LOAD_FACTOR); // when to expand

	protected int currentPrime = 1; // jump by 4 primes each expand or whatever
	protected int initialBucketCapacity = INITAL_BUCKET_CAPACITY;

	public FlexibleHashMap() {
		this(INITAL_CAPACITY, INITAL_BUCKET_CAPACITY);
	}

	public FlexibleHashMap(int initialCapacity, int initialBucketCapacity) {
		buckets = (LinkedList<Entry>[])new LinkedList[initialCapacity];
		this.initialBucketCapacity = initialBucketCapacity;
	}

	@Override
	public boolean equals(K a, K b) {
		return a.equals(b);
	}

	@Override
	public int hashCode(K o) {
		return o.hashCode();
	}

	protected int getBucket(K key) {
		int hash = hashCode(key);
		int b = hash & (buckets.length-1); // assumes len is power of 2
		return b;
	}

	@Override
	public V get(Object o) {
		K key = (K)o;
		if ( key==null ) return null;
		int b = getBucket(key);
		LinkedList<Entry> bucket = buckets[b];
		if ( bucket==null ) return null; // no bucket
		for (Entry e : bucket) {
			if ( equals(e.key, key) ) return e.value; // use special equals
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		if ( key==null ) return null;
		if ( n > threshold ) expand();
		int b = getBucket(key);
		LinkedList<Entry> bucket = buckets[b];
		if ( bucket==null ) {
			bucket = buckets[b] = new LinkedList<Entry>();
		}
		for (Entry e : bucket) {
			if ( equals(e.key, key) ) {
				V prev = e.value;
				e.value = value;
				n++;
				return prev;
			}
		}
		// not there
		bucket.add(new Entry(key, value));
		return null;
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key)!=null;
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		int h = 0;
		for (LinkedList<Entry> bucket : buckets) {
			if ( bucket==null ) continue;
			for (Entry e : bucket) {
				if ( e==null ) break;
				h += hashCode(e.key);
			}
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	protected void expand() {
		LinkedList<Entry>[] old = buckets;
		currentPrime += 4;
		int newCapacity = buckets.length * 2;
		LinkedList<Entry>[] newTable = (LinkedList<Entry>[])new LinkedList[newCapacity];
		buckets = newTable;
		threshold = (int)(newCapacity * LOAD_FACTOR);
//		System.out.println("new size="+newCapacity+", thres="+threshold);
		// rehash all existing entries
		int oldSize = size();
		for (LinkedList<Entry> bucket : old) {
			if ( bucket==null ) continue;
			for (Entry e : bucket) {
				if ( e==null ) break;
				put(e.key, e.value);
			}
		}
		n = oldSize;
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public boolean isEmpty() {
		return n==0;
	}

	@Override
	public void clear() {
		buckets = (LinkedList<Entry>[])new LinkedList[INITAL_CAPACITY];
		n = 0;
	}

	public String toString() {
		if ( size()==0 ) return "{}";

		StringBuilder buf = new StringBuilder();
		buf.append('{');
		boolean first = true;
		for (LinkedList<Entry> bucket : buckets) {
			if ( bucket==null ) continue;
			for (Entry e : bucket) {
				if ( e==null ) break;
				if ( first ) first=false;
				else buf.append(", ");
				buf.append(e.toString());
			}
		}
		buf.append('}');
		return buf.toString();
	}

	public String toTableString() {
		StringBuilder buf = new StringBuilder();
		for (LinkedList<Entry> bucket : buckets) {
			if ( bucket==null ) {
				buf.append("null\n");
				continue;
			}
			buf.append('[');
			boolean first = true;
			for (Entry e : bucket) {
				if ( first ) first=false;
				else buf.append(" ");
				if ( e==null ) buf.append("_");
				else buf.append(e.toString());
			}
			buf.append("]\n");
		}
		return buf.toString();
	}

	public static void main(String[] args) {
		FlexibleHashMap<String,Integer> map = new FlexibleHashMap<String,Integer>();
		map.put("hi", 1);
		map.put("mom", 2);
		map.put("foo", 3);
		map.put("ach", 4);
		map.put("cbba", 5);
		map.put("d", 6);
		map.put("edf", 7);
		map.put("mom", 8);
		map.put("hi", 9);
		System.out.println(map);
		System.out.println(map.toTableString());
	}
}