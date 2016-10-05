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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.raden.jsonid.utils.koleksi.Larik;
import org.raden.jsonid.utils.koleksi.PetaObyek;
import org.raden.jsonid.utils.koleksi.PetaOrder;

/**
 * <p>
 * <b> Menulis dan membaca json ke dan dari Pojo (Plain Old Java Object)</b><br>
 * {@link JsonID} ini menggunakan konstruktor default sebagai tipe dasar ,jadi
 * setiap entity wajib memiliki konstruktor agar dapat di baca menggunakan
 * {@linkplain JsonID}
 * </p>
 * <p>
 * atur {@link #aktifkanSpasi()} ke true untuk membuat printer cantik yang dapat
 * dengan mudah dibaca oleh penglihatan anda.. ini juga baik untuk kesehatan
 * anda
 * </p>
 * 
 * 
 * Untuk dapat serialize dan menulis json anda dapat menggunakan
 * {@linkplain #keJson(Object)}<br>
 * 
 * contoh seperti dibawah ini :
 * 
 * <pre>
 * JsonID jsonId = JsonID.baru();
 * ObyekSaya obyek = new ObyekSaya();
 * String json = jsonID.keJson(obyek);
 * System.out.println(json);

 * </pre>
 * <p>
 * Sedangkan untuk membaca dan deserialize json anda dapat menggunakan
 * {@linkplain #dariJson(File, Class)}
 * </p>
 * 
 * contoh seperti dibawah ini :
 * 
 * <pre>
 * obyek = jsonId.dariJson(json, ObyekSaya.class);
 * System.out.println(obyek);
 * </pre>
 * 
 * @author kiditz
 * 
 */
public class JsonID {
	private JsonPenulis penulis;
	private final PetaObyek<Class<?>, String> kelasKeTag = new PetaObyek<Class<?>, String>();
	private final PetaObyek<String, Class<?>> tagKekelas = new PetaObyek<String, Class<?>>();
	private final PetaObyek<Class<?>, PetaOrder<String, FieldData>> tipeKeFields = new PetaObyek<Class<?>, PetaOrder<String, FieldData>>();
	private final PetaObyek<Class<?>, Object[]> kelasKeNilaiDefault = new PetaObyek<Class<?>, Object[]>();
	private String namaTipe = "kelas";
	private String patternTanggal = "EEE MMM dd HH:mm:ss zzz yyyy";
	private PetaObyek<Class<?>, JsonSerializer<?>> kelasSerializer = new PetaObyek<Class<?>, JsonSerializer<?>>();
	private boolean namaEnum = true;
	private final Object[] equals1 = { null }, equals2 = { null };
	private String spasi = "  ";
	private boolean pencetakCantik = false;
	// so, set to true for advance programmer only. :D happy coding
	private boolean debug =false;
	private boolean lenient = false;
	private boolean prototipe = false;

	/**
	 * Instant {@link #baru()} digunakan untuk memanggil jsonID dengan method
	 * statis untuk memanggil konstruktor dari {@link JsonID} yang bersifat
	 * private.
	 * 
	 * @return new {@link JsonID}
	 */
	public static JsonID baru() {
		return new JsonID();
	}

	private JsonID() {
		if (debug) {
			int i = 0;
			System.out.println("Debuging");
			while (i < 50) {
				System.out.print(".");
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				i++;
			}
			System.out.println();
		}
	}

	/**
	 * Membuat Serializer
	 * 
	 * @param tipe
	 *            adalah tipe dari serializer
	 * @param serializer
	 *            adalah serializer yang ingin diatur
	 * @return kelas ini sendiri
	 * 
	 */
	public JsonID aturSerializer(Class<?> tipe, JsonSerializer<?> serializer) {
		this.kelasSerializer.taruh(tipe, serializer);
		return this;
	}

	/**
	 * Mengaktifkan prototipe untuk menggunakan nilai default yang terdapat di
	 * dalam kelas yang ingin di ubah kedalam json. saat ini tidak diaktifkan
	 * maka nilai default akan bernilai null dan untuk mengisi nilai tersebut
	 * maka nilai tersebut di panggil melalui obyek baru.
	 * 
	 * nilai default dari {@linkplain #prototipe} adalah false
	 * 
	 * @return kelas ini
	 */
	public JsonID aktifkanPrototipe() {
		this.prototipe = true;
		return this;
	}

	/**
	 * Mengatur whitespace yang ingin di gunakan untuk indent pada json yang
	 * akan memberikan hasil pencetak cantik
	 * 
	 * @param spasi
	 *            adalah nilai whitespace
	 * @return kelas ini
	 */
	public JsonID aturSpasi(String spasi) {
		this.spasi = spasi;
		return this;
	}

	/**
	 * Mematikan nama enum akan menggunakan {@link Enum#toString()} untuk
	 * menulis nilai enumurasi, mungkin tidak akan dapat meraih nilai unique
	 * dengan mematikan ini. dan akan menggunakan {@link Enum#name()} ketika
	 * true. Nilai awalnya adalah true.
	 * 
	 * @return kelas ini
	 */
	public JsonID matikanNamaEnum() {
		this.namaEnum = false;
		return this;
	}

	/**
	 * Ganti nama tipe dari kelas ke nama lain yang anda inginkan. nilai awalnya
	 * adalah <b>'kelas'</b>
	 * 
	 * @param namaTipe
	 *            adalah nama dari tag kelas yang ingin diganti
	 * @return kelas ini
	 */
	public JsonID aturNamaTipe(String namaTipe) {
		this.namaTipe = namaTipe;
		return this;
	}

	/**
	 * Mengatur pattern tanggal yang ingin digunakan saat menggunaka obyek
	 * {@link Date}. patter ini menggunakan {@link SimpleDateFormat} untuk
	 * menyesuaikan format tanggal yang di inginkan oleh end user
	 * 
	 * @param patternTanggal
	 *            adalah format tanggal yang ingin diatur
	 * @return kelas ini
	 */
	public JsonID aturPatternTanggal(String patternTanggal) {
		this.patternTanggal = patternTanggal;
		return this;
	}

	/**
	 * @param writer
	 *            adalah jenis writer yang digunakan sebagai output json
	 */
	public void aturPenulis(Writer writer) {
		// if (!(writer instanceof JsonPenulis))
		this.penulis = new JsonPenulis(writer);
		this.penulis.aturHtmlAman(false);
		this.penulis.aturLenient(lenient);
		this.penulis.aturSerializeNulls(true);
		if (pencetakCantik)
			this.penulis.aturIndent(spasi);
	}

	/**
	 * Menambah tag akan memudahkan json untuk dibaca
	 * 
	 * @param kunci
	 *            adalah kelas yang di input kedalam json
	 * @param nama
	 *            adalah nama variabel yang ingin di ubah
	 * @return kelas ini
	 */
	public JsonID tambahTag(Class<?> kunci, String nama) {
		tagKekelas.taruh(nama, kunci);
		kelasKeTag.taruh(kunci, nama);
		return this;
	}

	/**
	 * Mengaktifkan spasi alias print cantik yang akan membuat json menjadi
	 * lebih mudah di baca oleh mata manusia.
	 * 
	 * @return kelas ini
	 */
	public JsonID aktifkanSpasi() {
		this.pencetakCantik = true;
		return this;
	}

	/**
	 * Mengecek apakah spasi sudah aktif atau belum
	 * 
	 * @return true apabila sudah dipanggil melalui {@link #aktifkanSpasi()} dan
	 *         false apabila belum dipanggil
	 */
	public boolean spasiAktif() {
		return this.pencetakCantik;
	}

	/**
	 * Mengaktifkan lenient untuk membaca json
	 * 
	 * @see JsonPenulis#aturLenient(boolean)
	 * @return kelas ini
	 */
	public JsonID aktifkanLenient() {
		this.lenient = true;
		return this;
	}

	/**
	 * Memeriksa apakah lenient sudah aktif atau belum
	 * 
	 * @return true apa bila lenient sudah diaktifkan via
	 *         {@link #aktifkanLenient()} dan false apabila belum diaktifkan.
	 * 
	 */
	public boolean lenientAktif() {
		return lenient;
	}

	/**
	 * Meraih spasi yang digunakan untuk pencetak cantik
	 * 
	 * @return {@link #spasi} adalah nilai spasi, nilai awalnya adalah <b>"
	 *         "</b>
	 */
	public String raihSpasi() {
		return spasi;
	}

	/**
	 * Meraih nilai tag dari kunci yang digunakan
	 * 
	 * @param kunci
	 *            adalah kunci tag
	 * @return nama tag apabila kunci ditemuka, atau null apabila tidak
	 *         ditemukan
	 */
	public String raihTag(Class<?> kunci) {
		return kelasKeTag.raih(kunci);
	}

	/**
	 * Meraih nilai kunci dari nama tag yang digunakan
	 * 
	 * @param kunci
	 *            adalah kunci kelas
	 * @return kelas yang digunakan apabila kunci ditemukan, dan null apabila
	 *         tidak ditemukan
	 */
	public Class<?> raihKunci(String kunci) {
		return tagKekelas.raih(kunci);
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}
	 * 
	 * @param obyek
	 *            adalah obyek yang ingin di konversi ke json
	 * @return kelas ini
	 */
	public String keJson(Object obyek) {
		return this.keJson(obyek, obyek != null ? obyek.getClass() : null, (Class<?>) null);
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}
	 * 
	 * @param obyek
	 *            adalah obyek yang ingin di konversi ke json
	 * @param tipeDiketahui
	 *            adalah tipe yang di ketahui oleh user
	 * @return json berupa string
	 */
	public String keJson(Object obyek, Class<?> tipeDiketahui) {
		return this.keJson(obyek, tipeDiketahui, (Class<?>) null);
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}. untuk menghapus
	 * kelas tag anda bisa mengisi tipeDiketahui dengan kasus spesial.
	 * 
	 * @param obyek
	 *            adalah obyek yang ingin di konversi ke json
	 * @param tipeDiketahui
	 *            bisa berupa kasus spesial berupa {@link HashMap},
	 *            {@link PetaObyek}, {@link Larik}, {@link ArrayList} dan
	 *            lain-lain yang memiliki bermacam-macam nilai.
	 * @param tipeElement
	 *            adalah tipe yang di ketahui oleh user
	 * @return json berupa string
	 */
	public String keJson(Object obyek, Class<?> tipeDiketahui, Class<?> tipeElement) {
		StringWriter buff = new StringWriter();
		this.keJson(obyek, tipeDiketahui, tipeElement, buff);
		return buff.toString();
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}
	 * 
	 * @param nilai
	 *            adalah nilai obyek yang ingin di konversi ke json
	 * 
	 * @param berkas
	 *            adalah file output berupa {@link File} yang dipakai untuk
	 *            penyimpanan json
	 * 
	 */
	public void keJson(Object nilai, File berkas) {
		this.keJson(nilai, nilai != null ? nilai.getClass() : null, null, berkas);
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}
	 * 
	 * @param nilai
	 *            adalah obyek yang ingin di konversi ke json
	 * @param tipeDiketahui
	 *            bisa berupa kasus spesial berupa {@link HashMap},
	 *            {@link PetaObyek}, {@link Larik}, {@link ArrayList} dan
	 *            lain-lain yang memiliki bermacam-macam nilai *
	 * @param berkas
	 *            adalah file output berupa {@link File} yang dipakai untuk
	 *            penyimpanan json
	 */
	public void keJson(Object nilai, Class<?> tipeDiketahui, File berkas) {
		this.keJson(nilai, tipeDiketahui, null, berkas);
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}
	 * 
	 * @param nilai
	 *            adalah obyek yang ingin di konversi ke json
	 * @param penulis
	 *            adalah file output berupa {@link Writer} yang dipakai untuk
	 *            penyimpanan json
	 */
	public void keJson(Object nilai, Writer penulis) {
		this.keJson(nilai, nilai != null ? nilai.getClass() : null, null, penulis);
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}
	 * 
	 * @param obyek
	 *            adalah obyek yang ingin di konversi ke json
	 * @param tipeDiketahui
	 *            bisa berupa kasus spesial berupa {@link HashMap},
	 *            {@link PetaObyek}, {@link Larik}, {@link ArrayList} dan
	 *            lain-lain yang memiliki bermacam-macam nilai
	 * @param penulis
	 *            adalah file output berupa {@link Writer} yang dipakai untuk
	 *            penyimpanan json
	 */
	public void keJson(Object obyek, Class<?> tipeDiketahui, Writer penulis) {
		this.keJson(obyek, tipeDiketahui, null, penulis);
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}
	 * 
	 * @param obyek
	 *            adalah obyek yang ingin di konversi ke json
	 * @param tipeDiketahui
	 *            bisa berupa kasus spesial berupa {@link HashMap},
	 *            {@link PetaObyek}, {@link Larik}, {@link ArrayList} dan
	 *            lain-lain yang memiliki bermacam-macam nilai
	 * @param tipeElement
	 *            adalah element yang merupakan sebuah kelas
	 * @param berkas
	 *            adalah file output berupa {@link File} yang dipakai untuk
	 *            penyimpanan json
	 */
	public void keJson(Object obyek, Class<?> tipeDiketahui, Class<?> tipeElement, File berkas) {
		if (berkas == null)
			throw new NullPointerException("berkas = null");
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(berkas), "UTF-8");
			keJson(obyek, tipeDiketahui, tipeElement, writer);
		} catch (IOException e) {
			throw new JsonKesalahan("Kesalahan dalam menulis berkas : " + berkas);
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// ABAIKAN
				}
		}

	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String}
	 * 
	 * @param obyek
	 *            adalah obyek yang ingin di konversi ke json
	 * @param tipeDiketahui
	 *            bisa berupa kasus spesial berupa {@link HashMap},
	 *            {@link PetaObyek}, {@link Larik}, {@link ArrayList} dan
	 *            lain-lain yang memiliki bermacam-macam nilai
	 * @param tipeElement
	 *            adalah element yang merupakan sebuah kelas
	 * @param writer
	 *            adalah file output berupa {@link Writer} yang dipakai untuk
	 *            penyimpanan json
	 */
	public void keJson(Object obyek, Class<?> tipeDiketahui, Class<?> tipeElement, Writer writer) {
		try {
			this.aturPenulis(writer);
			this.tulisNilai(obyek, tipeDiketahui, tipeElement);
		} finally {
			if (this.penulis != null) {
				try {
					this.penulis.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	/**
	 * Menulis nilai json <b>Catatan</b> jangan menggunakan
	 * 
	 * @param nilai
	 *            hanya dapat digunakan untuk tipe primitif dan memiliki sifat
	 *            nullable
	 * @throws JsonKesalahan
	 *             saat menggunakan {@link #tulisMulaiObyek()} dan
	 *             {@link #tulisAkhirObyek()} saat menggunakan nilai dalam
	 *             method ini. untuk mengatasinya. harap gunakan
	 *             {@link #tulisNilai(String, Object)} yang memiliki parameter
	 *             nama
	 */
	public void tulisNilai(Object nilai) {
		if (nilai == null)
			tulisNilai(nilai, null, null);
		else
			tulisNilai(nilai, nilai.getClass(), null);
	}

	/**
	 * Menulis nilai json <b>Catatan</b> jangan menggunakan
	 * 
	 * @param nilai
	 *            hanya dapat digunakan untuk tipe primitif dan memiliki sifat
	 *            nullable
	 * @param tipeDiketahui
	 *            adalah tipe yang diketahui oleh user
	 * @throws JsonKesalahan
	 *             saat menggunakan {@link #tulisMulaiObyek()} dan
	 *             {@link #tulisAkhirObyek()} saat menggunakan nilai dalam
	 *             method ini.
	 */
	public void tulisNilai(Object nilai, Class<?> tipeDiketahui) {
		tulisNilai(nilai, tipeDiketahui, null);
	}

	/**
	 * Melakukan konversi {@link Object} ke json {@link String} dengan
	 * menggunakan nama
	 * 
	 * @param nama
	 *            adalah nama variable json
	 * @param nilai
	 *            adalah obyek yang ingin di konversi ke json
	 * @param tipeDiketahui
	 *            bisa berupa kasus spesial berupa {@link HashMap},
	 *            {@link PetaObyek}, {@link Larik}, {@link ArrayList} dan
	 *            lain-lain yang memiliki bermacam-macam nilai
	 * @param tipeElement
	 *            adalah element yang merupakan sebuah kelas
	 * 
	 */
	public void tulisNilai(String nama, Object nilai, Class<?> tipeDiketahui, Class<?> tipeElement) {
		penulis.nama(nama);
		tulisNilai(nilai, tipeDiketahui, tipeElement);
	}

	/**
	 * Mapping json untuk menulis nilai dari setiap variabel ini adalah kelas
	 * dasar yang membentuk method {@link #keJson} dan seluruh method lain yang
	 * berhubungan dengan serialize json
	 * 
	 * @param nilai
	 *            adalah inisialisasi nilai dari variabel
	 * @param tipeDiketahui
	 *            bisa berupa kasus spesial berupa {@link HashMap},
	 *            {@link PetaObyek}, {@link Larik}, {@link ArrayList} dan
	 *            lain-lain yang memiliki bermacam-macam nilai
	 * @param tipeElement
	 *            adalah element yang merupakan sebuah kelas
	 * 
	 */
	public void tulisNilai(Object nilai, Class<?> tipeDiketahui, Class<?> tipeElement) {
		try {
			if (nilai == null) {
				penulis.nilai(null);
				return;
			}
			if ((tipeDiketahui != null && tipeDiketahui.isPrimitive()) || tipeDiketahui == String.class
					|| tipeDiketahui == Integer.class || tipeDiketahui == Boolean.class || tipeDiketahui == Float.class
					|| tipeDiketahui == Long.class || tipeDiketahui == Double.class || tipeDiketahui == Short.class
					|| tipeDiketahui == Byte.class || tipeDiketahui == Character.class) {
				penulis.nilai(nilai);
				return;
			}
			Class<?> tipeAktual = nilai.getClass();
			if (tipeAktual.isPrimitive() || tipeAktual == String.class || tipeAktual == Integer.class
					|| tipeAktual == Boolean.class || tipeAktual == Float.class || tipeAktual == Long.class
					|| tipeAktual == Double.class || tipeAktual == Short.class || tipeAktual == Byte.class
					|| tipeAktual == Character.class) {
				tulisMulaiObyek(tipeAktual, null);
				tulisNilai("nilai", nilai);
				tulisAkhirObyek();
				return;
			}

			JsonSerializer<?> serializer = kelasSerializer.raih(tipeAktual);
			if (serializer != null) {
				serializer.tulis(this, nilai, tipeDiketahui);
				return;
			}

			if (nilai instanceof JsonSerializeable) {
				tulisMulaiObyek(tipeAktual, tipeDiketahui);
				((JsonSerializeable) nilai).tulis(this);
				tulisAkhirObyek();
				return;
			}
			// Handling Date with those SimpleDateFormat is so simple
			if (nilai instanceof Date) {
				if (tipeDiketahui == null)
					tipeDiketahui = Date.class;
				DateFormat format = new SimpleDateFormat(patternTanggal);
				penulis.nilai(format.format((Date) nilai));
				return;
			}

			// Kasus khusus penanganan larik
			if (nilai instanceof ArrayList) {
				if (tipeDiketahui != null && tipeAktual != tipeDiketahui && tipeAktual != ArrayList.class)
					throw new JsonKesalahan("Serialization larik selain dari tipe yang diketahui tidak didukung.\n"
							+ "Tipe diketahui : " + tipeDiketahui + "\nTipe aktual : " + tipeAktual);

				ArrayList<?> larik = (ArrayList<?>) nilai;
				tulisMulaiLarik();
				for (int i = 0; i < larik.size(); i++) {
					tulisNilai(((ArrayList<?>) nilai).get(i), tipeElement, null);
				}
				tulisAkhirLarik();
				if (debug)
					System.out.println("menulis via tipe " + tipeAktual.getName());
				return;
			}
			if (tipeAktual.isArray()) {
				if (tipeElement == null)
					tipeElement = tipeAktual.getComponentType();
				int len = Array.getLength(nilai);
				tulisMulaiLarik();
				for (int i = 0; i < len; i++) {
					tulisNilai(Array.get(nilai, i), tipeElement, null);
				}
				tulisAkhirLarik();
				if (debug)
					System.out.println("menulis via tipe " + tipeAktual.getName());
				return;
			}
			if (nilai instanceof Collection) {
				if (debug)
					System.out.println("menulis via " + nilai.getClass().getName());
				if (namaTipe != null && tipeAktual != ArrayList.class
						&& (tipeDiketahui == null || tipeDiketahui != tipeAktual)) {
					tulisMulaiObyek(namaTipe);
					tulisMulaiLarik("materi");
					for (Object item : (Collection<?>) nilai) {
						tulisNilai(item, tipeElement, null);
					}
					tulisAkhirLarik();
					tulisAkhirObyek();
				} else {
					tulisMulaiLarik();
					for (Object item : (Collection<?>) nilai) {
						tulisNilai(item, tipeElement, null);
					}
					tulisAkhirLarik();
				}
				return;
			}

			if (nilai instanceof Larik) {
				if (tipeDiketahui != null && tipeAktual != tipeDiketahui && tipeAktual != Larik.class)
					throw new JsonKesalahan("Serialization larik selain dari tipe yang diketahui tidak didukung.\n"
							+ "Tipe diketahui : " + tipeDiketahui + "\nTipe aktual : " + tipeAktual);
				if (debug)
					System.out.println("menulis via " + nilai.getClass().getName());
				Larik<?> larik = (Larik<?>) nilai;
				tulisMulaiLarik();
				for (int i = 0; i < larik.ukuran(); i++) {
					tulisNilai(((Larik<?>) nilai).raih(i), tipeElement, null);
				}
				tulisAkhirLarik();
				return;
			}
			if (nilai instanceof PetaObyek) {
				if (tipeDiketahui == null)
					tipeDiketahui = PetaObyek.class;
				tulisMulaiObyek(tipeAktual, tipeDiketahui);

				for (PetaObyek.Catat<?, ?> catat : ((PetaObyek<?, ?>) nilai).catatan()) {
					penulis.nama(konversiKeString(catat.kunci));
					tulisNilai(catat.nilai, tipeElement, null);
				}
				tulisAkhirObyek();
				return;
			}

			if (nilai instanceof Map<?, ?>) {
				if (tipeDiketahui == null)
					tipeDiketahui = HashMap.class;
				tulisMulaiObyek(tipeAktual, tipeDiketahui);
				for (Entry<?, ?> entry : ((Map<?, ?>) nilai).entrySet()) {
					penulis.nama(konversiKeString(entry.getKey()));
					tulisNilai(entry.getValue(), tipeElement, null);
				}
				tulisAkhirObyek();
				return;
			}

			// Enumuration will work too :D
			if (Enum.class.isAssignableFrom(tipeAktual)) {
				if (namaTipe != null && (tipeDiketahui == null || tipeDiketahui != tipeAktual)) {
					if (tipeAktual.getEnumConstants() == null)
						tipeAktual = tipeAktual.getSuperclass();
					tulisMulaiObyek(tipeAktual, null);
					penulis.nama("nilai");
					penulis.nilai(konversiKeString((Enum<?>) nilai));
					tulisAkhirObyek();
				} else {
					penulis.nilai(konversiKeString((Enum<?>) nilai));
				}
				return;
			}
			tulisMulaiObyek(tipeAktual, tipeDiketahui);
			tulisField(nilai);
			tulisAkhirObyek();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Menulis nilai json menggunakan nama dapat digunakan sebagai obyek
	 * 
	 * @param nama
	 *            adalah inisialisasi nama variabel
	 * @param nilai
	 *            adalah inisialisasi nilai dari variabel
	 */
	public void tulisNilai(String nama, Object nilai) {
		penulis.nama(nama);
		if (nilai == null)
			tulisNilai(nilai, null, null);
		else
			tulisNilai(nilai, nilai.getClass(), null);
	}

	/**
	 * Mengakhiri penulisan obyek json
	 * 
	 * @throws JsonKesalahan
	 *             saat anda belum memanggil {@link #tulisMulaiObyek()}
	 */
	public void tulisAkhirObyek() {
		try {
			penulis.akhirObyek();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Memulai penulisan obyek dengan nama harus berada dalam nested obyek.
	 * 
	 * @throws JsonKesalahan
	 *             saat nama tidak berada di dalam nested obyek
	 * @param nama
	 *            adalah inisialisasi nama obyek
	 */
	public void tulisMulaiObyek(String nama) {
		try {
			penulis.nama(nama);
		} catch (Exception ex) {
			throw new JsonKesalahan(ex);
		}
		tulisMulaiObyek();
	}

	/**
	 * Memulai penulisan obyek dengan nama harus berada dalam nested obyek.
	 * 
	 * @throws JsonKesalahan
	 *             saat nama tidak berada di dalam nested obyek
	 * 
	 */
	public void tulisMulaiObyek() {
		try {
			penulis.mulaiObyek();
		} catch (Exception ex) {
			throw new JsonKesalahan(ex);
		}
	}

	/**
	 * Memulai penulisan obyek dengan nama harus berada dalam nested obyek.
	 * 
	 * @throws JsonKesalahan
	 *             saat nama tidak berada di dalam nested obyek
	 * @param nama
	 *            adalah inisialisasi nama obyek
	 * @param tipeAktual
	 *            merupakan tipe aktual yang dapat dibaca melalui
	 *            {@link Object#getClass()}
	 * 
	 * @param tipeDiketahui
	 *            adalah tipe yang diketahui oleh user
	 */
	public void tulisMulaiObyek(String nama, Class<?> tipeAktual, Class<?> tipeDiketahui) {
		penulis.nama(nama);
		tulisMulaiObyek(tipeAktual, tipeDiketahui);
	}

	/**
	 * Memulai penulisan obyek dengan nama harus berada dalam nested obyek.
	 * 
	 * @throws JsonKesalahan
	 *             saat nama tidak berada di dalam nested obyek
	 * @param tipeAktual
	 *            merupakan tipe aktual yang dapat dibaca melalui
	 *            {@link Object#getClass()}
	 * 
	 * @param tipeDiketahui
	 *            adalah tipe yang diketahui oleh user
	 */
	public void tulisMulaiObyek(Class<?> tipeAktual, Class<?> tipeDiketahui) {
		try {
			penulis.mulaiObyek();
		} catch (Exception ex) {
			throw new JsonKesalahan(ex);
		}
		if (tipeDiketahui == null || tipeDiketahui != tipeAktual)
			tulisTipe(tipeAktual);
	}

	/**
	 * Memulai penulisan larik dengan nama harus berada dalam nested obyek.
	 * 
	 * @throws JsonKesalahan
	 *             saat nama tidak berada di dalam nested obyek
	 * 
	 * @param tipeAktual
	 *            merupakan tipe aktual yang dapat dibaca melalui
	 *            {@link Object#getClass()}
	 * 
	 * @param tipeDiketahui
	 *            adalah tipe yang diketahui oleh user
	 */
	public void tulisMulaiLarik(Class<?> tipeAktual, Class<?> tipeDiketahui) {
		try {
			penulis.mulaiLarik();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
		if (tipeDiketahui == null || tipeDiketahui != tipeAktual)
			tulisTipe(tipeAktual);
	}

	/**
	 * Memulai penulisan larik dengan nama harus berada dalam nested obyek atau.
	 * 
	 * @throws JsonKesalahan
	 *             saat nama tidak berada di dalam nested obyek atau larik
	 * @param nama
	 *            adalah inisialisasi nama larik
	 */
	public void tulisMulaiLarik(String nama) {
		try {
			penulis.nama(nama);
		} catch (Exception e) {
			throw new JsonKesalahan(e);
		}
		tulisMulaiLarik();
	}

	/**
	 * Memulai penulisan larik dengan nama harus berada dalam nested obyek.
	 * 
	 * @throws JsonKesalahan
	 *             saat nama tidak berada di dalam nested larik
	 */
	public void tulisMulaiLarik() {
		try {
			penulis.mulaiLarik();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Mengakhiri penulisan larik
	 */
	public void tulisAkhirLarik() {
		try {
			penulis.akhirLarik();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Menulis kelas tag untuk di gunakan dalam kelas saat tipe tidak diketahui
	 * 
	 * @param tipe
	 *            adalah tipe kelas
	 */
	public void tulisTipe(Class<?> tipe) {
		if (namaTipe == null)
			return;
		String className = raihTag(tipe);
		if (className == null)
			className = tipe.getName();
		try {
			penulis.atur(namaTipe, className);
		} catch (Exception ex) {
			throw new JsonKesalahan(ex);
		}
		if (debug)
			System.out.println("Menulis tipe: " + tipe.getName());
	}

	/**
	 * Deserialize json menggunakan {@link Reader} dan tipe
	 * 
	 * @param reader
	 *            adalah jenis pembaca seperti {@link BufferedReader} etc.
	 * @param tipe
	 *            adalah tipe dari kelas yang ingin di deserialize
	 * @param <T>
	 *            adalah obyek yang akan di deserialize
	 * @return T baca nilai json
	 */
	public <T> T dariJson(Reader reader, Class<?> tipe) {
		try {
			JsonPembaca pembaca = new JsonPembaca(reader);
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, null);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Deserialize json menggunakan {@link Reader} dan tipe
	 * 
	 * @param reader
	 *            adalah jenis pembaca seperti {@link BufferedReader} etc.
	 * @param tipe
	 *            adalah tipe dari kelas yang ingin di deserialize
	 * @param element
	 *            saat menggunakan kasus spesial seperti {@link ArrayList},
	 *            {@link Larik}, maka tipe diketahui adalah Larik.class atau
	 *            ArrayList.class dan element adalah kelas yang mau digunakan
	 * @param <T>
	 *            adalah obyek yang akan di deserialize
	 * @return T baca nilai json
	 */
	public <T> T dariJson(Reader reader, Class<?> tipe, Class<?> element) {
		try {
			JsonPembaca pembaca = new JsonPembaca(reader);
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, element);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Deserialize json menggunakan {@link Reader} dan tipe
	 * 
	 * @param stream
	 *            adalah jenis pembaca seperti {@link InputStreamReader} etc.
	 * @param tipe
	 *            adalah tipe dari kelas yang ingin di deserialize
	 * @param <T>
	 *            adalah obyek yang akan di deserialize
	 * @return T baca nilai json
	 */
	public <T> T dariJson(InputStream stream, Class<?> tipe) {
		try {
			JsonPembaca pembaca = new JsonPembaca(new InputStreamReader(stream));
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, null);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Deserialize json menggunakan {@link InputStream} dan tipe
	 * 
	 * @param stream
	 *            adalah jenis pembaca seperti {@link InputStreamReader},
	 *            {@link FileInputStream} etc.
	 * @param tipe
	 *            adalah tipe dari kelas yang ingin di deserialize
	 * @param element
	 *            saat menggunakan kasus spesial seperti {@link ArrayList},
	 *            {@link Larik}, maka tipe diketahui adalah Larik.class atau
	 *            ArrayList.class dan element adalah kelas yang mau digunakan
	 * @param <T>
	 *            adalah obyek yang akan di deserialize
	 * @return T baca nilai json
	 */
	public <T> T dariJson(InputStream stream, Class<?> tipe, Class<?> element) {
		try {
			JsonPembaca pembaca = new JsonPembaca(new InputStreamReader(stream));
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, element);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Deserialize json menggunakan {@link InputStream} dan tipe
	 * 
	 * @param berkas
	 *            adalah jenis pembaca seperti {@link File}, etc.
	 * @param tipe
	 *            adalah tipe dari kelas yang ingin di deserialize
	 * @param <T>
	 *            adalah obyek yang akan di deserialize
	 * @return T baca nilai json
	 */
	public <T> T dariJson(File berkas, Class<?> tipe) {
		try {
			JsonPembaca pembaca = new JsonPembaca(new InputStreamReader(new FileInputStream(berkas)));
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, null);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Deserialize json menggunakan {@link InputStream} dan tipe
	 * 
	 * @param berkas
	 *            adalah jenis pembaca seperti {@link InputStreamReader},
	 *            {@link FileInputStream} etc.
	 * @param tipe
	 *            adalah tipe dari kelas yang ingin di deserialize
	 * @param element
	 *            saat menggunakan kasus spesial seperti {@link ArrayList},
	 *            {@link Larik}, maka tipe diketahui adalah Larik.class atau
	 *            ArrayList.class dan element adalah kelas yang mau digunakan
	 * @param <T>
	 *            adalah obyek yang akan di deserialize
	 * @return T baca nilai json
	 */
	public <T> T dariJson(File berkas, Class<?> tipe, Class<?> element) {
		try {
			JsonPembaca pembaca = new JsonPembaca(new InputStreamReader(new FileInputStream(berkas)));
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, element);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	/**
	 * Deserialize json menggunakan {@link InputStream} dan tipe
	 * 
	 * @param json
	 *            adalah sebuah string json
	 * @param tipe
	 * 
	 *            adalah tipe dari kelas yang ingin di deserialize
	 * @param <T>
	 *            adalah obyek yang akan di deserialize
	 * @return T baca nilai json
	 */
	public <T> T dariJson(String json, Class<?> tipe) {
		return dariJson(json, tipe, null);
	}

	/**
	 * Deserialize json menggunakan {@link InputStream} dan tipe
	 * 
	 * @param json
	 *            adalah sebuah string json
	 * @param tipe
	 *            adalah tipe berupa kasus spesial seperti {@link ArrayList},
	 *            {@link Larik}, etc
	 * @param element
	 *            saat menggunakan kasus spesial seperti {@link ArrayList},
	 *            {@link Larik}, maka tipe diketahui adalah Larik.class atau
	 *            ArrayList.class dan element adalah kelas yang mau digunakan
	 * @param <T>
	 *            adalah obyek yang akan di deserialize
	 * @return T baca nilai json
	 */
	public <T> T dariJson(String json, Class<?> tipe, Class<?> element) {
		JsonPembaca pembaca = new JsonPembaca(new StringReader(json));
		pembaca.aturLenient(lenient);
		try {
			return bacaNilai(new JsonParser().parse(pembaca), tipe, element);
		} catch (IOException e) {
			throw new JsonKesalahan("gagal membaca json " + e);
		}
	}

	public <T> T bacaNilai(JsonElement bagian, String nama, Class<T> tipe, T manual) {
		JsonElement other = bagian.sebagaiObyek().raih(nama);
		if (other == null)
			return manual;
		return bacaNilai(bagian, tipe, null);
	}

	public <T> T bacaNilai(JsonElement bagian, String nama, Class<T> tipe) {
		return bacaNilai(bagian.sebagaiObyek().raih(nama), tipe, null);
	}

	public <T> T bacaNilai(JsonElement bagian, String nama, Class<T> tipe, Class<T> element) {
		return bacaNilai(bagian.sebagaiObyek().raih(nama), tipe, element);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T bacaNilai(JsonElement bagian, Class<?> tipe, Class<?> element) {
		if (bagian.iniObyek()) {
			JsonObyek jsonObyek = bagian.sebagaiObyek();
			String namaKelas = null;
			try {
				namaKelas = namaTipe == null ? null : jsonObyek.raih(namaTipe).sebagaiString();
				if (namaKelas != null) {
					jsonObyek.hapus(namaTipe);
					tipe = Class.forName(namaKelas);
				}
			} catch (ClassNotFoundException e) {
				throw new JsonKesalahan("kesalahan cating kelas :" + e);
			} catch (Exception e) {
				if (debug)
					System.out.println("Tidak menggunakan nama kelas");
			}
			if (tipe == String.class || tipe == Integer.class || tipe == Boolean.class || tipe == Float.class
					|| tipe == Long.class || tipe == Double.class || tipe == Short.class || tipe == Byte.class
					|| tipe == Character.class || Enum.class.isAssignableFrom(tipe)) {
				bacaNilai(bagian, "nilai", tipe);
			}
			if (tipe == null && Collections.class.isAssignableFrom(tipe)) {
				bagian = jsonObyek.raih("materi");
			} else {
				Object obyek = instant(tipe);
				JsonSerializer<?> serializer = kelasSerializer.raih(tipe);
				if (serializer != null) {
					return (T) serializer.baca(this, jsonObyek, tipe);
				}
				if (obyek instanceof JsonSerializeable) {
					((JsonSerializeable) obyek).baca(this, jsonObyek);
					return (T) obyek;
				}
				if (obyek instanceof PetaObyek) {
					PetaObyek hasil = (PetaObyek) obyek;
					for (PetaObyek.Catat<String, JsonElement> mencatat : jsonObyek.catatan()) {
						hasil.taruh(mencatat.kunci, bacaNilai(mencatat.nilai, element, null));
					}
					return (T) hasil;
				}
				if (obyek instanceof Map) {
					Map hasil = (Map) obyek;
					for (PetaObyek.Catat<String, JsonElement> mencatat : jsonObyek.catatan()) {
						hasil.put(mencatat.kunci, bacaNilai(mencatat.nilai, element, null));
					}
					return (T) hasil;
				}
				bacaField2(obyek, jsonObyek);
				return (T) obyek;
			}

		}
		if (tipe != null) {
			JsonSerializer<?> serializer = kelasSerializer.raih(tipe);
			if (serializer != null)
				return (T) serializer.baca(this, bagian, tipe);
		}
		if (bagian.iniLarik()) {
			if (debug)
				System.out.println("iniLarik :" + bagian.iniLarik());
			JsonLarik larik = bagian.sebagaiLarik();
			if (tipe == null || tipe == Object.class)
				tipe = (Class<T>) ArrayList.class;

			if (Collection.class.isAssignableFrom(tipe)) {
				Collection result = tipe.isInterface() ? new ArrayList() : (Collection) instant(tipe);
				Iterator<JsonElement> iterator = larik.iterator();
				while (iterator.hasNext()) {
					JsonElement jsonBagian = iterator.next();
					result.add(bacaNilai(jsonBagian, element, null));
				}
				return (T) result;
			}
			if (tipe.isArray()) {
				Class<?> komponen = tipe.getComponentType();
				if (element == null)
					element = komponen;
				Object result = Array.newInstance(komponen, larik.ukuran());
				Iterator<JsonElement> iterator = larik.iterator();
				int i = 0;
				while (iterator.hasNext()) {
					JsonElement jsonBagian = iterator.next();
					Array.set(result, i++, bacaNilai(jsonBagian, element, null));
				}
				return (T) result;
			}
			if (Larik.class.isAssignableFrom(tipe)) {
				Larik result = new Larik();
				Iterator<JsonElement> iterator = larik.iterator();
				while (iterator.hasNext()) {
					JsonElement jsonBagian = iterator.next();
					result.tambah(bacaNilai(jsonBagian, element, null));
				}
				return (T) result;
			}
			throw new JsonKesalahan(
					"gagal mengkonversi tipe: " + bagian.getClass().getSimpleName() + " (" + tipe.getName() + ")");
		}
		if (bagian.iniNilai()) {
			JsonNilai nilai = bagian.sebagaiNilai();
			if (nilai.iniString()) {
				if (debug)
					System.out.println("parsing string");
				String data = bagian.sebagaiString();
				if (tipe == null || tipe == String.class) {
					return ((T) data);
				}
				try {
					if (tipe == int.class || tipe == Integer.class)
						return (T) Integer.valueOf(data);
					if (tipe == float.class || tipe == Float.class)
						return (T) Float.valueOf(data);
					if (tipe == long.class || tipe == Long.class)
						return (T) Long.valueOf(data);
					if (tipe == double.class || tipe == Double.class)
						return (T) Double.valueOf(data);
					if (tipe == short.class || tipe == Short.class)
						return (T) Short.valueOf(data);
					if (tipe == byte.class || tipe == Byte.class)
						return (T) Byte.valueOf(data);
				} catch (NumberFormatException e) {
					throw new JsonKesalahan(e);
				}
				if (tipe == boolean.class || tipe == Boolean.class)
					return (T) Boolean.valueOf(data);
				if (tipe == char.class || tipe == Character.class)
					return (T) (Character) data.charAt(0);
				if (Enum.class.isAssignableFrom(tipe)) {
					Enum[] constants = (Enum[]) tipe.getEnumConstants();
					for (int i = 0, n = constants.length; i < n; i++) {
						Enum e = constants[i];
						if (data.equals(konversiKeString(e)))
							return (T) e;
					}
				}
			}

			if (nilai.iniAngka()) {
				try {
					if (debug)
						System.out.println("parse angka");
					if (tipe == null || tipe == float.class || tipe == Float.class)
						return (T) (Float) nilai.sebagaiFloat();
					if (tipe == int.class || tipe == Integer.class)
						return (T) (Integer) nilai.sebagaiInt();
					if (tipe == long.class || tipe == Long.class)
						return (T) (Long) nilai.sebagaiLong();
					if (tipe == double.class || tipe == Double.class)
						return (T) (Double) nilai.sebagaiDouble();
					if (tipe == short.class || tipe == Short.class)
						return (T) (Short) nilai.sebagaiShort();
					if (tipe == byte.class || tipe == Byte.class)
						return (T) (Byte) nilai.sebagaiByte();
					if (tipe == BigInteger.class)
						return (T) (BigInteger) nilai.sebagaiBigInteger();
					if (tipe == BigDecimal.class)
						return (T) (BigDecimal) nilai.sebagaiBigDecimal();
				} catch (NumberFormatException e) {
					// DO: unhandle exception
				}
				nilai = new JsonNilai(nilai.sebagaiString());
			}
			if (nilai.iniBoolean()) {
				if (tipe == null || tipe == boolean.class || tipe == Boolean.class)
					return (T) (Boolean) nilai.sebagaiBoolean();
			}
			Object obyek = instant(tipe);
			if (obyek instanceof Date) {
				return (T) (Date) obyek;
			}
		}
		return null;
	}

	public JsonID aturTipeElement(Class<?> tipe, String namaField, Class<?> element) {
		PetaOrder<String, FieldData> order = raihField2(tipe);
		FieldData data = order.raih(namaField);
		if (data == null)
			throw new JsonKesalahan("field gagal ditemukan : " + namaField + " dalam tipe :" + tipe.getName());
		data.element = element;
		return this;
	}

	public PetaOrder<String, FieldData> raihField2(Class<?> tipe) {
		PetaOrder<String, FieldData> fields = tipeKeFields.raih(tipe);
		if (fields != null)
			return fields;
		ArrayList<Field> allFields = new ArrayList<Field>();
		Class<?> nextClass = tipe;
		while (nextClass != Object.class) {
			Collections.addAll(allFields, nextClass.getDeclaredFields());
			nextClass = nextClass.getSuperclass();
		}

		PetaOrder<String, FieldData> namaKeFields = new PetaOrder<String, FieldData>();
		for (int i = 0; i < allFields.size(); i++) {
			Field field = allFields.get(i);
			int modofiers = field.getModifiers();
			if (Modifier.isTransient(modofiers))
				continue;
			if (Modifier.isStatic(modofiers))
				continue;
			if (Modifier.isSynchronized(modofiers))
				continue;

			if (!field.isAccessible()) {
				try {
					field.setAccessible(true);
				} catch (AccessControlException e) {
					continue;
				}
			}
			namaKeFields.taruh(field.getName(), new FieldData(field));
		}
		tipeKeFields.taruh(tipe, namaKeFields);

		return namaKeFields;
	}

	private boolean abaikanField = true;

	public void bacaField(Object obyek, JsonObyek jsonObyek, String namaField, String namaJson, Class<?> tipeElement) {
		Class<?> tipe = obyek.getClass();
		PetaOrder<String, FieldData> petaData = raihField2(tipe);
		FieldData data = petaData.raih(namaJson);
		if (data == null)
			throw new JsonKesalahan("data tidak ditemukan " + namaField + "(" + tipe.getName() + ")");
		if (tipeElement == null)
			tipeElement = data.element;
		bacaField(obyek, jsonObyek, data.field, namaJson, tipeElement);
	}

	public void bacaField(Object obyek, JsonObyek jsonObyek, Field field, String namaJson, Class<?> tipeElement) {
		JsonElement element = jsonObyek.raih(namaJson);
		if (element == null)
			return;
		Class<?> tipe = obyek.getClass();
		try {
			field.set(obyek, bacaNilai(element, field.getType(), tipeElement));
		} catch (IllegalAccessException e) {
			throw new JsonKesalahan("Error accessing field: " + field.getName() + " (" + tipe.getName() + ")", e);
		} catch (JsonKesalahan e) {
			e.addTrace(field.getName() + " (" + tipe.getName() + ")");
			throw e;
		}
	}

	public void bacaField2(Object obyek, JsonObyek jsonObyek) {
		Class<?> tipe = obyek.getClass();
		PetaOrder<String, FieldData> fields = raihField2(obyek.getClass());
		for (PetaObyek.Catat<String, JsonElement> bag : jsonObyek.catatan()) {
			String nama = bag.kunci;
			JsonElement anak = bag.nilai;
			FieldData data = fields.raih(nama);
			if (data == null) {
				if (abaikanField) {
					if (debug)
						System.out.println("Mengabaikan Field: " + nama + " (" + tipe.getName() + ")");
					continue;
				} else
					throw new JsonKesalahan("Field tidak di temukan: " + nama + " (" + tipe.getName() + ")");
			}
			Field field = data.field;
			try {
				if (debug)
					System.out.println("Field dibuat : " + nama + " (" + tipe.getName() + ")");
				field.set(obyek, bacaNilai(anak, field.getType(), data.element));
			} catch (IllegalAccessException e) {
				throw new JsonKesalahan("Gagal mengakse field: " + field.getName() + " (" + tipe.getName() + ")", e);
			} catch (JsonKesalahan e) {
				e.addTrace(field.getName() + " (" + tipe.getName() + ")");
				throw e;
			}

		}
	}

	public void tulisField(Object object) {
		Class<?> tipe = object.getClass();
		PetaOrder<String, FieldData> fields = raihField2(tipe);
		Object[] nilaiDefault = raihNilai2Default(tipe);
		int i = 0;
		for (FieldData fieldData : fields.nilaiPeta()) {
			Field field = fieldData.field;

			try {
				Object nilai = field.get(object);
				if (nilaiDefault != null) {
					Object defaultValue = nilaiDefault[i++];
					if (nilai == null && defaultValue == null)
						continue;
					if (nilai != null && defaultValue != null) {
						if (nilai.equals(defaultValue))
							continue;
						if (nilai.getClass().isArray() && defaultValue.getClass().isArray()) {
							equals1[0] = nilai;
							equals2[0] = defaultValue;
							if (Arrays.deepEquals(equals1, equals2))
								continue;
						}
					}
				}
				if (debug)
					System.out.println("Menulis field: " + field.getName() + " (" + tipe.getName() + ")");

				penulis.nama(field.getName());
				tulisNilai(nilai, field.getType(), fieldData.element);
			} catch (IllegalAccessException e) {
				throw new JsonKesalahan(e);
			}
		}
	}

	public Object[] raihNilai2Default(Class<?> tipe) {
		if (!prototipe)
			return null;

		if (kelasKeNilaiDefault.berisiKunci(tipe))
			return kelasKeNilaiDefault.raih(tipe);
		Object object = null;
		try {
			object = instant(tipe);
		} catch (Exception e) {
			kelasKeNilaiDefault.taruh(tipe, null);
		}

		PetaOrder<String, FieldData> fields = raihField2(tipe);
		Object[] values = new Object[fields.ukuran];
		kelasKeNilaiDefault.taruh(tipe, values);
		int i = 0;
		for (FieldData metadata : fields.nilaiPeta()) {
			Field field = metadata.field;
			try {
				values[i++] = field.get(object);
			} catch (IllegalAccessException ex) {
				throw new JsonKesalahan("Gagal mengakses field: " + field.getName() + " (" + tipe.getName() + ")", ex);
			} catch (JsonKesalahan ex) {
				ex.addTrace(field + " (" + tipe.getName() + ")");
				throw ex;
			} catch (RuntimeException runtimeEx) {
				JsonKesalahan ex = new JsonKesalahan("Konstruktor tidak dapat dibaca : " + field, runtimeEx);
				ex.addTrace(field + " (" + tipe.getName() + ")");
				throw ex;
			}
		}

		return values;
	}

	private String konversiKeString(Enum<?> e) {
		return namaEnum ? e.name() : e.toString();
	}

	private String konversiKeString(Object object) {
		if (object instanceof Enum)
			return konversiKeString((Enum<?>) object);
		if (object instanceof Class)
			return ((Class<?>) object).getName();
		return String.valueOf(object);
	}

	protected Object instant(Class<?> type) {
		try {
			return type.newInstance();
		} catch (Exception ex) {
			try {
				// Try a private constructor.
				// FIX BUG konstruktor pata type File
				if (type.equals(File.class)) {
					Constructor<?> constructor = type.getDeclaredConstructor(String.class);
					return constructor.newInstance("path");
				} else {
					Constructor<?> constructor = type.getDeclaredConstructor();
					constructor.setAccessible(true);
					return constructor.newInstance();
				}

			} catch (SecurityException ignored) {
			} catch (IllegalAccessException ignored) {
				if (Enum.class.isAssignableFrom(type)) {
					if (type.getEnumConstants() == null)
						type = type.getSuperclass();
					return type.getEnumConstants()[0];
				}
				if (type.isArray())
					throw new JsonKesalahan("Json tak terhitung ketika membaca tipe larik: " + type.getName(), ex);
				else if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers()))
					throw new JsonKesalahan("Kelas tidak dapat dibuat  (ini bukan (member-statis)): " + type.getName(),
							ex);
				else
					throw new JsonKesalahan("Kelas tidak tapat di buat tanpa Konstruktor\n " + "(" + type.getName()
							+ ") tidak memiliki kontruktor default", ex);
			} catch (Exception privateConstructorException) {
				ex = privateConstructorException;
			}

			throw new JsonKesalahan("Tidak dapat membuat instant untuk kelas : " + type.getName(), ex);
		}
	}

	static public class FieldData {
		Field field;
		Class<?> element;

		public FieldData(Field field) {
			this.field = field;
			Type genericTipe = field.getGenericType();
			if (genericTipe instanceof ParameterizedType) {
				Type[] aktualTipeArr = ((ParameterizedType) genericTipe).getActualTypeArguments();
				if (aktualTipeArr.length == 1) {
					Type aktualType = aktualTipeArr[0];
					if (aktualType instanceof Class)
						element = (Class<?>) aktualType;
					else if (aktualType instanceof ParameterizedType)
						element = (Class<?>) ((ParameterizedType) aktualType).getRawType();
				}
			}
		}
	}
}
