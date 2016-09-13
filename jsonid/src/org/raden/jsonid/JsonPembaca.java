/**
 * 
 */
package org.raden.jsonid;

import java.io.IOException;
import java.io.Reader;

import org.raden.jsonid.utils.RadenKesalahanRuntime;



/**
 * @author Rifky A.B
 *
 */
public class JsonPembaca {
	private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();
	private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;

	private static final int PEEKED_NONE = 0;
	private static final int PEEKED_BEGIN_OBJECT = 1;
	private static final int PEEKED_END_OBJECT = 2;
	private static final int PEEKED_BEGIN_ARRAY = 3;
	private static final int PEEKED_END_ARRAY = 4;
	private static final int PEEKED_TRUE = 5;
	private static final int PEEKED_FALSE = 6;
	private static final int PEEKED_NULL = 7;
	private static final int PEEKED_SINGLE_QUOTED = 8;
	private static final int PEEKED_DOUBLE_QUOTED = 9;
	private static final int PEEKED_UNQUOTED = 10;
	/** When this is returned, the string value is stored in peekedString. */
	private static final int PEEKED_BUFFERED = 11;
	private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
	private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
	private static final int PEEKED_UNQUOTED_NAME = 14;
	/** When this is returned, the integer value is stored in peekedLong. */
	private static final int PEEKED_LONG = 15;
	private static final int PEEKED_NUMBER = 16;
	private static final int PEEKED_EOF = 17;

	/* State machine when parsing numbers */
	private static final int NUMBER_CHAR_NONE = 0;
	private static final int NUMBER_CHAR_SIGN = 1;
	private static final int NUMBER_CHAR_DIGIT = 2;
	private static final int NUMBER_CHAR_DECIMAL = 3;
	private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
	private static final int NUMBER_CHAR_EXP_E = 5;
	private static final int NUMBER_CHAR_EXP_SIGN = 6;
	private static final int NUMBER_CHAR_EXP_DIGIT = 7;

	/** The input JSON. */
	private final Reader in;

	/** True to accept non-spec compliant JSON */
	private boolean lenient = false;

	/**
	 * Use a manual buffer to easily read and unread upcoming characters, and
	 * also so we can create strings without an intermediate StringBuilder. We
	 * decode literals directly out of this buffer, so it must be at least as
	 * long as the longest token that can be reported as a number.
	 */
	private final char[] buffer = new char[1024];
	private int pos = 0;
	private int limit = 0;

	private int lineNumber = 0;
	private int lineStart = 0;

	private int peeked = PEEKED_NONE;

	/**
	 * A peeked value that was composed entirely of digits with an optional
	 * leading dash. Positive values may not have a leading 0.
	 */
	private long peekedLong;

	/**
	 * The number of characters in a peeked number literal. Increment 'pos' by
	 * this after reading a number.
	 */
	private int peekedNumberLength;

	/**
	 * A peeked string that should be parsed on the next double, long or string.
	 * This is populated before a numeric value is parsed and used if that
	 * parsing fails.
	 */
	private String peekedString;

	/*
	 * The nesting stack. Using a manual array rather than an ArrayList saves
	 * 20%.
	 */
	private int[] stack = new int[32];
	private int ukuranStack = 0;
	{
		stack[ukuranStack++] = JsonScope.DokumenKosong;
	}

	private String[] pathNames = new String[32];
	private int[] pathIndices = new int[32];

	public JsonPembaca(Reader in) {
		this.in = in;
	}

	/**
	 * @param lenient
	 *            the lenient to set
	 */
	public void aturLenient(boolean lenient) {
		this.lenient = lenient;
	}

	/**
	 * @return the lenient
	 */
	public boolean iniLenient() {
		return lenient;
	}

	public void push(int newTop) {
		if (ukuranStack == stack.length) {
			int[] newStack = new int[ukuranStack * 2];
			int[] newPathIndices = new int[ukuranStack * 2];
			String[] newPathNames = new String[ukuranStack * 2];
			System.arraycopy(stack, 0, newStack, 0, ukuranStack);
			System.arraycopy(pathIndices, 0, newPathIndices, 0, ukuranStack);
			System.arraycopy(pathNames, 0, newPathNames, 0, ukuranStack);
			stack = newStack;
			pathIndices = newPathIndices;
			pathNames = newPathNames;
		}
		stack[ukuranStack++] = newTop;
	}

	/**
	 * @return the in
	 */
	public Reader getIn() {
		return in;
	}

	public void tutup() throws IOException {
		peeked = PEEKED_NONE;
		stack[0] = JsonScope.TutupAjaUdah;
		ukuranStack = 1;
		in.close();
	}

	public void mulaiObyek() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_BEGIN_OBJECT) {
			push(JsonScope.ObyekKosong);
			peeked = PEEKED_NONE;
		} else {
			throw new JsonKesalahan("Kesalahan mulai obyek karena " + peek()
					+ " dibaris " + getLineNumber() + " kolum "
					+ getColumnNumber() + " path " + getPath());
		}
	}

	public boolean ketemu() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		return p != PEEKED_END_OBJECT && p != PEEKED_END_ARRAY;
	}

	public void lanjutKosong() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_NULL) {
			peeked = PEEKED_NONE;
			pathIndices[ukuranStack - 1]++;
		} else {
			throw new IllegalStateException("Kesalahan kosong  " + peek()
					+ " at line " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
	}

	public String lanjutNama() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		String result;
		if (p == PEEKED_UNQUOTED_NAME) {
			result = raihBukanNilaiKata();
		} else if (p == PEEKED_SINGLE_QUOTED_NAME) {
			result = raihNilaiKata('\'');
		} else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
			result = raihNilaiKata('"');
		} else {
			throw new IllegalStateException("Expected a name but was " + peek()
					+ " at line " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
		peeked = PEEKED_NONE;
		pathNames[ukuranStack - 1] = result;
		return result;
	}

	public long lanjutInt() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_LONG) {
			peeked = PEEKED_NONE;
			pathIndices[ukuranStack - 1]++;
			return peekedLong;
		}
		if (p == PEEKED_NUMBER) {
			peekedString = new String(buffer, pos, peekedNumberLength);
			pos += peekedNumberLength;
		} else if (p == PEEKED_UNQUOTED) {
			peekedString = raihBukanNilaiKata();
		} else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
			peekedString = raihNilaiKata(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
		} else if (p != PEEKED_BUFFERED) {
			throw new IllegalStateException("Expected a double but was "
					+ peek() + " at line " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
		peeked = PEEKED_BUFFERED;
		int res = Integer.parseInt(peekedString);
		if (!lenient && (Double.isNaN(res) || Double.isInfinite(res))) {
			throw new NumberFormatException(
					"JSON tidak mendukung nan dan infinite pada angka double: "
							+ res + " di baris " + getLineNumber() + " kolum "
							+ getColumnNumber() + " path " + getPath());

		}
		peekedString = null;
		peeked = PEEKED_NONE;
		pathIndices[ukuranStack - 1]++;
		return res;
	}

	public String toString() {
		return getClass().getSimpleName() + " at line " + getLineNumber()
				+ " column " + getColumnNumber();
	}

	public boolean lanjutBoolean() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_TRUE) {
			peeked = PEEKED_NONE;
			pathIndices[ukuranStack - 1]++;
			return true;
		} else if (p == PEEKED_FALSE) {
			peeked = PEEKED_NONE;
			pathIndices[ukuranStack - 1]++;
			return false;
		}
		throw new IllegalStateException("Expected a boolean but was " + peek()
				+ " at line " + getLineNumber() + " column "
				+ getColumnNumber() + " path " + getPath());
	}

	public long lanjutLong() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_LONG) {
			peeked = PEEKED_NONE;
			pathIndices[ukuranStack - 1]++;
			return peekedLong;
		}
		if (p == PEEKED_NUMBER) {
			peekedString = new String(buffer, pos, peekedNumberLength);
			pos += peekedNumberLength;
		} else if (p == PEEKED_UNQUOTED) {
			peekedString = raihBukanNilaiKata();
		} else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
			peekedString = raihNilaiKata(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
		} else if (p != PEEKED_BUFFERED) {
			throw new IllegalStateException("Expected a double but was "
					+ peek() + " at line " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
		peeked = PEEKED_BUFFERED;
		long res = Long.parseLong(peekedString);
		if (!lenient && (Double.isNaN(res) || Double.isInfinite(res))) {
			throw new NumberFormatException(
					"JSON tidak mendukung nan dan infinite pada angka double: "
							+ res + " di baris " + getLineNumber() + " kolum "
							+ getColumnNumber() + " path " + getPath());

		}
		peekedString = null;
		peeked = PEEKED_NONE;
		pathIndices[ukuranStack - 1]++;
		return res;
	}

	public double lanjutDouble() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_LONG) {
			peeked = PEEKED_NONE;
			pathIndices[ukuranStack - 1]++;
			return peekedLong;
		}
		if (p == PEEKED_NUMBER) {
			peekedString = new String(buffer, pos, peekedNumberLength);
			pos += peekedNumberLength;
		} else if (p == PEEKED_UNQUOTED) {
			peekedString = raihBukanNilaiKata();
		} else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
			peekedString = raihNilaiKata(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
		} else if (p != PEEKED_BUFFERED) {
			throw new IllegalStateException("Expected a double but was "
					+ peek() + " at line " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
		peeked = PEEKED_BUFFERED;
		double res = Double.parseDouble(peekedString);
		if (!lenient && (Double.isNaN(res) || Double.isInfinite(res))) {
			throw new NumberFormatException(
					"JSON tidak mendukung nan dan infinite pada angka double: "
							+ res + " di baris " + getLineNumber() + " kolum "
							+ getColumnNumber() + " path " + getPath());

		}
		peekedString = null;
		peeked = PEEKED_NONE;
		pathIndices[ukuranStack - 1]++;

		return res;
	}

	public String lanjutString() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		String result;
		if (p == PEEKED_UNQUOTED) {
			result = raihBukanNilaiKata();
		} else if (p == PEEKED_SINGLE_QUOTED) {
			result = raihNilaiKata('\'');
		} else if (p == PEEKED_DOUBLE_QUOTED) {
			result = raihNilaiKata('"');
		} else if (p == PEEKED_BUFFERED) {
			result = peekedString;
			peekedString = null;
		} else if (p == PEEKED_LONG) {
			result = Long.toString(peekedLong);
		} else if (p == PEEKED_NUMBER) {
			result = new String(buffer, pos, peekedNumberLength);
			pos += peekedNumberLength;
		} else {
			throw new IllegalStateException("Expected a string but was "
					+ peek() + " at line " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
		peeked = PEEKED_NONE;
		pathIndices[ukuranStack - 1]++;
		return result;
	}

	public void akhirObyek() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_END_OBJECT) {
			ukuranStack--;
			pathNames[ukuranStack] = null; // Free the last path name so that it
											// can be garbage collected!
			pathIndices[ukuranStack - 1]++;
			peeked = PEEKED_NONE;
		} else {
			throw new IllegalStateException("Kesalahan Mulai_Obyek karena "
					+ peek() + " di baris " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
	}

	public void mulaiLarik() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_BEGIN_ARRAY) {
			push(JsonScope.ArrayKosong);
			pathIndices[ukuranStack - 1] = 0;
			peeked = PEEKED_NONE;
		} else {
			throw new IllegalStateException("Expected BEGIN_ARRAY but was "
					+ peek() + " at line " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
	}

	public void akhirLarik() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}
		if (p == PEEKED_END_ARRAY) {
			ukuranStack--;
			pathIndices[ukuranStack - 1]++;
			peeked = PEEKED_NONE;
		} else {
			throw new IllegalStateException("Expected END_ARRAY but was "
					+ peek() + " at line " + getLineNumber() + " column "
					+ getColumnNumber() + " path " + getPath());
		}
	}

	private String raihBukanNilaiKata() throws IOException {
		StringBuilder builder = null;
		int i = 0;
		findNonLiteralCharacter: while (true) {
			for (; pos + i < limit; i++) {
				switch (buffer[pos + i]) {
				case '/':
				case '\\':
				case ';':
				case '#':
				case '=':
					lenientCek(); // fall-through
				case '{':
				case '}':
				case '[':
				case ']':
				case ':':
				case ',':
				case ' ':
				case '\t':
				case '\f':
				case '\r':
				case '\n':
					break findNonLiteralCharacter;
				}
			}

			// Attempt to load the entire literal into the buffer at once.
			if (i < buffer.length) {
				if (isiBuffer(i + 1)) {
					continue;
				} else {
					break;
				}
			}

			// use a StringBuilder when the value is too long. This is too long
			// to be a number!
			if (builder == null) {
				builder = new StringBuilder();
			}
			builder.append(buffer, pos, i);
			pos += i;
			i = 0;
			if (!isiBuffer(1)) {
				break;
			}
		}

		String result;
		if (builder == null) {
			result = new String(buffer, pos, i);
		} else {
			builder.append(buffer, pos, i);
			result = builder.toString();
		}
		pos += i;
		return result;
	}

	private String raihNilaiKata(char quote) throws IOException {
		// Like nextNonWhitespace, this uses locals 'p' and 'l' to save
		// inner-loop field access.
		char[] buffer = this.buffer;
		StringBuilder builder = new StringBuilder();
		while (true) {
			int p = pos;
			int l = limit;
			int start = p;
			while (p < l) {
				int c = buffer[p++];

				if (c == quote) {
					pos = p;
					builder.append(buffer, start, p - start - 1);
					return builder.toString();
				} else if (c == '\\') {
					pos = p;
					builder.append(buffer, start, p - start - 1);
					builder.append(readEscapeCharacter());
					p = pos;
					l = limit;
					start = p;
				} else if (c == '\n') {
					lineNumber++;
					lineStart = p;
				}
			}

			builder.append(buffer, start, p - start);
			pos = p;
			if (!isiBuffer(1)) {
				throw syntaxError("Unterminated string");
			}
		}
	}

	public boolean iniObyek() throws IOException {
		return peek() == JsonToken.BEGIN_OBJECT;
	}

	public boolean iniLarik() throws IOException {
		return peek() == JsonToken.BEGIN_ARRAY;
	}

	public boolean iniNama() throws IOException {
		return peek() == JsonToken.NAME;
	}

	public boolean iniString() throws IOException {
		return peek() == JsonToken.STRING;
	}

	public boolean iniAngka() throws IOException {
		return peek() == JsonToken.STRING;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public JsonToken peek() throws IOException {
		int p = peeked;
		if (p == PEEKED_NONE) {
			p = lakukanPeek();
		}

		switch (p) {
		case PEEKED_BEGIN_OBJECT:
			return JsonToken.BEGIN_OBJECT;
		case PEEKED_END_OBJECT:
			return JsonToken.END_OBJECT;
		case PEEKED_BEGIN_ARRAY:
			return JsonToken.BEGIN_ARRAY;
		case PEEKED_END_ARRAY:
			return JsonToken.END_ARRAY;
		case PEEKED_SINGLE_QUOTED_NAME:
		case PEEKED_DOUBLE_QUOTED_NAME:
		case PEEKED_UNQUOTED_NAME:
			return JsonToken.NAME;
		case PEEKED_TRUE:
		case PEEKED_FALSE:
			return JsonToken.BOOLEAN;
		case PEEKED_NULL:
			return JsonToken.NULL;
		case PEEKED_SINGLE_QUOTED:
		case PEEKED_DOUBLE_QUOTED:
		case PEEKED_UNQUOTED:
		case PEEKED_BUFFERED:
			return JsonToken.STRING;
		case PEEKED_LONG:
		case PEEKED_NUMBER:
			return JsonToken.ANGKA;
		case PEEKED_EOF:
			return JsonToken.END_DOCUMENT;
		default:
			throw new AssertionError("Gagal Melakukan Peek");
		}
	}

	public int lakukanPeek() throws IOException {
		int peekStack = stack[ukuranStack - 1];
		if (peekStack == JsonScope.ArrayKosong) {
			stack[ukuranStack - 1] = JsonScope.ArrayBerisi;
		} else if (peekStack == JsonScope.ArrayBerisi) {
			// Look for a comma before the next element.
			int c = nextNonWhitespace(true);
			switch (c) {
			case ']':
				return peeked = PEEKED_END_ARRAY;
			case ';':
				lenientCek(); // fall-through
			case ',':
				break;
			default:
				throw syntaxError("Unterminated array");
			}
		} else if (peekStack == JsonScope.ObyekKosong
				|| peekStack == JsonScope.ObyekBerisi) {
			stack[ukuranStack - 1] = JsonScope.Nama;
			// Look for a comma before the next element.
			if (peekStack == JsonScope.ObyekBerisi) {
				int c = nextNonWhitespace(true);
				switch (c) {
				case '}':
					return peeked = PEEKED_END_OBJECT;
				case ';':
					lenientCek();
					; // fall-through
				case ',':
					break;
				default:
					throw syntaxError("Unterminated object");
				}
			}
			int c = nextNonWhitespace(true);
			switch (c) {
			case '"':
				return peeked = PEEKED_DOUBLE_QUOTED_NAME;
			case '\'':
				lenientCek();
				return peeked = PEEKED_SINGLE_QUOTED_NAME;
			case '}':
				if (peekStack != JsonScope.ObyekBerisi) {
					return peeked = PEEKED_END_OBJECT;
				} else {
					throw syntaxError("Expected name");
				}
			default:
				lenientCek();
				pos--; // Don't consume the first character in an unquoted
						// string.
				if (isLiteral((char) c)) {
					return peeked = PEEKED_UNQUOTED_NAME;
				} else {
					throw syntaxError("Expected name");
				}
			}
		} else if (peekStack == JsonScope.Nama) {
			stack[ukuranStack - 1] = JsonScope.ObyekBerisi;
			// Look for a colon before the value.
			int c = nextNonWhitespace(true);
			switch (c) {
			case ':':
				break;
			case '=':
				lenientCek();
				if ((pos < limit || isiBuffer(1)) && buffer[pos] == '>') {
					pos++;
				}
				break;
			default:
				throw syntaxError("Expected ':'");
			}
		} else if (peekStack == JsonScope.DokumenKosong) {
			if (lenient) {
				consumeNonExecutePrefix();
			}
			stack[ukuranStack - 1] = JsonScope.DokumenBerisi;
		} else if (peekStack == JsonScope.DokumenBerisi) {
			int c = nextNonWhitespace(false);
			if (c == -1) {
				return peeked = PEEKED_EOF;
			} else {
				lenientCek();
				pos--;
			}
		} else if (peekStack == JsonScope.TutupAjaUdah) {
			throw new IllegalStateException("JsonReader is closed");
		}

		int c = nextNonWhitespace(true);
		switch (c) {
		case ']':
			if (peekStack == JsonScope.ArrayKosong) {
				return peeked = PEEKED_END_ARRAY;
			}
			// fall-through to handle ",]"
		case ';':
		case ',':
			// In lenient mode, a 0-length literal in an array means 'null'.
			if (peekStack == JsonScope.ArrayKosong
					|| peekStack == JsonScope.ArrayBerisi) {
				lenientCek();
				pos--;
				return peeked = PEEKED_NULL;
			} else {
				throw syntaxError("Unexpected value");
			}
		case '\'':
			lenientCek();
			return peeked = PEEKED_SINGLE_QUOTED;
		case '"':
			return peeked = PEEKED_DOUBLE_QUOTED;
		case '[':
			return peeked = PEEKED_BEGIN_ARRAY;
		case '{':
			return peeked = PEEKED_BEGIN_OBJECT;
		default:
			pos--; // Don't consume the first character in a literal value.
		}

		int result = peekKeyword();
		if (result != PEEKED_NONE) {
			return result;
		}

		result = peekNumber();
		if (result != PEEKED_NONE) {
			return result;
		}

		if (!isLiteral(buffer[pos])) {
			throw syntaxError("Expected value");
		}

		lenientCek();
		return peeked = PEEKED_UNQUOTED;
	}

	private int peekKeyword() throws IOException {
		// Figure out which keyword we're matching against by its first
		// character.
		char c = buffer[pos];
		String keyword;
		String keywordUpper;
		int peeking;
		if (c == 't' || c == 'T') {
			keyword = "true";
			keywordUpper = keyword.toUpperCase();
			peeking = PEEKED_TRUE;
		} else if (c == 'f' || c == 'F') {
			keyword = "false";
			keywordUpper = keyword.toUpperCase();
			peeking = PEEKED_FALSE;
		} else if (c == 'k' || c == 'K') {
			keyword = "kosong";
			keywordUpper = keyword.toUpperCase();
			peeking = PEEKED_NULL;
		} else {
			return PEEKED_NONE;
		}

		// Confirm that chars [1..length) match the keyword.
		int length = keyword.length();
		for (int i = 1; i < length; i++) {
			if (pos + i >= limit && !isiBuffer(i + 1)) {
				return PEEKED_NONE;
			}
			c = buffer[pos + i];
			if (c != keyword.charAt(i) && c != keywordUpper.charAt(i)) {
				return PEEKED_NONE;
			}
		}

		if ((pos + length < limit || isiBuffer(length + 1))
				&& isLiteral(buffer[pos + length])) {
			return PEEKED_NONE; // Don't match trues, falsey or nullsoft!
		}

		// We've found the keyword followed either by EOF or by a non-literal
		// character.
		pos += length;
		return peeked = peeking;
	}

	private int peekNumber() throws IOException {
		// Like nextNonWhitespace, this uses locals 'p' and 'l' to save
		// inner-loop field access.
		char[] buffer = this.buffer;
		int p = pos;
		int l = limit;

		long value = 0; // Negative to accommodate Long.MIN_VALUE more easily.
		boolean negative = false;
		boolean fitsInLong = true;
		int last = NUMBER_CHAR_NONE;

		int i = 0;

		charactersOfNumber: for (; true; i++) {
			if (p + i == l) {
				if (i == buffer.length) {
					// Though this looks like a well-formed number, it's too
					// long to continue reading. Give up
					// and let the application handle this as an unquoted
					// literal.
					return PEEKED_NONE;
				}
				if (!isiBuffer(i + 1)) {
					break;
				}
				p = pos;
				l = limit;
			}

			char c = buffer[p + i];
			switch (c) {
			case '-':
				if (last == NUMBER_CHAR_NONE) {
					negative = true;
					last = NUMBER_CHAR_SIGN;
					continue;
				} else if (last == NUMBER_CHAR_EXP_E) {
					last = NUMBER_CHAR_EXP_SIGN;
					continue;
				}
				return PEEKED_NONE;

			case '+':
				if (last == NUMBER_CHAR_EXP_E) {
					last = NUMBER_CHAR_EXP_SIGN;
					continue;
				}
				return PEEKED_NONE;

			case 'e':
			case 'E':
				if (last == NUMBER_CHAR_DIGIT
						|| last == NUMBER_CHAR_FRACTION_DIGIT) {
					last = NUMBER_CHAR_EXP_E;
					continue;
				}
				return PEEKED_NONE;

			case '.':
				if (last == NUMBER_CHAR_DIGIT) {
					last = NUMBER_CHAR_DECIMAL;
					continue;
				}
				return PEEKED_NONE;

			default:
				if (c < '0' || c > '9') {
					if (!isLiteral(c)) {
						break charactersOfNumber;
					}
					return PEEKED_NONE;
				}
				if (last == NUMBER_CHAR_SIGN || last == NUMBER_CHAR_NONE) {
					value = -(c - '0');
					last = NUMBER_CHAR_DIGIT;
				} else if (last == NUMBER_CHAR_DIGIT) {
					if (value == 0) {
						return PEEKED_NONE; // Leading '0' prefix is not allowed
											// (since it could be octal).
					}
					long newValue = value * 10 - (c - '0');
					fitsInLong &= value > MIN_INCOMPLETE_INTEGER
							|| (value == MIN_INCOMPLETE_INTEGER && newValue < value);
					value = newValue;
				} else if (last == NUMBER_CHAR_DECIMAL) {
					last = NUMBER_CHAR_FRACTION_DIGIT;
				} else if (last == NUMBER_CHAR_EXP_E
						|| last == NUMBER_CHAR_EXP_SIGN) {
					last = NUMBER_CHAR_EXP_DIGIT;
				}
			}
		}

		// We've read a complete number. Decide if it's a PEEKED_LONG or a
		// PEEKED_NUMBER.
		if (last == NUMBER_CHAR_DIGIT && fitsInLong
				&& (value != Long.MIN_VALUE || negative)) {
			peekedLong = negative ? value : -value;
			pos += i;
			return peeked = PEEKED_LONG;
		} else if (last == NUMBER_CHAR_DIGIT
				|| last == NUMBER_CHAR_FRACTION_DIGIT
				|| last == NUMBER_CHAR_EXP_DIGIT) {
			peekedNumberLength = i;
			return peeked = PEEKED_NUMBER;
		} else {
			return PEEKED_NONE;
		}
	}

	private void consumeNonExecutePrefix() throws IOException {
		// fast forward through the leading whitespace
		nextNonWhitespace(true);
		pos--;

		if (pos + NON_EXECUTE_PREFIX.length > limit
				&& !isiBuffer(NON_EXECUTE_PREFIX.length)) {
			return;
		}

		for (int i = 0; i < NON_EXECUTE_PREFIX.length; i++) {
			if (buffer[pos + i] != NON_EXECUTE_PREFIX[i]) {
				return; // not a security token!
			}
		}

		// we consumed a security token!
		pos += NON_EXECUTE_PREFIX.length;
	}

	private void lenientCek() {
		if (!lenient)
			throw new RadenKesalahanRuntime(
					"atur lenient ke true untuk memperoleh banyak baris json");
	}

	private char readEscapeCharacter() throws IOException {
		if (pos == limit && !isiBuffer(1)) {
			throw syntaxError("Unterminated escape sequence");
		}

		char escaped = buffer[pos++];
		switch (escaped) {
		case 'u':
			if (pos + 4 > limit && !isiBuffer(4)) {
				throw syntaxError("Unterminated escape sequence");
			}
			// Equivalent to Integer.parseInt(stringPool.get(buffer, pos, 4),
			// 16);
			char result = 0;
			for (int i = pos, end = i + 4; i < end; i++) {
				char c = buffer[i];
				result <<= 4;
				if (c >= '0' && c <= '9') {
					result += (c - '0');
				} else if (c >= 'a' && c <= 'f') {
					result += (c - 'a' + 10);
				} else if (c >= 'A' && c <= 'F') {
					result += (c - 'A' + 10);
				} else {
					throw new NumberFormatException("\\u"
							+ new String(buffer, pos, 4));
				}
			}
			pos += 4;
			return result;

		case 't':
			return '\t';

		case 'b':
			return '\b';

		case 'n':
			return '\n';

		case 'r':
			return '\r';

		case 'f':
			return '\f';

		case '\n':
			lineNumber++;
			lineStart = pos;
			// fall-through

		case '\'':
		case '"':
		case '\\':
		default:
			return escaped;
		}
	}

	private boolean isLiteral(char c) throws IOException {
		switch (c) {
		case '/':
		case '\\':
		case ';':
		case '#':
		case '=':
			lenientCek();
		case '{':
		case '}':
		case '[':
		case ']':
		case ':':
		case ',':
		case ' ':
		case '\t':
		case '\f':
		case '\r':
		case '\n':
			return false;
		default:
			return true;
		}
	}

	int nextNonWhitespace(boolean throwOnEof) throws IOException {
		/*
		 * This code uses ugly local variables 'p' and 'l' representing the
		 * 'pos' and 'limit' fields respectively. Using locals rather than
		 * fields saves a few field reads for each whitespace character in a
		 * pretty-printed document, resulting in a 5% speedup. We need to flush
		 * 'p' to its field before any (potentially indirect) call to
		 * fillBuffer() and reread both 'p' and 'l' after any (potentially
		 * indirect) call to the same method.
		 */
		char[] buffer = this.buffer;
		int p = pos;
		int l = limit;
		while (true) {
			if (p == l) {
				pos = p;
				if (!isiBuffer(1)) {
					break;
				}
				p = pos;
				l = limit;
			}

			int c = buffer[p++];
			if (c == '\n') {
				lineNumber++;
				lineStart = p;
				continue;
			} else if (c == ' ' || c == '\r' || c == '\t') {
				continue;
			}

			if (c == '/') {
				pos = p;
				if (p == l) {
					pos--; // push back '/' so it's still in the buffer when
							// this method returns
					boolean charsLoaded = isiBuffer(2);
					pos++; // consume the '/' again
					if (!charsLoaded) {
						return c;
					}
				}

				lenientCek();
				char peek = buffer[pos];
				switch (peek) {
				case '*':
					// skip a /* c-style comment */
					pos++;
					if (!skipTo("*/")) {
						throw syntaxError("Unterminated comment");
					}
					p = pos + 2;
					l = limit;
					continue;

				case '/':
					// skip a // end-of-line comment
					pos++;
					lewatiEof();
					p = pos;
					l = limit;
					continue;

				default:
					return c;
				}
			} else if (c == '#') {
				pos = p;
				/*
				 * Skip a # hash end-of-line comment. The JSON RFC doesn't
				 * specify this behaviour, but it's required to parse existing
				 * documents. See http://b/2571423.
				 */
				lenientCek();
				lewatiEof();
				p = pos;
				l = limit;
			} else {
				pos = p;
				return c;
			}
		}
		if (throwOnEof) {
			throw new JsonKesalahan(
					"Anda yakin ini json? kesalahan ada di baris "
							+ getLineNumber());
		} else {
			return -1;
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private IOException syntaxError(String message) {
		throw new JsonKesalahan(message + " dibaris " + getLineNumber()
				+ " kolum " + getColumnNumber() + " path " + getPath());
	}

	public String getPath() {
		StringBuilder result = new StringBuilder().append('$');
		for (int i = 0, size = ukuranStack; i < size; i++) {
			switch (stack[i]) {
			case JsonScope.ArrayKosong:
			case JsonScope.ArrayBerisi:
				result.append('[').append(pathIndices[i]).append(']');
				break;

			case JsonScope.ObyekKosong:
			case JsonScope.Nama:
			case JsonScope.ObyekBerisi:
				result.append('.');
				if (pathNames[i] != null) {
					result.append(pathNames[i]);
				}
				break;

			case JsonScope.DokumenBerisi:
			case JsonScope.DokumenKosong:
			case JsonScope.TutupAjaUdah:
				break;
			}
		}
		return result.toString();
	}

	int getLineNumber() {
		return lineNumber + 1;
	}

	int getColumnNumber() {
		return pos - lineStart + 1;
	}

	/**
	 * @param string
	 * @return
	 * @throws IOException
	 */
	private boolean skipTo(String toFind) throws IOException {
		outer: for (; pos + toFind.length() <= limit
				|| isiBuffer(toFind.length()); pos++) {
			if (buffer[pos] == '\n') {
				lineNumber++;
				lineStart = pos + 1;
				continue;
			}
			for (int c = 0; c < toFind.length(); c++) {
				if (buffer[pos + c] != toFind.charAt(c)) {
					continue outer;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @throws IOException
	 * 
	 */
	private void lewatiEof() throws IOException {
		while (pos < limit || isiBuffer(1)) {
			char c = buffer[pos++];
			if (c == '\n') {
				lineNumber++;
				lineStart = pos;
				break;
			} else if (c == '\r') {
				break;
			}
		}
	}

	private boolean isiBuffer(int min) throws IOException {
		char[] buffer = this.buffer;
		lineStart -= pos;
		if (limit != pos) {
			limit -= pos;
			System.arraycopy(buffer, pos, buffer, 0, limit);
		} else {
			limit = 0;
		}
		pos = 0;
		int total = 0;

		while ((total = in.read(buffer, limit, buffer.length - limit)) != -1) {
			limit += total;
			if (lineNumber == 0 && lineStart == 0 && limit > 0
					&& buffer[0] == '\ufeff') {
				++pos;
				++lineStart;
				++min;
			}
			if (limit >= min)
				return true;
		}
		return false;
	}
}