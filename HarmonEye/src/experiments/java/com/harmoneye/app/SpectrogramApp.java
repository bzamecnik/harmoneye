package com.harmoneye.app;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.time.StopWatch;

import processing.core.PApplet;
import processing.core.PImage;

import com.harmoneye.app.spectrogram.AudioReader;
import com.harmoneye.app.spectrogram.BasicSpectrograph;
import com.harmoneye.app.spectrogram.MagnitudeSpectrogram;
import com.harmoneye.app.spectrogram.MagnitudeSpectrograph;
import com.harmoneye.app.spectrogram.PhaseDiffReassignedSpectrograph;
import com.harmoneye.app.spectrogram.SampledAudio;
import com.harmoneye.audio.RmsCalculator;

public class SpectrogramApp extends PApplet {
	private static final long serialVersionUID = -1188263388156753697L;

	public static void main(String args[]) {
		PApplet.main(SpectrogramApp.class.getName(), args);
	}

	private MagnitudeSpectrograph spectrograph;

	private String inputFile;
	private String outputFile;

	private SampledAudio audio;
	private PImage spectrumImage;

	private boolean guiEnabled = true;
	private int windowSize = 2 * 1024;
	private double overlapRatio = 0;

	private boolean reassignmentEnabled;

	public void setup() {
		try {
			prepareOptions();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}

		size(640, 512, JAVA2D);
		// if (frame != null) {
		// frame.setResizable(true);
		// }

		if (!guiEnabled) {
			System.setProperty("java.awt.headless", "true");
		}

		audio = new AudioReader().readAudio(inputFile);
		if (reassignmentEnabled) {
			spectrograph = new PhaseDiffReassignedSpectrograph(windowSize,
				overlapRatio);
		} else {
			spectrograph = new BasicSpectrograph(windowSize, overlapRatio);
		}
		spectrumImage = prepareSpectrum(audio);

		colorMode(HSB, 1.0f);
		smooth();
		noLoop();
	}

	private PImage prepareSpectrum(SampledAudio audio) {
		MagnitudeSpectrogram magSpectrogram = spectrograph
			.computeMagnitudeSpectrogram(audio);

		int frames = magSpectrogram.getFrameCount();
		int frequencies = magSpectrogram.getBinCount();
		PImage image = new PImage(frames, frequencies);

		colorMode(HSB, 1.0f);

		int lastPercent = 0;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		for (int x = 0; x < frames; x++) {
			double[] magnitudeSpectrum = magSpectrogram.getFrame(x);
			for (int y = 0; y < frequencies; y++) {
				int i = (frequencies - y - 1) * frames + x;
				image.pixels[i] = color((float) magnitudeSpectrum[y]);
			}
			float percent = 100 * x / (float) frames;
			if ((int) percent > lastPercent) {
				System.out.println(percent + " %");
				lastPercent = (int) percent;
			}
		}
		stopWatch.stop();
		System.out.println("100%");
		System.out.println("Computed spectrogram in " + stopWatch.getTime()
			+ " ms");
		return image;
	}

	private void prepareOptions() throws Exception {
		Options options = new Options();
		options.addOption("i", true, "input file");
		options.addOption("o", true, "output file");
		options.addOption("n", "no-gui", false, "No GUI (headless mode)");
		options.addOption("w", "window-size", true, "Window size");
		options.addOption("l", "overlap-factor", true, "Overlap factor");
		options.addOption("r", "reassign", false, "Spectral reassignment");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		guiEnabled = !cmd.hasOption("no-gui");
		reassignmentEnabled = cmd.hasOption("reassign");
		if (cmd.hasOption("w")) {
			windowSize = Integer.valueOf(cmd.getOptionValue("w"));
		}
		if (cmd.hasOption("l")) {
			int overlapFactor = Integer.valueOf(cmd.getOptionValue("l"));
			overlapRatio = 1 - (1.0 / overlapFactor);
		}
		inputFile = cmd.getOptionValue("i");
		if (cmd.hasOption("o")) {
			outputFile = cmd.getOptionValue("o");
		} else {
			outputFile = inputFile.replaceAll("\\.[a-z]+$", "_win_"
				+ windowSize + "_overlap_" + overlapRatio
				+ (reassignmentEnabled ? "_ra" : "") + ".png");
		}
	}

	public void draw() {
		background(1.0f);

		drawSpectrum();
		// drawWaveForm(audio);

		// saveFrame(outputFile);
		if (!guiEnabled) {
			exit();
		}
	}

	private void drawSpectrum() {
		pushMatrix();

		scale(1, -1);
		translate(0, -height);

		image(spectrumImage, 0, 0, width, height);
		System.out.println("output file:" + outputFile);
		if (outputFile != null) {
			String path = savePath(outputFile);
			System.out.println("Saving:" + path);
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			spectrumImage.save(path);
			stopWatch.stop();
			System.out.println("Saved ok in " + stopWatch.getTime() + " ms");
		}

		popMatrix();
	}

	private void drawWaveForm(SampledAudio audio) {
		pushMatrix();

		scale(1, -1);
		translate(0, -height);

		double[] amplitudes = audio.getSamples();
		int frameCount = this.width;
		int frameSize = amplitudes.length / frameCount;
		double[] amplitudeFrame = new double[frameSize];
		for (int i = 0; i < frameCount; i++) {
			System.arraycopy(amplitudes,
				i * frameSize,
				amplitudeFrame,
				0,
				frameSize);
			double rms = RmsCalculator.computeRms(amplitudeFrame);
			line(i, 0, i, (float) (this.height * rms));
		}

		popMatrix();
	}

	public boolean displayable() {
		return guiEnabled;
	}

}
