package com.harmoneye.viz;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.analysis.MusicAnalyzer.AnalyzedFrame;
import com.harmoneye.math.cqt.CqtContext;
import com.jogamp.opengl.util.Animator;

// TODO: rewrite to use vertex buffers instead of immediate mode

public class OpenGlLinearVisualizer implements SwingVisualizer<AnalyzedFrame>,
	GLEventListener {

	private static final float DEFAULT_LINE_WIDTH = 1f;
	private static final float WAIT_SCROBBLER_LINE_WIDTH = 1.5f;

	private static final int OCTAVE_SIZE = 12;

	private ColorFunction colorFunction = new TemperatureColorFunction();
	private Component component;

	private AnalyzedFrame frame;

	private boolean isLandscape;
	private double aspectRatio = 1.0;
	
	private Map<String, Object> config = new HashMap<String, Object>();

	public OpenGlLinearVisualizer() {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);

		caps.setSampleBuffers(true);

		GLCanvas canvas = new GLCanvas(caps);

		canvas.addGLEventListener(this);

		component = canvas;

		Animator animator = new Animator(canvas);
		animator.start();
		// TODO: stop the animator if the computation is stopped
	}

	@Override
	public void update(AnalyzedFrame frame) {
		if (frame == null) {
			return;
		}

		this.frame = frame;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		render(drawable);
	}

	private void render(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		if (isDataAvailable()) {
			drawFrame(gl);

		} else {
			drawWaitingAnimation(gl);
		}
	}

	private void drawWaitingAnimation(GL2 gl) {
		long millis = System.currentTimeMillis();
		long millisInSecond = millis % 1000;
		float phaseOffset = (float) (millisInSecond * 0.001);

		gl.glLineWidth(WAIT_SCROBBLER_LINE_WIDTH);
		double halfToneCountInv = 1.0 / OCTAVE_SIZE;
		double maxRadius = 0.09;
		double innerRadius = maxRadius * 0.25;
		double outerRadius = maxRadius * 0.75;
		gl.glBegin(GL.GL_LINES);
		for (int i = 0; i < OCTAVE_SIZE; i++) {
			double unitAngle = (i - 0.5) * halfToneCountInv;
			double angle = 2 * FastMath.PI * unitAngle;

			float value = (float) (0.25 + 0.5 * ((1 - unitAngle + phaseOffset) % 1.0));
			Color color = colorFunction.toColor((float) value);
			gl.glColor3ub((byte) color.getRed(),
				(byte) color.getGreen(),
				(byte) color.getBlue());

			double x = FastMath.sin(angle);
			double y = FastMath.cos(angle);
			gl.glVertex2d(innerRadius * x, innerRadius * y);
			gl.glVertex2d(outerRadius * x, outerRadius * y);
		}
		gl.glEnd();
		gl.glLineWidth(DEFAULT_LINE_WIDTH);
	}

	private void drawFrame(GL2 gl) {
		gl.glPushMatrix();
		if (isLandscape) {
			gl.glTranslated(-aspectRatio, -1, 0);
			gl.glScaled(2 * aspectRatio, 2, 0);
		} else {
			gl.glTranslated(-1, -aspectRatio, 0);
			gl.glScaled(2, 2 * aspectRatio, 0);
		}

		drawBorders(gl);
		
		gl.glColor3f(0.75f, 0.75f, 0.75f);
		drawBins(gl, frame.getAllBins());
		
		gl.glColor3f(0.4f, 1.0f, 0.4f);
		drawBins(gl, frame.getDetectedPitchClasses());

		gl.glPopMatrix();
	}

	private void drawBorders(GL2 gl) {
		CqtContext ctx = frame.getCtxContext();

		gl.glColor3f(0.4f, 0.4f, 0.4f);
		gl.glLineWidth(0.1f);
		int halftonesPerOctave = ctx.getHalftonesPerOctave();
		// lines between bins
		gl.glBegin(GL.GL_LINES);
		int octaves = ctx.getOctaves();
		int totalBins = octaves * halftonesPerOctave;
		double xStep = 1.0 / totalBins;
		for (int i = 0; i <= totalBins; i++) {
			if (i % halftonesPerOctave != 0) {
				double x = i * xStep;
				gl.glVertex2d(x, -1);
				gl.glVertex2d(x, 1);
			}
		}
		gl.glEnd();

		gl.glColor3f(0.5f, 0.5f, 0.5f);
		gl.glLineWidth(1f);
		gl.glBegin(GL.GL_LINES);
		totalBins = octaves;
		xStep = 1.0 / totalBins;
		for (int i = 0; i <= totalBins; i++) {
			double x = i * xStep;
			gl.glVertex2d(x, -1);
			gl.glVertex2d(x, 1);
		}
		gl.glEnd();
	}

	private void drawBins(GL2 gl, double[] bins) {
		if (bins == null) {
			return;
		}

		gl.glPushMatrix();

		int totalBins = bins.length;
		double xStep = 1.0 / totalBins;
		//gl.glBegin(GL.GL_TRIANGLES);
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex2d(0, bins[0]);
		for (int i = 1; i < totalBins; i++) {
			double value = bins[i];

//			Color color = colorFunction.toColor((float) value);
//			gl.glColor3ub((byte) color.getRed(),
//				(byte) color.getGreen(),
//				(byte) color.getBlue());

			double xFrom = i * xStep;
			double xTo = xFrom + xStep;
			double yTo = value;

//			gl.glVertex2d(xFrom, 0);
//			gl.glVertex2d(0.5 * (xFrom + xTo), yTo);
//			gl.glVertex2d(xTo, 0);
			
			gl.glVertex2d(xTo, yTo);
		}
		gl.glEnd();

		gl.glPopMatrix();

	}

	private boolean isDataAvailable() {
		return frame != null;
	}

	private void setConstantAspectRatio(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		double w = drawable.getWidth();
		double h = drawable.getHeight();
		isLandscape = w > h;
		if (isLandscape) {
			aspectRatio = w / h;
			gl.glOrtho(-aspectRatio, aspectRatio, -1, 1, -1, 1);
		} else {
			aspectRatio = h / w;
			gl.glOrtho(-1, 1, -aspectRatio, aspectRatio, -1, 1);
		}

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// Synchronize the FPS with the refresh rate of the display (v-sync).
		// Otherwise we can use the FPSAnimator instead of the plain Animator.
		GL gl = drawable.getGL();
		gl.setSwapInterval(1);

		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
		gl.glLineWidth(0.5f);

		gl.glClearColor(0.25f, 0.25f, 0.25f, 1f);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
		int height) {
		setConstantAspectRatio(drawable);
	}

	@Override
	public Component getComponent() {
		return component;
	}

	@Override
	public Map<String, Object> getConfig() {
		return config;
	}
}
