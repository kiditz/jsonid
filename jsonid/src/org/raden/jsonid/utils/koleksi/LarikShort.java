package org.raden.jsonid.utils.koleksi;

import java.util.Arrays;

/**
 * 
 * */
public class LarikShort {
	private short[] materi;
	private int ukuran;
	private final boolean pesanan;

	public LarikShort() {
		this(true, 16);
	}

	public LarikShort(int kapasitas) {
		this(true, kapasitas);
	}

	public LarikShort(boolean pesanan, int kapasitas) {
		this.pesanan = pesanan;
		this.materi = new short[kapasitas];
	}

	public LarikShort(short[] larik, boolean pesanan, int mulai, int jumlah) {
		this(pesanan, jumlah);
		this.ukuran = jumlah;
		System.arraycopy(larik, mulai, this.materi, 0, this.ukuran);
	}

	public LarikShort(short... larik) {
		this(larik, true, 0, larik.length);
	}

	public LarikShort(LarikShort larik) {
		this.pesanan = larik.pesanan;
		this.ukuran = larik.ukuran;
		this.materi = new short[ukuran];
		System.arraycopy(larik.materi, 0, this.materi, 0, this.ukuran);
	}

	public void tambah(short nilai) {
		short[] materi = this.materi;
		if (ukuran == materi.length)
			materi = aturUkuran(Math.max(8, (int) (ukuran * 1.75f)));
		materi[ukuran++] = nilai;
	}

	public void tambahSemua(short[] nilai, int mulai, int jumlah) {
		short[] materi = this.materi;
		int total = this.ukuran + jumlah;
		if (total > nilai.length)
			materi = aturUkuran((int) Math.max(8, total * 1.75f));
		System.arraycopy(nilai, mulai, materi, ukuran, jumlah);
		ukuran += jumlah;
	}

	public void tambahSemua(LarikShort larik, int mulai, int jumlah) {
		int total = ukuran + jumlah;
		if (total > larik.ukuran) {
			throw new IllegalArgumentException("mulai + jumlah harus lebih kecil dari ukuran " + " jumlah = " + jumlah
					+ "Ukuran = " + ukuran + "ukuran Larik " + larik.ukuran);
		}
		tambahSemua(larik.materi, mulai, jumlah);
	}

	public void tambahSemua(short... nilai) {
		tambahSemua(nilai, 0, nilai.length);
	}

	public void tambahSemua(LarikShort larik) {
		tambahSemua(larik.materi, 0, larik.ukuran);
	}

	public short raih(int indeks) {
		return materi[indeks];
	}

	public int ukuran() {
		return ukuran;
	}

	public void atur(short nilai, int indeks) {
		if (indeks >= ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		materi[indeks] = nilai;
	}

	public void taruh(short nilai, int indeks) {
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
		short NPertama = this.materi[pertama];
		short NKedua = this.materi[kedua];
		this.materi[pertama] = NKedua;
		this.materi[kedua] = NPertama;
	}

	public void kali(short nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		this.materi[indeks] *= nilai;
	}

	public void tambah(short nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		this.materi[indeks] += nilai;
	}

	public void kurang(short nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		this.materi[indeks] -= nilai;
	}

	public void bagi(short nilai, int indeks) {
		if (indeks > ukuran)
			throw new IndexOutOfBoundsException("Indeks tidak bisa lebih besar dari ukuran " + indeks + " > " + ukuran);
		this.materi[indeks] /= nilai;
	}

	public boolean berisi(short nilai, boolean identitas) {
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

	public short pop() {
		return materi[--ukuran];
	}

	public short peek() {
		if (this.ukuran == 0)
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : " + ukuran);
		return materi[ukuran - 1];
	}

	public short pertama() {
		if (this.ukuran == 0) {
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : " + ukuran);
		}
		return materi[0];
	}

	public void aturAwal(short awal) {
		this.materi[0] = awal;
	}

	public void aturAkhir(short akhir) {
		this.materi[ukuran - 1] = akhir;
	}

	public short akhir() {
		if (this.ukuran == 0) {
			throw new IllegalArgumentException(
					"Ukuran Array anda kosong dan mungkin anda tidak memasukan array dengan benar. Ukuran : " + ukuran);
		}
		return materi[ukuran - 1];
	}

	public boolean berisi(short nilai) {
		return berisi(nilai, true);
	}

	public short[] pastikanKapasitas(int penambah) {
		if (this.ukuran + penambah > this.materi.length) {
			aturUkuran(Math.max(8, this.ukuran + penambah));
		}
		return this.materi;
	}

	public boolean hapus(short nilai) {
		return hapus(nilai, true);
	}

	public boolean hapus(short nilai, boolean identitas) {
		for (int i = 0; i < ukuran; i++) {
			if (materi[i] == nilai) {
				hapusIndeks(i);
				return true;
			}
		}
		return false;
	}

	public short hapusIndeks(int indeks) {
		if (indeks >= ukuran)
			throw new IndexOutOfBoundsException(
					"Indeks tidak bisa lebih besar dari ukuran " + indeks + " >= " + ukuran);
		short[] materi = this.materi;
		short nilai = materi[indeks];
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

	public boolean hapusSemua(LarikShort larik) {
		int ukuran = this.ukuran;
		int ukuranMulai = ukuran;
		for (int i = 0, n = larik.ukuran; i < n; i++) {
			short materi = larik.materi[i];
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

	public short[] keLarik() {
		short[] larik = new short[ukuran];
		System.arraycopy(materi, 0, larik, 0, ukuran);
		return larik;
	}

	public int indeksDari(short nilai) {
		for (int i = 0; i < ukuran; i++) {
			if (materi[i] == nilai) {
				return i;
			}
		}
		return -1;
	}

	public int indeksTerakhirDari(short nilai) {
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

	private short[] aturUkuran(int maksimal) {
		short[] materiBaru = new short[maksimal];
		short materi[] = this.materi;
		System.arraycopy(materi, 0, maksimal, 0, Math.min(ukuran, materiBaru.length));
		this.materi = materiBaru;
		return materiBaru;
	}

	@Override
	public String toString() {
		return Arrays.toString(materi);
	}
}
