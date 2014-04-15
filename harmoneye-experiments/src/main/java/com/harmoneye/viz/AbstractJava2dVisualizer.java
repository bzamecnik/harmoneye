package com.harmoneye.viz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.harmoneye.analysis.MusicAnalyzer.AnalyzedFrame;

abstract class AbstractJava2dVisualizer extends JPanel implements SwingVisualizer<AnalyzedFrame> {

	private static final long serialVersionUID = 1L;

	private AnalyzedFrame pcProfile;
	protected Rectangle2D.Float line = new Rectangle2D.Float();
	private ColorFunction colorFunction = new TemperatureColorFunction();

	private Map<String, Object> config = new  HashMap<String, Object>();

	public AbstractJava2dVisualizer() {
		config.put("pitchStep", 1);
	}
	
	public void update(AnalyzedFrame pcProfile) {
		this.pcProfile = pcProfile;
		repaint();
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	public int getPitchStep() {
		Integer pitchStep = (Integer) config.get("pitchStep");
		return pitchStep != null ? pitchStep : 1;
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

	protected AnalyzedFrame getPcProfile() {
		return pcProfile;
	}
	
	@Override
	public Map<String, Object> getConfig() {
		return config;
	}

}
