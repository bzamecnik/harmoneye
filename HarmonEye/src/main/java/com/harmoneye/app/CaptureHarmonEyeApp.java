package com.harmoneye.app;

import com.harmoneye.audio.Capture;

public class CaptureHarmonEyeApp extends AbstractHarmonEyeApp {

	private Capture capture;

	public CaptureHarmonEyeApp() {
		capture = new Capture(soundAnalyzer, AUDIO_SAMPLE_RATE, AUDIO_BITS_PER_SAMPLE);
	}

	public void start() {
		super.start();
		capture.start();
	}

	public void stop() {
		super.stop();
		capture.stop();
	}
}
