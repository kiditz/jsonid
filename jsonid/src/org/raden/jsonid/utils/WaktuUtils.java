/**
 * 
 */
package org.raden.jsonid.utils;

/**
 * @author Rifky A.B
 *
 */
public class WaktuUtils {
	public static long nanoDetik() {
		return System.nanoTime();
	}

	public static long milliDetik() {
		return System.currentTimeMillis();
	}

	public static long nanoKeMilliDetik(long nanoDetik) {
		long pembagi = 1000000;
		return nanoDetik / pembagi;
	}

	public static long milliKeNanoDetik(long milliDetik) {
		long pengali = 1000000;
		return milliDetik * pengali;
	}

	public static long waktuSejakNanoDetik(long waktuSebelumnya) {
		return nanoDetik() - waktuSebelumnya;
	}

	public static long waktuSejakMilliDetik(long waktuSebelumnya) {
		return milliDetik() - waktuSebelumnya;
	}
	
	

}
