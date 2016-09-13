/**
 * 
 */
package org.raden.jsonid.utils;

import java.util.Iterator;

/**
 * @author Rifky A.B
 *
 */
public class PembacaIterable<T> implements Iterable<T> {
	private final T[] larik;
	private PembacaIterator<T> iter1, iter2;

	public PembacaIterable(T[] larik) {
		this.larik = larik;
	}

	@Override
	public Iterator<T> iterator() {
		if (iter1 == null) {
			this.iter1 = new PembacaIterator<T>(larik);
			this.iter2 = new PembacaIterator<T>(larik);
		}
		if (!iter1.iniValidasi()) {
			iter1.aturUlang();
			iter1.aturValidasi(true);
			iter2.aturValidasi(false);
			return iter1;
		}
		iter2.aturUlang();
		iter2.aturValidasi(true);
		iter1.aturValidasi(false);
		return iter2;
	}
}
