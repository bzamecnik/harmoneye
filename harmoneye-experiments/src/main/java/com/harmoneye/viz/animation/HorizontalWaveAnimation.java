package com.harmoneye.viz.animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class HorizontalWaveAnimation extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final int TIME_PERIOD_MILLIS = 20;

	Wave wave = new Wave(300);

	private Timer timer;

	public HorizontalWaveAnimation() {
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

		g2.setColor(Color.BLUE);

		wave.update();
		wave.paint(g2);
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Horizontally moving wave");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new HorizontalWaveAnimation());
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	class Wave {
		private static final float TWO_PI = (float) (2 * Math.PI);

		private float[] values;
		private Path2D.Float path = new Path2D.Float();
		private float phase = 0;
		private float horizontalFrequency = 0.5f;
		private float phaseStep = horizontalFrequency / TWO_PI;

		public Wave(int count) {
			values = new float[count];
		}

		public void update() {
			phase = (phase + phaseStep) % TWO_PI;
			float factor = (float) (TWO_PI / (float) values.length);
			for (int i = 0; i < values.length; i++) {
				values[i] = (float) (0.5 * Math.sin(i * factor + phase) + 0.5);
			}
		}

		public void paint(Graphics2D graphics) {
			float x = 0;
			Dimension size = getSize();
			float height = (float) size.getHeight();
			float step = (float) size.getWidth() / (values.length - 1);

			path.reset();
			path.moveTo(0, values[0] * height);
			for (int i = 1; i < values.length; i++) {
				x += step;
				path.lineTo(x, values[i] * height);
			}
			graphics.draw(path);
		}
	}
}