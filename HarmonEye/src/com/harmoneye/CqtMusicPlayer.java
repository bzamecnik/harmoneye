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

	private static final String INPUT_FILE_NAME = "/Users/bzamecnik/dev/harmoneye/data/wav/c-scale.wav";
	private static final int BUFFER_SIZE = 4 * 1024;

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
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2.setRenderingHints(rh);

		g2.setColor(Color.BLACK);
		g2.setPaint(Color.BLACK);
		spectrum.paint(g2);
	}

	public void actionPerformed(ActionEvent e) {
		amplitudes = playback.getFrameAmplitudes(amplitudes);
		spectrum.updateSignal(amplitudes);
		repaint();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Sound playback + spectrogram");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		CqtMusicPlayer player = new CqtMusicPlayer();
		frame.add(player);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	class Spectrum {
		private final double DB_THRESHOLD = -(20 * Math.log10(2 << (16 - 1)));
		private static final int PITCH_BIN_COUNT = 5 * 12;

		private Complex[] cqSpectrum;
		/** peak amplitude spectrum */
		private double[] amplitudeSpectrumDb;
		private double[] octaveBinsDb = new double[PITCH_BIN_COUNT];
		private double[] pitchClassProfileDb = new double[12];
		private Rectangle2D.Float line = new Rectangle2D.Float();
		private Cqt cqt = new FastCqt();

		public void updateSignal(double[] signal) {
			computeAmplitudeSpectrum(signal);
			computePitchClassProfile();
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
				double scaledAmplitudeDb = 1 + amplitudeDb / -DB_THRESHOLD;
				amplitudeSpectrumDb[i] = scaledAmplitudeDb;
			}
		}

		private void computePitchClassProfile() {
//			double octaveCountInv = 1.0 / 3;
//			for (int i = 0; i < PITCH_BIN_COUNT; i++) {
////				double value = 0;
////				for (int j = i; j < amplitudeSpectrumDb.length; j += PITCH_BIN_COUNT) {
////					value += amplitudeSpectrumDb[j];
////				}
////				value *= octaveCountInv;
//				
//				double value = amplitudeSpectrumDb[i + 1 * PITCH_BIN_COUNT];
//				
//				
//				octaveBinsDb[i] = value;
//			}

						octaveBinsDb = amplitudeSpectrumDb;
			
		}

		public void paint(Graphics2D graphics) {
			if (amplitudeSpectrumDb == null) {
				return;
			}
			float x = 0;
			Dimension size = getSize();
			float height = (float) size.getHeight();
			float step = (float) size.getWidth() / (octaveBinsDb.length);

			for (int i = 1; i < octaveBinsDb.length; i++) {
				line.setFrameFromDiagonal(x, height, x + step - 1, (1 - octaveBinsDb[i]) * height);
				x += step;
				graphics.fill(line);
				graphics.draw(line);
			}
		}
	}

	public static class Playback implements Runnable {

		SourceDataLine line;
		Thread thread;
		private byte[] buffer;

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
				while (bytesToWriteCount > 0) {
					int writtenBytesCount = playbackLine.write(buffer, 0, bytesToWriteCount);
					bytesToWriteCount -= writtenBytesCount;
				}
				readBytesCount = audioInputStream.read(buffer);
			}

			playbackLine.drain();
			playbackLine.stop();
			playbackLine.close();
		}

		public double[] getFrameAmplitudes(double[] amplitudes) {
			//			printByteArray(buffer);
			byte[] monoSignal = stereoToMono(buffer);
			//			printByteArray(monoSignal);
			if (amplitudes == null) {
				amplitudes = new double[monoSignal.length / 2];
			}
			littleEndianBytesToDoubles(monoSignal, amplitudes);
			//			printFloatArray(amplitudes);
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