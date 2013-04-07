package com.harmoneye;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

abstract class AbstractVisualizer {

	protected static final String[] HALFTONE_NAMES = { "C", "Db", "D", "Eb",
			"E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };

	protected int pitchBinCount;
	protected int binsPerHalftone;

	protected double[] pitchClassProfile;

	protected JPanel panel;
	protected Rectangle2D.Float line = new Rectangle2D.Float();

	public AbstractVisualizer(JPanel panel) {
		this.panel = panel;
	}

	public abstract void paint(Graphics2D graphics);

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

}