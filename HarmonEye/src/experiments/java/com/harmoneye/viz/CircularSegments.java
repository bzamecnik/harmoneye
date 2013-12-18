package com.harmoneye.viz;

import java.awt.Color;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.harmoneye.viz.ColorFunction;
import com.harmoneye.viz.TemperatureColorFunction;
import com.jogamp.opengl.util.Animator;

public class CircularSegments implements GLEventListener {

	/** time for animation */
	private double theta = 0;
	private static final int VALUE_COUNT = 5 * 12;
	private double[] values = new double[VALUE_COUNT];
	private double[] borderRadii = new double[VALUE_COUNT];

	private ColorFunction colorFunction = new TemperatureColorFunction();

	public static void main(String[] args) {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		GLCanvas canvas = new GLCanvas(caps);

		canvas.addGLEventListener(new CircularSegments());

		JFrame frame = new JFrame("JOGL & Swing - circular segments");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 300);
		frame.setLocationRelativeTo(null);
		frame.add(canvas);
		frame.setVisible(true);

		Animator animator = new Animator(canvas);
		animator.add(canvas);
		animator.start();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		update();
		render(drawable);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// Synchronize the FPS with the refresh rate of the display (v-sync).
		// Otherwise we can use the FPSAnimator instead of the plain Animator.
		drawable.getGL().setSwapInterval(1);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	private void update() {
		theta = System.currentTimeMillis() / 1000.0;

		int segmentCount = values.length;
		double segmentCountInv = 1.0 / segmentCount;
		double stepAngle = 2 * Math.PI * segmentCountInv;
		double angle = 0;
		for (int i = 0; i < values.length; i++, angle += stepAngle) {
			double alpha = 0.5 + 0.5 * Math.sin(2 * Math.PI * (0.5 * theta) - angle);
			values[i] = alpha * alpha * alpha;
		}
		for (int i = 0; i < values.length; i++) {
			borderRadii[i] = getAverageValue(i);
		}
	}

	private void render(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		float byteToUnit = 1 / 255.0f;
		double segmentCountInv = 1.0 / values.length;
		double stepAngle = 2 * Math.PI * segmentCountInv;

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		gl.glBegin(GL.GL_TRIANGLES);
		double angle = 0;
		for (int i = 0; i < values.length; i++, angle += stepAngle) {
			double value = values[i];
			Color color = colorFunction.toColor((float) value);
			gl.glColor3f(color.getRed() * byteToUnit, color.getGreen() * byteToUnit, color.getBlue() * byteToUnit);

			gl.glVertex2d(0, 0);

			double startRadius = borderRadii[i];
			double startAngle = angle - 0.5 * stepAngle;
			gl.glVertex2d(startRadius * Math.sin(startAngle), startRadius * Math.cos(startAngle));

			double endRadius = borderRadii[(i + 1) % borderRadii.length];
			double endAngle = angle + 0.5 * stepAngle;
			gl.glVertex2d(endRadius * Math.sin(endAngle), endRadius * Math.cos(endAngle));
		}
		gl.glEnd();
	}

	double getAverageValue(int upperIndex) {
		int lowerIndex = (upperIndex + values.length + 1) % values.length;
		upperIndex = upperIndex % values.length;
		return 0.5 * (values[lowerIndex] + values[upperIndex]);
	}
}