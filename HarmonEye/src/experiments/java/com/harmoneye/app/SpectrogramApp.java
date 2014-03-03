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
	private boolean animationEnabled;
	private int windowSize = 2 * 1024;
	private double overlapRatio = 0;

	private boolean reassignmentEnabled;

	private MagnitudeSpectrogram magSpectrogram;

	public void setup() {
		try {
			prepareOptions();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}

		size(640, 512, JAVA2D);

		if (!guiEnabled) {
			System.setProperty("java.awt.headless", "true");
		}

		audio = new AudioReader().readAudio(inputFile);
		if (reassignmentEnabled) {
			spectrograph = new PhaseDiffReassignedSpectrograph(windowSize,
				overlapRatio, audio.getSampleRate());
		} else {
			spectrograph = new BasicSpectrograph(windowSize, overlapRatio);
		}
		MagnitudeSpectrogram spectrogram = spectrograph
		.computeMagnitudeSpectrogram(audio);
		magSpectrogram = spectrogram;
		spectrumImage = prepareSpectrumImage(magSpectrogram);

		colorMode(HSB, 1.0f);
		smooth();
		if(!animationEnabled) {
			noLoop();
		}
	}

	public void draw() {
		background(1.0f);

		if(animationEnabled) {
			drawSpectrumPlotAnimation();
		} else {
			drawSpectrumImage();
		}

		// drawWaveForm(audio);

		// saveFrame(outputFile);
		 if (!guiEnabled) {
			 exit();
		 }
	}

	private void drawSpectrumImage() {
		pushMatrix();


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

	private void drawSpectrumPlotAnimation() {
		pushMatrix();

		scale(1, -1);
		translate(0, -height);

		int binCount = magSpectrogram.getBinCount();
		float xScale = width / (float) binCount;
		float yScale = height;

		int frameIndex = frameCount % magSpectrogram.getFrameCount();
		double[] spectrumFrame = magSpectrogram.getFrame(frameIndex);
		for (int i = 1; i < spectrumFrame.length; i++) {
			float prevValue = (float) spectrumFrame[i - 1];
			float value = (float) spectrumFrame[i];
			line((i - 1) * xScale, prevValue * yScale, i * xScale, value
				* yScale);
		}

		popMatrix();
	}

	}

	private PImage prepareSpectrumImage(MagnitudeSpectrogram magSpectrogram) {
		System.out.println("Converting spectrogram to image.");
		int frames = magSpectrogram.getFrameCount();
		int frequencies = magSpectrogram.getBinCount();
		PImage image = new PImage(frames, frequencies);

		colorMode(HSB, 1.0f);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		for (int x = 0; x < frames; x++) {
			double[] magnitudeSpectrum = magSpectrogram.getFrame(x);
			for (int y = 0; y < frequencies; y++) {
				int i = (frequencies - y - 1) * frames + x;
				image.pixels[i] = color((float) magnitudeSpectrum[y]);
			}
			
		}
		stopWatch.stop();
		System.out.println("Computed spectrogram image in "
			+ stopWatch.getTime() + " ms");
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
		options.addOption("a", "anim", false, "Animation");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		animationEnabled = cmd.hasOption("anim");
		guiEnabled = !cmd.hasOption("no-gui") || animationEnabled;
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
			outputFile = inputFile.replaceAll("\\.[a-zA-Z0-9]+$", "_win_"
				+ windowSize + "_overlap_" + overlapRatio
				+ (reassignmentEnabled ? "_ra" : "") + ".png");
		}
	}

	public boolean displayable() {
		return guiEnabled;
	}

}
