package com.harmoneye.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteConverter {

	// signed short to [-1; 1]
	private static final double shortToDouble = 2.0 / 0xffff;
	private static final float shortToFloat = 2.0f / 0xffff;

	public static void bytesToDoubles(byte[] bytes, double[] doubles,
		ByteOrder byteOrder) {
		int count = doubles.length;
		assert bytes.length == 2 * count;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(byteOrder);
		for (int i = 0; i < count && byteBuffer.hasRemaining(); i++) {
			doubles[i] = shortToDouble * byteBuffer.getShort();
		}
	}

	public static void littleEndianBytesToDoubles(byte[] bytes, double[] doubles) {
		bytesToDoubles(bytes, doubles, ByteOrder.LITTLE_ENDIAN);
	}

	public static void bigEndianBytesToDoubles(byte[] bytes, double[] doubles) {
		bytesToDoubles(bytes, doubles, ByteOrder.BIG_ENDIAN);
	}

	public static void bytesToFloats(byte[] bytes, float[] floats,
		ByteOrder byteOrder) {
		int count = floats.length;
		assert bytes.length == 2 * count;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(byteOrder);
		for (int i = 0; i < count && byteBuffer.hasRemaining(); i++) {
			floats[i] = shortToFloat * byteBuffer.getShort();
		}
	}

	public static void littleEndianBytesToFloats(byte[] bytes, float[] floats) {
		bytesToFloats(bytes, floats, ByteOrder.LITTLE_ENDIAN);
	}

	public static void bigEndianBytesToFloats(byte[] bytes, float[] floats) {
		bytesToFloats(bytes, floats, ByteOrder.BIG_ENDIAN);
	}

}
