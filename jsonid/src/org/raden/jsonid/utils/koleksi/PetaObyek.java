package org.raden.jsonid.utils.koleksi;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * 
 * {@link PetaObyek} adalah implementasi peta yang bersifat seperti
 * {@linkplain Map}. implementasi ini menggunakan 3 hash untuk mengatasi masalah
 * kunci yang berjalan secara acak dan stash yang memiliki beban lebih kecil.
 * tidak ada alokasi yang benar-benar baik selain menambahkan ukuran pada
 * {@link #kunciTabel}
 * 
 * @author Nathan Sweet
 * @author Rifky A.B
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PetaObyek<K, V> implements Iterable<PetaObyek.Catat<K, V>> {
	@SuppressWarnings("unused")
	private static final int PRIME1 = 0xbe1f14b1;
	private static final int PRIME2 = 0xb4b82e39;
	private static final int PRIME3 = 0xced1c241;

	public int ukuran;

	K[] kunciTabel;
	V[] nilaiTabel;
	int kapasitas, ukuranStash;

	private float memuatFaktor;
	private int telahShift, mask, threshold;
	private int kapasitasStash;
	private int iterasiPush;

	private Catatan catatan1, catatan2;
	private PetaNilai nilai1, nilai2;
	private PetaKunci keys1, keys2;

	public PetaObyek() {
		this(32, 0.8f);
	}

	public PetaObyek(int kapasitas) {
		this(kapasitas, 0.8f);
	}

	public PetaObyek(int kapasitas, float memuatFaktor) {
		if (kapasitas < 0)
			throw new IllegalArgumentException("inisial >= 0: " + kapasitas);
		if (kapasitas > 1 << 30)
			throw new IllegalArgumentException("kapasitas terlalu besar: " + kapasitas);
		this.kapasitas = pot(kapasitas);

		if (memuatFaktor <= 0)
			throw new IllegalArgumentException("memuat faktor haris > 0: " + memuatFaktor);
		this.memuatFaktor = memuatFaktor;

		threshold = (int) (this.kapasitas * memuatFaktor);
		mask = this.kapasitas - 1;
		telahShift = 31 - Integer.numberOfTrailingZeros(this.kapasitas);
		kapasitasStash = Math.max(3, (int) Math.ceil(Math.log(this.kapasitas)) * 2);
		iterasiPush = Math.max(Math.min(this.kapasitas, 8), (int) Math.sqrt(this.kapasitas) / 8);

		kunciTabel = (K[]) new Object[this.kapasitas + kapasitasStash];
		nilaiTabel = (V[]) new Object[kunciTabel.length];
	}

	public PetaObyek(PetaObyek<? extends K, ? extends V> peta) {
		this(peta.kapasitas, peta.memuatFaktor);
		ukuranStash = peta.ukuranStash;
		System.arraycopy(peta.kunciTabel, 0, kunciTabel, 0, peta.kunciTabel.length);
		System.arraycopy(peta.nilaiTabel, 0, nilaiTabel, 0, peta.nilaiTabel.length);
		ukuran = peta.ukuran;
	}

	public V taruh(K kunci, V nilai) {
		if (kunci == null)
			throw new IllegalArgumentException("kunci tidak boleh kosong.");
		return taruh_internal(kunci, nilai);
	}

	private V taruh_internal(K kunci, V nilai) {
		K[] kunciTabel = this.kunciTabel;
		// Check for existing keys.
		int hashCode = kunci.hashCode();
		int index1 = hashCode & mask;
		K key1 = kunciTabel[index1];
		if (kunci.equals(key1)) {
			V oldValue = nilaiTabel[index1];
			nilaiTabel[index1] = nilai;
			return oldValue;
		}

		int index2 = hash2(hashCode);
		K key2 = kunciTabel[index2];
		if (kunci.equals(key2)) {
			V oldValue = nilaiTabel[index2];
			nilaiTabel[index2] = nilai;
			return oldValue;
		}

		int index3 = hash3(hashCode);

		K key3 = kunciTabel[index3];
		if (kunci.equals(key3)) {
			V oldValue = nilaiTabel[index3];
			nilaiTabel[index3] = nilai;
			return oldValue;
		}

		// Update key in the stash.
		for (int i = this.kapasitas, n = i + ukuranStash; i < n; i++) {
			if (kunci.equals(kunciTabel[i])) {
				V oldValue = nilaiTabel[i];
				nilaiTabel[i] = nilai;
				return oldValue;
			}
		}

		// Check for empty buckets.
		if (key1 == null) {
			kunciTabel[index1] = kunci;
			nilaiTabel[index1] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return null;
		}

		if (key2 == null) {
			kunciTabel[index2] = kunci;
			nilaiTabel[index2] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return null;
		}

		if (key3 == null) {
			kunciTabel[index3] = kunci;
			nilaiTabel[index3] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return null;
		}

		dorong(kunci, nilai, index1, key1, index2, key2, index3, key3);
		return null;
	}

	public void taruhSekaligus(PetaObyek<K, V> map) {
		pastikanKapasitas(map.ukuran);
		for (Catat<K, V> entry : map)
			taruh(entry.kunci, entry.nilai);
	}

	/** Skips checks for existing keys. */
	private void taruhResize(K kunci, V nilai) {
		// Check for empty buckets.
		int hashCode = kunci.hashCode();
		int index1 = hashCode & mask;
		K key1 = kunciTabel[index1];
		if (key1 == null) {
			kunciTabel[index1] = kunci;
			nilaiTabel[index1] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return;
		}

		int index2 = hash2(hashCode);
		K key2 = kunciTabel[index2];
		if (key2 == null) {
			kunciTabel[index2] = kunci;
			nilaiTabel[index2] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return;
		}

		int index3 = hash3(hashCode);
		K key3 = kunciTabel[index3];
		if (key3 == null) {
			kunciTabel[index3] = kunci;
			nilaiTabel[index3] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return;
		}

		dorong(kunci, nilai, index1, key1, index2, key2, index3, key3);
	}

	private void dorong(K insertKey, V insertValue, int index1, K key1, int index2, K key2, int index3, K key3) {
		K[] kunciTabel = this.kunciTabel;
		V[] nilaiTabel = this.nilaiTabel;
		int mask = this.mask;

		// Push keys until an empty bucket is found.
		K kunciEvicted;
		V nilaiEvicted;
		int i = 0, pushIterations = this.iterasiPush;
		Random random = new Random();
		do {
			// Replace the key and value for one of the hashes.

			switch (random.nextInt(3)) {
			case 0:
				kunciEvicted = key1;
				nilaiEvicted = nilaiTabel[index1];
				kunciTabel[index1] = insertKey;
				nilaiTabel[index1] = insertValue;
				break;
			case 1:
				kunciEvicted = key2;
				nilaiEvicted = nilaiTabel[index2];
				kunciTabel[index2] = insertKey;
				nilaiTabel[index2] = insertValue;
				break;
			default:
				kunciEvicted = key3;
				nilaiEvicted = nilaiTabel[index3];
				kunciTabel[index3] = insertKey;
				nilaiTabel[index3] = insertValue;
				break;
			}

			// If the evicted key hashes to an empty bucket, put it there and
			// stop.
			int hashCode = kunciEvicted.hashCode();
			index1 = hashCode & mask;
			key1 = kunciTabel[index1];
			if (key1 == null) {
				kunciTabel[index1] = kunciEvicted;
				nilaiTabel[index1] = nilaiEvicted;
				if (ukuran++ >= threshold)
					resize(this.kapasitas << 1);
				return;
			}

			index2 = hash2(hashCode);
			key2 = kunciTabel[index2];
			if (key2 == null) {
				kunciTabel[index2] = kunciEvicted;
				nilaiTabel[index2] = nilaiEvicted;
				if (ukuran++ >= threshold)
					resize(this.kapasitas << 1);
				return;
			}

			index3 = hash3(hashCode);
			key3 = kunciTabel[index3];
			if (key3 == null) {
				kunciTabel[index3] = kunciEvicted;
				nilaiTabel[index3] = nilaiEvicted;
				if (ukuran++ >= threshold)
					resize(this.kapasitas << 1);
				return;
			}

			if (++i == pushIterations)
				break;
			insertKey = kunciEvicted;
			insertValue = nilaiEvicted;
		} while (true);

		taruhStash(kunciEvicted, nilaiEvicted);
	}

	private void taruhStash(K kunci, V nilai) {
		if (ukuranStash == kapasitasStash) {
			// Too many pushes occurred and the stash is full, increase the
			// table size.
			resize(this.kapasitas << 1);
			taruh_internal(kunci, nilai);
			return;
		}
		// Store key in the stash.
		int index = this.kapasitas + ukuranStash;
		kunciTabel[index] = kunci;
		nilaiTabel[index] = nilai;
		ukuranStash++;
		ukuran++;
	}

	public V raih(K kunci) {
		int hashCode = kunci.hashCode();
		int index = hashCode & mask;
		if (!kunci.equals(kunciTabel[index])) {
			index = hash2(hashCode);
			if (!kunci.equals(kunciTabel[index])) {
				index = hash3(hashCode);
				if (!kunci.equals(kunciTabel[index]))
					return raihStash(kunci);
			}
		}
		return nilaiTabel[index];
	}

	private V raihStash(K kunci) {
		K[] kunciTabel = this.kunciTabel;
		for (int i = this.kapasitas, n = i + ukuranStash; i < n; i++)
			if (kunci.equals(kunciTabel[i]))
				return nilaiTabel[i];
		return null;
	}

	/**
	 * Returns the value for the specified key, or the default value if the key
	 * is not in the map.
	 */
	public V raih(K kunci, V nilaiAwal) {
		int hashCode = kunci.hashCode();
		int index = hashCode & mask;
		if (!kunci.equals(kunciTabel[index])) {
			index = hash2(hashCode);
			if (!kunci.equals(kunciTabel[index])) {
				index = hash3(hashCode);
				if (!kunci.equals(kunciTabel[index]))
					return raihStash(kunci, nilaiAwal);
			}
		}
		return nilaiTabel[index];
	}

	private V raihStash(K kunci, V nilaiAwal) {
		K[] kunciTabel = this.kunciTabel;
		for (int i = this.kapasitas; i < i + ukuranStash; i++)
			if (kunci.equals(kunciTabel[i]))
				return nilaiTabel[i];
		return nilaiAwal;
	}

	public V hapus(K kunci) {
		int hashCode = kunci.hashCode();
		int index = hashCode & mask;
		if (kunci.equals(kunciTabel[index])) {
			kunciTabel[index] = null;
			V oldValue = nilaiTabel[index];
			nilaiTabel[index] = null;
			ukuran--;
			return oldValue;
		}

		index = hash2(hashCode);
		if (kunci.equals(kunciTabel[index])) {
			kunciTabel[index] = null;
			V oldValue = nilaiTabel[index];
			nilaiTabel[index] = null;
			ukuran--;
			return oldValue;
		}

		index = hash3(hashCode);
		if (kunci.equals(kunciTabel[index])) {
			kunciTabel[index] = null;
			V oldValue = nilaiTabel[index];
			nilaiTabel[index] = null;
			ukuran--;
			return oldValue;
		}

		return hapusStash(kunci);
	}

	private V hapusStash(K kunci) {
		K[] kunciTabel = this.kunciTabel;
		for (int i = this.kapasitas, n = i + ukuranStash; i < n; i++) {
			if (kunci.equals(kunciTabel[i])) {
				V oldValue = nilaiTabel[i];
				removeStashIndex(i);
				ukuran--;
				return oldValue;
			}
		}
		return null;
	}

	void removeStashIndex(int indeks) {
		ukuranStash--;
		int indeksTerakhir = this.kapasitas + ukuranStash;
		if (indeks < indeksTerakhir) {
			kunciTabel[indeks] = kunciTabel[indeksTerakhir];
			nilaiTabel[indeks] = nilaiTabel[indeksTerakhir];
			nilaiTabel[indeksTerakhir] = null;
		} else
			nilaiTabel[indeks] = null;
	}

	public void susutkan(int kapasitasMaksimal) {
		if (kapasitasMaksimal < 0)
			throw new IllegalArgumentException("kapasitasMaksimal harus >= 0: " + kapasitasMaksimal);
		if (ukuran > kapasitasMaksimal)
			kapasitasMaksimal = ukuran;
		if (this.kapasitas <= kapasitasMaksimal)
			return;
		kapasitasMaksimal = pot(kapasitasMaksimal);
		resize(kapasitasMaksimal);
	}

	public void bersih(int kapasitasMaksimal) {
		if (this.kapasitas <= kapasitasMaksimal) {
			bersih();
			return;
		}
		ukuran = 0;
		resize(kapasitasMaksimal);
	}

	public void bersih() {
		if (ukuran == 0)
			return;
		K[] kunciTabel = this.kunciTabel;
		V[] nilaiTabel = this.nilaiTabel;
		for (int i = this.kapasitas + ukuranStash; i-- > 0;) {
			kunciTabel[i] = null;
			nilaiTabel[i] = null;
		}
		ukuran = 0;
		ukuranStash = 0;
	}

	public boolean berisiNilai(Object nilai, boolean identitas) {
		V[] nilaiTabel = this.nilaiTabel;
		if (nilai == null) {
			K[] kunciTabel = this.kunciTabel;
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (kunciTabel[i] != null && nilaiTabel[i] == null)
					return true;
		} else if (identitas) {
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (nilaiTabel[i] == nilai)
					return true;
		} else {
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (nilai.equals(nilaiTabel[i]))
					return true;
		}
		return false;
	}

	public boolean berisiKunci(K kunci) {
		int hashCode = kunci.hashCode();
		int index = hashCode & mask;
		if (!kunci.equals(kunciTabel[index])) {
			index = hash2(hashCode);
			if (!kunci.equals(kunciTabel[index])) {
				index = hash3(hashCode);
				if (!kunci.equals(kunciTabel[index]))
					return containsKeyStash(kunci);
			}
		}
		return true;
	}

	private boolean containsKeyStash(K kunci) {
		K[] kunciTabel = this.kunciTabel;
		for (int i = this.kapasitas, n = i + ukuranStash; i < n; i++)
			if (kunci.equals(kunciTabel[i]))
				return true;
		return false;
	}

	public K cariKunci(Object nilai, boolean identitas) {
		V[] nilaiTabel = this.nilaiTabel;
		if (nilai == null) {
			K[] kunciTabel = this.kunciTabel;
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (kunciTabel[i] != null && nilaiTabel[i] == null)
					return kunciTabel[i];
		} else if (identitas) {
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (nilaiTabel[i] == nilai)
					return kunciTabel[i];
		} else {
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (nilai.equals(nilaiTabel[i]))
					return kunciTabel[i];
		}
		return null;
	}

	public void pastikanKapasitas(int tambahanKapasitas) {
		int ukuranDiButuhkan = ukuran + tambahanKapasitas;
		if (ukuranDiButuhkan >= threshold)
			resize(pot((int) (ukuranDiButuhkan / memuatFaktor)));
	}

	private void resize(int newSize) {
		int oldEndIndex = this.kapasitas + ukuranStash;

		this.kapasitas = newSize;
		threshold = (int) (newSize * memuatFaktor);
		mask = newSize - 1;
		telahShift = 31 - Integer.numberOfTrailingZeros(newSize);
		kapasitasStash = Math.max(3, (int) Math.ceil(Math.log(newSize)) * 2);
		iterasiPush = Math.max(Math.min(newSize, 8), (int) Math.sqrt(newSize) / 8);

		K[] oldKunciTabel = kunciTabel;
		V[] oldNilaiTabel = nilaiTabel;

		kunciTabel = (K[]) new Object[newSize + kapasitasStash];
		nilaiTabel = (V[]) new Object[newSize + kapasitasStash];

		int oldSize = ukuran;
		ukuran = 0;
		ukuranStash = 0;
		if (oldSize > 0) {
			for (int i = 0; i < oldEndIndex; i++) {
				K key = oldKunciTabel[i];
				if (key != null)
					taruhResize(key, oldNilaiTabel[i]);
			}
		}
	}

	private int hash2(int h) {
		h *= PRIME2;
		return (h ^ h >>> telahShift) & mask;
	}

	private int hash3(int h) {
		h *= PRIME3;
		return (h ^ h >>> telahShift) & mask;
	}

	public String keString(String separator) {
		return keString(separator, false);
	}

	public String keString() {
		return keString(", ", true);
	}

	private String keString(String separator, boolean braces) {
		if (ukuran == 0)
			return braces ? "{}" : "";
		StringBuilder buffer = new StringBuilder(32);
		if (braces)
			buffer.append('{');
		K[] kunciTabel = this.kunciTabel;
		V[] nilaiTabel = this.nilaiTabel;
		int i = kunciTabel.length;
		while (i-- > 0) {
			K key = kunciTabel[i];
			if (key == null)
				continue;
			buffer.append(key);
			buffer.append('=');
			buffer.append(nilaiTabel[i]);
			break;
		}
		while (i-- > 0) {
			K key = kunciTabel[i];
			if (key == null)
				continue;
			buffer.append(separator);
			buffer.append(key);
			buffer.append('=');
			buffer.append(nilaiTabel[i]);
		}
		if (braces)
			buffer.append('}');
		return buffer.toString();
	}

	public Iterator<Catat<K, V>> iterator() {
		return catatan();
	}

	public Catatan<K, V> catatan() {
		if (catatan1 == null) {
			catatan1 = new Catatan(this);
			catatan2 = new Catatan(this);
		}
		if (!catatan1.valid) {
			catatan1.reset();
			catatan1.valid = true;
			catatan2.valid = false;
			return catatan1;
		}
		catatan2.reset();
		catatan2.valid = true;
		catatan1.valid = false;
		return catatan2;
	}

	/**
	 * Returns an iterator for the values in the map. Remove is supported. Note
	 * that the same iterator instance is returned each time this method is
	 * called. Use the {@link Values} constructor for nested or multithreaded
	 * iteration.
	 */
	public PetaNilai<V> nilaiPeta() {
		if (nilai1 == null) {
			nilai1 = new PetaNilai(this);
			nilai2 = new PetaNilai(this);
		}
		if (!nilai1.valid) {
			nilai1.reset();
			nilai1.valid = true;
			nilai2.valid = false;
			return nilai1;
		}
		nilai2.reset();
		nilai2.valid = true;
		nilai1.valid = false;
		return nilai2;
	}

	public PetaKunci<K> kunciPeta() {
		if (keys1 == null) {
			keys1 = new PetaKunci(this);
			keys2 = new PetaKunci(this);
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

	static public class Catat<K, V> {
		public K kunci;
		public V nilai;

		public String toString() {
			return kunci + "=" + nilai;
		}
	}

	static private abstract class PetaIterasi<K, V, I> implements Iterable<I>, Iterator<I> {
		public boolean hasNext;

		final PetaObyek<K, V> peta;
		int nextIndex, currentIndex;
		boolean valid = true;

		public PetaIterasi(PetaObyek<K, V> map) {
			this.peta = map;
			reset();
		}

		public void reset() {
			currentIndex = -1;
			nextIndex = -1;
			findNextIndex();
		}

		void findNextIndex() {
			hasNext = false;
			K[] kunciTabel = peta.kunciTabel;
			for (int n = peta.kapasitas + peta.ukuranStash; ++nextIndex < n;) {
				if (kunciTabel[nextIndex] != null) {
					hasNext = true;
					break;
				}
			}
		}

		public void remove() {
			if (currentIndex < 0)
				throw new IllegalStateException("next harus dipanggil sebelum hapus.");
			if (currentIndex >= peta.kapasitas) {
				peta.removeStashIndex(currentIndex);
				nextIndex = currentIndex - 1;
				findNextIndex();
			} else {
				peta.kunciTabel[currentIndex] = null;
				peta.nilaiTabel[currentIndex] = null;
			}
			currentIndex = -1;
			peta.ukuran--;
		}
	}

	static public class Catatan<K, V> extends PetaIterasi<K, V, Catat<K, V>> {
		Catat<K, V> entry = new Catat();

		public Catatan(PetaObyek<K, V> map) {
			super(map);
		}

		public Catat<K, V> lanjut() {
			return next();
		}

		public boolean saatLanjut() {
			return hasNext();
		}

		public Catat<K, V> next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new IllegalStateException("#iterator() tidak dapat menggunakan perulangan bersarang.");
			K[] kunciTabel = peta.kunciTabel;
			entry.kunci = kunciTabel[nextIndex];
			entry.nilai = peta.nilaiTabel[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return entry;
		}

		public boolean hasNext() {
			if (!valid)
				throw new IllegalStateException("#iterator() tidak dapat menggunakan perulangan bersarang.");
			return hasNext;
		}

		public Iterator<Catat<K, V>> iterator() {
			return this;
		}
	}

	static public class PetaNilai<V> extends PetaIterasi<Object, V, V> {
		public PetaNilai(PetaObyek<?, V> map) {
			super((PetaObyek<Object, V>) map);
		}

		public boolean hasNext() {
			if (!valid)
				throw new IllegalStateException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public V lanjut() {
			return next();
		}

		public boolean saatLanjut() {
			return hasNext();
		}

		public V next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new IllegalStateException("#iterator() cannot be used nested.");
			V value = peta.nilaiTabel[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return value;
		}

		public Iterator<V> iterator() {
			return this;
		}

		/** Returns a new array containing the remaining keys. */
		public Larik<V> keLarik() {
			return keLarik(new Larik(peta.ukuran));
		}

		/** Adds the remaining keys to the array. */
		public Larik<V> keLarik(Larik<V> larik) {
			while (hasNext)
				larik.tambah(next());
			return larik;
		}
	}

	static public class PetaKunci<K> extends PetaIterasi<K, Object, K> {
		public PetaKunci(PetaObyek<K, ?> map) {
			super((PetaObyek<K, Object>) map);
		}

		public boolean hasNext() {
			if (!valid)
				throw new IllegalStateException("#iterator() tidak dapat menggunakan perulangan bersarang.");
			return hasNext;
		}

		public K lanjut() {
			return next();
		}

		public boolean saatLanjut() {
			return hasNext();
		}

		public K next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new IllegalStateException("#iterator() tidak dapat menggunakan perulangan bersarang.");
			K key = peta.kunciTabel[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		public Iterator<K> iterator() {
			return this;
		}

		/** Returns a new array containing the remaining keys. */
		public Larik<K> keLarik() {
			return keLarik(new Larik(peta.ukuran));
		}

		/** Adds the remaining keys to the array. */
		public Larik<K> keLarik(Larik<K> larik) {
			while (hasNext)
				larik.tambah(next());
			return larik;
		}
	}

	static public int pot(int nilai) {
		if (nilai == 0)
			return 1;
		nilai--;
		nilai |= nilai >> 1;
		nilai |= nilai >> 2;
		nilai |= nilai >> 4;
		nilai |= nilai >> 8;
		nilai |= nilai >> 16;
		return nilai + 1;
	}
}