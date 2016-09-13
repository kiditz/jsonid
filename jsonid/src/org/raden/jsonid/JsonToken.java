package org.raden.jsonid.utils.json;

public enum JsonToken {

	BEGIN_ARRAY,

	END_ARRAY,

	BEGIN_OBJECT,

	END_OBJECT,

	NAME,

	STRING,

	/**
	 * A JSON number represented in this API by a Java {@code double},
	 * {@code long}, or {@code int}.
	 */
	ANGKA,

	/**
	 * A JSON {@code true} or {@code false}.
	 */
	BOOLEAN,

	/**
	 * A JSON {@code null}.
	 */
	NULL,

	/**
	 * The end of the JSON stream. This sentinel value is returned by
	 * {@link JsonReader#peek()} to signal that the JSON-encoded value has no
	 * more tokens.
	 */
	END_DOCUMENT
}
