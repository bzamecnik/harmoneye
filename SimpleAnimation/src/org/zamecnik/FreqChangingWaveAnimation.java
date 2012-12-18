package org.zamecnik;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class FreqChangingWaveAnimation extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final int TIME_PERIOD_MILLIS = 100;
	private static final int SAMPLE_COUNT = 512;

	Signal signal = new Signal(SAMPLE_COUNT);
	Spectrum spectrum = new Spectrum(signal);

	private Timer timer;

	public FreqChangingWaveAnimation() {
		timer = new Timer(TIME_PERIOD_MILLIS, this);
		timer.setInitialDelay(190);
		timer.start();
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		g2.setRenderingHints(rh);

		signal.update();
		g2.setColor(Color.BLUE);
		signal.paint(g2);

		spectrum.update();
		g2.setColor(Color.RED);
		spectrum.paint(g2);
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Frequency-changing wave + its spectrum");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new FreqChangingWaveAnimation());
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	class Signal {
		private static final float TWO_PI = (float) (2 * Math.PI);

		private float[] values;
		private Path2D.Float path = new Path2D.Float();
		private float frequency = 1;
		private float minFrequency = 0;
		private float maxFrequency;
		private float freqStep = 1;

		public Signal(int sampleCount) {
			values = new float[sampleCount];
			maxFrequency = sampleCount / 2;
		}

		public float[] getValues() {
			return Arrays.copyOf(values, values.length);
		}

		public int getSampleCount() {
			return values.length;
		}

		public void update() {
			frequency = minFrequency + (frequency - minFrequency + freqStep)
					% (maxFrequency - minFrequency);

			float factor = (float) (frequency * TWO_PI / (float) values.length);
			for (int i = 0; i < values.length; i++) {
				float amplitude = (float) Math.cos(0.1 * frequency);
				values[i] = amplitude * (float) Math.cos(i * factor);
			}
		}

		public void paint(Graphics2D graphics) {
			float x = 0;
			Dimension size = getSize();
			float height = (float) size.getHeight();
			float step = (float) size.getWidth() / (values.length - 1);

			path.reset();
			path.moveTo(0, (0.5 * values[0] + 0.5) * height);
			for (int i = 1; i < values.length; i++) {
				x += step;
				path.lineTo(x, (0.5 * values[i] + 0.5) * height);
			}
			graphics.draw(path);
		}
	}

	class Spectrum {
		private final float DB_THRESHOLD = (float) -(20 * Math
				.log10(2 << (16 - 1)));
		private Signal signal;
		/** peak amplitude spectrum */
		private float[] amplitudeSpectrumDb;
		private Line2D.Float line = new Line2D.Float();
		private FloatFFT_1D fft;
		private int sampleCount;

		private boolean printed = false;

		public Spectrum(Signal signal) {
			this.signal = signal;
			sampleCount = signal.getSampleCount();
			amplitudeSpectrumDb = new float[sampleCount / 2];
			fft = new FloatFFT_1D(sampleCount);
		}

		public void update() {
			float[] signalValues = signal.getValues();
			computeAmplitudeSpectrum(signalValues);

			if (!printed) {
				for (float value : signal.getValues()) {
					System.out.print(value + ", ");
				}
				System.out.println();
				for (float value : amplitudeSpectrumDb) {
					System.out.print(value + ", ");
				}
				System.out.println();
				printed = true;
			}
		}

		private void computeAmplitudeSpectrum(float[] signal) {
			assert sampleCount == signal.length;
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
				float amplitudeDb = (float) (20 * Math.log10(peakAmplitude
						/ referenceAmplitude));
				float scaledAmplitudeDb = 1 + amplitudeDb / -DB_THRESHOLD;
				amplitudeSpectrumDb[i] = scaledAmplitudeDb;
			}
		}

		public void paint(Graphics2D graphics) {
			float x = 0;
			Dimension size = getSize();
			float height = (float) size.getHeight();
			float step = (float) size.getWidth()
					/ (amplitudeSpectrumDb.length - 2);

			for (int i = 1; i < amplitudeSpectrumDb.length; i++) {
				x += step;
				line.setLine(x, 0, x, amplitudeSpectrumDb[i] * height);
				graphics.draw(line);
			}
		}
	}
}