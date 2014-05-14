package com.harmoneye.app.chordtimeline;

import java.awt.event.KeyEvent;
import java.io.Reader;
import java.util.ArrayList;
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

	private Minim minim;
	private AudioPlayer audioPlayer;

	double currentMillis;

	private String MODE_LINEAR = "LINEAR";
	private String MODE_FIFTHS = "FIFTHS";
	private String MODE_TONIC_DIST = "TONIC_DIST";

	private String mode = MODE_FIFTHS;
	private int diatonicStep = 1;

	private PanZoomController panZoomController;

	private KeyDetector keyDetector = new KeyDetector();

	public static void main(String[] args) {
		ChordTimeLineApp applet = new ChordTimeLineApp();

		PApplet.runSketch((String[]) PApplet
			.concat(new String[] { ChordTimeLineApp.class.getName() }, args),
			applet);
	}

	public void setup() {
		size(1000, 240);

		panZoomController = new PanZoomController(this);

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
		if (audioPlayer.isPlaying()) {
			double audioTotalTime = audioPlayer.length() * 0.001;
			currentMillis = audioPlayer.position() * totalTime / audioTotalTime;
		}
		TimedChordLabel currentLabel = labelForTime(0.001 * currentMillis);

		background(0.15f);

		pushMatrix();

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
		if (mode.equals(MODE_TONIC_DIST)) {
			fill(0, 0.2f, 0.15f);
			rect(0, 7 * yScale, scaleX * width, 5 * yScale);
			fill(0.125f, 0.2f, 0.2f);
			rect(0, 5 * yScale, scaleX * width, 2 * yScale);
			fill(0.25f, 0.2f, 0.2f);
			rect(0, 0, scaleX * width, 5 * yScale);
			fill(0.25f, 0.2f, 0.3f);
			rect(0, 0, scaleX * width, yScale);
		} else if (mode.equals(MODE_LINEAR)) {
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
				// float dist = tonicDist.distance(chordLabel.getRoot(), tonic);
				// float dist = (float)chordIndexer.getLogIndex(tones);
				// //println(dist);
				// fill(tonicDist.distanceToHue(dist), 0.5f, 1);
				// float xSize = xEnd - xStart;
				// if (xSize <= 2.0f) {
				// noStroke();
				// } else {
				// stroke(0);
				// }
				// rect(scaleX*xStart, height*(1-dist), scaleX*xSize, height);
				int root = chordLabel.getRoot();
				boolean hightlightEnabled = currentLabel == null
					|| currentLabel == label;
				for (Integer tone : tones) {
					float hue = tonicDist.distanceToHue(tonicDist
						.distance(tone, tonic));
					float brightness = tone == root ? 1 : 0.5f;
					brightness *= hightlightEnabled ? 1 : 0.8f;
					fill(hue, 0.5f, brightness);
					int t = toneToIndex(tone);
					float xSize = xEnd - xStart;
					// if (xSize <= 2.0f) {
					// noStroke();
					// } else {
					// stroke(0);
					// }
					rect(scaleX * xStart, t * yScale, scaleX * xSize, yScale);
				}
			}
		}

		popMatrix();

		drawPositionMarker();

		popMatrix();

		drawChordTitle(currentLabel);
	}

	private void drawPositionMarker() {
		double relativePosition = currentMillis / (totalTime * 1000);
		float scaleX = panZoomController.getScale();
		float markerX = (float) (scaleX * width * relativePosition);
		stroke(0, 0, 1, 0.5f);
		line(markerX, 0, markerX, height);
	}

	private void drawChordTitle(TimedChordLabel label) {
		if (label != null) {
			String title = relativeChordTitle(label.getChordLabel(), tonic);
			fill(0, 0, 1, 0.5f);
			textAlign(CENTER, CENTER);
			textSize(100);
			text(title, width / 2, height - 65);
		}
	}

	private String relativeChordTitle(ChordLabel chordLabel, int tonic) {
		if (chordLabel.getTones().isEmpty()) {
			return "";
		}
		String title = chordLabel.getTitle();
		String[] parts = title.split(":");
		int root = chordLabel.getRoot();
		int relativeRoot = (root - tonic + 12) % 12;
		String rootTitle = romanFlatNamer.getName(relativeRoot);
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
		// int t = ((tone - tonic + 12) * 7 + 1) % 12;
		else if (mode.equals(MODE_TONIC_DIST)) {
			t = tonicDist.distanceInt(tone, tonic);
			if (t < 7) {
				t = (t * diatonicStep) % 7;
			}// else {
				// t = (((t - 7) * 2) % 7) + 7;
				// }
		}
		return t;
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
				// mode = MODE_TONIC_DIST;
				mode = MODE_LINEAR;
			}
			// else if (mode.equals(MODE_TONIC_DIST)) {
			// mode = MODE_LINEAR;
			// }
		} else if (key == 'd') {
			diatonicStep = 1 + (diatonicStep % 2);
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
		panZoomController.mouseDragged();
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
		panZoomController.setPan(new PVector(0, 0));
		panZoomController.setScale(1);
		redraw();
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
}
