package com.harmoneye.app.chordtimeline;

import java.awt.event.KeyEvent;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import sojamo.drop.DropEvent;
import sojamo.drop.SDrop;

import com.harmoneye.music.KeyDetector;
import com.harmoneye.music.PitchClassNamer;
import com.harmoneye.music.TonicDistance;
import com.harmoneye.music.chord.ChordLabel;
import com.harmoneye.music.chord.ChordLabels;
import com.harmoneye.music.chord.TimedChordLabel;
import com.harmoneye.p5.PanZoomController;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;

public class ChordTimeLineApp extends PApplet {

	private static final long serialVersionUID = 6524424444348489408L;

	private String dir = "/Users/bzamecnik/Documents/harmoneye-labs/music-information-retrieval/columbia-music-signal-processing/data/beatles/";
	private String labelFileName = dir
		+ "chordlabs/A_Hard_Day_s_Night/05-And_I_Love_Her.lab";
	private String audioFileName = dir
		+ "mp3s-32k/A_Hard_Day_s_Night/05-And_I_Love_Her.mp3";

	private List<TimedChordLabel> chordLabels = new ArrayList<TimedChordLabel>();
	private double totalTime;
	private int tonic = 0;

	private TonicDistance tonicDist = new TonicDistance(12);

	private PitchClassNamer englishFlatNamer = PitchClassNamer
		.defaultInstance();
	private PitchClassNamer romanFlatNamer = PitchClassNamer.romanNumeralFlat();
	private PitchClassNamer intervalNamer = PitchClassNamer.intervalFlat();

	private Minim minim;
	private AudioPlayer audioPlayer;

	double currentMillis;

	private String MODE_LINEAR = "LINEAR";
	private String MODE_FIFTHS = "FIFTHS";

	private String mode = MODE_FIFTHS;

	private KeyDetector keyDetector = new KeyDetector();

	private int toneTitleWidth;

	private ChordTimeLine chordTimeLine;
	private ToneCircle toneCircle;

	public static void main(String[] args) {
		ChordTimeLineApp applet = new ChordTimeLineApp();

		PApplet.runSketch((String[]) PApplet
			.concat(new String[] { ChordTimeLineApp.class.getName() }, args),
			applet);
	}

	public void setup() {
		size(1200, 360);

		SDrop sdrop = new SDrop(this);
		minim = new Minim(this);
		loadAudio(audioFileName);
		loadChordLabels(labelFileName);

		// println(chordLabels);
		// println(totalTime);

		colorMode(HSB, 1.0f);
		smooth();
		noLoop();
	}

	public void draw() {
		toneTitleWidth = Math.min(height * 4 / 12, 80);
		if (chordTimeLine == null) {
			chordTimeLine = new ChordTimeLine(height, width
				- (toneTitleWidth + height), toneTitleWidth, 0);
		}
		if (toneCircle == null) {
			toneCircle = new ToneCircle(width - height, 0, height / 2);
		}

		if (audioPlayer.isPlaying()) {
			double audioTotalTime = audioPlayer.length() * 0.001;
			currentMillis = audioPlayer.position() * totalTime / audioTotalTime;
		}
		TimedChordLabel currentLabel = labelForTime(0.001 * currentMillis);

		background(0.15f);

		chordTimeLine.draw(currentLabel);
		toneCircle.draw(currentLabel);

		drawToneTitles(toneTitleWidth);
	}

	private void drawToneTitles(int width) {
		fill(0.15f);
		noStroke();
		rect(0, 0, width, height);
		stroke(0.25f);
		line(width, 0, width, height);
		line(width / 2, 0, width / 2, height);

		float step = 1 / 12.0f;
		float textSize = width * 0.24f;
		textSize(textSize);
		textAlign(RIGHT, BOTTOM);
		int selectedTone = (int) Math.floor((height - mouseY) * 12.0 / height);
		for (int tone = 0; tone < 12; tone++) {
			int i = toneToIndex((tone + tonic) % 12);
			fill(0, 0, i == selectedTone ? 0.75f : 0.5f);
			String title = intervalNamer.getName(tone);
			text(title, textSize * 1.75f, (1 - i * step) * height);
		}
		textAlign(LEFT, BOTTOM);
		for (int tone = 0; tone < 12; tone++) {
			int i = toneToIndex(tone);
			fill(0, 0, i == selectedTone ? 0.75f : 0.5f);
			String title = englishFlatNamer.getName(tone);
			text(title, textSize * 2.5f, (1 - i * step) * height);
		}

	}

	private String relativeChordTitle(ChordLabel chordLabel, int tonic,
		PitchClassNamer namer) {
		if (chordLabel.getTones().isEmpty()) {
			return "";
		}
		String title = chordLabel.getTitle();
		String[] parts = title.split(":");
		int root = chordLabel.getRoot();
		int relativeRoot = (root - tonic + 12) % 12;
		String rootTitle = namer.getName(relativeRoot);
		if (parts.length > 1) {
			rootTitle = rootTitle + ":" + parts[1];
		}
		return rootTitle;
	}

	// O(N), could be O(log(N))
	private TimedChordLabel labelForTime(double timeSecs) {
		int length = chordLabels.size();
		for (int i = 0; i < length; i++) {
			TimedChordLabel label = chordLabels.get(i);
			if (label.getEndTime() > timeSecs) {
				return label;
			}
		}
		return null;
	}

	// maps a pitch class the index of the line where it should be displayed
	private int toneToIndex(int tone) {
		int t = tone;
		if (mode.equals(MODE_LINEAR)) {
			t = ((tone - tonic + 12)) % 12;
		}
		if (mode.equals(MODE_FIFTHS)) {
			t = ((tone - tonic + 12) * 7 + 6) % 12;
		}
		return t;
	}

	private float toneHue(Integer tone) {
		float hue = tonicDist.distanceToHue(tonicDist.distance(tone, tonic));
		return hue;
	}

	public void keyPressed() {
		if (keyCode == UP) {
			shiftTonic(7);
		} else if (keyCode == DOWN) {
			shiftTonic(5);
		} else if (keyCode == KeyEvent.VK_PAGE_UP) {
			shiftTonic(1);
		} else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
			shiftTonic(11);
		} else if (keyCode == RIGHT) {
			TimedChordLabel label = labelForTime(currentMillis * 0.001);
			if (label != null) {
				int index = chordLabels.indexOf(label);
				if (index >= 0 && index < chordLabels.size() - 1) {
					TimedChordLabel nextLabel = chordLabels.get(index + 1);
					skipTo(nextLabel.getStartTime() * 1000);
					redraw();
				}
			}
		} else if (keyCode == LEFT) {
			TimedChordLabel label = labelForTime(currentMillis * 0.001);
			if (label != null) {
				int index = chordLabels.indexOf(label);
				if (index >= 1 && index < chordLabels.size()) {
					TimedChordLabel prevLabel = chordLabels.get(index - 1);
					skipTo(prevLabel.getStartTime() * 1000);
					redraw();
				}
			}
		} else if (key == 'm') {
			if (mode.equals(MODE_LINEAR)) {
				mode = MODE_FIFTHS;
			} else if (mode.equals(MODE_FIFTHS)) {
				mode = MODE_LINEAR;
			}
		} else if (key == 'r') {
			resetPanZoom();
		} else if (key == ' ') {
			togglePlayback();
		} else if (keyCode == BACKSPACE) {
			audioPlayer.pause();
			audioPlayer.rewind();
			currentMillis = 0;
			noLoop();
		}
		redraw();
		// panZoomController.keyPressed();
	}

	public void mouseDragged() {
		chordTimeLine.mouseDragged();
	}

	public void mouseClicked() {
		chordTimeLine.mouseClicked();
	}

	@Override
	public void mouseMoved() {
		redraw();
	}

	private void skipTo(double selectedMillis) {
		double diff = selectedMillis - audioPlayer.position();
		audioPlayer.skip((int) Math.round(diff));
		currentMillis = selectedMillis;
	}

	private void togglePlayback() {
		if (audioPlayer.isPlaying()) {
			audioPlayer.pause();
			noLoop();
			redraw();
		} else {
			audioPlayer.play();
			loop();
		}
	}

	void shiftTonic(int offset) {
		setTonic((tonic + offset) % 12);
	}

	private void loadChordLabels(String fileName) {
		if (!fileName.endsWith(".lab")) {
			println("Unsupported file format: " + fileName);
			return;
		}

		this.labelFileName = fileName;
		try {
			Reader reader = createReader(fileName);
			chordLabels = new ChordLabels().parseTimedChords(reader);
			if (!chordLabels.isEmpty()) {
				totalTime = chordLabels.get(chordLabels.size() - 1)
					.getEndTime();
				println("labels total time: " + totalTime + " sec");
				setTonic(keyDetector.findTonic(chordLabels));
			}

			resetPanZoom();
		} catch (Exception ex) {
			ex.printStackTrace();
			// exit();
		}

		redraw();
	}

	private void loadAudio(String fileName) {
		if (audioPlayer != null) {
			audioPlayer.pause();
		}
		audioPlayer = minim.loadFile(fileName);
		println("audio total time: " + audioPlayer.length() * 0.001 + " sec");
		this.audioFileName = fileName;
		currentMillis = 0;
		titleChanged();
	}

	private void resetPanZoom() {
		if (chordTimeLine != null) {
			chordTimeLine.resetPanZoom();
		}
	}

	void setTonic(int tonic) {
		this.tonic = tonic;
		titleChanged();
	}

	void titleChanged() {
		StringBuilder sb = new StringBuilder();
		sb.append(labelFileName.replaceAll(".*/", "")).append(" / ");
		sb.append(audioFileName.replaceAll(".*/", "")).append(" ");
		sb.append("(key: " + englishFlatNamer.getName(tonic) + ")");
		frame.setTitle(sb.toString());
	}

	public void dropEvent(DropEvent event) {
		if (event.isFile()) {
			String fileName = event.toString();
			if (fileName.endsWith(".lab")) {
				loadChordLabels(fileName);
			} else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
				loadAudio(fileName);
			}
		}
	}

	private class ChordTimeLine {
		private int height;
		private int width;
		private int left;
		private int top;

		private PanZoomController panZoomController;

		public ChordTimeLine(int height, int width, int left, int top) {
			this.height = height;
			this.width = width;
			this.left = left;
			this.top = top;

			panZoomController = new PanZoomController(ChordTimeLineApp.this);
			resetPanZoom();
		}

		public void resetPanZoom() {
			panZoomController.setPan(new PVector(left, top));
			panZoomController.setScale(1);
			redraw();
		}

		public void draw(TimedChordLabel currentLabel) {
			pushMatrix();

			// translate(left, top);

			PVector pan = panZoomController.getPan();
			translate(pan.x, pan.y);
			float scaleX = panZoomController.getScale();
			// scale(scaleX, 1);

			pushMatrix();

			translate(0, height);
			scale(1, -1);

			float xScale = (float) (width / totalTime);
			float yScale = height / 12.0f;

			rectMode(CORNER);

			fill(0.2f);
			noStroke();
			if (mode.equals(MODE_LINEAR)) {
				for (int i = 0; i < 12; i++) {
					if (tonicDist.distanceInt(i, 0) < 7) {
						fill(0.25f, 0.2f, i == 0 ? 0.3f : 0.2f);
						rect(0, i * yScale, scaleX * width, yScale);
					}
				}
			} else if (mode.equals(MODE_FIFTHS)) {
				fill(0.125f, 0.2f, 0.2f);
				rect(0, 11 * yScale, scaleX * width, yScale);
				fill(0.25f, 0.2f, 0.2f);
				rect(0, 7 * yScale, scaleX * width, 4 * yScale);
				fill(0.25f, 0.2f, 0.3f);
				rect(0, 6 * yScale, scaleX * width, yScale);
				fill(0.125f, 0.2f, 0.2f);
				rect(0, 5 * yScale, scaleX * width, yScale);
				fill(0, 0.2f, 0.15f);
				rect(0, 0, scaleX * width, 5 * yScale);
			}

			stroke(0);

			for (TimedChordLabel label : chordLabels) {
				float xStart = (float) (label.getStartTime() * xScale);
				float xEnd = (float) (label.getEndTime() * xScale);
				ChordLabel chordLabel = label.getChordLabel();
				List<Integer> tones = chordLabel.getTones();
				if (!tones.isEmpty()) {
					// float dist = tonicDist.distance(chordLabel.getRoot(),
					// tonic);
					// float dist = (float)chordIndexer.getLogIndex(tones);
					// //println(dist);
					// fill(tonicDist.distanceToHue(dist), 0.5f, 1);
					// float xSize = xEnd - xStart;
					// if (xSize <= 2.0f) {
					// noStroke();
					// } else {
					// stroke(0);
					// }
					// rect(scaleX*xStart, height*(1-dist), scaleX*xSize,
					// height);
					int root = chordLabel.getRoot();
					boolean hightlightEnabled = currentLabel == label;
					float xSize = xEnd - xStart;
					if (hightlightEnabled) {
						fill(0, 0, 0.5f, 0.25f);
						noStroke();
						rect(scaleX * xStart, 0, scaleX * xSize, height);
					}
					stroke(0);
					for (Integer tone : tones) {
						float hue = toneHue(tone);
						float brightness = tone == root ? 1 : 0.5f;
						// brightness *= hightlightEnabled ? 1 : 0.8f;
						fill(hue, 0.5f, brightness);
						int t = toneToIndex(tone);

						// if (xSize <= 2.0f) {
						// noStroke();
						// } else {
						// stroke(0);
						// }
						rect(scaleX * xStart,
							t * yScale,
							scaleX * xSize,
							yScale);
					}
				}
			}

			popMatrix();

			drawPositionMarker();

			popMatrix();
		}

		private void drawPositionMarker() {
			double relativePosition = currentMillis / (totalTime * 1000);
			float scaleX = panZoomController.getScale();
			float markerX = (float) (scaleX * width * relativePosition);
			stroke(0, 0, 1, 0.5f);
			line(markerX, 0, markerX, height);
		}

		public void mouseClicked() {
			double totalMillis = 1000 * totalTime;
			float panX = panZoomController.getPan().x;
			float scaleX = panZoomController.getScale();
			double selectedMillis = totalMillis * (mouseX - panX)
				/ (scaleX * width);
			skipTo(selectedMillis);
			redraw();
		}

		public void mouseDragged() {
			panZoomController.mouseDragged();
		}
	}

	private class ToneCircle {
		private static final float TONE_COUNT = 12;

		private int radius;
		private int left;
		private int top;
		private PVector center;

		private float textSize;

		private List<Bead> beads = new ArrayList<Bead>();

		public ToneCircle(int left, int top, int radius) {
			this.radius = radius;
			this.left = left;
			this.top = top;
			this.center = new PVector(left + radius, top + radius);

			float windowRadius = 0.5f * (float) Math.min(width, height);
			float beadRadius = 0.19f * windowRadius;
			float bigRadius = 0.8f * windowRadius;
			float apexRadius = bigRadius - beadRadius;
			// apexRadius *= 0.9f;

			float toneCountInv = 1.0f / TONE_COUNT;
			for (int i = 0; i < TONE_COUNT; i++) {
				float p = i * toneCountInv;

				double angle = p * TWO_PI - HALF_PI;
				float x = (float) Math.cos(angle);
				float y = (float) Math.sin(angle);

				PVector center = new PVector(bigRadius * x, bigRadius * y);
				PVector apex = new PVector(apexRadius * x, apexRadius * y);
				Bead bead = new Bead(center, apex, beadRadius);
				beads.add(bead);
			}

			textSize = beadRadius;
		}

		public void draw(TimedChordLabel currentLabel) {
			pushMatrix();

			translate(center.x, center.y);

			fill(0.15f);
			noStroke();
			rectMode(RADIUS);
			rect(0, 0, radius, radius);

			scale(0.9f);

			drawBeadCircle(currentLabel);

			drawChordTitle(currentLabel);

			popMatrix();

			stroke(0.25f);
			line(left, 0, left, height);
		}

		private void drawBeadCircle(TimedChordLabel label) {
			ChordLabel chordLabel = label.getChordLabel();
			List<Integer> activeTones = Collections.emptyList();
			if (chordLabel != null) {
				activeTones = chordLabel.getTones();
			}

			textAlign(CENTER, CENTER);
			textSize(textSize);
			noStroke();
			ellipseMode(RADIUS);
			int step = mode == MODE_FIFTHS ? 7 : 1;
			for (int i = 0; i < 12; i++) {
				int tone = (i * step + tonic) % 12;
				Bead bead = beads.get(i);
				PVector center = bead.getCenter();
				boolean isActive = activeTones.contains(tone);
				boolean isRoot = chordLabel.getRoot() == tone;
				float brightness = isActive ? (isRoot ? 1 : 0.75f) : 0.25f;
				float saturation = isActive ? 0.5f : 0.25f;
				fill(toneHue(tone), saturation, brightness);
				ellipse(center.x, center.y, bead.getRadius(), bead.getRadius());
				String toneName = intervalNamer.getName((i * step) % 12);
				fill(0);
				text(toneName, center.x, center.y);
			}
		}

		private void drawChordTitle(TimedChordLabel label) {
			if (label != null) {
				String relTitle = relativeChordTitle(label.getChordLabel(),
					tonic,
					romanFlatNamer);
				fill(0, 0, 1, 0.5f);
				textAlign(CENTER, CENTER);
				float bigTextSize = radius * 0.3f;
				textSize(bigTextSize);
				text(relTitle, 0, 0);

				String absTitle = relativeChordTitle(label.getChordLabel(),
					0,
					englishFlatNamer);
				fill(0, 0, 1, 0.5f);
				textAlign(CENTER, CENTER);
				textSize(bigTextSize * 0.5f);
				text(absTitle, 0, bigTextSize);
			}
		}
	}

	private static class Bead {
		private PVector center;
		// connection point for the curves joining the beads inside the circle
		private PVector apex;
		private float radius;

		public Bead(PVector center, PVector apex, float radius) {
			this.center = center;
			this.apex = apex;
			this.radius = radius;
		}

		public PVector getCenter() {
			return center;
		}

		public PVector getApex() {
			return apex;
		}

		public float getRadius() {
			return radius;
		}
	}
}
