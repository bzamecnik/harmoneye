package com.harmoneye.audio;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class SoundCapture implements Runnable {

	// TODO: Automatically find out minimum usable buffer size.
	// If its too small, buffer underruns make weird artifacts!!!

	private static final int DEFAULT_READ_BUFFER_SIZE_SAMPLES = 1024;

	private SoundConsumer soundConsumer;

	private Thread thread;
	private AtomicBoolean isRunning = new AtomicBoolean();

	private int bufferSizeInBytes;
	private int bufferSizeInSamples;
	private AudioFormat audioFormat;

	public SoundCapture(SoundConsumer soundConsumer, double sampleRate,
		int sampleSizeBits) {
		this(soundConsumer, sampleRate, sampleSizeBits,
			DEFAULT_READ_BUFFER_SIZE_SAMPLES);
	}

	public SoundCapture(SoundConsumer soundConsumer, double sampleRate,
		int bitsPerSample, int bufferSizeSamples) {
		this.soundConsumer = soundConsumer;
		this.bufferSizeInSamples = bufferSizeSamples;

		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		int channelCount = 1;
		int sampleSizeBytes = bitsPerSample / 8;
		int frameSizeBytes = channelCount * sampleSizeBytes;
		boolean bigEndian = false;

		audioFormat = new AudioFormat(encoding, (float)sampleRate, bitsPerSample,
			channelCount, frameSizeBytes, (float)sampleRate, bigEndian);

		bufferSizeInBytes = sampleSizeBytes * bufferSizeInSamples;
		System.out.println("buffer size: " + bufferSizeInBytes + " B, "
			+ bufferSizeInSamples + " samples");
	}

	public void start() {
		thread = new Thread(this);
		thread.setName("Capture");
		thread.start();
		isRunning.set(true);
	}

	public void stop() {
		isRunning.set(false);
	}

	public void run() {
		try {
			capture();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			thread = null;
		}
	}

	private void capture() throws Exception {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
			audioFormat);

		TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

		line.open(audioFormat, bufferSizeInBytes);

		byte[] samples = new byte[bufferSizeInBytes];
		double[] amplitudes = new double[bufferSizeInSamples];

		line.start();

		while (isRunning.get()) {
			int readBytesCount = line.read(samples, 0, bufferSizeInBytes);
			if (readBytesCount == -1) {
				break;
			}
			ByteConverter.littleEndianBytesToDoubles(samples, amplitudes);

			soundConsumer.consume(amplitudes);
		}

		// we reached the end of the stream.
		// stop and close the line.
		line.stop();
		line.close();
	}
}