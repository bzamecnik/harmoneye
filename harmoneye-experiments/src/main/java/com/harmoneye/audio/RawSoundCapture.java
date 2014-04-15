package com.harmoneye.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * Allows to capture raw sound data from an input device.
 * 
 * Usage:
 * 
 * <pre>
 * {@code
 * SoundCapture capture = new SoundCapture(...);
 * try {
 * 	capture.start();
 * 	while (capture.hasNext()) {
 * 		byte[] rawSamples = capture.next();
 * 	}
 * finally {
 * 	capture.close();
 * }
 * }
 * </pre>
 * 
 * It can be paused/resumed via the {@link #start()} and {@link #stop()}
 * methods. Be sure to close it using the {@link #close()} method.
 * 
 * When there is nothing more to read the next() method automatically stops the
 * capturing.
 */
public class RawSoundCapture {

	/** sound input line */
	private TargetDataLine line;

	private byte[] rawSampleBuffer;
	/** size of {@link #rawSampleBuffer} in bytes */
	private int bufferSizeInBytes;

	private boolean started;

	public RawSoundCapture(int bufferSizeInSamples, int bytesPerSample, AudioFormat format)
		throws LineUnavailableException {
		this.bufferSizeInBytes = bytesPerSample * bufferSizeInSamples;

		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		line = (TargetDataLine) AudioSystem.getLine(info);
		line.open(format, bufferSizeInBytes);

		this.rawSampleBuffer = new byte[bufferSizeInBytes];
	}

	public static RawSoundCapture createWithDefaultAudioFormat(int bufferSizeInSamples) throws LineUnavailableException {
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		float sampleRate = 11025.0f;
		int channelCount = 1;
		int bytesPerSample = 2;
		int sampleSizeInBits = 8 * bytesPerSample;
		int frameSizeInBytes = channelCount * bytesPerSample;
		boolean bigEndian = false;
		AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channelCount, frameSizeInBytes,
			sampleRate, bigEndian);
		return new RawSoundCapture(bufferSizeInSamples, bytesPerSample, format);
	}

	public void start() {
		line.start();
		started = true;
	}

	public void stop() {
		line.stop();
		started = false;
	}

	public void close() {
		line.close();
	}

	public byte[] next() {
		int readBytesCount = line.read(rawSampleBuffer, 0, bufferSizeInBytes);
		if (readBytesCount == -1) {
			line.stop();
		}
		return rawSampleBuffer;
	}

	public boolean hasNext() {
		return started;
	}
}
