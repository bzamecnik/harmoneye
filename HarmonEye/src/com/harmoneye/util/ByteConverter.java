package com.harmoneye.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteConverter {

	public static void littleEndianBytesToDoubles(byte[] bytes, double[] floats) {
		assert bytes.length == 2 * floats.length;
		// signed short to [-1; 1]
		float normalizationFactor = 2 / (float) 0xffff;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < floats.length && byteBuffer.hasRemaining(); i++) {
			floats[i] = normalizationFactor * byteBuffer.getShort();
		}
	}

}
