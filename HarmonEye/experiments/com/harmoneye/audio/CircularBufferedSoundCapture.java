package com.harmoneye.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import com.harmoneye.util.DoubleCircularBuffer;

public class CircularBufferedSoundCapture {

	// buffer sizes in samples
	private static final int INPUT_LINE_BUFFER_SIZE = 1024;
	private static final int OUTPUT_WINDOW_SIZE = 4 * 1024;
	private static final int CIRCULAR_BUFFER_SIZE = 8 * 1024; 
	
	public static void main(String s[]) throws Exception {
		DoubleCircularBuffer buffer = new DoubleCircularBuffer(CIRCULAR_BUFFER_SIZE);
		
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
		int bufferSize = sampleSizeBytes * INPUT_LINE_BUFFER_SIZE;
		line.open(format, bufferSize);
		
		byte[] data = new byte[bufferSize];
		double[] newAmplitudes = new double[INPUT_LINE_BUFFER_SIZE];
		double[] window = new double[OUTPUT_WINDOW_SIZE];
		
		System.out.println("buffer size: " + bufferSize);

		line.start();

		boolean captureEnabled = true;
		while (captureEnabled) {
			int readBytesCount = line.read(data, 0, bufferSize);
			if (readBytesCount == -1) {
				break;
			}
			littleEndianBytesToDoubles(data, newAmplitudes);

			buffer.write(newAmplitudes);
			
			buffer.readLast(window, window.length);
			
			double rms = getRms(window);
//			System.out.print("min: " + min(amplitudes) + "\t");
			System.out.print("max: " + max(window) + "\t");
//			System.out.print(rms);
			System.out.print("\t| " + doubleToStars(rms));
			System.out.println();
		}

		// we reached the end of the stream.
		// stop and close the line.
		line.stop();
		line.close();
	}

	private static void littleEndianBytesToDoubles(byte[] bytes, double[] doubles) {
		assert bytes.length == 2 * doubles.length;
		// signed short to [-1; 1]
		double normalizationFactor = 2 / (double) 0xffff;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < doubles.length && byteBuffer.hasRemaining(); i++) {
			doubles[i] = normalizationFactor * byteBuffer.getShort();
		}
	}

	private static double getRms(double[] amplitudes) {
		double sum = 0;
		for (double amplitide : amplitudes) {
			sum += amplitide * amplitide;
		}
		return (double) Math.sqrt(sum / (double)amplitudes.length);
	}
	
	private static String doubleToStars(double amplitude) {
		StringBuilder sb =  new StringBuilder();
		int starCount = (int) Math.round(amplitude * 100);
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		return sb.toString();
	}
	
	private static double min(double[] amplitudes) {
		double min = Double.MAX_VALUE;
		for (double amplitide : amplitudes) {
			min = Math.min(Math.abs(amplitide), min);
		}
		return min;
	}
	
	private static double max(double[] amplitudes) {
		double max = Double.MIN_VALUE;
		for (double amplitide : amplitudes) {
			max = Math.max(Math.abs(amplitide), max);
		}
		return max;
	}
}