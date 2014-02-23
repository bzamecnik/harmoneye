package com.harmoneye.app.spectrogram;

import java.io.File;
import java.nio.ByteOrder;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileFormat;

import com.harmoneye.audio.ByteConverter;

public class AudioReader {
	private static final int MONO_READ_BUFFER_SIZE_SAMPLES = 4096;
	private static final int BYTES_PER_INPUT_SAMPLE = 2;

	public SampledAudio readAudio(String inputFileName) {
		try {
			File file = new File(inputFileName);
			AudioInputStream audioInputStream = AudioSystem
				.getAudioInputStream(file);

			AudioFormat inputFormat = audioInputStream.getFormat();
			AudioFormat readingFormat = prepareReadingFormat(inputFormat);

			int inputChannelCount = inputFormat.getChannels();
			int readBufferSizeSamples = inputChannelCount
				* MONO_READ_BUFFER_SIZE_SAMPLES;
			int readBufferSizeBytes = BYTES_PER_INPUT_SAMPLE
				* MONO_READ_BUFFER_SIZE_SAMPLES;

			int sampleCount = getSampleCount(file, audioInputStream);

			double[] amplitudes = new double[sampleCount];

			byte[] readBuffer = new byte[readBufferSizeBytes];
			double[] amplitudeBuffer = new double[readBufferSizeSamples];
			double[] amplitudeBufferMono = null;

			ByteOrder byteOrder = readingFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN
				: ByteOrder.LITTLE_ENDIAN;

			AudioInputStream wrappedInputStream = convertToPcm(audioInputStream,
				readingFormat);

			int destIndex = 0;
			int readBytesCount = 0;
			while (readBytesCount >= 0) {
				readBytesCount = wrappedInputStream.read(readBuffer);
				if (readBytesCount < 0) {
					break;
				}

				ByteConverter.bytesToDoubles(readBuffer,
					amplitudeBuffer,
					byteOrder);
				amplitudeBufferMono = toMono(amplitudeBuffer,
					amplitudeBufferMono,
					inputChannelCount);

				int readSampleCount = readBytesCount / (inputChannelCount * BYTES_PER_INPUT_SAMPLE);
				System.arraycopy(amplitudeBufferMono,
					0,
					amplitudes,
					destIndex,
					readSampleCount);
				destIndex += readSampleCount;
			}
			return new SampledAudio(amplitudes, inputFormat.getSampleRate());
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private double[] toMono(double[] amplitudes, double[] amplitudesMono,
		int inputChannelCount) {
		if (inputChannelCount <= 1) {
			return amplitudes;
		} else {
			if (amplitudesMono == null) {
				amplitudesMono = new double[MONO_READ_BUFFER_SIZE_SAMPLES];
			}
			double inputChannelCountInv = 1.0 / inputChannelCount;
			for (int i = 0, origIndex = 0; i < amplitudesMono.length; i++, origIndex += inputChannelCount) {
				double avg = 0;
				for (int channel = 0; channel < inputChannelCount; channel++) {
					avg += amplitudes[origIndex + channel];
				}
				amplitudesMono[i] = avg * inputChannelCountInv;
			}
			return amplitudesMono;
		}
	}

	private int getSampleCount(File file, AudioInputStream audioInputStream) {
		int sampleCount = (int) audioInputStream.getFrameLength();
		if (sampleCount < 0) {
			sampleCount = getMp3SampleCount(file);
		}
		return sampleCount;
	}

	private int getMp3SampleCount(File file) {
		try {
			AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
			if (!(format instanceof MpegAudioFileFormat)) {
				throw new IllegalArgumentException("Unknown file format");
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> properties = ((MpegAudioFileFormat) format)
				.properties();
			long durationMicroSecs = (long) properties.get("duration");
			double durationSecs = durationMicroSecs / 1e6;
			int sampleRate = (int) properties.get("mp3.frequency.hz");
			return (int) Math.ceil(durationSecs * sampleRate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private AudioFormat prepareReadingFormat(AudioFormat inputFormat) {
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
