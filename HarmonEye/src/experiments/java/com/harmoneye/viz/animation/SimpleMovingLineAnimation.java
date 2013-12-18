package com.harmoneye.viz.animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class SimpleMovingLineAnimation extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final int LINE_PERIOD_MILLIS = 5000;
	private static final int TIME_PERIOD_MILLIS = 20;
	
	private Line2D.Float line = new Line2D.Float();
	/** normalized horizontal position of the line [0.0; 1.0] */
	private float linePos = 0;
	private float linePosStep = TIME_PERIOD_MILLIS / (float)LINE_PERIOD_MILLIS;

	private Timer timer;

	public SimpleMovingLineAnimation() {		
		timer = new Timer(TIME_PERIOD_MILLIS, this);
		timer.setInitialDelay(190);
		timer.start();
	}

	public void step(int width, int height) {
		linePos = (linePos + linePosStep) % 1f;
		float x = linePos * width;
		line.setLine(x, 0, x, height);
	}

	public void render(Graphics2D graphics) {
		graphics.setColor(Color.BLUE);
		graphics.draw(line);
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		g2.setRenderingHints(rh);
		Dimension size = getSize();

		step(size.width, size.height);
		render(g2);
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Simple moving line animation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new SimpleMovingLineAnimation());
		frame.setSize(300, 200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}