package com.harmoneye.p5;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import processing.core.PApplet;
import processing.core.PVector;

public class P5AppletInFrame {
	public static void main(String[] args) {
		Frame frame = new ExampleFrame();
		frame.setSize(400, 400);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});

		frame.setVisible(true);
	}

	public static class ExampleFrame extends Frame {

		private static final long serialVersionUID = -2570178784915358454L;

		public ExampleFrame() {
			super("Embedded PApplet");

			setLayout(new BorderLayout());
			PApplet applet = new EmbeddedApplet();
			add(applet, BorderLayout.CENTER);

			// important to call this whenever embedding a PApplet.
			// It ensures that the animation thread is started and
			// that other internal variables are properly set.
			applet.init();

			// setSize(embed.getSize());
		}
	}

	public static class EmbeddedApplet extends PApplet {

		private static final long serialVersionUID = -1620562933160685673L;

		private static float MOUSE_WEIGHT = 0.1f;

		private PVector position = new PVector();

		public void setup() {
			size(400, 400);
		}

		public void draw() {
			background(255);
			fill(255, 128, 128);
			position.x = lerp(position.x, mouseX, MOUSE_WEIGHT);
			position.y = lerp(position.y, mouseY, MOUSE_WEIGHT);
			ellipse(position.x, position.y, 50, 50);
		}
	}
}
