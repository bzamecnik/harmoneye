package com.harmoneye;

public class CaptureHarmonEyeApp extends AbstractHarmonEyeApp {

	private Capture capture;

	public CaptureHarmonEyeApp() {
		capture = new Capture(soundAnalyzer);
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
