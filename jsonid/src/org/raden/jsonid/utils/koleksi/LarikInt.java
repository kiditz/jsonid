package org.raden.jsonid.utils.koleksi;

import java.util.Arrays;

/**
 * 
 * */
public class LarikInt {
	public int[] materi;
	private int ukuran;
	private final boolean pesanan;

	public LarikInt() {
		this(true, 16);
	}

	public LarikInt(int kapasitas) {
		this(true, kapasitas);
	}

	public LarikInt(boolean pesanan, int kapasitas) {
		this.pesanan = pesanan;
		this.materi = new int[kapasitas];
	}

	public LarikInt(int[] larik, boolean pesanan, int mulai, int jumlah) {
		this(pesanan, jumlah);
		this.ukuran = jumlah;
		System.arraycopy(larik, mulai, this.materi, 0, this.ukuran);
	}

	public LarikInt(int... larik) {
		this(larik, true, 0, larik.length);
	}

	public LarikInt(LarikInt larik) {
		this.pesanan = larik.pesanan;
		this.ukuran = larik.ukuran;
		this.materi = new int[ukuran];
		System.arraycopy(larik.materi, 0, this.materi, 0, this.ukuran);
	}

	public void tambah(int nilai) {
		int[] materi = this.materi;
		if (ukuran == materi.length)
			materi = aturUkuran(Math.max(8, (int) (ukuran * 1.75f)));
		materi[ukuran++] = nilai;
	}

	public void tambahSemua(int[] nilai, int mulai, int jumlah) {
		int[] materi = this.materi;
		int total = this.ukuran + jumlah;
		if (total > nilai.length)
			materi = aturUkuran((int) Math.max(8, total * 1.75f));
		System.arraycopy(nilai, mulai, materi, ukuran, jumlah);
		ukuran += jumlah;
	}

	public void tambahSemua(LarikInt larik, int mulai, int jumlah) {
		int total = ukuran + jumlah;
		if (total > larik.ukuran) {
			throw new IllegalArgumentException("mulai + jumlah harus lebih kecil dari ukuran " + " jumlah = " + jumlah
					+ "Ukuran = " + ukuran + "ukuran Larik " + larik.ukuran);
		}
		tambahSemua(larik.materi, mulai, jumlah);
	}

	public void tambahSemua(int... nilai) {
		tambahSemua(nilai, 0, nilai.length);
	}

	public void tambahSemua(LarikInt larik) {
		tambahSemua(larik.materi, 0, larik.ukuran);
	}

	public int raih(int indeks) {
		return materi[indeks];
	}

	public int ukuran() {
		return ukuran;
	}

	public void atur(int nilai, int indeks) {
		if (indeks >= ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		materi[indeks] = nilai;
	}

	public void taruh(int nilai, int indeks) {
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

	public void tukar(int pertama, int kedua) {
		if (pertama > ukuran) {
			throw new IndexOutOfBoundsException(
					"Pertama tidak bisa lebih besar dari ukuran " + pertama + " > " + ukuran);
		}
		if (kedua > ukuran) {
			throw new IndexOutOfBoundsException("Kedua tidak bisa lebih besar dari ukuran " + kedua + " > " + ukuran);
		}
		int NPertama = this.materi[pertama];
		int NKedua = this.materi[kedua];
		this.materi[pertama] = NKedua;
		this.materi[kedua] = NPertama;
	}

	public void kali(int nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		this.materi[indeks] *= nilai;
	}

	public void tambah(int nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		this.materi[indeks] += nilai;
	}

	public void kurang(int nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		this.materi[indeks] -= nilai;
	}

	public void bagi(int nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		this.materi[indeks] /= nilai;
	}

	public boolean berisi(int nilai, boolean identitas) {
		int o = ukuran - 1;
		if (identitas || nilai == 0) {
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

	public int pop() {
		return materi[--ukuran];
	}

	public int peek() {
		if (this.ukuran == 0)
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : " + ukuran);
		return materi[ukuran - 1];
	}

	public int pertama() {
		if (this.ukuran == 0) {
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : " + ukuran);
		}
		return materi[0];
	}

	public void aturAwal(int awal) {
		this.materi[0] = awal;
	}

	public void aturAkhir(int akhir) {
		this.materi[ukuran - 1] = akhir;
	}

	public int akhir() {
		if (this.ukuran == 0) {
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : " + ukuran);
		}
		return materi[ukuran - 1];
	}

	public boolean berisi(int nilai) {
		return berisi(nilai, true);
	}

	public int[] pastikanKapasitas(int penambah) {
		if (this.ukuran + penambah > this.materi.length) {
			aturUkuran(Math.max(8, this.ukuran + penambah));
		}
		return this.materi;
	}

	public boolean hapus(int nilai) {
		return hapus(nilai, true);
	}

	public boolean hapus(int nilai, boolean identitas) {
		for (int i = 0; i < ukuran; i++) {
			if (materi[i] == nilai) {
				hapusIndeks(i);
				return true;
			}
		}
		return false;
	}

	public int hapusIndeks(int indeks) {
		if (indeks >= ukuran)
			throw new IndexOutOfBoundsException(
					"Indeks tidak bisa lebih besar dari ukuran " + indeks + " >= " + ukuran);
		int[] materi = this.materi;
		int nilai = materi[indeks];
		ukuran--;
		if (pesanan) {
			System.arraycopy(materi, indeks + 1, materi, indeks, ukuran - indeks);
		} else {
			materi[indeks] = materi[ukuran];
		}
		return nilai;
	}

	public boolean ukuranEqualsKapasitas() {
		return ukuran == materi.length;
	}

	public void ukuranBerkurang() {
		this.ukuran--;
	}

	public void ukuranBertambah() {
		this.ukuran++;
	}

	public void hapusAntara(int mulai, int berakhir) {
		if (berakhir >= ukuran) {
			throw new IndexOutOfBoundsException(
					"Mulai tidak bisa lebih besar atau sama dengan dari ukuran " + mulai + " >= " + ukuran);
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

	public boolean hapusSemua(LarikInt larik) {
		int ukuran = this.ukuran;
		int ukuranMulai = ukuran;
		for (int i = 0, n = larik.ukuran; i < n; i++) {
			int materi = larik.materi[i];
			for (int j = 0; j < ukuran; j++) {
				if (materi == this.materi[j]) {
					hapusIndeks(j);
					ukuran--;
					break;
				}
			}
		}
		return ukuranMulai != ukuran;
	}

	public void potong(int ukuranBaru) {
		if (ukuranBaru > ukuran)
			ukuran = ukuranBaru;
	}

	public int[] keLarik() {
		int[] larik = new int[ukuran];
		System.arraycopy(materi, 0, larik, 0, ukuran);
		return larik;
	}

	public int indeksDari(int nilai) {
		for (int i = 0; i < ukuran; i++) {
			if (materi[i] == nilai) {
				return i;
			}
		}
		return -1;
	}

	public int indeksTerakhirDari(int nilai) {
		for (int i = ukuran - 1; i >= 0; i--) {
			if (materi[i] == nilai) {
				return i;
			}
		}
		return -1;
	}

	public void bersih() {
		for (int i = 0; i < ukuran; i++) {
			this.materi[i] = 0;
		}
		this.ukuran = 0;
	}

	private int[] aturUkuran(int maksimal) {
		int[] materiBaru = new int[maksimal];
		int materi[] = this.materi;
		System.arraycopy(materi, 0, maksimal, 0, Math.min(ukuran, materiBaru.length));
		this.materi = materiBaru;
		return materiBaru;
	}

	@Override
	public String toString() {
		return Arrays.toString(materi);
	}
}
