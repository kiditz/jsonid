/**
 * 
 */
package org.raden.jsonid.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Rifky A.B
 *
 */
public class PembacaIterator<T> implements Iterator<T>, Iterable<T> {
	private final T[] larik;
	private int indeks;
	private boolean validasi = true;

	/**
	 * 
	 */
	public PembacaIterator(T[] larik) {
		this.larik = larik;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		if (!validasi) {
			throw new IllegalArgumentException("Iterator Tidak di validasi");
		}
		boolean idxValidator = indeks < larik.length;
		return idxValidator;
	}

	@Override
	public T next() {
		if (indeks >= larik.length)
			throw new NoSuchElementException(String.valueOf(indeks));
		if (!validasi) {
			throw new IllegalArgumentException("Iterator Tidak di validasi");
		}
		return larik[indeks++];
	}

	@Override
	public void remove() {
		throw new RadenKesalahanRuntime("Hapus Tidak di dukung");
	}

	public void aturUlang() {
		this.indeks = 0;
	}

	public int indeks() {
		return indeks;
	}

	public void aturIndeks(int indeks) {
		this.indeks = indeks;
	}

	public boolean iniValidasi() {
		return validasi;
	}

	public void aturValidasi(boolean validasi) {
		this.validasi = validasi;
	}
	
}
