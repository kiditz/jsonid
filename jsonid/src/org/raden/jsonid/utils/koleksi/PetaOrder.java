package org.raden.jsonid.utils.koleksi;

import java.util.NoSuchElementException;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class PetaOrder<K, V> extends PetaObyek<K, V> {
	final Larik<K> keys;

	private Catatan entries1, entries2;
	private PetaNilai values1, values2;
	private PetaKunci keys1, keys2;

	public PetaOrder() {
		keys = new Larik();
	}

	public PetaOrder(int initialCapacity) {
		super(initialCapacity);
		keys = new Larik(kapasitas);
	}

	public PetaOrder(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		keys = new Larik(kapasitas);
	}

	public PetaOrder(PetaObyek<? extends K, ? extends V> map) {
		super(map);
		keys = new Larik(kapasitas);
	}

	public V taruh(K key, V value) {
		if (!berisiKunci(key))
			keys.tambah(key);
		return super.taruh(key, value);
	}

	public V hapus(K key) {
		keys.hapus(key);
		return super.hapus(key);
	}

	public void bersih(int maximumCapacity) {
		keys.bersih();
		super.bersih(maximumCapacity);
	}

	public void bersih() {
		keys.bersih();
		super.bersih();
	}

	public Larik<K> kunciOrder() {
		return keys;
	}

	public Catatan<K, V> iterator() {
		return catatan();
	}

	/**
	 * Returns an iterator for the entries in the map. Remove is supported. Note
	 * that the same iterator instance is returned each time this method is
	 * called. Use the {@link OrderedMapEntries} constructor for nested or
	 * multithreaded iteration.
	 */
	public Catatan<K, V> catatan() {
		if (entries1 == null) {
			entries1 = new PetaOrderCatatan(this);
			entries2 = new PetaOrderCatatan(this);
		}
		if (!entries1.valid) {
			entries1.reset();
			entries1.valid = true;
			entries2.valid = false;
			return entries1;
		}
		entries2.reset();
		entries2.valid = true;
		entries1.valid = false;
		return entries2;
	}

	/**
	 * Returns an iterator for the values in the map. Remove is supported. Note
	 * that the same iterator instance is returned each time this method is
	 * called. Use the {@link OrderedMapValues} constructor for nested or
	 * multithreaded iteration.
	 */
	public PetaNilai<V> nilaiPeta() {
		if (values1 == null) {
			values1 = new PetaOrderNilai(this);
			values2 = new PetaOrderNilai(this);
		}
		if (!values1.valid) {
			values1.reset();
			values1.valid = true;
			values2.valid = false;
			return values1;
		}
		values2.reset();
		values2.valid = true;
		values1.valid = false;
		return values2;
	}

	/**
	 * Returns an iterator for the keys in the map. Remove is supported. Note
	 * that the same iterator instance is returned each time this method is
	 * called. Use the {@link OrderedMapKeys} constructor for nested or
	 * multithreaded iteration.
	 */
	public PetaKunci<K> kunciPeta() {
		if (keys1 == null) {
			keys1 = new PetaOrderKunci(this);
			keys2 = new PetaOrderKunci(this);
		}
		if (!keys1.valid) {
			keys1.reset();
			keys1.valid = true;
			keys2.valid = false;
			return keys1;
		}
		keys2.reset();
		keys2.valid = true;
		keys1.valid = false;
		return keys2;
	}

	public String toString() {
		if (ukuran == 0)
			return "{}";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('{');
		Larik<K> keys = this.keys;
		for (int i = 0, n = keys.ukuran(); i < n; i++) {
			K key = keys.raih(i);
			if (i > 0)
				buffer.append(", ");
			buffer.append(key);
			buffer.append('=');
			buffer.append(raih(key));
		}
		buffer.append('}');
		return buffer.toString();
	}

	static public class PetaOrderCatatan<K, V> extends Catatan<K, V> {
		private Larik<K> keys;

		public PetaOrderCatatan(PetaOrder<K, V> map) {
			super(map);
			keys = map.keys;
		}

		public void reset() {
			nextIndex = 0;
			hasNext = peta.ukuran > 0;
		}

		public Catat next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new RuntimeException("#iterator() cannot be used nested.");
			entry.kunci = keys.raih(nextIndex);
			entry.nilai = peta.raih(entry.kunci);
			nextIndex++;
			hasNext = nextIndex < peta.ukuran;
			return entry;
		}

		public void hapus() {
			if (currentIndex < 0)
				throw new IllegalStateException(
						"next must be called before remove.");
			peta.hapus(entry.kunci);
		}
	}

	static public class PetaOrderKunci<K> extends PetaKunci<K> {
		private Larik<K> keys;

		public PetaOrderKunci(PetaOrder<K, ?> map) {
			super(map);
			keys = map.keys;
		}

		public void reset() {
			nextIndex = 0;
			hasNext = peta.ukuran > 0;
		}

		public K next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new RuntimeException("#iterator() cannot be used nested.");
			K key = keys.raih(nextIndex);
			nextIndex++;
			hasNext = nextIndex < peta.ukuran;
			return key;
		}

		public void hapus() {
			if (currentIndex < 0)
				throw new IllegalStateException(
						"next must be called before remove.");
			peta.hapus(keys.raih(nextIndex - 1));
		}
	}

	static public class PetaOrderNilai<V> extends PetaNilai<V> {
		private Larik keys;

		public PetaOrderNilai(PetaOrder<?, V> map) {
			super(map);
			keys = map.keys;
		}

		public void reset() {
			nextIndex = 0;
			hasNext = peta.ukuran > 0;
		}

		public V next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new RuntimeException("#iterator() cannot be used nested.");
			V value = (V) peta.raih(keys.raih(nextIndex));
			nextIndex++;
			hasNext = nextIndex < peta.ukuran;
			return value;
		}

		public void hapus() {
			if (currentIndex < 0)
				throw new IllegalStateException(
						"next must be called before remove.");
			peta.hapus(keys.raih(nextIndex - 1));
		}
	}
}
