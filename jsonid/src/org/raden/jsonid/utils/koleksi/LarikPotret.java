/**
 * 
 */
package org.raden.jsonid.utils.koleksi;

import java.util.Comparator;

/**
 * @author kiditz TODO complite LarikPotret
 * @param <T>
 *
 */
public class LarikPotret<T> extends Larik<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private T[] potret, bin;
	private int snap;
	public LarikPotret() {
		super();
	}

	/**
	 * 
	 * @param tipeLarik
	 */
	public LarikPotret(Class<?> tipeLarik) {
		super(tipeLarik);
	}

	/**
	 * @param kapasitas
	 * @param pesanan
	 * @param tipeLarik
	 */
	public LarikPotret(int kapasitas, boolean pesanan, Class<?> tipeLarik) {
		super(kapasitas, pesanan, tipeLarik);
	}

	/**
	 * @param kapasitas
	 * @param pesanan
	 */
	public LarikPotret(int kapasitas, boolean pesanan) {
		super(kapasitas, pesanan);
	}

	/**
	 * @param kapasitas
	 */
	public LarikPotret(int kapasitas) {
		super(kapasitas);
	}

	/**
	 * @param larik
	 */
	public LarikPotret(Larik<? extends T> larik) {
		super(larik);
	}

	/**
	 * @param larik
	 * @param pesanan
	 * @param start
	 * @param jumlah
	 */
	public LarikPotret(T[] larik, boolean pesanan, int start, int jumlah) {
		super(larik, pesanan, start, jumlah);
	}

	/**
	 * @param larik
	 */
	public LarikPotret(T[] larik) {
		super(larik);
	}

	public T[] mulai() {
		modifikasi();
		potret = materi;
		snap++;
		return materi;
	}

	public void selesai() {
		snap = Math.max(0, snap - 1);
		
		if (potret == null)
			return;
		if (potret != materi && snap == 0) {
			bin = potret;
			for (int i = 0; i < bin.length; i++) {
				bin[i] = null;
			}
		}
		potret = null;
	}

	public void modifikasi() {
		if (potret == null || potret != materi)
			return;
		if (bin != null && bin.length > ukuran()) {
			System.arraycopy(materi, 0, bin, 0, ukuran());
			materi = bin;
			bin = null;
		} else
			resize(materi.length);
	}

	@Override
	public void atur(T nilai, int indeks) {
		modifikasi();
		super.atur(nilai, indeks);
	}

	@Override
	public void tukar(int pertama, int kedua) {
		modifikasi();
		super.tukar(pertama, kedua);
	}

	@Override
	public void potong(int ukuranBaru) {
		modifikasi();
		super.potong(ukuranBaru);
	}

	@Override
	public boolean hapus(T nilai, boolean identitas) {
		modifikasi();
		return super.hapus(nilai, identitas);
	}

	@Override
	public T hapusIndeks(int indeks) {
		modifikasi();
		return super.hapusIndeks(indeks);
	}

	@Override
	public void hapusAntara(int mulai, int berakhir) {
		modifikasi();
		super.hapusAntara(mulai, berakhir);
	}

	@Override
	public boolean hapusSemua(Larik<? extends T> larik, boolean identitas) {
		modifikasi();
		return super.hapusSemua(larik, identitas);
	}

	@Override
	public T pop() {
		modifikasi();
		return super.pop();
	}

	@Override
	public void sort() {
		modifikasi();
		super.sort();
	}

	@Override
	public void sort(Comparator<? super T> c) {
		modifikasi();
		super.sort(c);
	}

	@Override
	public void bersih() {
		modifikasi();
		super.bersih();
	}
}
