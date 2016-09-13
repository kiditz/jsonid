/**
 * 
 */
package org.raden.jsonid.utils;

/**
 * @author Rifky A.B
 *
 */
public class Predictable {
	public static <T> T cekTidakNull(T obj) {
		if (obj == null) {
			throw new NullPointerException();
		}
		return obj;
	}

	public static void cekArgument(boolean condition) {
		if (!condition) {
			throw new IllegalArgumentException();
		}
	}
}
