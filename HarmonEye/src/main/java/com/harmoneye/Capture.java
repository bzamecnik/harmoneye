package com.harmoneye;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import com.harmoneye.util.ByteConverter;

public class Capture implements Runnable {

	private static final int DEFAULT_READ_BUFFER_SIZE_SAMPLES = 256;

	private Thread thread;
	private AtomicBoolean isRunning = new AtomicBoolean();

	private int readBufferSizeInSamples;
	private AudioFormat format;
	private int bufferSize;

	private SoundConsumer soundConsumer;

	public Capture(SoundConsumer soundConsumer, float sampleRate, int sampleSizeBits) {
		this.soundConsumer = soundConsumer;

		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		int channelCount = 1;
		int sampleSizeBytes = sampleSizeBits / 8;
		int frameSizeBytes = channelCount * sampleSizeBytes;
		boolean bigEndian = false;

		format = new AudioFormat(encoding, sampleRate, sampleSizeBits, channelCount, frameSizeBytes, sampleRate,
			bigEndian);

		readBufferSizeInSamples = DEFAULT_READ_BUFFER_SIZE_SAMPLES;
		bufferSize = sampleSizeBytes * readBufferSizeInSamples;
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
			System.err.println(e);
			thread = null;
		}
	}

	private void capture() throws Exception {

		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

		line.open(format, bufferSize);

		byte[] data = new byte[bufferSize];
		double[] amplitudes = new double[readBufferSizeInSamples];

		line.start();

		while (isRunning.get()) {
			int readBytesCount = line.read(data, 0, bufferSize);
			if (readBytesCount == -1) {
				break;
			}
			ByteConverter.littleEndianBytesToDoubles(data, amplitudes);

			soundConsumer.consume(amplitudes);
		}

		// we reached the end of the stream.
		// stop and close the line.
		line.stop();
		line.close();
	}
}