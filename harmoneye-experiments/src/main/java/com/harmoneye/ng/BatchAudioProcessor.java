package com.harmoneye.ng;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import com.harmoneye.analysis.StreamingReassignedSpectrograph;
import com.harmoneye.app.spectrogram.AudioReader;
import com.harmoneye.app.spectrogram.SampledAudio;
import com.harmoneye.music.ChromaKeyDetector;
import com.harmoneye.ng.ChromaExtractor.Chroma;
import com.harmoneye.ng.RmsExtractor.RmsFeature;
import com.harmoneye.ng.SpectrumExtractor.MagnitudeSpectrum;
import com.harmoneye.p5.PanZoomController;

public class BatchAudioProcessor {

	private static final String inputFile =
		"../midi/test/minor7Chords.wav";
//		"/Users/bzamecnik/Documents/harmoneye-labs/music-information-retrieval/GTZAN Genre Collection/genres/reggae/reggae.00001.au";
//		"bossa-loop-097_1.wav";
//		"/Users/bzamecnik/Documents/harmoneye-labs/music-information-retrieval/GTZAN Genre Collection/genres/classical/classical.00000.au";
//		"../midi/test/minor7Chords.wav";
//	 "c-scale-piano-mono.wav";
//	 "../mp3/01-Mourn.mp3";
//	"tartini-example-violin.wav";
//		"/Users/bzamecnik/Documents/harmoneye-labs/music-information-retrieval/columbia-music-signal-processing/data/beatles/mp3s-32k/Help_/12-I_ve_Just_Seen_A_Face.mp3";

	private SampledAudio audio;
	private FeatureObserver<Object> observer;
	private int windowSize;

	public BatchAudioProcessor(SampledAudio audio,
		FeatureObserver<Object> observer) {
		this.audio = audio;
		this.observer = observer;
		windowSize = 4096;
	}

	public void process() {
		BasicAudioFrameProvider audioProvider = new BasicAudioFrameProvider(
			audio, windowSize, 4);
		// RmsExtractor extractor = new RmsExtractor();
		
//		ShortTimeFourierTransform stft = new ShortTimeFourierTransform(
//			windowSize, new BlackmanWindow());
//		SpectrumExtractor extractor = new SpectrumExtractor(stft, windowSize);
		
		StreamingReassignedSpectrograph chromagraph = new StreamingReassignedSpectrograph(windowSize,
		audio.getSampleRate());
		ChromaExtractor extractor = new ChromaExtractor(
			chromagraph, windowSize);

		int frameCount = audioProvider.getFrameCount();
		
		Map<String, Object> summary = new HashMap<String, Object>();
		summary.put("frameCount", frameCount);
		summary.put("durationMillis", audio.getDurationMillis());
		summary.put("binsPerOctave", chromagraph.getBinsPerOctave());
		summary.put("binsPerTone", chromagraph.getBinsPerTone());
		summary.put("octaveStartTone", chromagraph.getOctaveBinShift());
		summary.put("title", inputFile.replaceFirst(".*/([^/]+)$", "$1"));
		observer.summary(summary);
		
		double[] samples = null;
		for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
			samples = audioProvider.getFrame(frameIndex, samples);
			Chroma feature = extractor.extract(samples);
			observer.update(frameIndex, feature);
		}
	}

	public static void main(String[] args) {
//		final P5Plot observer = new P5Plot();

		// setup() must run before update() on the PApplet

//		PApplet.runSketch((String[]) PApplet.concat(new String[] { observer
//			.getClass().getName() }, args), observer);
		
		
		//final FeatureObserver<Object> observer = new TextPrintingFeatureObserver();
		final FeatureObserver<Object> observer = new KeyDetectionObserver();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				SampledAudio audio = new AudioReader().readAudio(inputFile);
				final BatchAudioProcessor processor = new BatchAudioProcessor(
					audio, observer);
				// FeatureObserver<Object> observer = new
				// TextPrintingFeatureObserver();
				processor.process();
			}
		}).start();
	}

	interface FeatureObserver<T> {
		void summary(Map<String, Object> summary);
		void update(int frameIndex, T features);
	}

	static class TextPrintingFeatureObserver implements FeatureObserver<Object> {
		@Override
		public void update(int frameIndex, Object features) {
//			System.out.println(frameIndex + "\t" + features);
			System.out.println(features);
		}

		@Override
		public void summary(Map<String, Object> summary) {
			System.out.println("summary: " + summary);
		}
	}

	static class KeyDetectionObserver implements FeatureObserver<Object> {
		private double[][] chromagram;
		private int frameCount; 
		@Override
		public void update(int frameIndex, Object features) {
			Chroma chromaFeature = (Chroma) features;
			double[] chroma = chromaFeature.getChroma();
			chromagram[frameIndex] = chroma;
			System.out.println(Arrays.toString(chroma));
			if (frameIndex == frameCount - 1) {
				detectKey(chromagram);
			}
		}

		private void detectKey(double[][] chromagram) {
			ChromaKeyDetector keyDetector = new ChromaKeyDetector();
			int tonic = keyDetector.findTonic(chromagram);
			System.out.println("key: " + tonic);
		}

		@Override
		public void summary(Map<String, Object> summary) {
			frameCount = (int) summary.get("frameCount");
			chromagram = new double[frameCount][12];
		}
	}

	
	static class P5Plot extends PApplet implements FeatureObserver<Object> {

		private volatile double[] rmsValues;
		private volatile PImage spectrogram;
		private PanZoomController panZoomController;
		private Integer totalFrameCount;
		private Double durationMillis;
		private Integer binsPerOctave;
		private Integer binsPerTone;
		private int octaveStartTone = 0;
		private Map<String, Object> summary = new HashMap<String, Object>();

		public void setup() {
			size(640, 480);
			smooth();
			colorMode(HSB, 1.0f);
			panZoomController = new PanZoomController(this);
			frameRate(20);
			noLoop();
		}

		@Override
		public void draw() {
			background(1f);
			pushMatrix();
			// scale(1, -1);
			// translate(0, -height);

			PVector pan = panZoomController.getPan();
			translate(pan.x, pan.y);
			float scale = panZoomController.getScale();
			scale(scale, 1);

			// drawRmsPlot();
			drawSpectrogram();
			
			if (durationMillis != null) {
				double durationSecs = durationMillis * 0.001f;
				float step = (float) (width / durationSecs);
				noFill();
				stroke(0, 0, 0, 0.15f);
				strokeWeight(1f / scale);
				for (int i = 0; i < durationSecs; i++) {
					float x = i * step;
					line(x, 0, x, height);
				}
			}
			
			pushMatrix();
			
			scale(1, -1);
			translate(0, -height);
			
			if (spectrogram != null && binsPerTone != null && binsPerOctave != null) {
				noFill();
				strokeWeight(1f / scale);
				float count = spectrogram.height / (float)binsPerTone;
				float ypix = height / (float)spectrogram.height;
				float ystep = binsPerTone * ypix;
				int tonesPerOctave = binsPerOctave / binsPerTone;
				for (int i = 0; i < count; i++) {
					if ((i - octaveStartTone) % tonesPerOctave == 0) {
						stroke(0, 0, 0, 0.25f);
					} else {
						stroke(0, 0, 0, 0.15f);
					}
					float y = i * ystep + 0.5f * ypix;
					line(0, y, width, y);
				}
			}
			
			popMatrix();

			popMatrix();
		}

		public void keyPressed() {
			panZoomController.keyPressed();
		}

		public void mouseDragged() {
			panZoomController.mouseDragged();
		}

		private void drawSpectrogram() {
			if (spectrogram != null) {
				image(spectrogram, 0, 0, width, height);

//				noFill();
//				stroke(0, 0, 1, 0.25f);
//				strokeWeight(0.01f);
//				int count = spectrogram.width;
//				float xstep = width / (float)count;
//				for (int i = 0; i <= count; i++) {
//					float x = i*xstep;
//					line(x, 0, x, height);
//				}
			}
			noFill();
			stroke(0, 0, 0, 0.15f);
			strokeWeight(1f / panZoomController.getScale());
			rect(0, 0, width, height);
		}

		private void drawRmsPlot() {
			if (rmsValues == null) {
				return;
			}
			for (int i = 0; i < rmsValues.length - 1; i++) {
				line(i,
					(float) rmsValues[i] * height,
					i + 1,
					(float) rmsValues[i + 1] * height);
			}
		}

		@Override
		public void summary(Map<String, Object> summary) {
			this.summary = summary;
			totalFrameCount = (Integer) summary.get("frameCount");
			durationMillis = (Double) summary.get("durationMillis");
			binsPerOctave = (Integer) summary.get("binsPerOctave");
			binsPerTone = (Integer) summary.get("binsPerTone");
			octaveStartTone = (Integer) summary.get("octaveStartTone");
			final String title = (String) summary.get("title");
			if (title != null) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.setTitle(title);
					}
				});
			}
		}
		
		@Override
		public void update(int frameIndex, Object feature) {
			if (feature instanceof RmsFeature) {
				if (rmsValues == null) {
					rmsValues = new double[frameCount];
				}
				rmsValues[frameIndex] = ((RmsFeature) feature).getRms();
			} else if (feature instanceof MagnitudeSpectrum) {
				double[] spectrum = ((MagnitudeSpectrum) feature).getSpectrum();
				updateSpectrumImage(frameIndex, totalFrameCount, spectrum);
			} else if (feature instanceof Chroma) {
				double[] chromagram = ((Chroma) feature).getPreciseChroma();
				updateSpectrumImage(frameIndex, totalFrameCount, chromagram);
			}
			redraw();
		}

		private void updateSpectrumImage(int frameIndex,
			final int totalFrameCount, double[] spectrum) {
			final int length = spectrum.length;
			if (spectrogram == null) {
				spectrogram = createImage(totalFrameCount, length, RGB);
			}

			spectrogram.loadPixels();
			// for (int f = 0; f < totalFrameCount; f++) {
			// for (int y = 0; y < length; y++) {
			// int i = y * spectrogram.width + f;
			// spectrogram.pixels[i] = color(y / (float) length, f
			// / (float) totalFrameCount, 1);
			// }
			// }

			for (int y = 0; y < length; y++) {
				int i = (length - 1 - y) * spectrogram.width + frameIndex;
				float value = (float) spectrum[y];
				spectrogram.pixels[i] = color(1 - value, 1);
			}

			spectrogram.updatePixels();
		}
	}
}
