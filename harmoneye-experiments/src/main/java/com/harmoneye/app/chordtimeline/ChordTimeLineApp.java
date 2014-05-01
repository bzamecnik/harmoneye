package com.harmoneye.app.chordtimeline;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import sojamo.drop.DropEvent;
import sojamo.drop.SDrop;

import com.harmoneye.music.KeyAwarePitchClassSetIndexer;
import com.harmoneye.music.PitchClassNamer;
import com.harmoneye.music.PitchClassSet;
import com.harmoneye.music.TonicDistance;
import com.harmoneye.music.chord.ChordLabel;
import com.harmoneye.music.chord.ChordLabels;
import com.harmoneye.music.chord.TimedChordLabel;
import com.harmoneye.p5.PanZoomController;

public class ChordTimeLineApp extends PApplet {

	private static final long serialVersionUID = 6524424444348489408L;

	String dir = "/Users/bzamecnik/Documents/harmoneye-labs/music-information-retrieval/columbia-music-signal-processing/data/beatles/chordlabs/";
	String fileName = dir + "A_Hard_Day_s_Night/05-And_I_Love_Her.lab";

	SDrop sdrop;

	List<TimedChordLabel> chordLabels = new ArrayList<TimedChordLabel>();
	double totalTime;
	int tonic = 4;

	TonicDistance tonicDist = new TonicDistance(12);
	KeyAwarePitchClassSetIndexer chordIndexer = new KeyAwarePitchClassSetIndexer();

	PitchClassNamer pitchClassNamer = PitchClassNamer.defaultInstance();

	String MODE_LINEAR = "LINEAR";
	String MODE_FIFTHS = "FIFTHS";
	String MODE_TONIC_DIST = "TONIC_DIST";

	String mode = MODE_TONIC_DIST;
	int diatonicStep = 1;

	private PanZoomController panZoomController;

	public static void main(String[] args) {
		ChordTimeLineApp applet = new ChordTimeLineApp();

		PApplet.runSketch((String[]) PApplet
			.concat(new String[] { ChordTimeLineApp.class.getName() }, args),
			applet);
	}

	public void setup() {
		size(1000, 240);

		panZoomController = new PanZoomController(this);
		
		sdrop = new SDrop(this);
		loadFile(fileName);

		// println(chordLabels);
		// println(totalTime);

		colorMode(HSB, 1.0f);
		smooth();

		noLoop();
	}

	public void draw() {
		background(0.15f);

		translate(0, height);
		scale(1, -1);
		
		PVector pan = panZoomController.getPan();
		translate(pan.x, pan.y);
		float scale = panZoomController.getScale();
		scale(scale, 1);

		float xScale = (float) (width / totalTime);
		float yScale = height / 12.0f;

		rectMode(CORNER);

		fill(0.2f);
		noStroke();
		if (mode.equals(MODE_TONIC_DIST)) {
			fill(0, 0.2f, 0.15f);
			rect(0, 7 * yScale, width, 5 * yScale);
			fill(0.125f, 0.2f, 0.2f);
			rect(0, 5 * yScale, width, 2 * yScale);
			fill(0.25f, 0.2f, 0.2f);
			rect(0, 0, width, 5 * yScale);
			fill(0.25f, 0.2f, 0.3f);
			rect(0, 0, width, yScale);
		} else if (mode.equals(MODE_LINEAR)) {
			for (int i = 0; i < 12; i++) {
				int t = ((i - tonic + 12)) % 12;
				if (tonicDist.distanceInt(i, 0) < 7) {
					fill(0.25f, 0.2f, i == 0 ? 0.3f : 0.2f);
					rect(0, i * yScale, width, yScale);
				}
			}
		}

//		stroke(0);

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
				// rect(xStart, height*(1-dist), xSize, height);
				int root = chordLabel.getRoot();
				for (Integer tone : tones) {
					fill(tonicDist.distanceToHue(tonicDist
						.distance(tone, tonic)), 0.5f, tone == root ? 1 : 0.5f);
					int t = tone;
					if (mode.equals(MODE_LINEAR)) {
						t = ((tone - tonic + 12)) % 12;
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
					float xSize = xEnd - xStart;
//					if (xSize <= 2.0f) {
//						noStroke();
//					} else {
//						stroke(0);
//					}
					rect(xStart, t * yScale, xSize, yScale);
				}
			}
		}
	}

	public void keyPressed() {
		if (key == 'w') {
			shiftTonic(7);
		} else if (key == 's') {
			shiftTonic(5);
		} else if (key == 'q') {
			shiftTonic(1);
		} else if (key == 'a') {
			shiftTonic(11);
		} else if (key == 'm') {
			if (mode.equals(MODE_LINEAR)) {
				mode = MODE_TONIC_DIST;
				redraw();
			} else if (mode.equals(MODE_TONIC_DIST)) {
				mode = MODE_LINEAR;
				redraw();
			}
		} else if (key == 'd') {
			diatonicStep = 1 + (diatonicStep % 2);
			redraw();
		} else if (key == 'r') {
			resetPanZoom();
		}
		//panZoomController.keyPressed();
	}

	public void mouseDragged() {
		panZoomController.mouseDragged();
	}

	public void mouseClicked() {
		// TODO: fix it for non-zero pan/zoom
		double time = totalTime * mouseX / width;
		int labelCount = chordLabels.size();
		for (int i = 0; i < labelCount; i++) {
			TimedChordLabel label = chordLabels.get(i);
			if (label.getEndTime() > time) {
				ChordLabel chordLabel = label.getChordLabel();
				List<Integer> tones = chordLabel.getTones();
				String chordTitle = chordLabel.getTitle();
				println(time + " " + chordTitle + " " + tones);
				break;
			}
		}
	}

	void shiftTonic(int offset) {
		setTonic((tonic + offset) % 12);
		redraw();
	}

	void loadFile(String fileName) {
		if (!fileName.endsWith(".lab")) {
			println("Unsupported file format: " + fileName);
			return;
		}

		this.fileName = fileName;
		try {
			Reader reader = createReader(fileName);
			chordLabels = new ChordLabels().parseTimedChords(reader);
			if (!chordLabels.isEmpty()) {
				totalTime = chordLabels.get(chordLabels.size() - 1)
					.getEndTime();
				setTonic(findTonic(chordLabels));
			}
			
			resetPanZoom();
		} catch (Exception ex) {
			ex.printStackTrace();
			// exit();
		}

		redraw();
	}

	private void resetPanZoom() {
		panZoomController.setPan(new PVector(0, 0));
		panZoomController.setScale(1);
		redraw();
	}

	void setTonic(int tonic) {
		this.tonic = tonic;
		tonicChanged();
	}

	void tonicChanged() {
		frame.setTitle(fileName.replaceAll(".*/", "") + " (key: "
			+ pitchClassNamer.getName(tonic) + ")");
	}

	public void dropEvent(DropEvent event) {
		if (event.isFile()) {
			loadFile(event.toString());
		}
	}

	int findTonic(List<TimedChordLabel> labels) {
		double[] keyIndexSums = new double[12];
		int size = labels.size();
		// try to find the first key in case of later modulations
		int searchedSize = (int) Math.round(size * 0.25);
		for (int i = 0; i < searchedSize; i++) {
			TimedChordLabel label = labels.get(i);
			List<Integer> tones = label.getChordLabel().getTones();
			PitchClassSet pcs = PitchClassSet.fromSet(new HashSet<Integer>(
				tones));
			double weight = (label.getEndTime() - label.getStartTime()) / totalTime;
			for (int tonic = 0; tonic < 12; tonic++) {
				int index = chordIndexer.getIndex(pcs, tonic);
				keyIndexSums[tonic] += weight * index;
			}
		}
		println("keyIndexSums: " + Arrays.toString(keyIndexSums));
		int minKeyIndex = 0;
		double keyIndexSum = Double.MAX_VALUE;
		for (int i = 0; i < 12; i++) {
			if (keyIndexSums[i] < keyIndexSum) {
				keyIndexSum = keyIndexSums[i];
				minKeyIndex = i;
			}
		}
		println("best key: " + minKeyIndex);
		return minKeyIndex;
	}

}