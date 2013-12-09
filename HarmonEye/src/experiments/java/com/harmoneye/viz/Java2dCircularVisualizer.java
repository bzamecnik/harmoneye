package com.harmoneye.viz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.analysis.PitchClassProfile;

public class Java2dCircularVisualizer extends AbstractJava2dVisualizer {

	private static final long serialVersionUID = 1L;
	
	private double[] pcBins;
	private int pitchBinCount;
	private int binsPerHalftone;

	@Override
	public void update(PitchClassProfile pcProfile) {
		super.update(pcProfile);
		pcBins = pcProfile.getPitchClassBins();
		binsPerHalftone = pcProfile.getBinsPerHalftone();
		pitchBinCount = pcProfile.getTotalBinCount();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		drawPitchClassCircle((Graphics2D) g);
	}

	private void drawPitchClassCircle(Graphics2D graphics) {
		PitchClassProfile pitchClassProfile = getPcProfile();
		if (pitchClassProfile == null) {
			return;
		}

		Dimension panelSize = getSize();
		int size = (int) (0.9 * Math.min(panelSize.getWidth(), panelSize.getHeight()));
		float center = 0.5f * size;
		float x = 0.5f * ((float) panelSize.getWidth() - size);
		float y = 0.5f * ((float) panelSize.getHeight() - size);

		drawPitchClassFrame(graphics, size, center, x, y);
		drawPitchClassBins(graphics, size, center, x, y);
		drawHalftoneNames(graphics, size, center, x, y);
		drawCentralPupil(graphics, size, x, y);
	}

	private void drawPitchClassBins(Graphics2D graphics, int size, float center, float x, float y) {
		
		float arcAngleStep = 360.0f / pitchBinCount;
		for (int i = 0; i < pitchBinCount; i++) {
			int pitchClass = i / binsPerHalftone;
			int binInPitchClass = i % binsPerHalftone;
			int movedPitchClass = (pitchClass * getPitchStep()) % 12;
			int index = movedPitchClass * binsPerHalftone + binInPitchClass;
			
			float value = (float) pcBins[index];
			Color color = getColor(value);
			graphics.setColor(color);
			int startAngle = (int) (90 - arcAngleStep * (i - 0.5f * binsPerHalftone));
			float relativeLength = 0.85f * value;
			int barSize = (int) (relativeLength * size);
			graphics.fillArc((int) (x + (1 - relativeLength) * center), (int) (y + (1 - relativeLength) * center),
				barSize, barSize, startAngle, (int) -arcAngleStep);
		}
	}

	private void drawPitchClassFrame(Graphics2D graphics, int size, float center, float x, float y) {
		graphics.setColor(Color.GRAY);

		graphics.drawOval((int) x, (int) y, size, size);

		// lines between bins
		for (int i = 0; i < 12; i++) {
			double angle = 2 * FastMath.PI * ((i - 0.5) / 12.0);
			int startX = (int) (x + center);
			int startY = (int) (y + center);
			graphics.drawLine(startX, startY, (int) (x + center + 0.5 * size * FastMath.cos(angle)),
				(int) (y + center + 0.5 * size * FastMath.sin(angle)));
		}
	}

	private void drawHalftoneNames(Graphics2D graphics, int size, float center, float x, float y) {
		Font font = new Font("Arial", Font.BOLD, size / 15);
		graphics.setFont(font);
		FontMetrics fm = graphics.getFontMetrics();
		int offsetY = fm.getAscent() / 2;
		double angleStep = 2 * FastMath.PI / HALFTONE_NAMES.length;
		double angle = 9 * angleStep;
		for (int i = 0; i < HALFTONE_NAMES.length; i++, angle += angleStep) {
			int index = (i * getPitchStep()) % 12;
			float value = getMaxBinValue(index);
			Color color = getColor(value);
			graphics.setColor(color);
			String str = HALFTONE_NAMES[index];
			int offsetX = fm.stringWidth(str) / 2;
			graphics.drawString(str, (int) (x + center + 0.43 * size * FastMath.cos(angle) - offsetX), (int) (y + center
				+ 0.43 * size * FastMath.sin(angle) + offsetY));
		}
	}

	private void drawCentralPupil(Graphics2D graphics, int size, float x, float y) {

		float centerSizeFactor = 0.1f;
		int centerSize = (int) (centerSizeFactor * size);
		int centerOffset = (size - centerSize) / 2;

		graphics.setColor(Color.DARK_GRAY);
		graphics.fillOval((int) (x + centerOffset), (int) (y + centerOffset), centerSize, centerSize);

		graphics.setColor(Color.GRAY);
		graphics.drawOval((int) (x + centerOffset), (int) (y + centerOffset), centerSize, centerSize);
	}

	private float getMaxBinValue(int halftoneIndex) {
		float max = 0;
		int baseIndex = binsPerHalftone * halftoneIndex;
		for (int i = 0; i < binsPerHalftone; i++) {
			float value = (float) pcBins[baseIndex + i];
			max = FastMath.max(max, value);
		}
		return max;
	}

}
