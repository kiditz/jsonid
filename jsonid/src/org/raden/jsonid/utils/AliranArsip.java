/**
 * 
 */
package org.raden.jsonid.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Rifky Aditya Bastara
 *
 */
public class AliranArsip {
	public static final int UKURAN_PENYANGGA_DEFAULT = 8192;
	public static final byte[] BYTE_KOSONG = new byte[0];

	public static void salinAliran(InputStream masukan, OutputStream keluaran) {
		salinAliran(masukan, keluaran, UKURAN_PENYANGGA_DEFAULT);
	}

	public static void salinAliran(InputStream masukan, OutputStream keluaran,
			int ukuran) {
		byte[] penyangga = new byte[ukuran];
		int baca;
		try {
			while (((baca = masukan.read(penyangga)) != -1)) {
				keluaran.write(penyangga, 0, baca);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] salinAliranKeLarikByte(InputStream masukan,
			int estimasiUkuran) {
		ByteArrayOutputStream keluaran = new OptimalkanLarikByteKeluaran(
				Math.max(0, estimasiUkuran));
		salinAliran(masukan, keluaran);
		return keluaran.toByteArray();
	}

	public static void tutup(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				throw new RadenKesalahanRuntime("Ada yang Tidak bisa di tutup : "
						+ e);
			}
		}

	}

	public static class OptimalkanLarikByteKeluaran extends
			ByteArrayOutputStream {

		public OptimalkanLarikByteKeluaran(int ukuran) {
			super(ukuran);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.ByteArrayOutputStream#toByteArray()
		 */
		@Override
		public synchronized byte[] toByteArray() {
			if (count == buf.length) {
				return buf;
			} else {
				return super.toByteArray();
			}
		}

	}
}
