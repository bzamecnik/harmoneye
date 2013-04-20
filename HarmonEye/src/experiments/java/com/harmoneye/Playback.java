package com.harmoneye;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.harmoneye.util.ByteConverter;

public class Playback implements Runnable {

	private Thread thread;
	private byte[] readBuffer;
	private static final int READ_BUFFER_SIZE_SAMPLES = 256;
	private static final int READ_BUFFER_SIZE_BYTES = 2 * READ_BUFFER_SIZE_SAMPLES;
	private static final int PLAYBACK_BUFFER_SIZE_BYTES = 8 * 1024;
	private double[] amplitudes = new double[READ_BUFFER_SIZE_SAMPLES];
	private MusicAnalyzer soundAnalyzer;
	private String inputFileName;

	public Playback(MusicAnalyzer soundAnalyzer, String inputFileName) {
		this.soundAnalyzer = soundAnalyzer;
		this.inputFileName = inputFileName;
	}

	public void start() {
		thread = new Thread(this);
		thread.setName("Playback");
		thread.start();
	}

	public void stop() {
		thread = null;
	}

	public void run() {
		try {
			play();
		} catch (Exception e) {
			System.err.println(e);
			thread = null;
		}
	}

	private void play() throws UnsupportedAudioFileException, IOException,
			Exception, LineUnavailableException {
		File file = new File(inputFileName);
		AudioInputStream audioInputStream = AudioSystem
				.getAudioInputStream(file);

		AudioFormat format = audioInputStream.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			throw new Exception("Line matching " + info + " not supported.");
		}

		SourceDataLine playbackLine = (SourceDataLine) AudioSystem
				.getLine(info);
		playbackLine.open(format, PLAYBACK_BUFFER_SIZE_BYTES);

		readBuffer = new byte[READ_BUFFER_SIZE_BYTES];

		playbackLine.start();

		int readBytesCount = 0;
		while (thread != null && readBytesCount >= 0) {
			readBytesCount = audioInputStream.read(readBuffer);
			ByteConverter
					.littleEndianBytesToDoubles(readBuffer, amplitudes);
			soundAnalyzer.consume(amplitudes);

			// long start = System.nanoTime();
			playbackLine.write(readBuffer, 0, readBytesCount);
			// long stop = System.nanoTime();
			// System.out.println((stop - start) * 1e-6);
		}

		playbackLine.drain();
		playbackLine.stop();
		playbackLine.close();
	}
}