package com.harmoneye.viz.animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ShakyHorizonAnimation extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final int TIME_PERIOD_MILLIS = 20;

	Horizon horizon = new Horizon(300);

	private Timer timer;

	public ShakyHorizonAnimation() {
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

		horizon.update();
		horizon.paint(g2);
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Shaky horizon");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ShakyHorizonAnimation());
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	class Horizon {
		private float[] values;
		private Path2D.Float path = new Path2D.Float();
		private Random random = new Random();
		private float offsetAmount = 0.01f;

		public Horizon(int count) {
			values = new float[count];
			for (int i = 0; i < count; i++) {
				values[i] = 0.5f;
			}
		}

		public void update() {
			for (int i = 0; i < values.length; i++) {
				float diffToPrevious = i > 0 ? values[i] - values[i - 1] : 0;
				float diffToNext = i < values.length - 1 ? values[i]
						- values[i + 1] : 0;
				float diff = diffToPrevious + diffToNext;
				float offset = offsetAmount
						* (2 * (float) (random.nextDouble() - diff) - 1);
				float newValue = (float) (values[i] + offset);
				newValue = Math.min(newValue, 1);
				newValue = Math.max(newValue, 0);
				values[i] = newValue;
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