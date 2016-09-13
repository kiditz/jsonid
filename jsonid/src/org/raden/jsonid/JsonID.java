/**
 * 
 */
package org.raden.jsonid;

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
 * Untuk dapat menulis json anda dapat menggunakan
 * {@linkplain #keJson(Object)}<br>
 * 
 * contoh seperti dibawah ini :
 * 
 * <pre>
 * JsonID jsonId = JsonID.baru(); 
 * ObyekSaya obyek = new ObyekSaya();
 * System.out.println(jsonID.keJson(obyek));
 * 
 * <pre>
 * <p>
 * 
 * @author Rifky A.B
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
	private boolean debug = false;
	private boolean lenient = false;
	private boolean prototipe = false;

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
	 * @param serializer
	 *            the serializer to set
	 */

	public JsonID aturSerializer(Class<?> tipe, JsonSerializer<?> serializer) {
		this.kelasSerializer.taruh(tipe, serializer);
		return this;
	}

	public JsonID aktifkanPrototipe() {
		this.prototipe = true;
		return this;
	}

	public JsonID aturSpasi(String spasi) {
		this.spasi = spasi;
		return this;
	}

	/**
	 * @param patternTanggal
	 *            the patternTanggal to set
	 */
	public JsonID aturPatternTanggal(String patternTanggal) {
		this.patternTanggal = patternTanggal;
		return this;
	}

	private void aturPenulis(Writer writer) {
		// if (!(writer instanceof JsonPenulis))
		this.penulis = new JsonPenulis(writer);
		this.penulis.aturHtmlAman(false);
		this.penulis.aturLenient(lenient);
		this.penulis.aturSerializeNulls(true);
		if (pencetakCantik)
			this.penulis.aturIndent(spasi);
	}

	public JsonID aturTag(Class<?> kunci, String nilai) {
		kelasKeTag.taruh(kunci, nilai);
		tagKekelas.taruh(nilai, kunci);
		return this;
	}

	/**
	 * @param gunakanIndent
	 *            the gunakanIndent to set
	 */
	public JsonID aktifkanSpasi() {
		this.pencetakCantik = true;
		return this;
	}

	public boolean spasiAktif() {
		return this.pencetakCantik;
	}

	/**
	 * @param lenient
	 *            the lenient to set
	 */
	public JsonID aktifkanLenient() {
		this.lenient = true;
		return this;
	}

	public boolean lenientAktif() {
		return lenient;
	}

	public String raihSpasi() {
		return spasi;
	}

	public String raihTag(Class<?> type) {
		return kelasKeTag.raih(type);
	}

	public Class<?> raihKelas(String nama) {
		return tagKekelas.raih(nama);
	}

	public String keJson(Object obyek) {
		return this.keJson(obyek, obyek != null ? obyek.getClass() : null, (Class<?>) null);
	}

	public String keJson(Object obyek, Class<?> tipeDiketahui) {
		return this.keJson(obyek, tipeDiketahui, (Class<?>) null);
	}

	public String keJson(Object obyek, Class<?> tipeDiketahui, Class<?> tipeElement) {
		StringWriter buff = new StringWriter();
		this.keJson(obyek, tipeDiketahui, tipeElement, buff);
		return buff.toString();
	}

	public void keJson(Object obyek, File berkas) {
		this.keJson(obyek, obyek != null ? obyek.getClass() : null, null, berkas);
	}

	public void keJson(Object obyek, Class<?> tipeDiketahui, File berkas) {
		this.keJson(obyek, tipeDiketahui, null, berkas);
	}

	public void keJson(Object obyek, Writer penulis) {
		this.keJson(obyek, obyek != null ? obyek.getClass() : null, null, penulis);
	}

	public void keJson(Object obyek, Class<?> tipeDiketahui, Writer penulis) {
		this.keJson(obyek, tipeDiketahui, null, penulis);
	}

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

	public void tulisNilai(Object nilai) {
		if (nilai == null)
			tulisNilai(nilai, null, null);
		else
			tulisNilai(nilai, nilai.getClass(), null);
	}

	public void tulisNilai(Object nilai, Class<?> tipeDiketahui) {
		tulisNilai(nilai, tipeDiketahui, null);
	}

	public void tulisNilai(String nama, Object nilai, Class<?> tipeDiketahui, Class<?> tipeElement) {
		penulis.nama(nama);
		tulisNilai(nilai, tipeDiketahui, tipeElement);
	}

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
				// Do Not execute the next beybehhhh
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
				// Do Not execute the next beybehhhh
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
				// Do Not execute the next beybehhhh
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
				// Do Not execute the next beybehhhh
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
				if (tipeElement == null)
					throw new JsonKesalahan("Tipe Element tidak boloh kosong saat menulis via Larik");
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
					if (tipeElement == null) {
						throw new JsonKesalahan("tipeElement tidak boleh null");
					}
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

	public void tulisNilai(String name, Object nilai) {
		penulis.nama(name);
		if (nilai == null)
			tulisNilai(nilai, null, null);
		else
			tulisNilai(nilai, nilai.getClass(), null);
	}

	public void tulisAkhirObyek() {
		try {
			penulis.akhirObyek();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public void tulisMulaiObyek(String nama) {
		try {
			penulis.nama(nama);
		} catch (Exception ex) {
			throw new JsonKesalahan(ex);
		}
		tulisMulaiObyek();
	}

	public void tulisMulaiObyek() {
		try {
			penulis.mulaiObyek();
		} catch (Exception ex) {
			throw new JsonKesalahan(ex);
		}
	}

	public void tulisMulaiObyek(String nama, Class<?> tipeAktual, Class<?> tipeDiketahui) {
		penulis.nama(nama);
		tulisMulaiObyek(tipeAktual, tipeDiketahui);
	}

	public void tulisMulaiObyek(Class<?> tipeAktual, Class<?> tipeDiketahui) {
		try {
			penulis.mulaiObyek();
		} catch (Exception ex) {
			throw new JsonKesalahan(ex);
		}
		if (tipeDiketahui == null || tipeDiketahui != tipeAktual)
			tulisTipe(tipeAktual);
	}

	public void tulisMulaiLarik(Class<?> tipeAktual, Class<?> tipeDiketahui) {
		try {
			penulis.mulaiLarik();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
		if (tipeDiketahui == null || tipeDiketahui != tipeAktual)
			tulisTipe(tipeAktual);
	}

	public void tulisMulaiLarik(String nama) {
		try {
			penulis.nama(nama);
			penulis.mulaiLarik();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public void tulisMulaiLarik() {
		try {
			penulis.mulaiLarik();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public void tulisAkhirLarik() {
		try {
			penulis.akhirLarik();
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public void tulisTipe(Class<?> type) {
		if (namaTipe == null)
			return;
		String className = raihTag(type);
		if (className == null)
			className = type.getName();
		try {
			penulis.atur(namaTipe, className);
		} catch (Exception ex) {
			throw new JsonKesalahan(ex);
		}
		if (debug)
			System.out.println("Menulis tipe: " + type.getName());
	}

	public <T> T dariJson(Reader reader, Class<?> tipe) {
		try {
			JsonPembaca pembaca = new JsonPembaca(reader);
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, null);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public <T> T dariJson(Reader reader, Class<?> tipe, Class<?> element) {
		try {
			JsonPembaca pembaca = new JsonPembaca(reader);
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, element);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public <T> T dariJson(InputStream stream, Class<?> tipe) {
		try {
			JsonPembaca pembaca = new JsonPembaca(new InputStreamReader(stream));
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, null);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public <T> T dariJson(InputStream stream, Class<?> tipe, Class<?> element) {
		try {
			JsonPembaca pembaca = new JsonPembaca(new InputStreamReader(stream));
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, element);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public <T> T dariJson(File berkas, Class<?> tipe) {
		try {
			JsonPembaca pembaca = new JsonPembaca(new InputStreamReader(new FileInputStream(berkas)));
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, null);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public <T> T dariJson(File berkas, Class<?> tipe, Class<?> element) {
		try {
			JsonPembaca pembaca = new JsonPembaca(new InputStreamReader(new FileInputStream(berkas)));
			pembaca.aturLenient(lenient);
			return bacaNilai(new JsonParser().parse(pembaca), tipe, element);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
	}

	public <T> T dariJson(String json, Class<?> tipe) {
		return dariJson(json, tipe, null);
	}

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