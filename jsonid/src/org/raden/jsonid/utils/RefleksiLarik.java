/**
 * 
 */
package org.raden.jsonid.utils;

import java.lang.reflect.Array;

/**
 * @author Rifky Aditya Bastara
 *
 */
public final class RefleksiLarik {
	public static Object instantBaru(Class<?> c, int ukuran) {
		return Array.newInstance(c, ukuran);
	}

	public static int panjang(Object larik) {
		return Array.getLength(larik);
	}

	public static int ambil(Object larik, int indeks) {
		return Array.getInt(larik, indeks);
	}

	public static void atur(Object larik, int indeks, Object nilai) {
		Array.set(larik, indeks, nilai);
	}
}
