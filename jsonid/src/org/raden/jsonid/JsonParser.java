/**
 * 
 */
package org.raden.jsonid;

import java.io.IOException;
import java.io.Reader;

import org.raden.jsonid.utils.LazilyParsedNumber;
import org.raden.jsonid.utils.koleksi.PetaObyek.Catat;



/**
 * @author Rifky A.B
 *
 */
public class JsonParser {

	public JsonElement parse(Reader reader) {
		try {
			return parse(new JsonPembaca(reader));
		} catch (IOException e) {
			throw new JsonKesalahan("Kesalahan parsing " + e);
		}
	}

	public JsonElement parse(JsonPembaca pembaca) throws IOException {
		switch (pembaca.peek()) {
		case STRING:
			String string = pembaca.lanjutString();
			return new JsonNilai(string);
		case ANGKA:
			String nilai = pembaca.lanjutString();
			return new JsonNilai(new LazilyParsedNumber(nilai));
		case BOOLEAN:
			return new JsonNilai(pembaca.lanjutBoolean());
		case NULL:
			pembaca.lanjutKosong();
			return new JsonKosong();
		case BEGIN_ARRAY:
			pembaca.mulaiLarik();
			JsonLarik larik = new JsonLarik();
			while (pembaca.ketemu()) {
				larik.tambah(parse(pembaca));
			}
			pembaca.akhirLarik();
			return larik;
		case BEGIN_OBJECT:
			JsonObyek current = new JsonObyek();
			pembaca.mulaiObyek();
			while (pembaca.ketemu()) {
				current.tambah(pembaca.lanjutNama(), parse(pembaca));
			}
			pembaca.akhirObyek();
			return current;
		case END_DOCUMENT:
		case NAME:
		case END_OBJECT:
		case END_ARRAY:
		default:
			throw new IllegalArgumentException();
		}
	}

	public void tulis(JsonPenulis penulis, JsonElement element)
			throws IOException {
		if (element == null || element.iniKosong()) {
			penulis.nilaiKosong();
		} else if (element.iniNilai()) {
			JsonNilai nilai = element.sebagaiNilai();
			if (nilai.iniAngka()) {
				penulis.nilai(nilai.sebagaiAngka());
			} else if (nilai.iniBoolean()) {
				penulis.nilai(nilai.sebagaiBoolean());
			} else if (nilai.iniString()) {
				penulis.nilai(nilai.sebagaiString());
			}
		} else if (element.iniLarik()) {
			penulis.mulaiLarik();
			for (JsonElement el : element.sebagaiLarik()) {
				tulis(penulis, el);
			}
			penulis.akhirLarik();
		} else if (element.iniObyek()) {
			JsonObyek obyek = element.sebagaiObyek();
			penulis.mulaiObyek();
			for (Catat<String, JsonElement> mencatat : obyek.catatan()) {
				penulis.nama(mencatat.kunci);
				tulis(penulis, mencatat.nilai);
			}
			penulis.akhirObyek();
		}
	}
}
