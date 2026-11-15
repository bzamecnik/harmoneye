package com.harmoneye.viz;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.jogamp.opengl.util.Animator;

public class SimpleTriangleSwing implements GLEventListener {

	private double theta = 0;
	private double s = 0;
	private double c = 0;

	public static void main(String[] args) {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		GLCanvas canvas = new GLCanvas(caps);

		canvas.addGLEventListener(new SimpleTriangleSwing());

		JFrame frame = new JFrame("JOGL & Swing - triangle demo");
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
		theta += 0.01;
		s = Math.sin(theta);
		c = Math.cos(theta);
	}

	private void render(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		// draw a triangle filling the window
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glColor3f(1, 0, 0);
		gl.glVertex2d(-c, -c);
		gl.glColor3f(0, 1, 0);
		gl.glVertex2d(0, c);
		gl.glColor3f(0, 0, 1);
		gl.glVertex2d(s, -s);
		gl.glEnd();
	}
}