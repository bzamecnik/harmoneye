package com.harmoneye;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.commons.math3.complex.Complex;

import com.harmoneye.cqt.Cqt;
import com.harmoneye.cqt.FastCqt;

public class CqtMusicPlayer extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final int TIME_PERIOD_MILLIS = 100;

	private static final String INPUT_FILE_NAME = "/Users/bzamecnik/dev/harmoneye/data/wav/c-scale-piano-mono.wav";
	private static final int BUFFER_SIZE = 8 * 1024;
	private static final boolean IS_STEREO = false;

	Spectrum spectrum = new Spectrum();
	private Timer timer;
	private Playback playback;
	private double[] amplitudes;

	public CqtMusicPlayer() {
		timer = new Timer(TIME_PERIOD_MILLIS, this);
		timer.setInitialDelay(190);
		timer.start();

		playback = new Playback();
		playback.start();
	}

	public void paint(Graphics g) {
//		long stop = System.nanoTime();
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2.setRenderingHints(rh);

		spectrum.paint(g2);
//		long stop = System.nanoTime();
//		System.out.println((stop - start) / 1000000.0);
	}

	public void actionPerformed(ActionEvent e) {
		amplitudes = playback.getFrameAmplitudes(amplitudes);
		spectrum.updateSignal(amplitudes);
		repaint();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("HarmonEye");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		CqtMusicPlayer player = new CqtMusicPlayer();
		frame.add(player);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	class Spectrum {

		private final double DB_THRESHOLD = -(20 * Math.log10(2 << (16 - 1)));
		private static final int BINS_PER_HALFTONE = 3;
		private static final int PITCH_BIN_COUNT = BINS_PER_HALFTONE * 12;
		private static final int OCTAVE_COUNT = 2;

		private Complex[] cqSpectrum;
		/** peak amplitude spectrum */
		private double[] amplitudeSpectrumDb;
		private double[] octaveBinsDb = new double[PITCH_BIN_COUNT];
		private double[] pitchClassProfileDb = new double[12];
		private Rectangle2D.Float line = new Rectangle2D.Float();
		private Cqt cqt = new FastCqt();

		public void updateSignal(double[] signal) {
//			long start = System.nanoTime();
			computeAmplitudeSpectrum(signal);
			computePitchClassProfile();
//			long stop = System.nanoTime();
//			System.out.println("update: " + (stop - start) / 1000000.0);
		}

		private void computeAmplitudeSpectrum(double[] signal) {
			cqSpectrum = cqt.transform(signal);
			if (amplitudeSpectrumDb == null) {
				amplitudeSpectrumDb = new double[cqSpectrum.length];
			}
			for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
				double amplitude = cqSpectrum[i].abs();
				double referenceAmplitude = 1;
				double amplitudeDb = 20 * Math.log10(amplitude / referenceAmplitude);
				if (amplitudeDb < DB_THRESHOLD) {
					amplitudeDb = DB_THRESHOLD;
				}
				double scaledAmplitudeDb = 1 + amplitudeDb / -DB_THRESHOLD;
				amplitudeSpectrumDb[i] = scaledAmplitudeDb;
			}
		}

		private void computePitchClassProfile() {
			double octaveCountInv = 1.0 / OCTAVE_COUNT;
			for (int i = 0; i < PITCH_BIN_COUNT; i++) {
				double value = 0;
				for (int j = i; j < amplitudeSpectrumDb.length; j += PITCH_BIN_COUNT) {
					value += amplitudeSpectrumDb[j];
				}
				value *= octaveCountInv;

				//	double value = amplitudeSpectrumDb[i + 0 * PITCH_BIN_COUNT];

				octaveBinsDb[i] = value;
			}

//			octaveBinsDb = amplitudeSpectrumDb;
			pitchClassProfileDb = octaveBinsDb;

			// TODO: smooth the data, eg. with a Kalman filter
			
//			for (int i = 0, pitchClass = 0; i < octaveBinsDb.length; i += BINS_PER_HALFTONE, pitchClass++) {
//				double value = 0;
//
//				double center = octaveBinsDb[i + 2];
//				if (center >= octaveBinsDb[i] &&
//					center >= octaveBinsDb[i + 1] &&
//					center >= octaveBinsDb[i + 3] &&
//					center >= octaveBinsDb[i + 4])
//				{
//					value = center;
//				}
////				double center = octaveBinsDb[i + 1];
////				if (center >= octaveBinsDb[i] &&
////					center >= octaveBinsDb[i + 2])
////				{
////					value = center;
////				}
//				pitchClassProfileDb[pitchClass] = value;
//			}
		}

		public void paint(Graphics2D graphics) {
			if (amplitudeSpectrumDb == null) {
				return;
			}
			setBackground(Color.DARK_GRAY);
			
			drawPitchClassBars(graphics);
//			drawPitchClassCircle(graphics);
		}

		private void drawPitchClassCircle(Graphics2D graphics) {
			Dimension panelSize = getSize();
			int size = (int) Math.min(panelSize.getWidth(), panelSize.getHeight());
			int x = (int) (0.5f * (panelSize.getWidth() - size));
			int y = (int) (0.5f * (panelSize.getHeight() - size));

			float arcAngleStep = 360.0f / PITCH_BIN_COUNT;
			for (int i = 0; i < PITCH_BIN_COUNT; i++) {
				float hue = (float) amplitudeSpectrumDb[i];
				Color color = getColor(hue);
				graphics.setColor(color);
				int startAngle = (int) (90 - arcAngleStep * (i/* - 0.5f*/));
				graphics.fillArc(x, y, size, size, startAngle, (int) -(arcAngleStep * (i % BINS_PER_HALFTONE == (BINS_PER_HALFTONE - 1) ? 0.8f : 1f)));
			}
			
			graphics.setColor(getBackground());
			int centerSize = (int)(0.65f * size);
			int centerOffset = (size - centerSize) / 2;
			graphics.fillOval(x + centerOffset, y + centerOffset, centerSize, centerSize);
		}

		private void drawPitchClassBars(Graphics2D graphics) {
			float x = 0;
			Dimension size = getSize();
			float height = (float) size.getHeight();
			float step = (float) size.getWidth() / (pitchClassProfileDb.length);

			
			for (int i = 0; i < pitchClassProfileDb.length; i++) {
				line.setFrameFromDiagonal(x, height, x + step - 1, (1 - pitchClassProfileDb[i]) * height);
				x += step;
				Color color = getColor((float) pitchClassProfileDb[i]);
				graphics.setColor(color);
				graphics.setPaint(color);	
				graphics.fill(line);
				graphics.draw(line);
			}
			
			graphics.setColor(Color.GRAY);
			x = 0;
			int binCount = 12;
			step = (float) size.getWidth() / binCount;
			for (int i = 0; i < binCount; i++) {
				line.setFrameFromDiagonal(x, 0, x + step - 1, height);
				x += step;
				graphics.draw(line);
			}
		}
		
		Color getColor(float value) {
			float hue = (1.8f - value) % 1.0f;
			return Color.getHSBColor(hue, 0.25f + 0.75f * value, 0.25f + 0.75f * value);
		}
	}

	public static class Playback implements Runnable {

		SourceDataLine line;
		Thread thread;
		private byte[] buffer;
		private int bufferOffset = 0;
		private int bufferBlockSize = 8 * 1024;

		public void start() {
			thread = new Thread(this);
			thread.setName("Playback");
			thread.start();
		}

		public void stop() {
			thread = null;
		}

		public void run() {
			try {
				play();
			} catch (Exception e) {
				System.err.println(e);
				thread = null;
			}
		}

		private void play() throws UnsupportedAudioFileException, IOException, Exception, LineUnavailableException {
			File file = new File(INPUT_FILE_NAME);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);

			AudioFormat format = audioInputStream.getFormat();
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			if (!AudioSystem.isLineSupported(info)) {
				throw new Exception("Line matching " + info + " not supported.");
			}

			SourceDataLine playbackLine = (SourceDataLine) AudioSystem.getLine(info);
			playbackLine.open(format, BUFFER_SIZE);

			buffer = new byte[BUFFER_SIZE];

			playbackLine.start();

			int readBytesCount = 0;
			while (thread != null && readBytesCount >= 0) {
				int bytesToWriteCount = readBytesCount;
//				long start = System.nanoTime();
				while (bytesToWriteCount > 0) {
					int writtenBytesCount = playbackLine.write(buffer, 0, bytesToWriteCount);
					bytesToWriteCount -= writtenBytesCount;
				}
//				long stop = System.nanoTime();
//				System.out.println((stop - start) / 1000000.0);
				readBytesCount = audioInputStream.read(buffer);
				bufferOffset = 0;
			}

			playbackLine.drain();
			playbackLine.stop();
			playbackLine.close();
		}

		public double[] getFrameAmplitudes(double[] amplitudes) {
//			byte[] monoSignal =  (IS_STEREO) ? stereoToMono(buffer) : buffer;
			//			printByteArray(monoSignal);
//			if (amplitudes == null) {
//				amplitudes = new double[monoSignal.length / 2];
//			}
//			littleEndianBytesToDoubles(monoSignal, amplitudes);
			
			if (amplitudes == null) {
				amplitudes = new double[bufferBlockSize];
			}
//			int startIndex = bufferOffset;
//			int endIndex = startIndex + 2 * bufferBlockSize;
//			System.err.println(startIndex + " to " + endIndex);
//			bufferOffset += BUFFER_SIZE / (2 * (8 - 1));
//			byte[] bufferBlock = Arrays.copyOfRange(buffer, startIndex, endIndex);
			byte[] bufferBlock = buffer;
			littleEndianBytesToDoubles(bufferBlock, amplitudes);
			return amplitudes;
		}

		private static byte[] stereoToMono(byte[] input) {
			byte[] output = new byte[input.length / 2];
			for (int i = 0; i < output.length; i += 2) {
				output[i] = input[2 * i];
				output[i + 1] = input[2 * i + 1];
			}
			return output;
		}

		private static void littleEndianBytesToDoubles(byte[] bytes, double[] floats) {
			// assume 16-bit values
			assert bytes.length == 2 * floats.length;
			// signed short to [-1; 1]
			float normalizationFactor = 1 / (float) 0xffff;
			for (int i = 0; 2 * i < bytes.length; i++) {
				floats[i] = normalizationFactor * (bytes[2 * i] | (bytes[2 * i + 1] << 8));
			}
		}

		private static void printByteArray(byte[] data) {
			StringBuilder sb = new StringBuilder();
			for (byte item : data) {
				sb.append(String.format("%02X, ", item));
			}
			System.out.println(sb.toString());
		}

		private static void printFloatArray(float[] data) {
			StringBuilder sb = new StringBuilder();
			for (float item : data) {
				sb.append(String.format("%02f, ", item));
			}
			System.out.println(sb.toString());
		}
	}
}