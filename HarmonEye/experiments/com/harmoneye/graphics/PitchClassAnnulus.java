package com.harmoneye.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class PitchClassAnnulus extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	public void render(Graphics2D graphics) {
		setBackground(Color.DARK_GRAY);
		
		Dimension panelSize = getSize();
		int size = (int) Math.min(panelSize.getWidth(), panelSize.getHeight());
		int x = (int) (0.5f * (panelSize.getWidth() - size));
		int y = (int) (0.5f * (panelSize.getHeight() - size));

		int binCount = 36;
		float hueStep = 1.0f / binCount;
		float arcAngleStep = 360.0f / binCount;
		for (int i = 0; i < binCount; i++) {
			float hue = i * hueStep;
			Color color = getColor(hue);
			graphics.setColor(color);
			int startAngle = (int) (90 - arcAngleStep * (i/* - 0.5f*/));
			graphics.fillArc(x, y, size, size, startAngle, (int) -(arcAngleStep * (i % 3 == 2 ? 0.9f : 1f)));
		}
		
		graphics.setColor(getBackground());
		int centerSize = (int)(0.65f * size);
		int centerOffset = (size - centerSize) / 2;
		graphics.fillOval(x + centerOffset, y + centerOffset, centerSize, centerSize);
	}
	
	Color getColor(float value) {
//		float hue = (1.65f - 0.7f * value) % 1.0f;
		float hue = (1.8f - value) % 1.0f;
		System.out.println(hue);
		return Color.getHSBColor(hue, 0.35f + 0.5f * value, 0.1f + 0.9f * value);
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2.setRenderingHints(rh);

		render(g2);
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Annulus drawing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PitchClassAnnulus());
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}