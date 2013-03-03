package com.harmoneye;

public class PlaybackHarmonEyeApp extends AbstractHarmonEyeApp {

	private static final long serialVersionUID = 1L;
	private static final String INPUT_FILE_NAME = "/Users/bzamecnik/dev/harmoneye/data/wav/04-Sla-Maria-do-klastera-simple.wav";
	private Playback playback;

	public PlaybackHarmonEyeApp() {
		playback = new Playback(soundAnalyzer, INPUT_FILE_NAME);
	}

	public void start() {
		super.start();
		playback.start();
	}

	public static void main(String[] args) {
		new PlaybackHarmonEyeApp().start();
	}
}
