package com.harmoneye.app;

import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.harmoneye.analysis.MusicAnalyzer;
import com.harmoneye.analysis.MusicAnalyzer.AnalyzedFrame;
import com.harmoneye.audio.Playback;
import com.harmoneye.viz.Visualizer;

public class TextPrintingMusicAnalysis {

	public static void main(String[] args) {
		Config config = Config.fromDefault();
		String inputFileName = config.get("inputFile");
		Visualizer<AnalyzedFrame> visualizer = new TextVisualizer();
		final MusicAnalyzer soundAnalyzer = new MusicAnalyzer(visualizer, 44100, 16);
		soundAnalyzer.initialize();
		Playback playback = new Playback(soundAnalyzer, inputFileName);
		Timer updateTimer = new Timer("update timer");
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				soundAnalyzer.updateSignal();
			}
		};
		updateTimer.scheduleAtFixedRate(updateTask, 0, 25);
		playback.start();
	}

	public static class TextVisualizer implements Visualizer<AnalyzedFrame> {

		@Override
		public Map<String, Object> getConfig() {
			return null;
		}

		@Override
		public void update(AnalyzedFrame frame) {
			System.out.println(Arrays.toString(frame.getOctaveBins()));
		}
	}

}
