/**
 * 
 */
package org.raden.jsonid.utils;

/**
 * @author kiditz
 *
 */
public class LoadRDNNativesLibrary {

	static public void load() {
		new NativeImport("libs/raden-native.jar").memuat("raden");
	}
	
	public static void main(String[] args) {
		load();
	}
}
