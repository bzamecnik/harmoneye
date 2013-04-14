package com.harmoneye;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

abstract class AbstractVisualizer {

	protected static final String[] HALFTONE_NAMES = { "C", "Db", "D", "Eb",
			"E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };

	private static final int DEFAULT_PITCH_STEP = 1;

	private int pitchStep = DEFAULT_PITCH_STEP;

	protected int pitchBinCount;
	protected int binsPerHalftone;

	protected double[] pitchClassProfile;

	protected JPanel panel;
	protected Rectangle2D.Float line = new Rectangle2D.Float();

	public AbstractVisualizer(JPanel panel) {
		this.panel = panel;
	}

	public void paint(Graphics2D graphics) {
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHints(rh);

		panel.setBackground(Color.DARK_GRAY);
	}

	Color getColor(float value) {
		float hue = (1.8f - value) % 1.0f;
		return Color.getHSBColor(hue, 0.25f + 0.75f * value,
				0.25f + 0.75f * value);
	}

	public void setPitchBinCount(int pitchBinCount) {
		this.pitchBinCount = pitchBinCount;
	}

	public void setBinsPerHalftone(int binsPerHalftone) {
		this.binsPerHalftone = binsPerHalftone;
	}

	public void setPitchClassProfile(double[] pitchClassProfile) {
		this.pitchClassProfile = pitchClassProfile;
	}

	public int getPitchStep() {
		return pitchStep;
	}

	public void setPitchStep(int pitchStep) {
		this.pitchStep = pitchStep;
	}

}