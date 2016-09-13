package org.raden.jsonid.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.raden.jsonid.utils.koleksi.Larik;



public final class Penyangga {
	public static Larik<ByteBuffer> byteBerbahaya = new Larik<ByteBuffer>();
	private static int alokasiByteBerbahaya = 0;

	public static ByteBuffer buatPenyanggaByte(int jumlah) {
		ByteBuffer penyangga = ByteBuffer.allocateDirect(jumlah * 4);
		penyangga.order(ByteOrder.nativeOrder());
		return penyangga;
	}

	public static FloatBuffer buatPenyanggaFloat(int jumlah) {
		ByteBuffer penyangga = ByteBuffer.allocateDirect(jumlah * 4);
		penyangga.order(ByteOrder.nativeOrder());
		return penyangga.asFloatBuffer();
	}

	public static IntBuffer buatPenyanggaInteger(int jumlah) {
		ByteBuffer penyangga = ByteBuffer.allocateDirect(jumlah * 4);
		penyangga.order(ByteOrder.nativeOrder());
		return penyangga.asIntBuffer();
	}

	public static DoubleBuffer buatPenyanggaDouble(int jumlah) {
		ByteBuffer penyangga = ByteBuffer.allocateDirect(jumlah * 8);
		penyangga.order(ByteOrder.nativeOrder());
		return penyangga.asDoubleBuffer();
	}

	public static CharBuffer buatPenyanggaKarakter(int jumlah) {
		ByteBuffer penyangga = ByteBuffer.allocateDirect(jumlah * 2);
		penyangga.order(ByteOrder.nativeOrder());
		return penyangga.asCharBuffer();
	}

	public static ShortBuffer buatPenyanggaShort(int jumlah) {
		ByteBuffer penyangga = ByteBuffer.allocateDirect(jumlah * 2);
		penyangga.order(ByteOrder.nativeOrder());
		return penyangga.asShortBuffer();
	}

	public static LongBuffer buatPenyanggaLong(int jumlah) {
		ByteBuffer penyangga = ByteBuffer.allocateDirect(jumlah * 8);
		penyangga.order(ByteOrder.nativeOrder());
		return penyangga.asLongBuffer();
	}

	public static ByteBuffer buatByteBerbahaya(int kapasitas) {
		ByteBuffer penyangga = mallocByteBuffer(kapasitas);
		penyangga.order(ByteOrder.nativeOrder());
		alokasiByteBerbahaya += kapasitas;
		synchronized (byteBerbahaya) {
			byteBerbahaya.tambah(penyangga);
		}
		return penyangga;
	}

	public static void buangByteBerbahaya(ByteBuffer penyangga) {
		synchronized (byteBerbahaya) {
			boolean hapus = byteBerbahaya.hapus(penyangga, true);
			if (!hapus) {
				throw new RadenKesalahanRuntime("Gagal Menghapus byte berbahaya");
			}
		}
		alokasiByteBerbahaya -= penyangga.capacity();
		bebaskan(penyangga);
	}

	/**
	 * 
	 * */
	public static void salin(float[] sumber, Buffer tujuan, int jumlahFloat, int offset) {
		duplikasiJNI(sumber, tujuan, offset, jumlahFloat);
		tujuan.position(0);
		if (tujuan instanceof ByteBuffer)
			tujuan.limit(jumlahFloat << 2);
		else if (tujuan instanceof FloatBuffer)
			tujuan.limit(jumlahFloat);
	}

	public static void salin(byte[] sumber, int sumberOffset, Buffer tujuan,
			int jumlahElement) {
		duplikasiJNI(sumber, sumberOffset, tujuan, posisiDalamBytes(tujuan),
				jumlahElement);
		tujuan.limit(tujuan.position() + byteKeElements(tujuan, jumlahElement));
	}

	public static void salin(char[] sumber, int sumberOffset, Buffer tujuan,
			int jumlahElement) {
		duplikasiJNI(sumber, sumberOffset << 1, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 1);
		tujuan.limit(tujuan.position()
				+ byteKeElements(tujuan, jumlahElement << 1));
	}

	public static void salin(int[] sumber, int sumberOffset, Buffer tujuan,
			int jumlahElement) {
		duplikasiJNI(sumber, sumberOffset << 2, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 2);
		tujuan.limit(tujuan.position()
				+ byteKeElements(tujuan, jumlahElement << 2));
	}

	public static void salin(short[] sumber, int sumberOffset, Buffer tujuan,
			int jumlahElement) {
		duplikasiJNI(sumber, sumberOffset << 1, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 1);
		tujuan.limit(tujuan.position()
				+ byteKeElements(tujuan, jumlahElement << 1));
	}

	public static void salin(long[] sumber, int sumberOffset, Buffer tujuan,
			int jumlahElement) {
		duplikasiJNI(sumber, sumberOffset << 3, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 3);
		tujuan.limit(tujuan.position()
				+ byteKeElements(tujuan, jumlahElement << 3));
	}

	public static void salin(float[] sumber, int sumberOffset, Buffer tujuan,
			int jumlahElement) {
		duplikasiJNI(sumber, sumberOffset << 2, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 2);
		tujuan.limit(tujuan.position()
				+ byteKeElements(tujuan, jumlahElement << 2));
	}

	public static void salin(double[] sumber, int sumberOffset, Buffer tujuan,
			int jumlahElement) {
		duplikasiJNI(sumber, sumberOffset << 3, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 3);
		tujuan.limit(tujuan.position()
				+ byteKeElements(tujuan, jumlahElement << 3));
	}

	public static void salin(char[] sumber, int sumberOffset,
			int jumlahElement, Buffer tujuan) {
		duplikasiJNI(sumber, sumberOffset << 1, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 1);
	}

	public static void salin(int[] sumber, int sumberOffset, int jumlahElement,
			Buffer tujuan) {
		duplikasiJNI(sumber, sumberOffset << 2, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 2);
	}

	public static void salin(long[] sumber, int sumberOffset,
			int jumlahElement, Buffer tujuan) {
		duplikasiJNI(sumber, sumberOffset << 3, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 3);
	}

	public static void salin(float[] sumber, int sumberOffset,
			int jumlahElement, Buffer tujuan) {
		duplikasiJNI(sumber, sumberOffset << 2, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 2);
	}

	public static void salin(double[] sumber, int sumberOffset,
			int jumlahElement, Buffer tujuan) {
		duplikasiJNI(sumber, sumberOffset << 3, tujuan,
				posisiDalamBytes(tujuan), jumlahElement << 3);
	}

	public static void salin(Buffer sumber, Buffer tujuan, int numElements) {
		// TODO : salin dengan jni memcpy(dst + dstOffset, src + srcOffset,
		// numBytes);
		// int numBytes = elementKeByte(sumber, numElements);
		throw new RadenKesalahanRuntime(
				"Implementasi dulu mas rifky aditya bastara");
		// duplikasiJNI(sumber, posisiDalamBytes(sumber), tujuan,
		// posisiDalamBytes(tujuan), numBytes);
		// dst.limit(dst.position() + bytesToElements(dst, numBytes));
	}

	private static int posisiDalamBytes(Buffer penyangga) {
		if (penyangga instanceof ShortBuffer) {
			return penyangga.position() << 1;
		} else if (penyangga instanceof CharBuffer) {
			return penyangga.position() << 1;
		} else if (penyangga instanceof IntBuffer) {
			return penyangga.position() << 2;
		} else if (penyangga instanceof FloatBuffer) {
			return penyangga.position() << 2;
		} else if (penyangga instanceof LongBuffer) {
			return penyangga.position() << 3;
		} else if (penyangga instanceof DoubleBuffer) {
			return penyangga.position() << 3;
		} else if (penyangga instanceof ByteBuffer) {
			return penyangga.position();
		} else {
			throw new RadenKesalahanRuntime(
					"Tidak dapat menduplikasi penyangga >>> "
							+ penyangga.getClass().getName());
		}
	}

	private static int byteKeElements(Buffer penyangga, int bytes) {
		if (penyangga instanceof ByteBuffer)
			return bytes;
		else if (penyangga instanceof ShortBuffer)
			return bytes >>> 1;
		else if (penyangga instanceof CharBuffer)
			return bytes >>> 1;
		else if (penyangga instanceof IntBuffer)
			return bytes >>> 2;
		else if (penyangga instanceof LongBuffer)
			return bytes >>> 3;
		else if (penyangga instanceof FloatBuffer)
			return bytes >>> 2;
		else if (penyangga instanceof DoubleBuffer)
			return bytes >>> 3;
		else
			throw new RadenKesalahanRuntime("Can't copy to a "
					+ penyangga.getClass().getName() + " instance");
	}

	private static int elementKeByte(Buffer penyangga, int bytes) {
		if (penyangga instanceof ShortBuffer) {
			return bytes << 1;
		} else if (penyangga instanceof CharBuffer) {
			return bytes << 1;
		} else if (penyangga instanceof IntBuffer) {
			return bytes << 2;
		} else if (penyangga instanceof FloatBuffer) {
			return bytes << 2;
		} else if (penyangga instanceof LongBuffer) {
			return bytes << 3;
		} else if (penyangga instanceof DoubleBuffer) {
			return bytes << 3;
		} else if (penyangga instanceof ByteBuffer) {
			return bytes;
		} else {
			throw new RadenKesalahanRuntime(
					"Tidak dapat menduplikasi penyangga >>> "
							+ penyangga.getClass().getName());
		}
	}

	public static int alokasiByteBerbahaya() {
		return alokasiByteBerbahaya;
	}

	public static long alamatByteBerbahaya(Buffer penyangga) {
		return penyangga.position() + ambilAlamatPenyangga(penyangga);
	}

	private static native void duplikasiJNI(int[] src, int srcOffset,
			Buffer dst, int dstOffset, int numBytes);

	private static native void duplikasiJNI(float[] src, int srcOffset,
			Buffer dst, int dstOffset, int numBytes);

	private static native void duplikasiJNI(double[] src, int srcOffset,
			Buffer dst, int dstOffset, int numBytes);

	private static native void duplikasiJNI(short[] src, int asal, Buffer dst,
			int dstOffset, int numBytes);

	private static native void duplikasiJNI(char[] src, int srcOffset,
			Buffer dst, int dstOffset, int numBytes);

	private static native void duplikasiJNI(long[] src, int srcOffset,
			Buffer dst, int dstOffset, int numBytes);

	private static native void duplikasiJNI(byte[] src, int srcOffset,
			Buffer dst, int dstOffset, int numBytes);

	private static native void duplikasiJNI(float[] src, Buffer dst,
			int offset, int jumlah);

	private static native void bersihkan(ByteBuffer penyangga, int jumlah);

	private static native ByteBuffer mallocByteBuffer(int kapasitas);

	private static native void bebaskan(ByteBuffer penyangga);

	private static native long ambilAlamatPenyangga(Buffer penyangga);
}
