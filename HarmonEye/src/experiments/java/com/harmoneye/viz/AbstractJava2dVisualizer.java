package com.harmoneye.viz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import com.harmoneye.analysis.PitchClassProfile;

abstract class AbstractJava2dVisualizer extends JPanel implements SwingVisualizer<PitchClassProfile> {

	private static final long serialVersionUID = 1L;

	protected static final String[] HALFTONE_NAMES = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };
	private static final int DEFAULT_PITCH_STEP = 1;

	private int pitchStep = DEFAULT_PITCH_STEP;
	private PitchClassProfile pcProfile;
	protected Rectangle2D.Float line = new Rectangle2D.Float();
	private ColorFunction colorFunction = new TemperatureColorFunction();

	public void update(PitchClassProfile pcProfile) {
		this.pcProfile = pcProfile;
		repaint();
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	public int getPitchStep() {
		return pitchStep;
	}

	public void setPitchStep(int pitchStep) {
		this.pitchStep = pitchStep;
	}

	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D graphics = (Graphics2D) g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHints(rh);

		setBackground(Color.DARK_GRAY);
	}

	protected Color getColor(float value) {
		return colorFunction.toColor(value);
	}

	protected PitchClassProfile getPcProfile() {
		return pcProfile;
	}

}