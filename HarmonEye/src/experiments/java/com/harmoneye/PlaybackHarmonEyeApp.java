package com.harmoneye;

import javax.swing.SwingWorker;

import com.harmoneye.AbstractHarmonEyeApp;
import com.harmoneye.Playback;

public class PlaybackHarmonEyeApp extends AbstractHarmonEyeApp {

	private static final String INPUT_FILE_NAME = "/Users/bzamecnik/Documents/harmoneye-labs/harmoneye/data/wav/04-Sla-Maria-do-klastera-simple.wav";
	private Playback playback;

	public PlaybackHarmonEyeApp() {
		playback = new Playback(soundAnalyzer, INPUT_FILE_NAME);
	}

	public void start() {
		super.start();
		playback.start();
	}
	
	public void stop() {
		super.stop();
		playback.stop();
	}

	public static void main(String[] args) {
		final PlaybackHarmonEyeApp app = new PlaybackHarmonEyeApp();
		class Initializer extends SwingWorker<String, Object> {
			@Override
			public String doInBackground() {
				app.init();
				app.start();
				return null;
			}
		}
		new Initializer().execute();
	}
}
