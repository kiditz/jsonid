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
package org.raden.jsonid;

import org.raden.jsonid.utils.koleksi.PetaObyek.Catat;
import org.raden.jsonid.utils.koleksi.PetaObyek.Catatan;
import org.raden.jsonid.utils.koleksi.PetaOrder;

/**
 * 
 * @author Rifky A.B
 *
 */
public class JsonObyek extends JsonElement {
	private final PetaOrder<String, JsonElement> obyeks = new PetaOrder<String, JsonElement>();

	public void tambah(String properti, JsonElement obyek) {
		if (obyek == null)
			obyek = JsonKosong.baru();
		obyeks.taruh(properti, obyek);
	}

	public void tambahProperti(String properti, String nilai) {
		obyeks.taruh(properti, buatElement(nilai));
	}

	public void tambahProperti(String properti, Number nilai) {
		obyeks.taruh(properti, buatElement(nilai));
	}

	public void tambahProperti(String properti, Boolean nilai) {
		obyeks.taruh(properti, buatElement(nilai));
	}

	public void tambahProperti(String properti, Character nilai) {
		obyeks.taruh(properti, buatElement(nilai));
	}

	public JsonElement raih(String properti) {
		return obyeks.raih(properti);
	}

	public JsonElement raih(String properti, JsonElement bagian) {
		return obyeks.raih(properti, bagian);
	}

	public JsonObyek sebagaiObyek(String properti) {
		return (JsonObyek) obyeks.raih(properti);
	}

	public JsonLarik sebagaiLarik(String properti) {
		return (JsonLarik) obyeks.raih(properti);
	}

	public JsonNilai sebagaiNilai(String properti) {
		return (JsonNilai) obyeks.raih(properti);
	}

	public boolean equals(Object o) {
		return (o == this) || (o instanceof JsonObyek && ((JsonObyek) o).obyeks.equals(obyeks));
	}

	public boolean memiliki(String properti) {
		return obyeks.berisiKunci(properti);
	}

	@Override
	public int hashCode() {
		return obyeks.hashCode();
	}

	public JsonElement buatElement(Object nilai) {
		return nilai == null ? JsonKosong.baru() : new JsonNilai(nilai);
	}

	/**
	 * @return the obyeks
	 */
	public Catatan<String, JsonElement> catatan() {
		return obyeks.iterator();
	}

	@Override
	protected JsonObyek salin() {
		JsonObyek hasil = new JsonObyek();
		for (Catat<String, JsonElement> entry : obyeks.catatan()) {
			hasil.tambah(entry.kunci, entry.nilai);
		}
		return hasil;
	}

	public void hapus(String properti) {
		obyeks.hapus(properti);
	}

	public static JsonObyek baru() {
		return new JsonObyek();
	}
}
