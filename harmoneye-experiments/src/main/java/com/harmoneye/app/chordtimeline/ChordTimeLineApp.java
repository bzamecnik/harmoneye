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

public class ChordTimeLineApp extends PApplet {

	private static final long serialVersionUID = 6524424444348489408L;

	private String dir = "/Users/bzamecnik/Documents/harmoneye-labs/music-information-retrieval/columbia-music-signal-processing/data/beatles/chordlabs/";
	private String fileName = dir + "A_Hard_Day_s_Night/05-And_I_Love_Her.lab";

	private List<TimedChordLabel> chordLabels = new ArrayList<TimedChordLabel>();
	private double totalTime;
	private int tonic = 4;

	private TonicDistance tonicDist = new TonicDistance(12);

	private PitchClassNamer pitchClassNamer = PitchClassNamer.defaultInstance();

	private String MODE_LINEAR = "LINEAR";
	private String MODE_FIFTHS = "FIFTHS";
	private String MODE_TONIC_DIST = "TONIC_DIST";

	private String mode = MODE_TONIC_DIST;
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
				if (tonicDist.distanceInt(i, 0) < 7) {
					fill(0.25f, 0.2f, i == 0 ? 0.3f : 0.2f);
					rect(0, i * yScale, width, yScale);
				}
			}
		} else if (mode.equals(MODE_FIFTHS)) {
			fill(0.125f, 0.2f, 0.2f);
			rect(0, 11 * yScale, width, yScale);
			fill(0.25f, 0.2f, 0.2f);
			rect(0, 7 * yScale, width, 4 * yScale);
			fill(0.25f, 0.2f, 0.3f);
			rect(0, 6 * yScale, width, yScale);
			fill(0.125f, 0.2f, 0.2f);
			rect(0, 5 * yScale, width, yScale);
			fill(0, 0.2f, 0.15f);
			rect(0, 0, width, 5 * yScale);
		}

		// stroke(0);

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
					float hue = tonicDist.distanceToHue(tonicDist
						.distance(tone, tonic));
					fill(hue, 0.5f, tone == root ? 1 : 0.5f);
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
					float xSize = xEnd - xStart;
					// if (xSize <= 2.0f) {
					// noStroke();
					// } else {
					// stroke(0);
					// }
					rect(xStart, t * yScale, xSize, yScale);
				}
			}
		}
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
		} else if (key == 'm') {
			if (mode.equals(MODE_LINEAR)) {
				mode = MODE_FIFTHS;
				redraw();
			} else if (mode.equals(MODE_FIFTHS)) {
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
		// panZoomController.keyPressed();
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
				setTonic(keyDetector.findTonic(chordLabels));
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

	

}