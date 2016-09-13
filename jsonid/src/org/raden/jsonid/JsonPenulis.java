/**
 * 
 */
package org.raden.jsonid;

import java.io.IOException;
import java.io.Writer;

import org.raden.jsonid.utils.koleksi.LarikInt;



/**
 * @author Rifky A.B
 *
 */
public class JsonPenulis extends Writer {
	private final Writer output;
	private static final String[] PENGGANTI_KARAKTER;
	private static final String[] PENGGANTI_KARAKTER_HTML;
	private final LarikInt stack = new LarikInt(32);

	{
		push(JsonScope.DokumenKosong);
	}

	private String indent;

	private String separator = ":";

	private boolean lenient;

	private boolean htmlAman;

	private String deferredName;

	private boolean serializeNulls = true;
	static {
		PENGGANTI_KARAKTER = new String[128];
		for (int i = 0; i <= 0x1f; i++) {
			PENGGANTI_KARAKTER[i] = String.format("\\u%04x", (int) i);
		}
		PENGGANTI_KARAKTER['"'] = "\\\"";
		PENGGANTI_KARAKTER['\\'] = "\\\\";
		PENGGANTI_KARAKTER['\t'] = "\\t";
		PENGGANTI_KARAKTER['\b'] = "\\b";
		PENGGANTI_KARAKTER['\n'] = "\\n";
		PENGGANTI_KARAKTER['\r'] = "\\r";
		PENGGANTI_KARAKTER['\f'] = "\\f";
		PENGGANTI_KARAKTER_HTML = PENGGANTI_KARAKTER.clone();
		PENGGANTI_KARAKTER_HTML['<'] = "\\u003c";
		PENGGANTI_KARAKTER_HTML['>'] = "\\u003e";
		PENGGANTI_KARAKTER_HTML['&'] = "\\u0026";
		PENGGANTI_KARAKTER_HTML['='] = "\\u003d";
		PENGGANTI_KARAKTER_HTML['\''] = "\\u0027";
	}

	public void gantiAkhir(int atasBaru) {
		stack.aturAkhir(atasBaru);
	}

	public void push(int scope) {
		stack.tambah(scope);
	}

	public void aturSerializeNulls(boolean serializeNulls) {
		this.serializeNulls = serializeNulls;
	}

	public void aturIndent(String indent) {
		if (indent.length() == 0) {
			this.indent = null;
			this.separator = ":";
		} else {
			this.indent = indent;
			this.separator = ": ";
		}
	}

	/**
	 * @param lenient
	 *            the lenient to set
	 */
	public void aturLenient(boolean lenient) {
		this.lenient = lenient;
	}

	/**
	 * @param htmlSafe
	 *            the htmlSafe to set
	 */
	public void aturHtmlAman(boolean htmlAman) {
		this.htmlAman = htmlAman;
	}

	/**
	 * @return the htmlSafe
	 */
	public boolean iniHtmlAman() {
		return htmlAman;
	}

	/**
	 * @return the serializeNulls
	 */
	public boolean iniSerializeNulls() {
		return serializeNulls;
	}

	/**
	 * @return the lenient
	 */
	public boolean iniLenient() {
		return lenient;
	}

	public String getIndent() {
		return indent;
	}

	public void barisBaru() throws IOException {
		if (indent == null)
			return;
		output.write("\n");
		for (int i = 1; i < stack.ukuran(); i++) {
			output.write(indent);
		}
	}

	public void tulisDefName() throws IOException {
		if (deferredName != null) {
			sebelumNama();
			kata.atur(deferredName);
			// System.out.println(deferredName);
			deferredName = null;
		}
	}

	public JsonPenulis mulaiObyek() throws IOException {
		tulisDefName();
		return buka(JsonScope.ObyekKosong, "{");
	}

	public JsonPenulis akhirObyek() throws IOException {
		tulisDefName();
		return tutup(JsonScope.ObyekKosong, JsonScope.ObyekBerisi, "}");
	}

	public JsonPenulis mulaiLarik() throws IOException {
		tulisDefName();
		return buka(JsonScope.ArrayKosong, "[");
	}

	public JsonPenulis akhirLarik() throws IOException {
		tulisDefName();
		return tutup(JsonScope.ArrayKosong, JsonScope.ArrayBerisi, "]");
	}

	public void flush() throws IOException {
		output.flush();
	}

	public void tutup() throws IOException {
		output.close();
		if (stack.peek() != JsonScope.DokumenBerisi) {
			throw new IllegalArgumentException("Kesalahan Dokumen!!");
		}
		stack.bersih();
	}

	public JsonPenulis nama(String nama) {
		if (nama == null)
			throw new NullPointerException("Kata kunci kosong!");

		if (stack.ukuran() == 0)
			throw new IllegalStateException("apa anda sudah selesai?");
		this.deferredName = nama;
		return this;
	}

	public JsonPenulis atur(String nama, Object nilai) throws IOException {
		this.nama(nama).nilai(nilai);
		return this;
	}

	public JsonPenulis nilai(Object nilai) throws IOException {
		if (nilai == null)
			return nilaiKosong();
		tulisDefName();
		sebelumNilai();
		if (nilai instanceof Double) {
			Double val = (Double) nilai;
			if (Double.isNaN(val) || Double.isInfinite(val)) {
				throw new IllegalArgumentException(
						"Numeric values must be finite, but was " + val);
			}
			output.append(Double.toString((val)));
			return this;
		} else if (nilai instanceof Long) {
			output.append(Long.toString((Long) nilai));
			return this;
		} else if (nilai instanceof Number) {
			Number number = (Number) nilai;
			output.append(number.toString());
			return this;
		} else if (nilai instanceof Boolean) {
			Boolean boo = (Boolean) nilai;
			output.write('"' + boo.toString() + '"');
			return this;
		} else {
			kata.atur(String.valueOf(nilai));
			return this;
		}
	}

	private JsonPenulis buka(int kosong, String openBraket) throws IOException {
		sebelumNilai();
		push(kosong);
		output.write(openBraket);
		return this;
	}

	private JsonPenulis tutup(int kosong, int berisi, String closeBraket)
			throws IOException {
		int context = stack.peek();
		if (context != berisi && context != kosong) {
			throw new IllegalStateException("Nesting problem.");
		}
		if (deferredName != null) {
			throw new IllegalStateException("Penamaan " + JsonScope.Nama + "!");
		}
		stack.ukuranBerkurang();
		if (context == berisi)
			barisBaru();
		output.write(closeBraket);
		return this;
	}

	public void sebelumNama() throws IOException {
		int con = stack.peek();
		if (con == JsonScope.ObyekBerisi) {
			output.write(",");
		} else if (con != JsonScope.ObyekKosong) {
			throw new IllegalStateException("Nesting problem.");
		}
		barisBaru();
		gantiAkhir(JsonScope.Nama);
	}

	public void sebelumNilai() throws IOException {
		switch (stack.peek()) {
		case JsonScope.DokumenBerisi:
			if (!lenient) {
				throw new IllegalStateException(
						"Json harus memiliki top-level nilai");
			}
			// fall-through
		case JsonScope.DokumenKosong:
			gantiAkhir(JsonScope.DokumenBerisi);
			break;
		case JsonScope.ArrayKosong:
			gantiAkhir(JsonScope.ArrayBerisi);
			barisBaru();
			break;
		case JsonScope.ArrayBerisi:
			output.append(",");
			barisBaru();
			break;
		case JsonScope.Nama:
			output.append(separator);
			gantiAkhir(JsonScope.ObyekBerisi);
			break;
		default:
			throw new IllegalArgumentException("ada masalah nesting");
		}
	}

	public JsonPenulis nilaiKosong() throws IOException {
		if (deferredName != null) {
			if (serializeNulls) {
				tulisDefName();
			} else {
				deferredName = null;
				return this; // skip the name and the value
			}
		}
		sebelumNilai();
		output.write("null");
		return this;
	}

	private final AturKata kata;

	public JsonPenulis(Writer output) {
		this.output = output;
		this.kata = new AturKata();
	}

	public class AturKata {
		public void atur(String nilai) throws IOException {
			String[] arrpengganti = htmlAman ? PENGGANTI_KARAKTER_HTML
					: PENGGANTI_KARAKTER;
			output.write('"');
			int akhir = 0;
			int length = nilai.length();

			for (int i = 0; i < length; i++) {
				String pengganti = null;
				char kar = nilai.charAt(i);
				if (kar < 128) {
					pengganti = arrpengganti[kar];
					if (pengganti == null) {
						continue;
					}
				} else if (kar == '\u2028') {
					pengganti = "\u2028";
				} else if (kar == '\u2029') {
					pengganti = "\u2029";
				} else {
					continue;
				}
				if (akhir < i) {
					output.write(nilai, akhir, i - akhir);
				}
				output.write(pengganti);
				akhir = i + 1;
			}
			if (akhir < length) {
				output.write(nilai, akhir, length - akhir);
			}
			output.write('"');
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		output.write(cbuf, off, len);
	}

	@Override
	public void close() throws IOException {
		//tutup();
	}
}
