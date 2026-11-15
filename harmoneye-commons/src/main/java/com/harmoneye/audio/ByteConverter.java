package com.harmoneye.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteConverter {

	// signed short to [-1; 1]
	private static final double normalizationFactor = 2 / (double) 0xffff;

	public static void bytesToDoubles(byte[] bytes, double[] doubles, ByteOrder byteOrder) {
		assert bytes.length == 2 * doubles.length;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(byteOrder);
		for (int i = 0; i < doubles.length && byteBuffer.hasRemaining(); i++) {
			doubles[i] = normalizationFactor * byteBuffer.getShort();
		}
	}
	
	public static void littleEndianBytesToDoubles(byte[] bytes, double[] doubles) {
		bytesToDoubles(bytes, doubles, ByteOrder.LITTLE_ENDIAN);
	}
	
	public static void bigEndianBytesToDoubles(byte[] bytes, double[] doubles) {
		bytesToDoubles(bytes, doubles, ByteOrder.BIG_ENDIAN);
	}

}
