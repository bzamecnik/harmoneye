package com.harmoneye;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class CircularVisualizer extends AbstractVisualizer {

	public CircularVisualizer(JPanel panel) {
		super(panel);
	}

	@Override
	public void paint(Graphics2D graphics) {
		drawPitchClassCircle(graphics);
	}

	private void drawPitchClassCircle(Graphics2D graphics) {
		if (pitchClassProfile == null) {
			return;
		}
		
		Dimension panelSize = panel.getSize();
		int size = (int) (0.9 * Math.min(panelSize.getWidth(),
				panelSize.getHeight()));
		float center = 0.5f * size;
		float x = 0.5f * ((float) panelSize.getWidth() - size);
		float y = 0.5f * ((float) panelSize.getHeight() - size);

		graphics.setColor(Color.GRAY);
		graphics.drawOval((int) x, (int) y, size, size);
		// lines between bins
		for (int i = 0; i < 12; i++) {
			double angle = 2 * Math.PI * ((i - 0.5) / 12.0);
			graphics.drawLine((int) (x + center), (int) (y + center), (int) (x
					+ center + 0.5 * size * Math.cos(angle)),
					(int) (y + center + 0.5 * size * Math.sin(angle)));
		}

		// bins
		float arcAngleStep = 360.0f / pitchBinCount;
		for (int i = 0; i < pitchBinCount; i++) {
			float value = (float) pitchClassProfile[i];
			Color color = getColor(value);
			graphics.setColor(color);
			int startAngle = (int) (90 - arcAngleStep
					* (i - 0.5f * binsPerHalftone));
			float relativeLength = 0.85f * value;
			int barSize = (int) (relativeLength * size);
			graphics.fillArc((int) (x + (1 - relativeLength) * center),
					(int) (y + (1 - relativeLength) * center), barSize,
					barSize, startAngle, (int) -arcAngleStep);
		}
		// float arcAngleStep = 360.0f / 12;
		// for (int i = 0; i < 12; i++) {
		// float hue = (float) pitchClassProfileDb[i];
		// // Color color = getColor(hue);
		// graphics.setColor(Color.getHSBColor(0, 0, hue));
		// int startAngle = (int) (90 - arcAngleStep * (i/* - 0.5f*/));
		// graphics.fillArc(x, y, size, size, startAngle, (int)
		// -(arcAngleStep * 0.8f));
		// }

		drawHalftoneNames(graphics, size, center, x, y);

		graphics.setColor(Color.DARK_GRAY);
		float centerSizeFactor = 0.1f;
		int centerSize = (int) (centerSizeFactor * size);
		int centerOffset = (size - centerSize) / 2;
		graphics.fillOval((int) (x + centerOffset), (int) (y + centerOffset),
				centerSize, centerSize);

		graphics.setColor(Color.GRAY);
		graphics.drawOval((int) (x + centerOffset), (int) (y + centerOffset),
				centerSize, centerSize);
	}

	private void drawHalftoneNames(Graphics2D graphics, int size, float center,
			float x, float y) {
		Font font = new Font("Arial", Font.BOLD, size / 15);
		graphics.setFont(font);
		FontMetrics fm = graphics.getFontMetrics();
		int offsetY = fm.getAscent() / 2;
		double angleStep = 2 * Math.PI / HALFTONE_NAMES.length;
		double angle = 9 * angleStep;
		for (int i = 0; i < HALFTONE_NAMES.length; i++, angle += angleStep) {
			float value = getMaxBinValue(i);
			Color color = getColor(value);
			graphics.setColor(color);
			String str = HALFTONE_NAMES[i];
			int offsetX = fm.stringWidth(str) / 2;
			graphics.drawString(
					str,
					(int) (x + center + 0.43 * size * Math.cos(angle) - offsetX),
					(int) (y + center + 0.43 * size * Math.sin(angle) + offsetY));
		}
	}

	private float getMaxBinValue(int halftoneIndex) {
		float max = 0;
		int baseIndex = binsPerHalftone * halftoneIndex;
		for (int i = 0; i < binsPerHalftone; i++) {
			float value = (float) pitchClassProfile[baseIndex + i];
			max = Math.max(max, value);
		}
		return max;
	}

}
