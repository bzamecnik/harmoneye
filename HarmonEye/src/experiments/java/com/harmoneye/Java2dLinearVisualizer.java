package com.harmoneye;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Java2dLinearVisualizer extends AbstractJava2dVisualizer {

	private static final long serialVersionUID = 1L;

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		drawPitchClassBars((Graphics2D) g);
	}

	private void drawPitchClassBars(Graphics2D graphics) {
		PitchClassProfile pcProfile = getPcProfile();
		if (pcProfile == null) {
			return;
		}

		double[] pcBins = pcProfile.getPitchClassBins();
		int totalBinCount = pcProfile.getTotalBinCount();

		float x = 0;
		Dimension size = getSize();
		float height = (float) size.getHeight();
		float step = (float) size.getWidth() / totalBinCount;

		for (int i = 0; i < totalBinCount; i++) {

			line.setFrameFromDiagonal(x, height, x + step - 1, (1 - pcBins[i]) * height);
			x += step;
			Color color = getColor((float) pcBins[i]);
			graphics.setColor(color);
			graphics.setPaint(color);
			graphics.fill(line);
			graphics.draw(line);
		}

		graphics.setColor(Color.GRAY);
		x = 0;
		int binCount = pcProfile.getHalftoneCount();
		step = (float) size.getWidth() / binCount;
		for (int i = 0; i < binCount; i++) {
			line.setFrameFromDiagonal(x, 0, x + step - 1, height);
			x += step;
			graphics.draw(line);
		}
	}

}
