package com.harmoneye.gl;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.Animator;

public class SimpleTriangleAwt implements GLEventListener {
	
	private double theta = 0;
	private double s = 0;
	private double c = 0;
	
	public static void main(String[] args) {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		GLCanvas canvas = new GLCanvas(caps);

		Frame frame = new Frame("JOGL & AWT - triangle demo");
		frame.setSize(300, 300);
		frame.add(canvas);
		frame.setVisible(true);

		// by default, an AWT Frame doesn't do anything when you click
		// the close button; this bit of code will terminate the program when
		// the window is asked to close
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		canvas.addGLEventListener(new SimpleTriangleAwt());

		// FPSAnimator animator = new FPSAnimator(canvas, 60);
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