package com.harmoneye.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class BasicSoundCapture {

	public static void main(String s[]) throws Exception {
		AudioInputStream audioInputStream = null;

		// define the required attributes for our line,
		// and make sure a compatible line is supported.

		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		float sampleRate = 11025.0f;
		int channelCount = 1;
		int sampleSizeBytes = 2;
		int sampleSizeBits = 8 * sampleSizeBytes;
		int frameSizeBytes = channelCount * sampleSizeBytes;
		boolean bigEndian = true;

		AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeBits, channelCount, frameSizeBytes,
			sampleRate, bigEndian);

		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
//		int bufferSize = line.getBufferSize();
		int bufferSize = 2 * 1024;
		line.open(format, bufferSize);

//		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		byte[] data = new byte[bufferSize];
		float[] amplitudes = new float[bufferSize / 2];
		
		System.out.println("buffer size: " + bufferSize);

		line.start();

		int readBytesCount = 0;
		boolean captureEnabled = true;
		while (captureEnabled) {
//			long start = System.nanoTime();
			if ((readBytesCount = line.read(data, 0, bufferSize)) == -1) {
				break;
			}
//			long end = System.nanoTime();
//			System.out.println((end - start) / 1e6 + " ms");
			littleEndianBytesToFloats(data, amplitudes);
			// sb = new StringBuilder();
			// for (float amplitude : amplitudes) {
			// sb.append(String.format("%02f ", amplitude));
			// }
			// System.out.println(sb.toString());

			float rms = getRms(amplitudes);
//			System.out.print("min: " + min(amplitudes) + "\t");
//			System.out.print("max: " + max(amplitudes) + "\t");
//			System.out.print(rms);
			System.out.print("\t| " + floatToStars(rms));
			System.out.println();
//			out.write(data, 0, readBytesCount);
			
//			captureEnabled = false;
		}
		
		printArray(data);

		// we reached the end of the stream.
		// stop and close the line.
		line.stop();
		line.close();

		// stop and close the output stream
//		out.flush();
//		out.close();

		// load bytes into the audio input stream for playback

//		byte audioBytes[] = out.toByteArray();
//		ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
//		audioInputStream = new AudioInputStream(bais, format, audioBytes.length);
//
//		audioInputStream.reset();
	}
	
	private static void printArray(byte[] array) {
		System.out.println("length: " + array.length);
//		for (int i = 0; i < array.length; i++) {
//			System.out.println(array[i]);
//		}
	}
	
	private static void littleEndianBytesToFloats(byte[] bytes, float[] floats) {
		assert bytes.length == 2 * floats.length;
		// signed short to [-1; 1]
		float normalizationFactor = 2 / (float) 0xffff;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < floats.length && byteBuffer.hasRemaining(); i++) {
			floats[i] = normalizationFactor * byteBuffer.getShort();
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