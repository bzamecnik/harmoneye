package com.harmoneye.app.chordtimeline;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

import com.harmoneye.music.KeyDetector;
import com.harmoneye.music.PitchClassNamer;
import com.harmoneye.music.chord.ChordLabel;
import com.harmoneye.music.chord.ChordLabels;
import com.harmoneye.music.chord.TimedChordLabel;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;

public class SyncPlaybackApp extends PApplet {

	private static final long serialVersionUID = 6524424444348489408L;

	private String dir = "/Users/bzamecnik/Documents/harmoneye-labs/music-information-retrieval/columbia-music-signal-processing/data/beatles/";
	private String labelFileName = dir
		+ "chordlabs/A_Hard_Day_s_Night/05-And_I_Love_Her.lab";
	private String audioFileName = dir
		+ "mp3s-32k/A_Hard_Day_s_Night/05-And_I_Love_Her.mp3";

	private List<TimedChordLabel> chordLabels = new ArrayList<TimedChordLabel>();
	private double totalMillis;

	private Minim minim;
	private AudioPlayer audioPlayer;

	private KeyDetector keyDetector = new KeyDetector();
	private int tonic = 0;
	private PitchClassNamer pitchClassNamer = PitchClassNamer
		.romanNumeralFlat();

	public static void main(String[] args) {
		SyncPlaybackApp applet = new SyncPlaybackApp();

		PApplet.runSketch((String[]) PApplet
			.concat(new String[] { SyncPlaybackApp.class.getName() }, args),
			applet);
	}

	public void setup() {
		size(1000, 240);

		minim = new Minim(this);
		audioPlayer = minim.loadFile(audioFileName);
		loadChordLabels(labelFileName);

		println(chordLabels);
		println(totalMillis);

		colorMode(HSB, 1.0f);
		smooth();
	}

	public void draw() {
		background(0.15f);
		double currentMillis = audioPlayer.position();
		double totalMillis = audioPlayer.length();
		double relativePosition = currentMillis / totalMillis;

		float markerX = (float) (width * relativePosition);
		stroke(0, 0, 1, 0.75f);
		line(markerX, 0, markerX, height);

		TimedChordLabel label = labelForTime(0.001 * currentMillis);
		if (label != null) {
			String title = relativeChordTitle(label.getChordLabel(), tonic);
			textAlign(CENTER, CENTER);
			textSize(100);
			text(title, width / 2, height / 2);
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
		String rootTitle = pitchClassNamer.getName(relativeRoot);
		if (parts.length > 1) {
			rootTitle = rootTitle + ":" + parts[1];
		}
		return rootTitle;
	}

	public void keyPressed() {
		if (key == ' ') {
			togglePlayback();
		} else if (key == CODED && keyCode == BACKSPACE) {
			audioPlayer.pause();
			audioPlayer.rewind();
		} else if (key == 'q') {
			tonic = (tonic + 1) % 12;
		} else if (key == 'a') {
			tonic = (tonic + 11) % 12;
		}
	}

	private void togglePlayback() {
		if (audioPlayer.isPlaying()) {
			audioPlayer.pause();
		} else {
			audioPlayer.play();
		}
	}

	public void mouseClicked() {
		double selectedMillis = totalMillis * mouseX / width;
		double diff = selectedMillis - audioPlayer.position();
		audioPlayer.skip((int) Math.round(diff));
	}

	// O(N), could be O(log(N))
	private TimedChordLabel labelForTime(double timeSecs) {
		int length = chordLabels.size();
		for (int i = 0; i < length; i++) {
			TimedChordLabel label = chordLabels.get(i);
			if (label.getEndTime() >= timeSecs) {
				return label;
			}
		}
		return null;
	}

	private void loadChordLabels(String fileName) {
		if (!fileName.endsWith(".lab")) {
			println("Unsupported file format: " + fileName);
			return;
		}
		try {
			Reader reader = createReader(fileName);
			chordLabels = new ChordLabels().parseTimedChords(reader);
			if (!chordLabels.isEmpty()) {
				totalMillis = 1000 * chordLabels.get(chordLabels.size() - 1)
					.getEndTime();
			}
			tonic = keyDetector.findTonic(chordLabels);
		} catch (Exception ex) {
			ex.printStackTrace();
			// exit();
		}
	}
}