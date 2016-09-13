/**
 * 
 */
package org.raden.jsonid.utils;

import org.raden.jsonid.utils.koleksi.Larik;

/**
 * @author Rifky Aditya Bastara
 *
 */
public abstract class Kolam<T> {
	public final int maksimal;
	public int puncak = 0;
	private final Larik<T> larikBebas;

	public Kolam() {
		this(16, Integer.MAX_VALUE);
	}

	public Kolam(int kapasitas) {
		this(kapasitas, Integer.MAX_VALUE);
	}

	public Kolam(int kapasitas, int maksimal) {
		larikBebas = new Larik<T>(kapasitas, false);
		this.maksimal = maksimal;
	}

	protected abstract T buat();

	public T raih() {
		return larikBebas.ukuran() == 0 ? buat() : larikBebas.pop();
	}

	public void bebaskan(T obyek) {
		if (obyek.equals(null)) {
			throw new IllegalArgumentException("objek tidak boleh kosong "
					+ obyek.toString());
		}

		if (larikBebas.ukuran() < this.maksimal) {
			larikBebas.tambah(obyek);
			puncak = Math.max(puncak, larikBebas.ukuran());
		}

		if (obyek instanceof PengelolaKolam) {
			((PengelolaKolam) obyek).aturUlang();
		}
	}

	public void bebaskanSemua(Larik<T> larik) {
		if (larik.equals(null)) {
			throw new IllegalArgumentException("objek tidak boleh kosong "
					+ larik.toString());
		}
		for (int i = 0; i < larikBebas.ukuran(); i++) {
			T obyek = larikBebas.raih(i);
			if (obyek == null) {
				continue;
			}
			if (larikBebas.ukuran() < this.maksimal) {
				larikBebas.tambah(obyek);
			}
			if (larik instanceof PengelolaKolam) {
				((PengelolaKolam) obyek).aturUlang();
			}
		}
		puncak = Math.max(puncak, larikBebas.ukuran());
	}

	public void bersih() {
		larikBebas.bersih();
	}

	public int ukuranBebas() {
		return larikBebas.ukuran();
	}

	public Larik<T> larikBebas() {
		return larikBebas;
	}

	public interface PengelolaKolam {
		public void aturUlang();
	}
}
