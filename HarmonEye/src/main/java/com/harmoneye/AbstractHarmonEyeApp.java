package com.harmoneye;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class AbstractHarmonEyeApp extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final int TIME_PERIOD_MILLIS = 20;

	private Timer timer;
	protected MusicAnalyzer soundAnalyzer;

	public AbstractHarmonEyeApp() {
		timer = new Timer(TIME_PERIOD_MILLIS, this);
		timer.setInitialDelay(190);
		soundAnalyzer = new MusicAnalyzer(this);

		JFrame frame = new JFrame("HarmonEye");
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void start() {
		timer.start();
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		soundAnalyzer.paint(g2);
	}

	public void actionPerformed(ActionEvent e) {
		soundAnalyzer.updateSignal();
		repaint();
	}
}