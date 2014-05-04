package com.harmoneye.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;

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

	public static void main(String[] args) throws URISyntaxException,
		IOException {
		String path = args.length > 0 ? args[0] : ".";
		BatchKeyDetectorApp app = new BatchKeyDetectorApp(path);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Result result = app.process();
		stopWatch.stop();

		System.out.println("elapsed time: " + stopWatch.getTime() + " ms");

		Map<String, String> detectedKeysByFiles = result.getKeysByFiles();
		// printDetectedKeys(detectedKeysByFiles);

		Map<String, String> refKeysByFiles = app.loadReferenceData(args[1]);

		app.compareResults(detectedKeysByFiles, refKeysByFiles);
	}

	private static void printDetectedKeys(Map<String, String> keysByFiles) {
		List<String> files = new ArrayList<String>(keysByFiles.keySet());
		Collections.sort(files);
		for (String file : files) {
			String key = keysByFiles.get(file);
			System.out.println(key + " " + file);
		}
	}

	private Result process() throws FileNotFoundException {
		return processPathRecursively(basePath, new Result());
	}

	private Result processPathRecursively(String path, Result result)
		throws FileNotFoundException {
		File file = new File(path);
		if (file.isDirectory()) {
			processDirectory(file, result);
		} else {
			detectKeyInFile(file.getAbsolutePath(), result);
		}
		return result;
	}

	private void processDirectory(File dir, Result result)
		throws FileNotFoundException {
		for (final File fileEntry : dir.listFiles()) {
			processPathRecursively(fileEntry.getAbsolutePath(), result);
		}
	}

	private void detectKeyInFile(String filePath, Result result)
		throws FileNotFoundException {
		KeyDetector keyDetector = new KeyDetector();
		List<TimedChordLabel> chordLabels = new ChordLabels()
			.parseTimedChords(new BufferedReader(new FileReader(filePath)));
		String key;
		if (!chordLabels.isEmpty()) {
			key = String.valueOf(keyDetector.findTonic(chordLabels));
		} else {
			key = "X";
		}
		URI relativePath = baseURI.relativize(URI.create(filePath));
		result.getKeysByFiles().put(relativePath.toString()
			.replaceAll("\\.lab$", ""),
			key);
	}

	private Map<String, String> loadReferenceData(String filePath)
		throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			Map<String, String> keysByFiles = new HashMap<String, String>();
			String line = reader.readLine();
			while (line != null) {
				String[] parts = line.split(" ");
				if (parts.length >= 2) {
					String key = parts[0];
					String songFile = parts[1];
					keysByFiles.put(songFile, key);
				}
				line = reader.readLine();
			}
			return keysByFiles;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public void compareResults(Map<String, String> detected,
		Map<String, String> reference) {
		Set<String> allFiles = new HashSet<String>();
		allFiles.addAll(detected.keySet());
		allFiles.addAll(reference.keySet());

		int totalCount = allFiles.size();
		int matchCount = 0;

		for (String file : allFiles) {
			String detKey = detected.get(file);
			String refKey = reference.get(file);
			if (detKey != null && detKey.equals(refKey)) {
				matchCount++;
			}
		}

		System.out.println("total: " + totalCount);
		System.out.println("positive matches: " + matchCount);
		System.out.println("error count: " + (totalCount - matchCount));
		double accuracy = 100.0 * matchCount / totalCount;
		System.out.println("accuracy (%): " + accuracy);
		System.out.println();

		System.out.println("erroneus files (referece, detected, diff):");
		List<String> files = new ArrayList<String>(detected.keySet());
		Collections.sort(files);
		for (String file : files) {
			String detKey = detected.get(file);
			String refKey = reference.get(file);
			if (!detKey.equals(refKey)) {
				String diff = "?";
				try {
					diff = String.valueOf(((Integer.valueOf(refKey)
						- Integer.valueOf(detKey) + 12) % 12));
				} catch (NumberFormatException ex) {
				}
				System.out.println(file + " " + refKey + " " + detKey + " "
					+ diff);
			}
		}
	}

	static class Result {
		private Map<String, String> keysByFiles = new HashMap<String, String>();

		public Map<String, String> getKeysByFiles() {
			return keysByFiles;
		}
	}
}
