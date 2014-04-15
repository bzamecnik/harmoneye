package com.harmoneye.audio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileFormat;

// TODO: allow rewinding
// - find out the total length of the file
// - find out current position
// http://www.jsresources.org/examples/RewindingAudioPlayer.java.html

public class Playback implements Runnable {

	private static final int MONO_READ_BUFFER_SIZE_SAMPLES = 256;
	int PLAYBACK_BUFFER_SIZE_BYTES = 8 * 1024;

	private Thread thread;

	private SoundConsumer soundAnalyzer;
	private String inputFileName;
	private int inputChannels;
	private int monoBufferSizeSamples;
	private int readBufferSizeBytes;
	private int readBufferSizeSamples;
	private double inputChannelsInv;

	public Playback(SoundConsumer soundAnalyzer, String inputFileName) {
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
			e.printStackTrace(System.err);
			thread = null;
		}
	}

	private void play() throws UnsupportedAudioFileException, IOException,
		Exception, LineUnavailableException {
		File file = new File(inputFileName);
		AudioInputStream audioInputStream = AudioSystem
			.getAudioInputStream(file);

		AudioFormat inputFormat = audioInputStream.getFormat();
		System.out.println("original input format:");
		printFormat(inputFormat);

		AudioFormat playbackFormat = preparePlaybackFormat(inputFormat);

		System.out.println("playback format:");
		printFormat(playbackFormat);

		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
			playbackFormat);
		if (!AudioSystem.isLineSupported(info)) {
			throw new Exception("Line matching " + info + " not supported.");
		}

		SourceDataLine playbackLine = (SourceDataLine) AudioSystem
			.getLine(info);
		playbackLine.open(playbackFormat, PLAYBACK_BUFFER_SIZE_BYTES);

		inputChannels = inputFormat.getChannels();
		inputChannelsInv = 1.0 / inputChannels;
		monoBufferSizeSamples = MONO_READ_BUFFER_SIZE_SAMPLES;
		readBufferSizeSamples = inputChannels * monoBufferSizeSamples;
		readBufferSizeBytes = 2 * readBufferSizeSamples;

		byte[] readBuffer = new byte[readBufferSizeBytes];
		double[] amplitudes = new double[readBufferSizeSamples];
		double[] amplitudesMono = null;

		ByteOrder byteOrder = playbackFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN
			: ByteOrder.LITTLE_ENDIAN;

		AudioInputStream wrappedInputStream = convertToPcm(audioInputStream,
			playbackFormat);

		printProperties(audioInputStream);
		
		playbackLine.start();

		int readBytesCount = 0;
		while (thread != null && readBytesCount >= 0) {
			readBytesCount = wrappedInputStream.read(readBuffer);
			if (readBytesCount < 0) {
				break;
			}
			
			ByteConverter.bytesToDoubles(readBuffer, amplitudes, byteOrder);
			amplitudesMono = toMono(amplitudes, amplitudesMono, inputChannels);

			soundAnalyzer.consume(amplitudesMono);

			// long start = System.nanoTime();
			playbackLine.write(readBuffer, 0, readBytesCount);
			// long stop = System.nanoTime();
			// System.out.println((stop - start) * 1e-6);
		}

		playbackLine.drain();
		playbackLine.stop();
		playbackLine.close();
	}

	private void printProperties(AudioInputStream inputStream) {
		if (inputStream instanceof javazoom.spi.PropertiesContainer) {
			System.out.println(((javazoom.spi.PropertiesContainer) inputStream)
				.properties());
		}
		if (inputStream.markSupported()) {
			try {
				AudioFileFormat format = AudioSystem
					.getAudioFileFormat(inputStream);
				if (format instanceof MpegAudioFileFormat) {
					System.out.println(((MpegAudioFileFormat) format)
						.properties());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private double[] toMono(double[] amplitudes, double[] amplitudesMono,
		int inputChannels) {
		if (inputChannels <= 1) {
			return amplitudes;
		} else {
			if (amplitudesMono == null) {
				amplitudesMono = new double[monoBufferSizeSamples];
			}
			for (int i = 0, origIndex = 0; i < amplitudesMono.length; i++, origIndex += inputChannels) {
				double avg = 0;
				for (int channel = 0; channel < inputChannels; channel++) {
					avg += amplitudes[origIndex + channel];
				}
				amplitudesMono[i] = avg * inputChannelsInv;
			}
			return amplitudesMono;
		}
	}

	private void printFormat(AudioFormat format) {
		System.out.println("format: " + format);
		System.out.println("encoding: " + format.getEncoding());
		System.out.println("bigEndian: " + format.isBigEndian());
		System.out.println("channels: " + format.getChannels());
		System.out.println("sampleRate: " + format.getSampleRate());
	}

	private AudioFormat preparePlaybackFormat(AudioFormat inputFormat) {
		int channels = inputFormat.getChannels();
		float rate = inputFormat.getSampleRate();
		int sampleSizeInBytes = 2;
		int sampleSizeInBits = 8 * sampleSizeInBytes;
		int frameSize = channels * sampleSizeInBytes;
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate,
			sampleSizeInBits, channels, frameSize, rate,
			inputFormat.isBigEndian());
	}

	private static AudioInputStream convertToPcm(AudioInputStream inputStream,
		AudioFormat playbackFormat) {
		AudioFormat inputFormat = inputStream.getFormat();
		if (inputFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
			inputStream = AudioSystem.getAudioInputStream(playbackFormat,
				inputStream);
		}

		return inputStream;
	}
}