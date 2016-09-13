/**
 * 
 */
package org.raden.jsonid.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.raden.jsonid.utils.koleksi.Larik;




/**
 * @author kiditz
 *
 */
public class NativeImport {
	static public boolean windows = System.getProperty("os.name").contains("Windows");
	static public boolean linux = System.getProperty("os.name").contains("Linux");
	static public boolean mac = System.getProperty("os.name").contains("Mac");
	static public boolean ios = false;
	static public boolean android = false;
	static public boolean ARM = System.getProperty("os.arch").startsWith("arm");
	static public boolean Bit64 = System.getProperty("os.arch").equals("amd64")
			|| System.getProperty("os.arch").equals("x86_64");

	// JDK 8 only.
	static public String abi = (System.getProperty("sun.arch.abi") != null ? System.getProperty("sun.arch.abi") : "");
	String berkasNative = null;
	private final Larik<String> namaPerpustakaan = new Larik<String>();

	/**
	 * 
	 */
	public NativeImport() {
	}

	public NativeImport(String berkasNative) {
		this.berkasNative = berkasNative;
	}

	static {
		String vm = System.getProperty("java.runtime.name");
		if (vm != null && vm.contains("Android Runtime")) {
			android = true;
			windows = false;
			linux = false;
			mac = false;
			Bit64 = false;
		}
		if (!android && !windows && !linux && !mac) {
			ios = true;
			Bit64 = false;
		}
	}

	public String crc(InputStream input) {
		if (input == null)
			throw new IllegalArgumentException("input cannot be null.");
		CRC32 crc = new CRC32();
		byte[] buffer = new byte[4096];
		try {
			while (true) {
				int length = input.read(buffer);
				if (length == -1)
					break;
				crc.update(buffer, 0, length);
			}
		} catch (Exception ex) {
			AliranArsip.tutup(input);
		}
		return Long.toString(crc.getValue(), 16);
	}

	public File ekstrakBerkas(String pathSumber, String lokasiDir) {

		try {
			File file = raihEkstrakan(lokasiDir, new File(pathSumber).getName());
			String crc = crc(bacaFile(pathSumber));
			if (lokasiDir == null)
				lokasiDir = crc;
			return ekstrakBerkas(pathSumber, crc, file);
		} catch (Exception e) {
		}
		return null;
	}

	public File ekstrakBerkas(String pathSumber, String crc, File lokasiEkstrak) {
		String ekstrakCrc = null;
		if (lokasiEkstrak.exists()) {
			try {
				ekstrakCrc = crc(new FileInputStream(lokasiEkstrak));
			} catch (FileNotFoundException e) {
			}
		}
		if (ekstrakCrc == null || !ekstrakCrc.equals(crc)) {
			InputStream in = bacaFile(pathSumber);
			lokasiEkstrak.getParentFile().mkdirs();
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(lokasiEkstrak);
				byte[] bytes = new byte[4096];
				int baca = -1;
				while ((baca = in.read(bytes)) != -1) {
					os.write(bytes, 0, baca);
				}

			} catch (IOException e) {
				throw new RadenKesalahanRuntime("kesalahan terjadi saat melakukan ekstrak " + pathSumber + " Ke "
						+ lokasiEkstrak.getAbsolutePath(), e);
			} finally {
				try {
					in.close();
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return lokasiEkstrak;
	}

	public synchronized void memuat(String namaLibrary) {
		if (namaLibrary == null) {
			throw new RadenKesalahanRuntime("namaLibrary tidak boleh kosong");
		}
		if (windows)
			namaLibrary = namaLibrary + (Bit64 ? "64.dll" : ".dll");
		if (linux)
			namaLibrary = "lib" + namaLibrary + (ARM ? "arm" + abi : "") + (Bit64 ? "64.so" : ".so");
		if (mac)
			namaLibrary = "lib" + namaLibrary + (Bit64 ? "64.dylib" : ".dylib");
		if (ios)
			return;
		if (namaPerpustakaan.berisi(namaLibrary))
			return;
		try {
			if (android)
				System.loadLibrary(namaLibrary);
			else
				muatBerkas(namaLibrary);
		} catch (Exception e) {
			throw new RadenKesalahanRuntime("Gagal memuat perpustakaan kode native " + namaLibrary
					+ " untuk sistem opersi " + System.getProperty("os.name"), e);
		}
		namaPerpustakaan.tambah(namaLibrary);
		namaPerpustakaan.sort();
	}

	public File raihEkstrakan(String dirName, String fileName) {
		File ideal = new File(
				System.getProperty("java.io.tmpdir") + "/rdn" + System.getProperty("user.name") + "/" + dirName,
				fileName);
		if (bisaDitulis(ideal))
			return ideal;
		try {
			File file = File.createTempFile(dirName, null);
			if (file.delete()) {
				file = new File(file, fileName);
				if (bisaDitulis(file))
					return file;
			}
		} catch (Exception e) {
		}
		File file = new File(System.getProperty("user.home") + "/.raden/" + dirName, fileName);
		if (bisaDitulis(file))
			return file;
		file = new File(".temp/" + dirName, fileName);
		if (bisaDitulis(file))
			return file;
		return ideal;
	}

	public boolean bisaDitulis(File file) {
		File orangTua = file.getParentFile();
		File test = null;
		if (file.exists()) {
			if (!file.canWrite() || bisaDiEksekusi(file))
				return false;
			test = new File(orangTua, UUID.randomUUID().toString());
		} else {
			orangTua.mkdirs();
			if (!orangTua.isDirectory())
				return false;
			test = file;
		}
		try {
			new FileOutputStream(test).close();
			if (!bisaDiEksekusi(test))
				return false;
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public boolean bisaDiEksekusi(File file) {
		try {
			Method canExecute = File.class.getMethod("canExecute");
			if ((Boolean) canExecute.invoke(file)) {
				return true;
			}

			Method setExecutable = File.class.getMethod("setExecutable", boolean.class, boolean.class);
			setExecutable.invoke(file, true, false);
			return (Boolean) canExecute.invoke(file);
		} catch (Exception e) {
			return false;
		}
	}

	public void muatBerkas(String pathSumber) {
		String crc = crc(bacaFile(pathSumber));
		String namaFile = new File(pathSumber).getName();
		File file = new File(
				System.getProperty("java.io.tmpdir") + "/rdn" + System.getProperty("user.name") + "/" + crc, namaFile);
		Throwable t = muatBerkas(pathSumber, crc, file);
		if (t == null)
			return;
		file = new File(".temp/" + crc, namaFile);
		try {
			file = File.createTempFile(pathSumber, crc, file);
			if (file.delete() && muatBerkas(pathSumber, crc, file) == null)
				return;
		} catch (Exception e) {
		}
		file = new File(System.getProperty("user.home") + "/.libgdx/" + crc, namaFile);
		if (muatBerkas(pathSumber, crc, file) == null)
			return;
		file = new File(".temp/" + pathSumber, namaFile);
		if (muatBerkas(pathSumber, crc, file) == null) {
			return;
		}

		file = new File(System.getProperty("java.library.path"), pathSumber);
		if (file.exists()) {
			System.load(file.getAbsolutePath());
			return;
		}
		throw new RadenKesalahanRuntime(t);
	}

	@SuppressWarnings("resource")
	public InputStream bacaFile(String path) {
		if (berkasNative == null) {
			InputStream in = NativeImport.class.getResourceAsStream("/" + path);
			if (in == null)
				throw new RadenKesalahanRuntime("gagal membaca berkas native");
			return in;
		}

		try {
			ZipFile zipFile = new ZipFile(berkasNative);
			ZipEntry entry = zipFile.getEntry(path);
			if (entry == null)
				throw new RadenKesalahanRuntime(
						"tidak dapat menemukan entry " + berkasNative + " dengan nama library " + path);
			return zipFile.getInputStream(entry);
		} catch (IOException e) {
			throw new RadenKesalahanRuntime(
					"kesalahan saat membaca file " + berkasNative + " dengan nama library " + path);
		}
	}

	public Throwable muatBerkas(String pathSumber, String crc, File lokasiEkstrak) {
		try {
			System.load(ekstrakBerkas(pathSumber, crc, lokasiEkstrak).getAbsolutePath());
			return null;
		} catch (Throwable e) {
			e.printStackTrace();
			return e;
		}
	}

	/**
	 * @return the namaPerpustakaan
	 */
	public Larik<String> raihNamaPerpustakaan() {
		return namaPerpustakaan;
	}
}
