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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

import com.harmoneye.cqt.FastCqt;
import com.harmoneye.util.DoubleCircularBuffer;

public class CqtMusicPlayer extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final int TIME_PERIOD_MILLIS = 20;

	private static boolean IS_CIRCLE_ENABLED = true;
	
	private static final String INPUT_FILE_NAME = "/Users/bzamecnik/dev/harmoneye/data/wav/04-Sla-Maria-do-klastera-simple.wav";

	Spectrum spectrum = new Spectrum();
	private Timer timer;
	private Playback playback;
	private DoubleCircularBuffer amplitudeBuffer = new DoubleCircularBuffer(spectrum.getSignalBlockSize());

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
		spectrum.updateSignal(amplitudeBuffer);
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

		private FastCqt cqt = new FastCqt();
		
		private final double DB_THRESHOLD = -(20 * Math.log10(2 << (16 - 1)));
		private final int BINS_PER_HALFTONE = cqt.getBinsPerHalftone();
		private final int PITCH_BIN_COUNT = cqt.getBinsPerOctave();
		private final int OCTAVE_COUNT = cqt.getOctaveCount();

		// in samples
		private int signalBlockSize = cqt.getSignalBlockSize();

		private double[] amplitudes = new double[signalBlockSize];
		private Complex[] cqSpectrum;
		/** peak amplitude spectrum */
		private double[] amplitudeSpectrumDb;
		private double[] octaveBinsDb = new double[PITCH_BIN_COUNT];
		private double[] smoothedOctaveBinsDb = new double[PITCH_BIN_COUNT];
		private double[] normalizedOctaveBinsDb = new double[PITCH_BIN_COUNT];
		private double[] pitchClassProfileDb = new double[12];
		private double[] smoothedPitchClassProfileDb = new double[12];
		private Rectangle2D.Float line = new Rectangle2D.Float();
		private ExpSmoother binSmoother = new ExpSmoother(PITCH_BIN_COUNT, 0.4);
		private ExpSmoother pcpSmoother = new ExpSmoother(12, 0.1);
		private ExpSmoother binMaxAvgSmoother = new ExpSmoother(2, 0.1);
		
		public void updateSignal(DoubleCircularBuffer amplitudeBuffer) {
			amplitudeBuffer.readLast(amplitudes, amplitudes.length);
//			long start = System.nanoTime();
			computeAmplitudeSpectrum(amplitudes);
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
// average over octaves:
//				double value = 0;
//				for (int j = i; j < amplitudeSpectrumDb.length; j += PITCH_BIN_COUNT) {
//					value += amplitudeSpectrumDb[j];
//				}
//				value *= octaveCountInv;

//				 maximum over octaves:
				double value = 0;
				for (int j = i; j < amplitudeSpectrumDb.length; j += PITCH_BIN_COUNT) {
					value = Math.max(value, amplitudeSpectrumDb[j]);
				}

//				double value = 1;
//				for (int j = i; j < amplitudeSpectrumDb.length; j += PITCH_BIN_COUNT) {
//					value *= amplitudeSpectrumDb[j];
//				}
//				value = Math.pow(value, octaveCountInv);
				
				//	double value = amplitudeSpectrumDb[i + 0 * PITCH_BIN_COUNT];

				octaveBinsDb[i] = value;
			}

			
//			octaveBinsDb = amplitudeSpectrumDb;
//			pitchClassProfileDb = octaveBinsDb;
//			smoothedOctaveBinsDb = binSmoother.smooth(octaveBinsDb);
//			smoothedOctaveBinsDb = octaveBinsDb;

//			double max = 0;
//			double average = 0;
//			for (int i = 0; i < octaveBinsDb.length; i++) {
//				max = Math.max(max, octaveBinsDb[i]);
//				average += octaveBinsDb[i];
//			}
//			average /= octaveBinsDb.length;
//			for (int pitchClass = 0; pitchClass < 12; pitchClass++) {
//				double pitchClassAverage = 0;
//				for (int i = 0; i < BINS_PER_HALFTONE; i++) {
//					pitchClassAverage += octaveBinsDb[BINS_PER_HALFTONE * pitchClass + i];
//				}
//				pitchClassAverage /= BINS_PER_HALFTONE;
//				if (pitchClassAverage <= average) {
//					for (int i = 0; i < BINS_PER_HALFTONE; i++) {
//						octaveBinsDb[BINS_PER_HALFTONE * pitchClass + i] = 0;
//					}
//				}
//			}
//			double[] result = binMaxAvgSmoother.smooth(new double[]{max, average});
//			max = result[0];
//			average = result[1];
//			double normalizationFactor = Math.abs(max - average) > 1e-6 ? 1 / Math.abs(max - average) : 1;
//			double normalizationFactor = max > 1e-6 ? 1 / max : 1;
//			double normalizationFactor = 1;
//			for (int i = 0; i < octaveBinsDb.length; i++) {
//				normalizedOctaveBinsDb[i] = (octaveBinsDb[i] - average) * normalizationFactor;
//			}
			
//			smoothedOctaveBinsDb = binSmoother.smooth(normalizedOctaveBinsDb);
			smoothedOctaveBinsDb = binSmoother.smooth(octaveBinsDb);
//			smoothedOctaveBinsDb = normalizedOctaveBinsDb;
//			
			pitchClassProfileDb = smoothedOctaveBinsDb;

//			for (int pitchClass = 0; pitchClass < pitchClassProfileDb.length; pitchClass++) {
//				double value = 0;
//				for (int i = 0; i < BINS_PER_HALFTONE; i++) {
//					value = Math.max(value, octaveBinsDb[BINS_PER_HALFTONE * pitchClass + i]);
//				}
//				pitchClassProfileDb[pitchClass] = value;
//			}
//			smoothedPitchClassProfileDb = pcpSmoother.smooth(pitchClassProfileDb);
//			pitchClassProfileDb = smoothedPitchClassProfileDb;
			
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
			
			if (IS_CIRCLE_ENABLED) {
				drawPitchClassCircle(graphics);
			} else {
				drawPitchClassBars(graphics);
			}
		}

		private void drawPitchClassCircle(Graphics2D graphics) {
			Dimension panelSize = getSize();
			int size = (int) (0.9 * Math.min(panelSize.getWidth(), panelSize.getHeight()));
			float center = 0.5f * size;
			float x = 0.5f * ((float)panelSize.getWidth() - size);
			float y = 0.5f * ((float)panelSize.getHeight() - size);

			graphics.setColor(Color.GRAY);
			graphics.drawOval((int)x, (int)y, size, size);
			for (int i = 0; i < 12; i++) {
				double angle = 2 * Math.PI * ((i - 0.5) / 12.0);
				graphics.drawLine((int)(x + center), (int)(y + center), (int)(x + center + 0.5 * size  * Math.cos(angle)), (int)(y + center + 0.5 * size * Math.sin(angle)));
			}
			
			float arcAngleStep = 360.0f / PITCH_BIN_COUNT;
			for (int i = 0; i < PITCH_BIN_COUNT; i++) {
				float value = (float) pitchClassProfileDb[i];
				Color color = getColor(value);
				graphics.setColor(color);
				int startAngle = (int) (90 - arcAngleStep * (i - 0.5f * BINS_PER_HALFTONE));
				int barSize = (int) (value * size);
				graphics.fillArc((int)(x + (1 - value) * center), (int)(y + (1 - value) * center), barSize, barSize, startAngle, (int) -arcAngleStep);
			}
//			float arcAngleStep = 360.0f / 12;
//			for (int i = 0; i < 12; i++) {
//				float hue = (float) pitchClassProfileDb[i];
////				Color color = getColor(hue);
//				graphics.setColor(Color.getHSBColor(0, 0, hue));
//				int startAngle = (int) (90 - arcAngleStep * (i/* - 0.5f*/));
//				graphics.fillArc(x, y, size, size, startAngle, (int) -(arcAngleStep * 0.8f));
//			}
			
			graphics.setColor(getBackground());
			float centerSizeFactor = 0.1f;
			int centerSize = (int)(centerSizeFactor * size);
			int centerOffset = (size - centerSize) / 2;
			graphics.fillOval((int)(x + centerOffset), (int)(y + centerOffset), centerSize, centerSize);
			
			graphics.setColor(Color.GRAY);
			graphics.drawOval((int)(x + centerOffset), (int)(y + centerOffset), centerSize, centerSize);
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
			int binCount = pitchClassProfileDb.length / BINS_PER_HALFTONE;
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
		
		public int getSignalBlockSize() {
			return signalBlockSize;
		}
	}

	public class Playback implements Runnable {

		SourceDataLine line;
		Thread thread;
		private byte[] readBuffer;
		private static final int READ_BUFFER_SIZE_SAMPLES = 256;
		private static final int READ_BUFFER_SIZE_BYTES = 2 * READ_BUFFER_SIZE_SAMPLES;
		private static final int PLAYBACK_BUFFER_SIZE_BYTES = 8 * 1024;
		private double[] amplitudes = new double[READ_BUFFER_SIZE_SAMPLES];

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
			playbackLine.open(format, PLAYBACK_BUFFER_SIZE_BYTES);

			readBuffer = new byte[READ_BUFFER_SIZE_BYTES];

			playbackLine.start();

			int readBytesCount = 0;
			while (thread != null && readBytesCount >= 0) {
				int bytesToWriteCount = readBytesCount;
//				long start = System.nanoTime();
				while (bytesToWriteCount > 0) {
					int writtenBytesCount = playbackLine.write(readBuffer, 0, bytesToWriteCount);
					bytesToWriteCount -= writtenBytesCount;
				}
//				long stop = System.nanoTime();
//				System.out.println((stop - start) / 1000000.0);
				readBytesCount = audioInputStream.read(readBuffer);
				littleEndianBytesToDoubles(readBuffer, amplitudes);
				amplitudeBuffer.write(amplitudes);
				
			}

			playbackLine.drain();
			playbackLine.stop();
			playbackLine.close();
		}

		private byte[] stereoToMono(byte[] input) {
			byte[] output = new byte[input.length / 2];
			for (int i = 0; i < output.length; i += 2) {
				output[i] = input[2 * i];
				output[i + 1] = input[2 * i + 1];
			}
			return output;
		}

		private void littleEndianBytesToDoubles(byte[] bytes, double[] floats) {
			assert bytes.length == 2 * floats.length;
			// signed short to [-1; 1]
			float normalizationFactor = 2 / (float) 0xffff;
			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < floats.length && byteBuffer.hasRemaining(); i++) {
				floats[i] = normalizationFactor * byteBuffer.getShort();
			}
		}

		private void printByteArray(byte[] data) {
			StringBuilder sb = new StringBuilder();
			for (byte item : data) {
				sb.append(String.format("%02X, ", item));
			}
			System.out.println(sb.toString());
		}

		private void printFloatArray(float[] data) {
			StringBuilder sb = new StringBuilder();
			for (float item : data) {
				sb.append(String.format("%02f, ", item));
			}
			System.out.println(sb.toString());
		}
	}
	
	private static class ExpSmoother {
		double[] data;
		double currentWeight;
		double previousWeight;
		
		public ExpSmoother(int size, double currentWeight) {
			data = new double[size];
			this.currentWeight = currentWeight;
			previousWeight = 1 - currentWeight;
		}
		
		public double[] smooth(double[] currentFrame) {
			assert data.length == currentFrame.length;
			
			for (int i = 0; i < data.length; i++) {
				data[i] = previousWeight * data[i] + currentWeight * currentFrame[i]; 
			}
			return data;
		}
	}
}