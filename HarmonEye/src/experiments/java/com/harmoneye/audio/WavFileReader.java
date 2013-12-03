package com.harmoneye.audio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.harmoneye.Config;

public class WavFileReader {
	private static final int BUFFER_SIZE = 4 * 1024;

	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
		Config config = Config.fromDefault();
		String inputFileName = config.get("inputFile");
		AudioInputStream inputStream = null;
		try {
			inputStream = AudioSystem.getAudioInputStream(new File(inputFileName));

			System.out.println("format: " + inputStream.getFormat());
			System.out.println("frame length: " + inputStream.getFrameLength());
			System.out.println("total time: " + inputStream.getFrameLength() / inputStream.getFormat().getFrameRate());

			byte[] buffer = new byte[BUFFER_SIZE];
			float[] amplitudes = new float[BUFFER_SIZE / 2];
			int readBytesCount = 0;
			boolean stopped = false;
			//			int loopCount = 10;
			while (!stopped) {
				readBytesCount = inputStream.read(buffer);
				StringBuilder sb = new StringBuilder();
				// for (byte b : buffer) {
				// sb.append(String.format("%02X ", b));
				// }
				// System.out.println(sb.toString());
				//
				littleEndianBytesToFloats(buffer, amplitudes);
//				sb = new StringBuilder();
//				for (float amplitude : amplitudes) {
//					sb.append(amplitude + ", ");
//				}
//				System.out.println(sb.toString());

//				System.out.print("min: " + min(amplitudes) + "\t");
//				System.out.print("max: " + max(amplitudes) + "\t");
				float rms = getRms(amplitudes);
				System.out.print(rms);
				System.out.print("\t" + floatToStars(rms));
				System.out.println();

				stopped = readBytesCount < 0;
				// stopped = loopCount-- <= 0;
			}

		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	private static void littleEndianBytesToFloats(byte[] bytes, float[] floats) {
		assert bytes.length == 2 * floats.length;
		// signed short to [-1; 1]
		float normalizationFactor = 2 / (float) 0xffff;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < floats.length && byteBuffer.hasRemaining(); i++) {
			floats[i] = normalizationFactor * byteBuffer.getShort();
		}
	}

	private static float getRms(float[] amplitudes) {
		float sum = 0;
		for (float amplitide : amplitudes) {
			sum += amplitide * amplitide;
		}
		return (float) Math.sqrt(sum / (float) amplitudes.length);
	}

	private static String floatToStars(float amplitude) {
		StringBuilder sb = new StringBuilder();
		int starCount = Math.round(amplitude * 100);
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		return sb.toString();
	}

	private static float min(float[] amplitudes) {
		float min = Float.MAX_VALUE;
		for (float amplitide : amplitudes) {
			min = Math.min(Math.abs(amplitide), min);
		}
		return min;
	}

	private static float max(float[] amplitudes) {
		float max = Float.MIN_VALUE;
		for (float amplitide : amplitudes) {
			max = Math.max(Math.abs(amplitide), max);
		}
		return max;
	}
}
