/**
 * 
 */
package org.raden.jsonid.utils.koleksi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.raden.jsonid.RefleksiLarik;
import org.raden.jsonid.Sort;
import org.raden.jsonid.utils.RadenKesalahanRuntime;



/**
 * @author Rifky Aditya Bastara
 *
 */
@SuppressWarnings("unchecked")
public class Larik<E> implements Iterable<E>, Serializable {

	private static final long serialVersionUID = 5201999155345426074L;
	/**
	 * Materi larik dengan tipe generik yang bekerja seperti
	 * {@link java.util.List}. Perbedaannya adalah {@link java.util.List}
	 * menggunakan {@link Object} sedangkan {@link Larik} menggunakan tipe
	 * generik <b>E</b>
	 */
	public E[] materi;
	/**
	 * Ukuran larik
	 */
	private int ukuran;
	private final boolean pesanan;
	private int indeks;

	private LarikIterable<E> iterable;

	/**
	 * Membuat sebuah pesanan larik dengan kapasitas 16
	 */
	public Larik() {
		this(16, true);
	}

	/**
	 * Membuat sebuah pesanan larik dengan kapasitas yang dapat ditentukan
	 * 
	 * @param kapasitas
	 *            adalah kapasistas dari {@link #materi} larik
	 */
	public Larik(int kapasitas) {
		this(kapasitas, true);
	}

	/**
	 * Membuat sebuah pesanan larik dengan kapasitas yang dapat ditentukan. Hal
	 * ini akan menyebabkan kesalahan {@linkplain ClassCastException} saat
	 * menggunakan {@link LarikPotret} karena alasan tipe generik T tidak dapat
	 * dikonversi ke dalam Obyek maka yang harus dilakukan adalah dengan
	 * menggunakan konstruktor {@link Larik#Larik(Class)}
	 * 
	 * @param kapasitas
	 *            adalah kapasistas dari {@link #materi} larik yang akan
	 *            dibangun
	 * @param pesanan
	 *            jika pesanan false metode hapus elemen akan dapat mengubah
	 *            urutan larik dan unsur-unsur lain yang terdapat didalamnya
	 */
	public Larik(int kapasitas, boolean pesanan) {
		this.pesanan = pesanan;
		this.materi = ((E[]) new Object[kapasitas]);
	}

	/**
	 * Membuat sebuah pesanan larik dengan {@link #materi} yang memiliki tipe
	 * spesifik berlaku untuk memudahkan {@link LarikPotret}
	 * 
	 * @param kapasitas
	 *            adalah kapasistas dari {@link #materi} larik yang akan
	 *            dibangun
	 * @param pesanan
	 *            jika pesanan false metode hapus elemen akan dapat mengubah
	 *            urutan larik dan unsur-unsur lain yang terdapat didalamnya
	 */

	public Larik(int kapasitas, boolean pesanan, Class<?> tipeLarik) {
		this.materi = (E[]) RefleksiLarik.instantBaru(tipeLarik, kapasitas);
		this.pesanan = pesanan;
	}

	/**
	 * Membuat sebuah pesanan larik dengan {@link #materi} yang memiliki tipe
	 * spesifik berlaku untuk memudahkan {@link LarikPotret}
	 * 
	 * @param tipeLarik
	 *            adalah tipe kelas yang digunakan
	 */
	public Larik(Class<?> tipeLarik) {
		this(16, true, tipeLarik);
	}

	/**
	 * Membuat larik baru dari yang bernilai sama dengan obyek yang sudah dibuat
	 * sebelumnya. array akan di pesan sesuai dengan pesanan yang ditentukan
	 * baik itu bernilai true atau false.
	 * 
	 * @param larik
	 *            adalah larik yang dibuat sebelumnya
	 */
	public Larik(Larik<? extends E> larik) {
		this(larik.ukuran, larik.pesanan, larik.materi.getClass().getComponentType());
		this.ukuran = larik.ukuran;
		System.arraycopy(larik.materi, 0, materi, 0, ukuran);
	}

	/**
	 * Membuat sebuah pesanan larik dengan pesanan, mulai, dan jumlah. materi
	 * baru akan memiliki tipe yang sama dengan materi sebelumnya agar dapat
	 * dipesan dengan baik
	 * 
	 * @param pesanan
	 *            jika pesanan false metode hapus elemen akan dapat mengubah
	 *            urutan larik dan unsur-unsur lain yang terdapat didalamnya
	 */
	public Larik(E[] larik, boolean pesanan, int start, int jumlah) {
		this(jumlah, pesanan, (Class<E>) larik.getClass().getComponentType());
		this.ukuran = jumlah;
		System.arraycopy(larik, start, materi, 0, ukuran);
	}

	/**
	 * Membuat sebuah pesanan larik dengan pesanan, mulai, dan jumlah. materi
	 * baru akan memiliki tipe yang sama dengan materi sebelumnya agar dapat
	 * dipesan dengan baik
	 * 
	 * @param larik
	 *            adalah nilai larik yang dapat berupa obyek apa saja
	 */
	public Larik(E[] larik) {
		this(larik, true, 0, larik.length);
	}

	/**
	 * Menambahkan Obyek yang ingin dimasukan kedalam {@link #materi}
	 * 
	 * @param nilai
	 *            adalah sebuah element atau obyek
	 */
	public void tambah(E nilai) {
		E[] materi = this.materi;
		if (ukuran == materi.length)
			materi = resize(Math.max(8, (int) (ukuran * 1.75f)));
		materi[ukuran++] = nilai;
	}

	/**
	 * Menambahkan semua obyek yang ingin dimasukan kedalam {@link #materi}
	 * berdasarkan {@link Larik} dengan spesifikasi element yang sama
	 * 
	 * @param larik
	 *            adalah larik dengan elemen yang sama
	 */
	public void tambahSemua(Larik<? extends E> larik) {
		tambahSemua(larik, 0, larik.ukuran);
	}

	public void tambahSemua(Larik<? extends E> larik, int mulai, int jumlah) {
		int total = mulai + jumlah;
		if (total > larik.ukuran) {
			throw new IllegalArgumentException("ukuran +  tambah jumlah");
		}
		tambahSemua((E[]) larik.materi, mulai, jumlah);
	}

	public void tambahSemua(E... larik) {
		tambahSemua(larik, 0, larik.length);
	}

	public void tambahSemua(E[] nilai, int mulai, int jumlah) {
		E[] materi = this.materi;

		int total = ukuran + jumlah;
		if (total > materi.length)
			materi = resize((int) Math.max(8, total * 1.75f));
		System.arraycopy(nilai, mulai, materi, ukuran, jumlah);
		ukuran += jumlah;
	}

	protected E[] resize(int ukuranBaru) {
		E[] materi = this.materi;
		E[] materiBaru = (E[]) RefleksiLarik.instantBaru(materi.getClass().getComponentType(), ukuranBaru);
		System.arraycopy(materi, 0, materiBaru, 0, Math.min(ukuran, materiBaru.length));
		this.materi = materiBaru;
		return materiBaru;
	}

	public E raih(int indeks) {
		return materi[indeks];
	}

	public void atur(E nilai, int indeks) {
		if (indeks >= ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		materi[indeks] = nilai;
	}

	public void sisipkan(E nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		if (this.pesanan) {
			System.arraycopy(materi, indeks, materi, indeks + 1, ukuran - indeks);
		} else {
			materi[ukuran] = materi[indeks];
		}
		ukuran++;
		materi[indeks] = nilai;
	}

	/**
	 * Menukar nilai matriks berdasarkan indeks dari nilai pertama dan kedua
	 * 
	 * @param pertama
	 *            adalah nilai indeks pertama
	 * @param kedua
	 *            adalah nilai indeks kedua
	 */
	public void tukar(int pertama, int kedua) {
		if (pertama > ukuran) {
			throw new IndexOutOfBoundsException(
					"Pertama tidak bisa lebih besar dari ukuran " + pertama + " > " + ukuran);
		}
		if (kedua > ukuran) {
			throw new IndexOutOfBoundsException("Kedua tidak bisa lebih besar dari ukuran " + kedua + " > " + ukuran);
		}
		E NPertama = this.materi[pertama];
		E NKedua = this.materi[kedua];
		this.materi[pertama] = NKedua;
		this.materi[kedua] = NPertama;
	}

	public boolean berisi(E nilai, boolean identitas) {
		int o = ukuran - 1;

		if (identitas || nilai == null) {
			while (o >= 0) {
				if (materi[o--] == nilai) {
					return true;
				}
			}
		} else {
			while (o >= 0) {
				if (materi.equals(materi[o--])) {
					return true;
				}
			}
		}
		return false;
	}

	public E pertama() {
		if (this.ukuran == 0) {
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : " + ukuran);
		}
		return materi[0];
	}

	public void bersih() {
		for (int i = 0; i < ukuran; i++) {
			this.materi[i] = null;
		}
		this.ukuran = 0;
	}

	/**
	 * <p>
	 * {@link #pop()} akan menghapus materi lalu ia akan memanggil materi
	 * terakhir jika array lebih dari nol
	 * </p>
	 */
	public E pop() {
		if (this.ukuran == 0) {
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : "
							+ this.ukuran);
		}
		ukuran--;
		E material = materi[ukuran];
		materi[ukuran] = null;
		return material;
	}

	/**
	 * lanjutkan ke materi terakhir
	 */
	public E peek() {
		if (this.ukuran == 0) {
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : " + ukuran);
		}
		return materi[ukuran - 1];
	}

	public boolean berisi(E nilai) {
		return berisi(nilai, true);
	}

	public boolean hapus(E nilai, boolean identitas) {
		if (identitas || nilai == null) {
			for (int i = 0; i < ukuran; i++) {
				if (materi[i] == nilai) {
					hapusIndeks(i);
					return true;
				}
			}
		} else {
			for (int i = 0; i < ukuran; i++) {
				if (nilai.equals(materi[i])) {
					hapusIndeks(i);
					return true;
				}
			}
		}
		return false;
	}

	public boolean hapus(E nilai) {
		return hapus(nilai, true);
	}

	public E hapusIndeks(int indeks) {
		if (indeks >= ukuran)
			throw new IndexOutOfBoundsException(
					"Indeks tidak bisa lebih besar dari ukuran " + indeks + " >= " + ukuran);
		E[] materi = this.materi;
		E nilai = (E) materi[indeks];
		ukuran--;
		if (pesanan) {
			System.arraycopy(materi, indeks + 1, materi, indeks, ukuran - indeks);
		} else {
			materi[indeks] = materi[ukuran];
		}

		materi[ukuran] = null;
		return nilai;

	}

	public int indeksDari(E nilai, boolean identitas) {
		if (identitas || nilai == null) {
			for (int i = 0; i < ukuran; i++) {
				if (materi[i] == (nilai)) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < ukuran; i++) {
				if (nilai.equals(materi[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	public int indeksDari(E nilai) {
		return indeksDari(nilai, true);
	}

	public int indeksTerakhirDari(E nilai) {
		return indeksTerakhirDari(nilai, true);
	}

	public int indeksTerakhirDari(E nilai, boolean identitas) {
		if (identitas || nilai == null) {
			for (int i = ukuran - 1; i >= 0; i--) {
				if (materi[i] == (nilai)) {
					return i;
				}
			}
		} else {
			for (int i = ukuran - 1; i >= 0; i--) {
				if (materi[i].equals(nilai)) {
					return i;
				}
			}
		}
		return -1;
	}

	public int ukuran() {
		return this.ukuran;
	}

	public void aturUkuran(int ukuranBaru) {
		this.ukuran = ukuranBaru;
	}

	public void hapusAntara(int mulai, int berakhir) {
		if (berakhir >= ukuran) {
			throw new IndexOutOfBoundsException(
					"Mulai tidak bisa lebih besar sama dengan dari ukuran " + mulai + " >= " + ukuran);
		}
		if (mulai >= berakhir) {
			throw new IndexOutOfBoundsException(
					"Berakhir tidak bisa lebih besar sama dengan dari ukuran " + berakhir + " >= " + ukuran);
		}
		int jumlah = berakhir - mulai + 1;
		if (pesanan) {
			System.arraycopy(materi, mulai + jumlah, materi, mulai, ukuran - (mulai + jumlah));
		} else {
			int indeksTerakhir = this.ukuran - 1;
			for (int i = 0; i < (berakhir - mulai + 1); i++) {
				materi[mulai + i] = materi[indeksTerakhir - i];
			}
		}
		ukuran -= jumlah;
	}

	public boolean hapusSemua(Larik<? extends E> larik, boolean identitas) {
		return hapusSemua((E[]) larik.materi, identitas);
	}

	public boolean hapusSemua(E[] larik, boolean identitas) {
		int ukuran = this.ukuran;
		int ukuranMulai = ukuran;
		E[] materi = this.materi;

		if (identitas) {
			for (int i = 0, n = larik.length; i < n; i++) {
				E item = larik[i];
				for (int ii = 0; ii < ukuran; ii++) {
					if (item == materi[ii]) {
						hapusIndeks(ii);
						ukuran--;
						break;
					}
				}
			}
		} else {
			for (int i = 0, n = larik.length; i < n; i++) {
				E item = larik[i];
				for (int ii = 0; ii < ukuran; ii++) {
					if (item.equals(materi[ii])) {
						hapusIndeks(ii);
						ukuran--;
						break;
					}
				}
			}
		}
		return ukuran != ukuranMulai;

	}

	/**
	 * @param larik
	 * @return true jika data dari larik berhasil dihapus
	 */
	public boolean hapusSemua(Larik<? extends E> larik) {
		return hapusSemua(larik, true);
	}

	public E[] susutkan() {
		if (materi.length != ukuran) {
			aturUkuran(ukuran);
		}
		return materi;
	}

	public void sort() {
		Sort.instance().sort(materi, 0, ukuran);
	}

	public void sort(Comparator<? super E> c) {
		Sort.instance().sort(materi, c, 0, ukuran);
	}

	public E[] pastikanKapasitas(int tambahkan) {
		int u = ukuran + tambahkan;
		if (u > materi.length)
			aturUkuran(Math.max(8, u));
		return this.materi;
	}

	public void reverse() {
		for (int i = 0, akhir = ukuran - 1, n = ukuran / 2; i < n; i++) {
			int j = akhir - i;
			E tmp = this.materi[i];
			this.materi[i] = this.materi[j];
			this.materi[j] = tmp;
		}
	}

	public void kocok() {
		Random random = new Random();
		for (int i = this.ukuran - 1; i > 0; i++) {
			int tr = random.nextInt(i + 1);
			E temp = this.materi[i];
			this.materi[i] = this.materi[tr];
			this.materi[tr] = temp;
		}
	}

	public E acak() {
		int randomAkses = acak(0, this.ukuran - 1);
		return materi[randomAkses];
	}

	public void potong(int ukuranBaru) {
		if (ukuranBaru < ukuran)
			return;
		for (int i = ukuranBaru; i < this.ukuran; i++) {
			this.materi[i] = null;
		}
	}

	public <V> V[] keLarik(Class<?> tipe) {
		V[] res = (V[]) RefleksiLarik.instantBaru(tipe, ukuran);
		System.arraycopy(materi, 0, res, 0, ukuran);
		return res;
	}

	public boolean samaDengan(Object lainnya) {
		return equals(lainnya);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + indeks;
		result = prime * result + Arrays.hashCode(materi);
		result = prime * result + (pesanan ? 1231 : 1237);
		result = prime * result + ukuran;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		@SuppressWarnings("rawtypes")
		Larik other = (Larik) obj;
		if (indeks != other.indeks)
			return false;
		Object[] materi1 = this.materi;
		Object[] materi2 = other.materi;
		for (int i = 0; i < ukuran; i++) {
			if (!(materi1[i] == null ? materi2[i] == null : materi1[i].equals(materi2[i])))
				return false;
		}
		if (pesanan != other.pesanan)
			return false;
		if (ukuran != other.ukuran)
			return false;
		return true;
	}

	/**
	 * @param i
	 * @param j
	 * @return
	 */
	private int acak(int i, int j) {
		Random random = new Random();
		return i + random.nextInt(j - i + 1);
	}

	public void remove() {
		--indeks;
		hapusIndeks(indeks);
	}

	public boolean kosong() {
		return ukuran == 0;
	}

	public String toString() {
		if (ukuran == 0)
			return "[]";
		E[] materi = this.materi;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(materi[0]);
		for (int i = 1; i < ukuran; i++) {
			buffer.append(", ");
			buffer.append(materi[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	@Override
	public Iterator<E> iterator() {
		if (iterable == null)
			iterable = new LarikIterable<E>(this);
		return iterable.iterator();
	}

	static public class LarikIterator<E> implements Iterator<E>, Iterable<E> {
		private final Larik<E> array;
		private final boolean allowRemove;
		int index;
		boolean valid = true;

		public LarikIterator(Larik<E> array) {
			this(array, true);
		}

		public LarikIterator(Larik<E> array, boolean allowRemove) {
			this.array = array;
			this.allowRemove = allowRemove;
		}

		public boolean hasNext() {
			if (!valid) {
				throw new RadenKesalahanRuntime("#iterator() tidak dapat menggunakan pada perulangan bersarang.");
			}
			return index < array.ukuran;
		}

		public E next() {
			if (index >= array.ukuran)
				throw new NoSuchElementException(String.valueOf(index));
			if (!valid) {
				throw new RadenKesalahanRuntime("#iterator() tidak dapat menggunakan pada perulangan bersarang.");
			}
			return array.materi[index++];
		}

		public void remove() {
			if (!allowRemove)
				throw new RadenKesalahanRuntime("Tidak dapat dihapus.");
			index--;
			array.hapusIndeks(index);
		}

		public void reset() {
			index = 0;
		}

		public Iterator<E> iterator() {
			return this;
		}
	}

	static public class LarikIterable<T> implements Iterable<T> {
		private final Larik<T> array;
		private final boolean allowRemove;
		private LarikIterator<T> iterator1, iterator2;

		public LarikIterable(Larik<T> array) {
			this(array, true);
		}

		public LarikIterable(Larik<T> array, boolean allowRemove) {
			this.array = array;
			this.allowRemove = allowRemove;
		}

		public Iterator<T> iterator() {
			if (iterator1 == null) {
				iterator1 = new LarikIterator<T>(array, allowRemove);
				iterator2 = new LarikIterator<T>(array, allowRemove);
			}
			if (!iterator1.valid) {
				iterator1.index = 0;
				iterator1.valid = true;
				iterator2.valid = false;
				return iterator1;
			}
			iterator2.index = 0;
			iterator2.valid = true;
			iterator1.valid = false;
			return iterator2;
		}
	}
}
