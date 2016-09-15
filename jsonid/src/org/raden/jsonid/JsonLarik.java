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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import org.raden.jsonid.utils.koleksi.Larik;

/**
 * @author Rifky A.B
 *
 */
public class JsonLarik extends JsonElement implements Iterable<JsonElement> {
	private final Larik<JsonElement> lariks;
	public JsonLarik() {
		lariks = new Larik<JsonElement>();
	}
	
	public void tambah(JsonElement nilai) {
		lariks.tambah(nilai);
	}

	public void tambah(JsonLarik larik) {
		lariks.tambahSemua(larik);
	}

	public void atur(JsonLarik larik) {
		lariks.tambahSemua(larik);
	}

	public void tambah(String nilai) {
		lariks.tambah(nilai == null ? JsonKosong.baru() : new JsonNilai(nilai));
	}

	public void atur(int indeks, JsonElement nilai) {
		lariks.atur(nilai, indeks);
	}

	public void hapus(JsonElement nilai) {
		lariks.hapus(nilai);
	}

	public void hapus(int indeks) {
		lariks.hapusIndeks(indeks);
	}

	public boolean berisi(JsonElement nilai) {
		return lariks.berisi(nilai);
	}

	public JsonElement raih(int indeks) {
		return lariks.raih(indeks);
	}

	public void tambah(Character nilai) {
		lariks.tambah(nilai == null ? JsonKosong.baru() : new JsonNilai(nilai));
	}

	public void tambah(Boolean nilai) {
		lariks.tambah(nilai == null ? JsonKosong.baru() : new JsonNilai(nilai));
	}

	public void tambah(Number nilai) {
		lariks.tambah(nilai == null ? JsonKosong.baru() : new JsonNilai(nilai));
	}

	@Override
	protected JsonElement salin() {
		return this;
	}

	@Override
	public Iterator<JsonElement> iterator() {
		return lariks.iterator();
	}

	public int ukuran() {
		return lariks.ukuran();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.raden.utils.json.JsonElement#sebagaiAngka()
	 */
	@Override
	public Number sebagaiAngka() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiAngka();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan angka");
	}

	@Override
	public boolean sebagaiBoolean() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiBoolean();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan boolean");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.raden.utils.json.JsonElement#sebagaiBigDecimal()
	 */
	@Override
	public BigDecimal sebagaiBigDecimal() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiBigDecimal();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan BigDecimal");
	}

	@Override
	public BigInteger sebagaiBigInteger() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiBigInteger();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan BigInteger");
	}

	@Override
	public byte sebagaiByte() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiByte();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan Byte");
	}

	@Override
	public double sebagaiDouble() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiDouble();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan Double");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.raden.utils.json.JsonElement#sebagaiFloat()
	 */
	@Override
	public float sebagaiFloat() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiFloat();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan angka");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.raden.utils.json.JsonElement#sebagaiInt()
	 */
	@Override
	public int sebagaiInt() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiInt();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan angka");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.raden.utils.json.JsonElement#sebagaiKarakter()
	 */
	@Override
	public char sebagaiKarakter() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiKarakter();
		}
		throw new JsonKesalahan("Tidak dapat mendapatkan angka");
	}

	@Override
	public long sebagaiLong() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiLong();
		}
		throw new JsonKesalahan();
	}

	@Override
	public short sebagaiShort() {
		if (lariks.ukuran() == 1) {
			return lariks.raih(0).sebagaiShort();
		}
		throw new JsonKesalahan();
	}
	
	public static JsonLarik baru() {
		return new JsonLarik();
	}
}
