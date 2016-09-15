/*******************************************************************************
 * Copyright 2016 By Raden Studio.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
