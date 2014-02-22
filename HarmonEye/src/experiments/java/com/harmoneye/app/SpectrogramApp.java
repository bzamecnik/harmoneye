package com.harmoneye.app;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import processing.core.PApplet;
import processing.core.PImage;

import com.harmoneye.app.spectrogram.AudioReader;
import com.harmoneye.app.spectrogram.SampledAudio;
import com.harmoneye.app.spectrogram.Spectrograph;
import com.harmoneye.app.spectrogram.Spectrograph.Spectrogram;
import com.harmoneye.audio.RmsCalculator;

public class SpectrogramApp extends PApplet {
	private static final long serialVersionUID = -1188263388156753697L;

	public static void main(String args[]) {
		PApplet.main(SpectrogramApp.class.getName(), args);
	}

	private boolean guiEnabled = true;
	// private Spectrogram spectrogram;
	private String inputFile;
	private String outputFile;
	private SampledAudio audio;
	private int windowSize = 2*1024;
	private PImage spectrumImage;
	private Spectrogram spectrogram;
	private Spectrograph spectrograph;

	public void setup() {
		try {
			prepareOptions();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}

		size(640, 512, JAVA2D);
//		if (frame != null) {
//			frame.setResizable(true);
//		}

		if (!guiEnabled) {
			System.setProperty("java.awt.headless", "true");
		}

		audio = new AudioReader().readAudio(inputFile);
		spectrograph = new Spectrograph(windowSize);
		spectrumImage = prepareSpectrum(audio);

		colorMode(HSB, 1.0f);
		smooth();
		noLoop();
	}

	private PImage prepareSpectrum(SampledAudio audio) {
		spectrogram = spectrograph.computeSpectrogram(audio);

		int frames = spectrogram.getFrameCount();
		int frequencies = spectrogram.getBinCount() / 2;
		PImage image = new PImage(frames, frequencies);

		colorMode(HSB, 1.0f);

		double[] magnitudeSpectrum = new double[spectrogram.getBinCount()];
		for (int x = 0; x < frames; x++) {
			magnitudeSpectrum = spectrogram.getMagnitudeFrame(x,
				magnitudeSpectrum);
			for (int y = 0; y < frequencies; y++) {
				int i = y * frames + x;
				image.pixels[i] = color((float) magnitudeSpectrum[y]);
			}
			System.out.println(100 * x / (float)frames + " %");
		}
		return image;
	}

	private void prepareOptions() throws Exception {
		Options options = new Options();
		options.addOption("i", true, "input file");
		options.addOption("o", true, "output file");
		options.addOption("n", "no-gui", false, "No GUI (headless mode)");
		options.addOption("w", "window-size", true, "Window size");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		guiEnabled = !cmd.hasOption("no-gui");
		inputFile = cmd.getOptionValue("i");
		outputFile = cmd.getOptionValue("o");
		if (cmd.hasOption("w")) {
			windowSize = Integer.valueOf(cmd.getOptionValue("w"));
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
		if (outputFile != null) {
			spectrumImage.save(savePath(outputFile));
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
