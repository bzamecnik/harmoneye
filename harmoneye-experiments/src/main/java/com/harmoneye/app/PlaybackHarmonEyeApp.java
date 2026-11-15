package com.harmoneye.app;

import javax.swing.SwingWorker;

import com.harmoneye.app.AbstractHarmonEyeApp;
import com.harmoneye.app.Config;
import com.harmoneye.audio.Playback;

public class PlaybackHarmonEyeApp extends AbstractHarmonEyeApp {

	private Playback playback;

	public PlaybackHarmonEyeApp() {
		Config config = Config.fromDefault();
		String inputFileName = config.get("inputFile");
		playback = new Playback(soundAnalyzer, inputFileName);
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
