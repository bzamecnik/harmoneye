package com.harmoneye.audio.android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Tries to find suitable parameters for using {@link AudioRecord}.
 */
public class AudioRecordDiscovery {

	// TODO: figure out the sample rate and other format information
	// automatically from multiple variants as it might vary over devices:
	// http://stackoverflow.com/questions/11549709/how-can-i-find-out-what-sampling-rates-are-supported-on-my-tablet

	private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
	private static final int AUDIO_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	private static final int AUDIO_SAMPLE_RATE = 44100;
	private static final int AUDIO_BYTES_PER_SAMPLE = 2;

	public AudioRecordParams findParams() {
		int audioSource = AUDIO_SOURCE;
		int channelConfig = AUDIO_CHANNELS;
		int audioFormat = AUDIO_FORMAT;
		int sampleRate = AUDIO_SAMPLE_RATE;
		int bytesPerSample = AUDIO_BYTES_PER_SAMPLE;

		int bufferSizeInBytes = findMinBufferSize(sampleRate,
			channelConfig,
			audioFormat);
		AudioRecord recorder = null;
		try {
			AudioRecordParams audioRecordParams = new AudioRecordParams(
				audioSource, channelConfig, audioFormat, sampleRate,
				bytesPerSample, bufferSizeInBytes);
			recorder = audioRecordParams.createAudioRecord();
			return audioRecordParams;
		} catch (IllegalArgumentException e) {
			throw new UnsupportedOperationException(
				"Could not initialize the AudioRecord.", e);
		} finally {
			if (recorder != null) {
				recorder.release();
			}
		}
	}

	private int findMinBufferSize(int sampleRate, int channelConfig,
		int audioFormat) {
		int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate,
			channelConfig,
			audioFormat);

		if (bufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE
			|| bufferSizeInBytes == AudioRecord.ERROR) {
			throw new UnsupportedOperationException(
				"Could not find a suitable audio input buffer size.");
		}
		return bufferSizeInBytes;
	}

	public static class AudioRecordParams {
		private int audioSource;
		private int channelConfig;
		private int audioFormat;
		private int sampleRate;
		private int bytesPerSample;
		private int bufferSizeInBytes;

		public AudioRecordParams(int audioSource, int channelConfig,
			int audioFormat, int sampleRate, int bytesPerSample,
			int bufferSizeInBytes) {
			this.audioSource = audioSource;
			this.channelConfig = channelConfig;
			this.audioFormat = audioFormat;
			this.sampleRate = sampleRate;
			this.bytesPerSample = bytesPerSample;
			this.bufferSizeInBytes = bufferSizeInBytes;
		}

		public AudioRecord createAudioRecord() {
			return new AudioRecord(audioSource, sampleRate, channelConfig,
				audioFormat, bufferSizeInBytes);
		}

		public int getAudioSource() {
			return audioSource;
		}

		public int getChannelConfig() {
			return channelConfig;
		}

		public int getAudioFormat() {
			return audioFormat;
		}

		public int getSampleRate() {
			return sampleRate;
		}

		public int getBytesPerSample() {
			return bytesPerSample;
		}

		public int getBitsPerSample() {
			return 8 * bytesPerSample;
		}

		public int getBufferSizeInBytes() {
			return bufferSizeInBytes;
		}

		public int getBufferSizeInSamples() {
			return bufferSizeInBytes / bytesPerSample;
		}

		@Override
		public String toString() {
			return "AudioRecordParams [audioSource=" + audioSource
				+ ", channelConfig=" + channelConfig + ", audioFormat="
				+ audioFormat + ", sampleRate=" + sampleRate
				+ ", bytesPerSample=" + bytesPerSample + ", bufferSizeInBytes="
				+ bufferSizeInBytes + "]";
		}
	}
}
