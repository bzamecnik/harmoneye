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

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;

public class SpectrogramApp extends PApplet {
	private static final int FRAME_RATE = 30;

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

	private boolean saveVideoFramesEnabled;
	private boolean movingSpectrogramEnabled;

	private Minim minim;
	private AudioPlayer player;

	private int startTime;
	private float time;

	public void setup() {
		try {
			prepareOptions();

			if (saveVideoFramesEnabled) {
				size(1280, 720, JAVA2D);
			} else {
				size(1042, 586, JAVA2D);
			}

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
			if (!animationEnabled) {
				noLoop();
			} else {
				frameRate(FRAME_RATE);
				if (movingSpectrogramEnabled && !saveVideoFramesEnabled) {
					minim = new Minim(this);
					player = minim.loadFile(inputFile);
					player.play();
					startTime = millis();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
	}

	public void draw() {
		background(1.0f);

		if (animationEnabled) {
			if (movingSpectrogramEnabled) {
				drawMovingSpectrumImage();
			} else {
				drawSpectrumPlotAnimation();
				// drawSpectrumCircleAnimation();
			}
		} else {
			drawSpectrumImage();
		}
		if (saveVideoFramesEnabled) {
			saveFrame("video-frames/######.tga");
		}

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

	private void drawMovingSpectrumImage() {
		background(0);

		float speed = (float) (1.0 / audio.getDurationMillis());
		if (!saveVideoFramesEnabled) {
			time = millis() - startTime;
		}

		float frameIndex = spectrumImage.width * (time * speed);
		if (saveVideoFramesEnabled) {
			time += 1000.0 / FRAME_RATE;
		}
		if (frameIndex >= spectrumImage.width) {
			noLoop();
		}
		// System.out.println(frameIndex + " / " + spectrumImage.width);
		// frames per width of the window
		// double framesPerWindowWidth = width;
		// int frameStartX = (int) Math.min(frameIndex - framesPerWindowWidth/2,
		// frameIndex);

		float xScalingFactor = 1; 
		float xScale = width / (spectrumImage.width * xScalingFactor);

		// continuous scrolling, start&end at the center of the screen
		// int frameStartX = Math.min(Math.max(frameIndex - width / 2, 0),
		// spectrumImage.width - width / 2);
		// int frameWidth = Math.min(Math.min(width, width / 2 + frameIndex),
		// spectrumImage.width + width / 2 - frameIndex - 1);
		// float imageStartX = Math.max(width / 2 - frameIndex, 0);

		// the window is stuck at the beginning and end and the "needle" moves
		float frameWidth = width / xScalingFactor;
		float frameStartX = (spectrumImage.width > frameWidth) ? constrain(frameIndex
			- frameWidth / 2,
			0,
			spectrumImage.width - frameWidth)
			: 0;
		float imageStartX = 0;

		// stroke(1, 0, 0.5f);
		// line(frameIndex * xScale, 0, frameIndex * xScale, height);
		//
		// stroke(0);
		// rect(frameStartX * xScale,
		// height * 0.25f,
		// frameWidth * xScale,
		// height * 0.5f);

		image(spectrumImage,
			imageStartX + (frameStartX - (int) frameStartX),
			0,
			width,
			height,
			(int) frameStartX,
			0,
			(int) (frameStartX + frameWidth),
			spectrumImage.height);

		stroke(1, 0, 0.2f);
		float needleX = (frameIndex - frameStartX) * xScalingFactor;
		line(needleX, 0, needleX, height);

		fill(1, 1, 0.85f);
		noStroke();
		line(frameIndex * xScale, 0, frameIndex * xScale, height);
		rect(0, height - 3, frameIndex * xScale, 3);
	}

	private void drawSpectrumPlotAnimation() {
		pushMatrix();

		scale(1, -1);
		translate(0, -height);

		int binCount = magSpectrogram.getBinCount();
		float xScale = width / (float) binCount;
		float yScale = height;

		stroke(0);
		int frameIndex = frameCount % magSpectrogram.getFrameCount();
		double[] spectrumFrame = magSpectrogram.getFrame(frameIndex);
		for (int i = 1; i < spectrumFrame.length; i++) {
			float prevValue = (float) spectrumFrame[i - 1];
			float value = (float) spectrumFrame[i];
			line((i - 1) * xScale, prevValue * yScale, i * xScale, value
				* yScale);
		}

		// double threshold = 2 * stdDev.evaluate(spectrumFrame, 0,
		// spectrumFrame.length);
		// stroke(1,1,1);
		// line(0, (float)(threshold * yScale), width, (float)(threshold *
		// yScale));

		popMatrix();
	}

	private void drawSpectrumCircleAnimation() {
		int frameIndex = frameCount % magSpectrogram.getFrameCount();

		strokeWeight(10);
		stroke(1, 1, 1);
		line(0,
			height,
			width * frameIndex / (float) magSpectrogram.getFrameCount(),
			height);

		pushMatrix();

		translate(width / 2, height / 2);

		double radius = min(width / 2, height / 2);

		strokeWeight(2);
		stroke(0);
		double[] spectrumFrame = magSpectrogram.getFrame(frameIndex);
		for (int i = 1; i < spectrumFrame.length; i++) {
			float value = (float) spectrumFrame[i];
			double angle = ((i * 2 / 120.0) + 1) * Math.PI;
			double r = radius * value;
			double x = r * Math.cos(angle);
			double y = r * Math.sin(angle);
			line(0, 0, (float) x, (float) y);
		}

		popMatrix();
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
		options.addOption("a", "anim", false, "Spectrum plot animation");
		options.addOption("m",
			"moving-spectrogram",
			false,
			"Moving spectrogram");
		options.addOption("v", "save-video", false, "Save video frames");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		animationEnabled = cmd.hasOption("anim");
		movingSpectrogramEnabled = cmd.hasOption("moving-spectrogram");
		saveVideoFramesEnabled = cmd.hasOption("save-video");
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

	public void stop() {
		player.close();
		minim.stop();
		super.stop();
	}
}
