package com.harmoneye.audio.android;

import java.util.concurrent.atomic.AtomicBoolean;

import android.media.AudioRecord;
import android.util.Log;

import com.harmoneye.audio.SoundConsumer;
import com.harmoneye.audio.android.AudioRecordDiscovery.AudioRecordParams;

public class SoundCapture implements Runnable {

	// signed short to [-1; 1]
	private static final double SHORT_TO_DOUBLE = 2 / (double) 0xffff;

	private static final String LOG_TAG = "SoundCapture";

	private SoundConsumer soundConsumer;

	private AtomicBoolean running = new AtomicBoolean();

	private short[] samples;
	private double[] amplitudes;

	private AudioRecordParams audioRecordParams;

	public SoundCapture(SoundConsumer soundConsumer,
		AudioRecordParams audioRecordParams) {
		this.soundConsumer = soundConsumer;
		this.audioRecordParams = audioRecordParams;

		int bufferSizeInSamples = audioRecordParams.getBufferSizeInSamples();
		samples = new short[bufferSizeInSamples];
		amplitudes = new double[bufferSizeInSamples];
		Log.i(LOG_TAG, "Input audio buffer initialized with size: "
			+ bufferSizeInSamples + " samples");
	}

	public void run() {
		running.set(true);

		int bufferSizeInSamples = audioRecordParams.getBufferSizeInSamples();
		
		AudioRecord recorder = null;
		try {
			recorder = audioRecordParams.createAudioRecord();
			recorder.startRecording();
			while (running.get()) {
				// this is a blocking operation - waits until there's enough
				// data
				recorder.read(samples, 0, bufferSizeInSamples);
				toAmplitudes(samples, amplitudes);
				soundConsumer.consume(amplitudes);
			}
			recorder.stop();
		} finally {
			if (recorder != null) {
				recorder.release();
			}
		}
	}

	public void stop() {
		running.set(false);
	}

	public boolean isRunning() {
		return running.get();
	}

	private void toAmplitudes(short[] samples, double[] amplitudes) {
		int length = samples.length;
		for (int i = 0; i < length; i++) {
			amplitudes[i] = samples[i] * SHORT_TO_DOUBLE;
		}
	}
}
