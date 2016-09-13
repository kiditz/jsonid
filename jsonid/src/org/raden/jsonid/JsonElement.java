/**
 * 
 */
package org.raden.jsonid.utils.json;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Rifky A.B
 *
 */
public abstract class JsonElement {
	protected abstract JsonElement salin();

	public boolean iniObyek() {
		return this instanceof JsonObyek;
	}

	public boolean iniLarik() {
		return this instanceof JsonLarik;
	}

	public boolean iniNilai() {
		return this instanceof JsonNilai;
	}

	public boolean iniKosong() {
		return this instanceof JsonKosong;
	}

	public JsonObyek sebagaiObyek() {
		if (iniObyek())
			return (JsonObyek) this;
		else
			throw new JsonKesalahan("ini bukan obyek!" + this);
	}

	public JsonLarik sebagaiLarik() {
		if (iniLarik())
			return (JsonLarik) this;
		else
			throw new JsonKesalahan("ini bukan larik!" + this);
	}

	public JsonNilai sebagaiNilai() {
		if (iniNilai())
			return (JsonNilai) this;
		else
			throw new JsonKesalahan("ini bukan nilai!" + this);
	}

	public boolean sebagaiBoolean() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public String sebagaiString() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public Number sebagaiAngka() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public double sebagaiDouble() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @param indeks
	 * @return
	 */
	public char sebagaiKarakter(int indeks) {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public char sebagaiKarakter() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public long sebagaiLong() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public byte sebagaiByte() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public int sebagaiInt() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public short sebagaiShort() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public float sebagaiFloat() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public BigDecimal sebagaiBigDecimal() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public BigInteger sebagaiBigInteger() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
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
	public String pretyPrint() {
		StringWriter writer = new StringWriter();
		JsonPenulis penulis = new JsonPenulis(writer);
		penulis.aturIndent("	");
		JsonParser parser = new JsonParser();
		try {
			parser.tulis(penulis, this);
		} catch (IOException e) {
			throw new JsonKesalahan(e);
		}
		return writer.toString();
	}
}
