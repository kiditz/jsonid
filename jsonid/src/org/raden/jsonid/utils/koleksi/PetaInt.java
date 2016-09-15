package org.raden.jsonid.utils.koleksi;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * 
 * {@link PetaInt} adalah implementasi peta yang bersifat seperti
 * {@linkplain Map}. implementasi ini menggunakan 3 hash untuk mengatasi masalah
 * kunci yang berjalan secara acak dan stash yang memiliki beban lebih kecil.
 * tidak ada alokasi yang benar-benar baik selain menambahkan ukuran pada
 * {@link #kunciTabel}
 * 
 * @author Rifky A.B
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PetaInt<V> implements Iterable<PetaInt.Catat<V>> {
	@SuppressWarnings("unused")
	private static final int PRIME1 = 0xbe1f14b1;
	private static final int PRIME2 = 0xb4b82e39;
	private static final int PRIME3 = 0xced1c241;

	public int ukuran;

	int[] kunciTabel;
	V[] nilaiTabel;
	int kapasitas, ukuranStash;
	boolean sudahNol;
	V nilaiNol;
	private float memuatFaktor;
	private int telahShift, mask, threshold;
	private int kapasitasStash;
	private int iterasiPush;

	private Catatan catatan1, catatan2;
	private PetaNilai nilai1, nilai2;
	private PetaKunci kunci1, kunci2;

	public PetaInt() {
		this(32, 0.8f);
	}

	public PetaInt(int kapasitas) {
		this(kapasitas, 0.8f);
	}

	public PetaInt(int kapasitas, float memuatFaktor) {
		if (kapasitas < 0)
			throw new IllegalArgumentException("inisial >= 0: " + kapasitas);
		if (kapasitas > 1 << 30)
			throw new IllegalArgumentException("kapasitas terlalu besar: " + kapasitas);
		this.kapasitas = pot(kapasitas);

		if (memuatFaktor <= 0)
			throw new IllegalArgumentException("faktor harus lebih besar dari 0 : " + memuatFaktor);
		this.memuatFaktor = memuatFaktor;

		threshold = (int) (this.kapasitas * memuatFaktor);
		mask = this.kapasitas - 1;
		telahShift = 31 - Integer.numberOfTrailingZeros(this.kapasitas);
		kapasitasStash = Math.max(3, (int) Math.ceil(Math.log(this.kapasitas)) * 2);
		iterasiPush = Math.max(Math.min(this.kapasitas, 8), (int) Math.sqrt(this.kapasitas) / 8);

		kunciTabel = new int[this.kapasitas + kapasitasStash];
		nilaiTabel = (V[]) new Object[kunciTabel.length];
	}

	public PetaInt(PetaInt<? extends V> peta) {
		this((int) Math.floor(peta.kapasitas * peta.memuatFaktor), peta.memuatFaktor);
		ukuranStash = peta.ukuranStash;
		System.arraycopy(peta.kunciTabel, 0, kunciTabel, 0, peta.kunciTabel.length);
		System.arraycopy(peta.nilaiTabel, 0, nilaiTabel, 0, peta.nilaiTabel.length);
		ukuran = peta.ukuran;
		this.nilaiNol = peta.nilaiNol;
		this.sudahNol = peta.sudahNol;
	}

	public V taruh(int kunci, V nilai) {
		if (kunci == 0) {
			V nilaiLama = nilaiNol;
			nilaiNol = nilai;
			if (!sudahNol) {
				sudahNol = true;
				ukuran++;
			}
			return nilaiLama;
		}

		return taruh_internal(kunci, nilai);
	}

	private V taruh_internal(int kunci, V nilai) {
		int[] kunciTabel = this.kunciTabel;
		// Check for existing keys.
		int index1 = kunci & mask;
		int kunci1 = kunciTabel[index1];
		if (kunci == kunci1) {
			V oldValue = nilaiTabel[index1];
			nilaiTabel[index1] = nilai;
			return oldValue;
		}

		int index2 = hash2(kunci);
		int kunci2 = kunciTabel[index2];
		if (kunci == kunci2) {
			V oldValue = nilaiTabel[index2];
			nilaiTabel[index2] = nilai;
			return oldValue;
		}

		int index3 = hash3(kunci);

		int kunci3 = kunciTabel[index3];
		if (kunci == kunci3) {
			V oldValue = nilaiTabel[index3];
			nilaiTabel[index3] = nilai;
			return oldValue;
		}

		// Update key in the stash.
		for (int i = this.kapasitas, n = i + ukuranStash; i < n; i++) {
			if (kunciTabel[i] == kunci) {
				V oldValue = nilaiTabel[i];
				nilaiTabel[i] = nilai;
				return oldValue;
			}
		}

		// Check for empty buckets.
		if (kunci1 == 0) {
			kunciTabel[index1] = kunci;
			nilaiTabel[index1] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return null;
		}

		if (kunci2 == 0) {
			kunciTabel[index2] = kunci;
			nilaiTabel[index2] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return null;
		}

		if (kunci3 == 0) {
			kunciTabel[index3] = kunci;
			nilaiTabel[index3] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return null;
		}

		dorong(kunci, nilai, index1, kunci1, index2, kunci2, index3, kunci3);
		return null;
	}

	public void taruhSekaligus(PetaInt<V> map) {
		pastikanKapasitas(map.ukuran);
		for (Catat<V> entry : map)
			taruh(entry.kunci, entry.nilai);
	}

	/** Skips checks for existing keys. */
	private void taruhResize(int kunci, V nilai) {
		// Check for empty buckets.

		int index1 = kunci & mask;
		int kunci1 = kunciTabel[index1];
		if (kunci1 == 0) {
			kunciTabel[index1] = kunci;
			nilaiTabel[index1] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return;
		}

		int index2 = hash2(kunci);
		int kunci2 = kunciTabel[index2];
		if (kunci2 == 0) {
			kunciTabel[index2] = kunci;
			nilaiTabel[index2] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return;
		}

		int index3 = hash3(kunci);
		int kunci3 = kunciTabel[index3];
		if (kunci3 == 0) {
			kunciTabel[index3] = kunci;
			nilaiTabel[index3] = nilai;
			if (ukuran++ >= threshold)
				resize(this.kapasitas << 1);
			return;
		}

		dorong(kunci, nilai, index1, kunci1, index2, kunci2, index3, kunci3);
	}

	private void dorong(int kunciSisipan, V nilaiSisipan, int index1, int kunci1, int index2, int kunci2, int index3,
			int kunci3) {
		int[] kunciTabel = this.kunciTabel;
		V[] nilaiTabel = this.nilaiTabel;
		int mask = this.mask;

		// Push keys until an empty bucket is found.
		int kunciEvicted;
		V nilaiEvicted;
		int i = 0, pushIterations = this.iterasiPush;
		Random rand = new Random();
		do {
			// Replace the key and value for one of the hashes.

			switch (rand.nextInt(3)) {
			case 0:
				kunciEvicted = kunci1;
				nilaiEvicted = nilaiTabel[index1];
				kunciTabel[index1] = kunciSisipan;
				nilaiTabel[index1] = nilaiSisipan;
				break;
			case 1:
				kunciEvicted = kunci2;
				nilaiEvicted = nilaiTabel[index2];
				kunciTabel[index2] = kunciSisipan;
				nilaiTabel[index2] = nilaiSisipan;
				break;
			default:
				kunciEvicted = kunci3;
				nilaiEvicted = nilaiTabel[index3];
				kunciTabel[index3] = kunciSisipan;
				nilaiTabel[index3] = nilaiSisipan;
				break;
			}

			// If the evicted key hashes to an empty bucket, put it there and
			// stop.

			index1 = kunciEvicted & mask;
			kunci1 = kunciTabel[index1];
			if (kunci1 == 0) {
				kunciTabel[index1] = kunciEvicted;
				nilaiTabel[index1] = nilaiEvicted;
				if (ukuran++ >= threshold)
					resize(this.kapasitas << 1);
				return;
			}

			index2 = hash2(kunciEvicted);
			kunci2 = kunciTabel[index2];
			if (kunci2 == 0) {
				kunciTabel[index2] = kunciEvicted;
				nilaiTabel[index2] = nilaiEvicted;
				if (ukuran++ >= threshold)
					resize(this.kapasitas << 1);
				return;
			}

			index3 = hash3(kunciEvicted);
			kunci3 = kunciTabel[index3];
			if (kunci3 == 0) {
				kunciTabel[index3] = kunciEvicted;
				nilaiTabel[index3] = nilaiEvicted;
				if (ukuran++ >= threshold)
					resize(this.kapasitas << 1);
				return;
			}

			if (++i == pushIterations)
				break;
			kunciSisipan = kunciEvicted;
			nilaiSisipan = nilaiEvicted;
		} while (true);

		taruhStash(kunciEvicted, nilaiEvicted);
	}

	private void taruhStash(int kunci, V nilai) {
		if (ukuranStash == kapasitasStash) {
			resize(this.kapasitas << 1);
			taruh(kunci, nilai);
			return;
		}
		// Store key in the stash.
		int index = this.kapasitas + ukuranStash;
		kunciTabel[index] = kunci;
		nilaiTabel[index] = nilai;
		ukuranStash++;
		ukuran++;
	}

	public V raih(int kunci) {
		if (kunci == 0) {
			if (!sudahNol) {
				return null;
			}

		}
		int index = kunci & mask;
		if (kunciTabel[index] != kunci) {
			index = hash2(kunci);
			if (kunciTabel[index] != kunci) {
				index = hash3(kunci);
				if (kunciTabel[index] != kunci)
					return raihStash(kunci, null);
			}
		}
		return nilaiTabel[index];
	}

	public V raih(int kunci, V nilaiAwal) {
		if (kunci == 0) {
			if (!sudahNol) {
				return null;
			}
			return nilaiNol;
		}
		int index = kunci & mask;
		if (kunciTabel[index] != kunci) {
			index = hash2(kunci);
			if (kunciTabel[index] != kunci) {
				index = hash3(kunci);
				if (kunciTabel[index] != kunci)
					return raihStash(kunci, nilaiAwal);
			}
		}
		return nilaiTabel[index];
	}

	private V raihStash(int kunci, V nilaiManual) {
		int[] kunciTabel = this.kunciTabel;
		for (int i = this.kapasitas, n = i + ukuranStash; i < n; i++)
			if (kunciTabel[i] == kunci)
				return nilaiTabel[i];
		return nilaiManual;
	}

	public V hapus(int kunci) {
		if (kunci == 0) {
			if (!sudahNol) {
				return null;
			}
			V nilaiLama = nilaiNol;
			sudahNol = false;
			nilaiNol = null;
			ukuran--;
			return nilaiLama;
		}
		int index = kunci & mask;
		if (kunciTabel[index] == kunci) {
			kunciTabel[index] = 0;
			V oldValue = nilaiTabel[index];
			nilaiTabel[index] = null;
			ukuran--;
			return oldValue;
		}

		index = hash2(kunci);
		if (kunciTabel[index] == kunci) {
			kunciTabel[index] = 0;
			V oldValue = nilaiTabel[index];
			nilaiTabel[index] = null;
			ukuran--;
			return oldValue;
		}

		index = hash3(kunci);
		if (kunciTabel[index] == kunci) {
			kunciTabel[index] = 0;
			V oldValue = nilaiTabel[index];
			nilaiTabel[index] = null;
			ukuran--;
			return oldValue;
		}

		return hapusStash(kunci);
	}

	private V hapusStash(int kunci) {
		int[] kunciTabel = this.kunciTabel;
		for (int i = this.kapasitas, n = i + ukuranStash; i < n; i++) {
			if (kunciTabel[i] == kunci) {
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
			throw new IllegalArgumentException("maximumCapacity must be >= 0: " + kapasitasMaksimal);
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
		int[] kunciTabel = this.kunciTabel;
		V[] nilaiTabel = this.nilaiTabel;
		for (int i = this.kapasitas + ukuranStash; i-- > 0;) {
			kunciTabel[i] = 0;
			nilaiTabel[i] = null;
		}
		ukuran = 0;
		ukuranStash = 0;
	}

	public boolean berisiNilai(Object nilai, boolean identitas) {
		V[] nilaiTabel = this.nilaiTabel;
		if (nilai == null) {
			if (sudahNol && nilaiNol == null)
				return true;
			int[] kunciTabel = this.kunciTabel;
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (kunciTabel[i] != 0 && nilaiTabel[i] == null)
					return true;
		} else if (identitas) {
			if (nilai == nilaiNol)
				return true;
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (nilaiTabel[i] == nilai)
					return true;
		} else {
			if (sudahNol && nilai.equals(nilaiNol))
				return true;
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (nilai.equals(nilaiTabel[i]))
					return true;
		}
		return false;
	}

	public boolean berisiKunci(int kunci) {
		if (kunci == 0)
			return sudahNol;
		int index = kunci & mask;
		if (kunciTabel[index] != kunci) {
			index = hash2(kunci);
			if (kunciTabel[index] != kunci) {
				index = hash3(kunci);
				if (kunciTabel[index] != kunci)
					return berisiKunciStash(kunci);
			}
		}
		return true;
	}

	private boolean berisiKunciStash(int kunci) {
		int[] kunciTabel = this.kunciTabel;
		for (int i = this.kapasitas, n = i + ukuranStash; i < n; i++)
			if (kunciTabel[i] != kunci)
				return true;
		return false;
	}

	public int cariKunci(Object nilai, boolean identitas, int takDiketahui) {
		V[] nilaiTabel = this.nilaiTabel;
		if (nilai == null) {
			if (sudahNol && nilaiNol == null)
				return 0;
			int[] kunciTabel = this.kunciTabel;
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (kunciTabel[i] != 0 && nilaiTabel[i] == null)
					return kunciTabel[i];
		} else if (identitas) {
			if (nilai == nilaiNol)
				return 0;
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)

				if (nilaiTabel[i] == nilai)
					return kunciTabel[i];
		} else {
			if (sudahNol && nilai.equals(nilaiNol))
				return 0;
			for (int i = this.kapasitas + ukuranStash; i-- > 0;)
				if (nilai.equals(nilaiTabel[i]))
					return kunciTabel[i];
		}
		return takDiketahui;
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

		int[] oldKunciTabel = kunciTabel;
		V[] oldNilaiTabel = nilaiTabel;

		kunciTabel = new int[newSize + kapasitasStash];
		nilaiTabel = (V[]) new Object[newSize + kapasitasStash];

		int oldSize = ukuran;
		ukuran = 0;
		ukuranStash = 0;
		if (oldSize > 0) {
			for (int i = 0; i < oldEndIndex; i++) {
				int kunci = oldKunciTabel[i];
				if (kunci != 0)
					taruhResize(kunci, oldNilaiTabel[i]);
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
		int[] kunciTabel = this.kunciTabel;
		V[] nilaiTabel = this.nilaiTabel;
		int i = kunciTabel.length;
		while (i-- > 0) {
			int kunci = kunciTabel[i];
			if (kunci == 0)
				continue;
			buffer.append(kunci);
			buffer.append('=');
			buffer.append(nilaiTabel[i]);
			break;
		}
		while (i-- > 0) {
			int kunci = kunciTabel[i];
			if (kunci == 0)
				continue;
			buffer.append(separator);
			buffer.append(kunci);
			buffer.append('=');
			buffer.append(nilaiTabel[i]);
		}
		if (braces)
			buffer.append('}');
		return buffer.toString();
	}

	public Iterator<Catat<V>> iterator() {
		return catatan();
	}

	public Catatan<V> catatan() {
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

	public PetaKunci kunciPeta() {
		if (kunci1 == null) {
			kunci1 = new PetaKunci(this);
			kunci2 = new PetaKunci(this);
		}
		if (!kunci1.valid) {
			kunci1.reset();
			kunci1.valid = true;
			kunci2.valid = false;
			return kunci1;
		}
		kunci2.reset();
		kunci2.valid = true;
		kunci1.valid = false;
		return kunci2;
	}

	static public class Catat<V> {
		public int kunci;
		public V nilai;

		public String toString() {
			return kunci + "=" + nilai;
		}
	}

	static private abstract class PetaIterasi<V> {
		public boolean hasNext;

		final PetaInt<V> peta;
		int nextIndex, currentIndex;
		boolean valid = true;

		public PetaIterasi(PetaInt<V> map) {
			this.peta = map;
			reset();
		}

		public void reset() {
			currentIndex = -2;
			nextIndex = -1;
			if (peta.sudahNol)
				hasNext = true;
			else
				findNextIndex();
		}

		void findNextIndex() {
			hasNext = false;
			int[] kunciTabel = peta.kunciTabel;
			for (int n = peta.kapasitas + peta.ukuranStash; ++nextIndex < n;) {
				if (kunciTabel[nextIndex] != 0) {
					hasNext = true;
					break;
				}
			}
		}

		public void remove() {
			if (currentIndex == -1 && peta.sudahNol) {
				peta.nilaiNol = null;
				peta.sudahNol = false;
			} else if (currentIndex < 0) {
				throw new IllegalStateException("next must be called before remove.");
			} else if (currentIndex >= peta.kapasitas) {
				peta.removeStashIndex(currentIndex);
				nextIndex = currentIndex - 1;
				findNextIndex();
			} else {
				peta.kunciTabel[currentIndex] = 0;
				peta.nilaiTabel[currentIndex] = null;
			}
			currentIndex = -2;
			peta.ukuran--;
		}
	}

	static public class Catatan<V> extends PetaIterasi<V> implements Iterable<Catat<V>>, Iterator<Catat<V>> {
		Catat<V> entry = new Catat();

		public Catatan(PetaInt<V> map) {
			super(map);
		}

		public boolean saatLanjut() {
			return hasNext();
		}

		public Catat<V> lanjut() {
			return next();
		}

		public Catat<V> next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new IllegalStateException("#iterator() cannot be used nested.");
			int[] kunciTabel = peta.kunciTabel;
			if (nextIndex == -1) {
				entry.kunci = 0;
				entry.nilai = peta.nilaiNol;
			} else {
				entry.kunci = kunciTabel[nextIndex];
				entry.nilai = peta.nilaiTabel[nextIndex];
			}
			currentIndex = nextIndex;
			findNextIndex();
			return entry;
		}

		public boolean hasNext() {
			if (!valid)
				throw new IllegalStateException("#iterator() cannot be used nested.");
			return hasNext;
		}

		@Override
		public Iterator<Catat<V>> iterator() {
			return this;
		}

	}

	static public class PetaNilai<V> extends PetaIterasi<V> implements Iterator<V>, Iterable<V> {
		public PetaNilai(PetaInt<V> map) {
			super((PetaInt<V>) map);
		}

		public boolean saatLanjut() {
			return hasNext();
		}

		public V lanjut() {
			return next();
		}

		public boolean hasNext() {
			if (!valid)
				throw new IllegalStateException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public V next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new IllegalStateException("#iterator() cannot be used nested.");
			V value;
			if (nextIndex == -1)
				value = peta.nilaiTabel[nextIndex];
			else
				value = peta.nilaiTabel[nextIndex];
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

	static public class PetaKunci extends PetaIterasi implements Iterable<Integer>, Iterator<Integer> {
		public PetaKunci(PetaInt<?> map) {
			super(map);
		}

		public boolean saatLanjut() {
			return hasNext();
		}

		public Integer lanjut() {
			return next();
		}

		public boolean hasNext() {
			if (!valid)
				throw new IllegalStateException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public Integer next() {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new IllegalStateException("#iterator() cannot be used nested.");
			int kunci = nextIndex == -1 ? 0 : peta.kunciTabel[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return kunci;
		}

		/**
		 * Mengkonversi Peta ke Larik Int
		 * 
		 * @return {@link LarikInt}
		 */
		public LarikInt keLarik() {
			LarikInt array = new LarikInt(peta.kapasitas);
			while (hasNext)
				array.tambah(next());
			return array;
		}

		@Override
		public Iterator<Integer> iterator() {
			return this;
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