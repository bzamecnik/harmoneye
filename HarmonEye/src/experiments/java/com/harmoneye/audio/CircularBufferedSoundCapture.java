package com.harmoneye.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.lang3.time.StopWatch;


public class CircularBufferedSoundCapture {

	// buffer sizes in samples
	private static final int INPUT_LINE_BUFFER_SIZE = 1024;
	private static final int OUTPUT_WINDOW_SIZE = 4 * 1024;
	private static final int CIRCULAR_BUFFER_SIZE = 8 * 1024;

	public static void main(String s[]) throws Exception {
		DoubleRingBuffer circularBuffer = new DoubleRingBuffer(CIRCULAR_BUFFER_SIZE);

		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		float sampleRate = 11025.0f;
		int channelCount = 1;
		int sampleSizeBytes = 2;
		int sampleSizeBits = 8 * sampleSizeBytes;
		int frameSizeBytes = channelCount * sampleSizeBytes;
		boolean bigEndian = false;

		AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeBits, channelCount, frameSizeBytes,
			sampleRate, bigEndian);

		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
		int inputBufferSizeInBytes = sampleSizeBytes * INPUT_LINE_BUFFER_SIZE;
		line.open(format, inputBufferSizeInBytes);

		byte[] inputBuffer = new byte[inputBufferSizeInBytes];
		double[] newSamples = new double[INPUT_LINE_BUFFER_SIZE];
		double[] recentSamples = new double[OUTPUT_WINDOW_SIZE];

		System.out.println("buffer size: " + inputBufferSizeInBytes);

		line.start();

		boolean captureEnabled = true;
		while (captureEnabled) {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			int readBytesCount = line.read(inputBuffer, 0, inputBufferSizeInBytes);
			stopWatch.stop();
			System.out.println(stopWatch.getNanoTime() * 1e-6);
			stopWatch.reset();
			if (readBytesCount == -1) {
				break;
			}
			ByteConverter.littleEndianBytesToDoubles(inputBuffer, newSamples);

			circularBuffer.write(newSamples);

			circularBuffer.readLast(recentSamples, recentSamples.length);

			double rms = getRms(recentSamples);
			// System.out.print("min: " + min(amplitudes) + "\t");
			System.out.print("max: " + max(recentSamples) + "\t");
			// System.out.print(rms);
			System.out.print("\t| " + doubleToStars(rms));
			System.out.println();
		}

		// we reached the end of the stream.
		// stop and close the line.
		line.stop();
		line.close();
	}

	private static double getRms(double[] amplitudes) {
		double sum = 0;
		for (double amplitide : amplitudes) {
			sum += amplitide * amplitide;
		}
		return (double) Math.sqrt(sum / (double) amplitudes.length);
	}

	private static String doubleToStars(double amplitude) {
		StringBuilder sb = new StringBuilder();
		int starCount = (int) Math.round(amplitude * 100);
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		return sb.toString();
	}

	private static double max(double[] amplitudes) {
		double max = Double.MIN_VALUE;
		for (double amplitide : amplitudes) {
			max = Math.max(Math.abs(amplitide), max);
		}
		return max;
	}
}