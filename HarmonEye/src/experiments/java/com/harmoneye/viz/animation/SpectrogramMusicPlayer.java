package com.harmoneye.viz.animation;

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

import com.harmoneye.app.Config;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class SpectrogramMusicPlayer extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final int TIME_PERIOD_MILLIS = 100;

	private static final int BUFFER_SIZE = 4 * 1024;
	private static final int SAMPLE_COUNT = BUFFER_SIZE / 4;

	Spectrum spectrum = new Spectrum(SAMPLE_COUNT);
	private Timer timer;
	private Playback playback;
	private float[] amplitudes;

	public SpectrogramMusicPlayer() {
		Config config = Config.fromDefault();
		String inputFileName = config.get("inputFile");

		timer = new Timer(TIME_PERIOD_MILLIS, this);
		timer.setInitialDelay(190);
		timer.start();

		playback = new Playback(inputFileName);
		playback.start();
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

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
		SpectrogramMusicPlayer player = new SpectrogramMusicPlayer();
		frame.add(player);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	class Spectrum {
		private final float DB_THRESHOLD = (float) -(20 * Math.log10(2 << (16 - 1)));
		/** peak amplitude spectrum */
		private float[] amplitudeSpectrumDb;
		private Rectangle2D.Float line = new Rectangle2D.Float();
		private FloatFFT_1D fft;
		private int sampleCount;

		public Spectrum(int sampleCount) {
			this.sampleCount = sampleCount;
			amplitudeSpectrumDb = new float[sampleCount / 2];
			fft = new FloatFFT_1D(sampleCount);
		}

		public void updateSignal(float[] signal) {
			computeAmplitudeSpectrum(signal);
		}

		private void computeAmplitudeSpectrum(float[] signal) {
			if (sampleCount != signal.length) {
				throw new IllegalArgumentException("expected sample count was " + sampleCount + ", not "
						+ signal.length);
			}
			fft.realForward(signal);
			// NOTE: now signalValues contain the transformed values
			// aligned as Re[0], Im[0], Re[1], Im[1], ...

			float normalizationFactor = 2 / (float) sampleCount;
			// the DC should normalized by 0.5 * normalizationFactor
			for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
				float re = signal[2 * i];
				float im = signal[2 * i + 1];
				float magnitude = (float) Math.sqrt(re * re + im * im);
				float peakAmplitude = magnitude * normalizationFactor;
				float referenceAmplitude = 1;
				float amplitudeDb = (float) (20 * Math.log10(peakAmplitude / referenceAmplitude));
				float scaledAmplitudeDb = 1 + amplitudeDb / -DB_THRESHOLD;
				amplitudeSpectrumDb[i] = scaledAmplitudeDb;
			}
		}

		public void paint(Graphics2D graphics) {
			float x = 0;
			Dimension size = getSize();
			float height = (float) size.getHeight();
			float step = (float) size.getWidth() / (amplitudeSpectrumDb.length - 2);

			for (int i = 1; i < amplitudeSpectrumDb.length; i++) {
				x += step;
				line.setFrameFromDiagonal(x, height, x + step - 1, (1 - amplitudeSpectrumDb[i]) * height);
				graphics.fill(line);
				graphics.draw(line);
			}
		}
	}

	public static class Playback implements Runnable {

		SourceDataLine line;
		Thread thread;
		private byte[] buffer;

		private String inputFileName;

		public Playback(String inputFileName) {
			this.inputFileName = inputFileName;
		}

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

		private void play() throws UnsupportedAudioFileException, IOException, Exception,
				LineUnavailableException {
			File file = new File(inputFileName);
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

		public float[] getFrameAmplitudes(float[] amplitudes) {
			// printByteArray(buffer);
			byte[] monoSignal = stereoToMono(buffer);
			// printByteArray(monoSignal);
			if (amplitudes == null) {
				amplitudes = new float[monoSignal.length / 2];
			}
			littleEndianBytesToFloats(monoSignal, amplitudes);
			// printFloatArray(amplitudes);
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

		private static void littleEndianBytesToFloats(byte[] bytes, float[] floats) {
			assert bytes.length == 2 * floats.length;
			// signed short to [-1; 1]
			float normalizationFactor = 2 / (float) 0xffff;
			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < floats.length && byteBuffer.hasRemaining(); i++) {
				floats[i] = normalizationFactor * byteBuffer.getShort();
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