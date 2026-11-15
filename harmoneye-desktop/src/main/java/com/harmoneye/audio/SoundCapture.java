package com.harmoneye.audio;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class SoundCapture implements Runnable {

	private static final int MIN_INPUT_BUFFER_SIZE_SAMPLES = 1024;

	private SoundConsumer soundConsumer;

	private Thread thread;
	private AtomicBoolean isRunning = new AtomicBoolean();

	private AudioFormat audioFormat;

	public SoundCapture(SoundConsumer soundConsumer, double sampleRate,
		int bitsPerSample) {
		this.soundConsumer = soundConsumer;

		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		int channelCount = 1;
		int bytesPerSample = bitsPerSample / 8;
		int bytesPerFrame = channelCount * bytesPerSample;
		boolean bigEndian = false;

		audioFormat = new AudioFormat(encoding, (float) sampleRate,
			bitsPerSample, channelCount, bytesPerFrame, (float) sampleRate,
			bigEndian);
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

		int bufferSizeInBytes = getBufferSize(line);
		int bytesPerSample = audioFormat.getSampleSizeInBits() / 8;
		int bufferSizeInSamples = bufferSizeInBytes / bytesPerSample;
		System.out.println("audio input buffer size: " + bufferSizeInBytes
			+ " B, " + bufferSizeInSamples + " samples");

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

	private int getBufferSize(TargetDataLine line) {
		int minBufferSize = ((DataLine.Info) line.getLineInfo())
			.getMinBufferSize();
		int bufferSizeInBytes = Math.max(minBufferSize,
			MIN_INPUT_BUFFER_SIZE_SAMPLES);
		return bufferSizeInBytes;
	}
}
