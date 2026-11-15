package com.harmoneye.p5;

import processing.core.PApplet;
import processing.core.PVector;

public class P5AppletStandalone extends PApplet {

	private static final long serialVersionUID = -6761026529847438431L;

	private static float MOUSE_WEIGHT = 0.1f;

	private PVector position = new PVector();

	public static void main(String args[]) {
		PApplet.main(P5AppletStandalone.class.getName(), args);
	}

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
