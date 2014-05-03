package com.harmoneye.app;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PApplet;
import processing.core.PImage;

import com.harmoneye.analysis.MusicAnalyzer;
import com.harmoneye.analysis.MusicAnalyzer.AnalyzedFrame;
import com.harmoneye.analysis.ReassignedMusicAnalyzer;
import com.harmoneye.audio.SoundCapture;
import com.harmoneye.math.Modulo;
import com.harmoneye.music.PitchClassNamer;
import com.harmoneye.viz.Visualizer;

public class P5Visualization {

	public static void main(String[] args) {
//		Config config = Config.fromDefault();
		// String inputFileName = config.get("inputFile");
		P5Visualizer visualizer = new P5Visualizer();
		 final MusicAnalyzer soundAnalyzer = new MusicAnalyzer(visualizer,
		 44100, 16);
//		final ReassignedMusicAnalyzer soundAnalyzer = new ReassignedMusicAnalyzer(
//			visualizer, 44100, 16);
		soundAnalyzer.initialize();

		PApplet.runSketch((String[]) PApplet
			.concat(new String[] { P5Visualizer.class.getName() }, args),
			visualizer);
		
		SoundCapture capture = new SoundCapture(soundAnalyzer, 44100, 16);
		// Playback playback = new Playback(soundAnalyzer, inputFileName);
		Timer updateTimer = new Timer("update timer");
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				soundAnalyzer.updateSignal();
			}
		};
		updateTimer.scheduleAtFixedRate(updateTask, 0, 25);
		capture.start();
		// playback.start();
		// PApplet.main(P5Visualizer.class.getName(), args);
	}

	public static class P5Visualizer extends PApplet implements
		Visualizer<AnalyzedFrame> {

		private static final long serialVersionUID = 339086927168271911L;

		private volatile AnalyzedFrame analyzedFrame;

		private volatile PImage image;
		private volatile int currentFrame = 0;
		private int lastDrawnFrame;

		PitchClassNamer pitchClassNamer = PitchClassNamer.defaultInstance();

		public void setup() {
			size(640, 480);
			smooth();
			colorMode(HSB, 1.0f);
			// noLoop();
		}

		public void draw() {
			background(0);
			// noStroke();
			// stroke(0);
			pushMatrix();
			scale(1, -1);
			translate(0, -height);
			// if (analyzedFrame == null) {
			// textAlign(CENTER);
			// text("no data", width / 2, height / 2);
			// return;
			// }

			if (image != null) {
				image(image, height/12, 0, width, height);
			}

			popMatrix();

			drawToneNames();

			// drawBarPlot(bins);
		}

		private void drawToneNames() {
			float textSize = height / (2 * 12.0f);
			textSize(textSize);
			for (int i = 0; i < 12; i++) {
				fill(1, 0.5f);
				float y = (i + 0.5f) * height / (12.0f);
				text(pitchClassNamer.getName(12 - i - 1), 0 + textSize * 0.5f, y
					+ textSize * 0.5f);
				stroke(1, 0.15f);
				line(2 * textSize, y, width, y);
			}
		}

		@Override
		public Map<String, Object> getConfig() {
			return null;
		}

		@Override
		public synchronized void update(AnalyzedFrame analyzedFrame) {
			this.analyzedFrame = analyzedFrame;
			if (analyzedFrame == null) {
				return;
			}

			double[] bins = analyzedFrame.getOctaveBins();
//			 double[] bins = analyzedFrame.getAllBins();
			if (image == null) {
				image = createImage((int) (10 * frameRate), bins.length, RGB);
			}
			image.loadPixels();
			for (int y = 0; y < bins.length; y++) {
				int i = y * image.width + currentFrame;
				image.pixels[i] = color((float) bins[y]);
			}
			image.updatePixels();
			currentFrame = Modulo.modulo(currentFrame + 1, image.width);
		}

		private void drawBarPlot(double bins[]) {
			stroke(0);
			for (int i = 0; i < bins.length; i++) {
				rect(i * width / bins.length,
					0,
					width / bins.length,
					(float) bins[i] * height);
			}
		}
	}
}
