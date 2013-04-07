package com.harmoneye;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class LinearVisualizer extends AbstractVisualizer {

	public LinearVisualizer(JPanel panel) {
		super(panel);
	}

	@Override
	public void paint(Graphics2D graphics) {
		drawPitchClassBars(graphics);
	}

	private void drawPitchClassBars(Graphics2D graphics) {
		if (pitchClassProfile == null) {
			return;
		}

		float x = 0;
		Dimension size = panel.getSize();
		float height = (float) size.getHeight();
		float step = (float) size.getWidth() / (pitchClassProfile.length);

		for (int i = 0; i < pitchClassProfile.length; i++) {
			line.setFrameFromDiagonal(x, height, x + step - 1,
					(1 - pitchClassProfile[i]) * height);
			x += step;
			Color color = getColor((float) pitchClassProfile[i]);
			graphics.setColor(color);
			graphics.setPaint(color);
			graphics.fill(line);
			graphics.draw(line);
		}

		graphics.setColor(Color.GRAY);
		x = 0;
		int binCount = pitchClassProfile.length / binsPerHalftone;
		step = (float) size.getWidth() / binCount;
		for (int i = 0; i < binCount; i++) {
			line.setFrameFromDiagonal(x, 0, x + step - 1, height);
			x += step;
			graphics.draw(line);
		}
	}

}
