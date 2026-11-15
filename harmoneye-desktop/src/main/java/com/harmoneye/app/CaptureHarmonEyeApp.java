package com.harmoneye.app;

import com.harmoneye.audio.SoundCapture;

public class CaptureHarmonEyeApp extends AbstractHarmonEyeApp {

	private SoundCapture soundCapture;

	public CaptureHarmonEyeApp() {
		soundCapture = new SoundCapture(soundAnalyzer, AUDIO_SAMPLE_RATE, AUDIO_BITS_PER_SAMPLE);
	}

	public void start() {
		super.start();
		soundCapture.start();
	}

	public void stop() {
		super.stop();
		soundCapture.stop();
	}
}
