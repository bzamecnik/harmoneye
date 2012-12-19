package org.zamecnik;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WavFileReader {
	private static final int BUFFER_SIZE = 16 * 1024;
	private static final String INPUT_FILE_NAME = "/Users/bzamecnik/dev/harmoneye/data/wav/kamca-02.wav";

	public static void main(String[] args)
			throws UnsupportedAudioFileException, IOException {
		AudioInputStream inputStream = null;
		try {
			inputStream = AudioSystem.getAudioInputStream(new File(
					INPUT_FILE_NAME));

			System.out.println("format: " + inputStream.getFormat());
			System.out.println("frame length: " + inputStream.getFrameLength());
			System.out.println("total time: " + inputStream.getFrameLength()
					/ inputStream.getFormat().getFrameRate());

			byte[] buffer = new byte[BUFFER_SIZE];
			float[] amplitudes = new float[BUFFER_SIZE / 2];
			int readBytesCount = 0;
			boolean stopped = false;
//			int loopCount = 10;
			while (!stopped) {
				readBytesCount = inputStream.read(buffer);
				// StringBuilder sb = new StringBuilder();
				// for (byte b : buffer) {
				// sb.append(String.format("%02X ", b));
				// }
				// System.out.println(sb.toString());
				//
				littleEndianBytesToFloats(buffer, amplitudes);
				// sb = new StringBuilder();
				// for (float amplitude : amplitudes) {
				// sb.append(String.format("%02f ", amplitude));
				// }
				// System.out.println(sb.toString());

				float rms = getRms(amplitudes);
				System.out.print(rms);
				System.out.print("\t" + floatToStars(rms));
				System.out.println();

				 stopped = readBytesCount < 0;
//				stopped = loopCount-- <= 0;
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
		float normalizationFactor = 1 / (float) 0xffff;
		for (int i = 0; 2 * i < bytes.length; i++) {
			floats[i] = normalizationFactor
					* (bytes[2 * i] | (bytes[2 * i + 1] << 8));
		}
	}

	private static float getRms(float[] amplitudes) {
		float sum = 0;
		for (float amplitide : amplitudes) {
			sum += amplitide * amplitide;
		}
		return (float) Math.sqrt(sum / (float)amplitudes.length);
	}
	
	private static String floatToStars(float amplitude) {
		StringBuilder sb =  new StringBuilder();
		int starCount = Math.round(amplitude * 100);
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		return sb.toString();
	}
}
