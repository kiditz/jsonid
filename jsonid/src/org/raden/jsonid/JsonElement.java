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

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * sebuah kelas abstrak representasi dari element json yang di gunakan oleh
 * {@link JsonKosong}, {@link JsonLarik}, {@link JsonNilai}, and
 * {@link JsonObyek}
 * 
 * @author Rifky A.B
 *
 */
public abstract class JsonElement {
	/**
	 * @return salin dari element ini, immutable element seperti nilai dan kosog
	 *         tidak akan dapat disalin
	 */
	protected abstract JsonElement salin();


	/**
	 * provider cek untuk verifikasi bahwa ini adalah sebuah object
	 * 
	 * @return true jika merupakan instance dari {@link JsonObyek}
	 */
	public boolean iniObyek() {
		return this instanceof JsonObyek;
	}

	/**
	 * provider cek untuk verifikasi bahwa ini adalah sebuah larik
	 * 
	 * @return true jika merupakan instance dari {@link JsonLarik}
	 */
	public boolean iniLarik() {
		return this instanceof JsonLarik;
	}

	/**
	 * provider cek untuk verifikasi bahwa ini adalah sebuah nilai
	 * 
	 * @return true jika merupakan instance dari {@link JsonNilai}
	 */
	public boolean iniNilai() {
		return this instanceof JsonNilai;
	}

	/**
	 * provider cek untuk verifikasi bahwa ini adalah kosong
	 * 
	 * @return true jika merupakan instance dari {@link JsonKosong}
	 */
	public boolean iniKosong() {
		return this instanceof JsonKosong;
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai {@link JsonObyek}. Jika
	 * element yang dihasilkan merupakan sebuah tipe lain selain
	 * {@link JsonObyek} maka akan menghasilkan {@link JsonKesalahan}.
	 * 
	 * @return raih element ini sebagai {@link JsonObyek}
	 * @throws JsonKesalahan
	 *             jika bukan merupakan bagian dari {@link JsonObyek}
	 */
	public JsonObyek sebagaiObyek() {
		if (iniObyek())
			return (JsonObyek) this;
		else
			throw new JsonKesalahan("ini bukan obyek!" + this);
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai {@link JsonLarik}. Jika
	 * element yang dihasilkan merupakan sebuah tipe lain selain
	 * {@link JsonLarik} maka akan menghasilkan {@link JsonKesalahan}.
	 * 
	 * @return raih element ini sebagai {@link JsonLarik}
	 * @throws JsonKesalahan
	 *             jika bukan merupakan bagian dari {@link JsonLarik}
	 */
	public JsonLarik sebagaiLarik() {
		if (iniLarik())
			return (JsonLarik) this;
		else
			throw new JsonKesalahan("ini bukan larik!" + this);
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai {@link JsonNilai}. Jika
	 * element yang dihasilkan merupakan sebuah tipe lain selain
	 * {@link JsonNilai} maka akan menghasilkan {@link JsonKesalahan}.
	 * 
	 * @return raih element ini sebagai {@link JsonNilai}
	 * @throws JsonKesalahan
	 *             jika bukan merupakan bagian dari {@link JsonNilai}
	 */
	public JsonNilai sebagaiNilai() {
		if (iniNilai())
			return (JsonNilai) this;
		else
			throw new JsonKesalahan("ini bukan nilai!" + this);
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai boolean
	 * 
	 * @return raih element ini sebagai primitif boolean.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai boolean
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 */
	public boolean sebagaiBoolean() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai {@link String}
	 * 
	 * @return raih element ini sebagai primitif string.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai {@link String}
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 */
	public String sebagaiString() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai {@link Number}
	 * 
	 * @return raih element ini sebagai primitif angka.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai {@link Number}
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 */
	public Number sebagaiAngka() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai double
	 * 
	 * @return raih element ini sebagai primitif double.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai double
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 */
	public double sebagaiDouble() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai karakter
	 * 
	 * @param indeks
	 *            adalah indeks dari karakter
	 * @return raih element ini sebagai primitif karakter.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai karakter
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public char sebagaiKarakter(int indeks) {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai karakter
	 * 
	 * @return raih element ini sebagai primitif karakter.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai karakter
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public char sebagaiKarakter() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai long
	 * 
	 * @return raih element ini sebagai primitif long.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai long
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public long sebagaiLong() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai byte
	 * 
	 * @return raih element ini sebagai primitif byte.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai byte
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public byte sebagaiByte() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai int
	 * 
	 * @return raih element ini sebagai primitif int.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai int
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public int sebagaiInt() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai short
	 * 
	 * @return raih element ini sebagai primitif short.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai short
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public short sebagaiShort() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai float
	 * 
	 * @return raih element ini sebagai primitif float.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai float
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public float sebagaiFloat() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai
	 * {@link BigDecimal}
	 * 
	 * @return raih element ini sebagai {@link BigDecimal}.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai {@link BigDecimal}
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public BigDecimal sebagaiBigDecimal() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Mengkonsumsi method agar dapat di gunakan sebagai nilai
	 * {@link BigInteger}
	 * 
	 * @return raih element ini sebagai {@link BigInteger}.
	 * @throws ClassCastException
	 *             jika element bukan sebuah {@link JsonNilai} dan bukan
	 *             merupakan bagian dari nilai {@link BigInteger}
	 * @throws JsonKesalahan
	 *             jika merupakan bagian dari {@link JsonLarik} tapi memiliki
	 *             lebih dari satu element didalam satu method
	 * 
	 */
	public BigInteger sebagaiBigInteger() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * Menulis {@link JsonElement} sebagai string json untuk menghasilkan output
	 * yang berupa nilai {@link String} json.
	 * 
	 * @return {@link StringWriter#toString()} parsing dan konversi via
	 *         {@link JsonParser}
	 * 
	 */
	public String toString() {
		StringWriter writer = new StringWriter();
		JsonPenulis penulis = new JsonPenulis(writer);
		JsonParser parser = new JsonParser();
		try {
			parser.tulis(penulis, this);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
		return writer.toString();
	}

	/**
	 * Menulis {@link JsonElement} sebagai string cantik dengan mengaktifkan
	 * indent via {@link JsonPenulis#aturIndent(String)} untuk file json yang
	 * hendak ditulis
	 * 
	 * 
	 * @return {@link StringWriter#toString()} parsing dan konversi via
	 *         {@link JsonParser}
	 * 
	 */
	public String cetakCantik() {
		StringWriter writer = new StringWriter();
		JsonPenulis penulis = new JsonPenulis(writer);
		penulis.aturIndent("  ");
		JsonParser parser = new JsonParser();
		try {
			parser.tulis(penulis, this);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
		return writer.toString();
	}

	/**
	 * Menulis {@link JsonElement} sebagai string cantik dengan mengaktifkan
	 * indent via {@link JsonPenulis#aturIndent(String)} untuk file json yang
	 * hendak ditulis
	 * 
	 * @param indent
	 *            adalah indent berupa whitespace yang ingin digunakan
	 * @return {@link StringWriter#toString()} parsing dan konversi via
	 *         {@link JsonParser}
	 * 
	 */
	public String cetakCantik(String indent) {
		StringWriter writer = new StringWriter();
		JsonPenulis penulis = new JsonPenulis(writer);
		penulis.aturIndent(indent);
		JsonParser parser = new JsonParser();
		try {
			parser.tulis(penulis, this);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
		return writer.toString();
	}
}
