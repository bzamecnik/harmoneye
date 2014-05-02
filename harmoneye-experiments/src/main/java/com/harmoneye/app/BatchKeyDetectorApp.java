package com.harmoneye.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.harmoneye.music.KeyDetector;
import com.harmoneye.music.chord.ChordLabels;
import com.harmoneye.music.chord.TimedChordLabel;

public class BatchKeyDetectorApp {

	private String basePath;
	private URI baseURI;

	public BatchKeyDetectorApp(String basePath) {
		this.basePath = basePath;
		this.baseURI = URI.create(new File(basePath).getAbsolutePath());
	}

	public static void main(String[] args) throws FileNotFoundException,
		URISyntaxException {
		String path = args.length > 0 ? args[0] : ".";
		new BatchKeyDetectorApp(path).process();
	}

	private void process() throws FileNotFoundException {
		processPathRecursively(basePath);
	}

	private void processPathRecursively(String path)
		throws FileNotFoundException {
		File file = new File(path);
		if (file.isDirectory()) {
			processDirectory(file);
		} else {
			detectKeyInFile(file.getAbsolutePath());
		}
	}

	private void processDirectory(File dir) throws FileNotFoundException {
		for (final File fileEntry : dir.listFiles()) {
			processPathRecursively(fileEntry.getAbsolutePath());
		}
	}

	private void detectKeyInFile(String filePath) throws FileNotFoundException {
		KeyDetector keyDetector = new KeyDetector();
		List<TimedChordLabel> chordLabels = new ChordLabels()
			.parseTimedChords(new FileReader(filePath));
		String key;
		if (!chordLabels.isEmpty()) {
			key = String.valueOf(keyDetector.findTonic(chordLabels));
		} else {
			key = "X";
		}
		URI relativePath = baseURI.relativize(URI.create(filePath));
		System.out.println(key + " " + relativePath.toString().replaceAll("\\.lab$", ""));
	}
}
