package com.harmoneye.viz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.analysis.AnalyzedFrame;
import com.harmoneye.math.cqt.CqtContext;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;

// TODO: rewrite to use vertex buffers instead of immediate mode

public class OpenGlCircularVisualizer implements
	SwingVisualizer<AnalyzedFrame>, GLEventListener {

	private static final float DEFAULT_LINE_WIDTH = 1f;
	private static final float WAIT_SCROBBLER_LINE_WIDTH = 1.5f;

	protected static final String[] HALFTONE_NAMES = { "C", "Db", "D", "Eb",
		"E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };

	private int pitchStep = 1;

	private double[] values;

	private ColorFunction colorFunction = new TemperatureColorFunction();
	private Component component;
	private int binsPerHalftone;
	private int halftoneCount;
	private double segmentCountInv;
	private double stepAngle;

	private TextRenderer renderer;

	public OpenGlCircularVisualizer() {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);

		caps.setSampleBuffers(true);

		GLCanvas canvas = new GLCanvas(caps);

		canvas.addGLEventListener(this);

		component = canvas;

		Animator animator = new Animator();
		animator.add(canvas);
		animator.start();
		// TODO: stop the animator if the computation is stopped
	}

	@Override
	public void update(AnalyzedFrame pcProfile) {
		if (pcProfile == null) {
			return;
		}

		CqtContext ctx = pcProfile.getCtxContext();

		binsPerHalftone = ctx.getBinsPerHalftone();
		halftoneCount = ctx.getHalftonesPerOctave();

		values = pcProfile.getOctaveBins();

		segmentCountInv = 1.0 / values.length;
		stepAngle = 2 * FastMath.PI * segmentCountInv;
	}

	@Override
	public void setPitchStep(int i) {
		this.pitchStep = i;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		render(drawable);
	}

	private void render(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		drawPitchClassFrame(gl);
		drawPitchClassBins(gl);
		drawHalftoneNames(drawable);
		drawCentralPupil(gl);
		if (!isDataAvailable()) {
			drawWaitingAnimation(gl);
		}
	}

	private void drawWaitingAnimation(GL2 gl) {
		long millis = System.currentTimeMillis();
		long millisInSecond = millis % 1000;
		float phaseOffset = (float) (millisInSecond * 0.001);

		gl.glLineWidth(WAIT_SCROBBLER_LINE_WIDTH);
		double halfToneCountInv = 1.0 / HALFTONE_NAMES.length;
		double maxRadius = 0.09;
		double innerRadius = maxRadius * 0.25;
		double outerRadius = maxRadius * 0.75;
		gl.glBegin(GL.GL_LINES);
		for (int i = 0; i < HALFTONE_NAMES.length; i++) {
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

	private void drawPitchClassFrame(GL2 gl) {
		gl.glColor3f(0.5f, 0.5f, 0.5f);

		// outer circle
		gl.glBegin(GL.GL_LINE_LOOP);
		drawCircle(gl, 0.9, 100);
		gl.glEnd();

		// lines between bins
		double halfToneCountInv = 1.0 / HALFTONE_NAMES.length;
		gl.glBegin(GL.GL_LINES);
		for (int i = 0; i < HALFTONE_NAMES.length; i++) {
			double angle = 2 * FastMath.PI * ((i - 0.5) * halfToneCountInv);
			double x = 0.9 * FastMath.sin(angle);
			double y = 0.9 * FastMath.cos(angle);
			gl.glVertex2d(x, y);
			gl.glVertex2d(-x, -y);
		}
		gl.glEnd();
	}

	private void drawCircle(GL2 gl, double radius, int steps) {
		double angleStep = 2 * FastMath.PI / steps;
		double angle = 0;

		for (int i = 0; i <= steps; i++, angle += angleStep) {
			double x = radius * FastMath.cos(angle);
			double y = radius * FastMath.sin(angle);
			gl.glVertex2d(x, y);
		}
	}

	private void drawPitchClassBins(GL2 gl) {
		if (!isDataAvailable()) {
			return;
		}

		double radius = 0.68;

		gl.glBegin(GL.GL_TRIANGLES);
		double angle = 0.5 * (1 - binsPerHalftone) * stepAngle;
		for (int i = 0; i < values.length; i++, angle += stepAngle) {
			int pitchClass = i / binsPerHalftone;
			int binInPitchClass = i % binsPerHalftone;
			int movedPitchClass = (pitchClass * pitchStep) % halftoneCount;
			int index = movedPitchClass * binsPerHalftone + binInPitchClass;
			double value = values[index];
			Color color = colorFunction.toColor((float) value);
			gl.glColor3ub((byte) color.getRed(),
				(byte) color.getGreen(),
				(byte) color.getBlue());

			gl.glVertex2d(0, 0);

			double startRadius = radius * values[index];
			double startAngle = angle - 0.5 * stepAngle;
			gl.glVertex2d(startRadius * FastMath.sin(startAngle), startRadius
				* FastMath.cos(startAngle));

			double endRadius = radius * values[index];
			double endAngle = angle + 0.5 * stepAngle;
			gl.glVertex2d(endRadius * FastMath.sin(endAngle), endRadius
				* FastMath.cos(endAngle));
		}
		gl.glEnd();
	}

	private boolean isDataAvailable() {
		return values != null;
	}

	private void drawHalftoneNames(GLAutoDrawable drawable) {
		int width = drawable.getWidth();
		int height = drawable.getHeight();

		double centerX = width / 2;
		double centerY = height / 2;
		double size = 0.9 * FastMath.min(width, height);
		double angleStep = 2 * FastMath.PI / HALFTONE_NAMES.length;
		double angle = 0;
		float scaleFactor = (float) (0.0015f * size);

		renderer.beginRendering(width, height);

		for (int i = 0; i < HALFTONE_NAMES.length; i++, angle += angleStep) {
			int index = (i * pitchStep) % HALFTONE_NAMES.length;
			float value = getMaxBinValue(index);
			Color color = colorFunction.toColor((float) value);
			renderer.setColor(color);
			String str = HALFTONE_NAMES[index];
			Rectangle2D bounds = renderer.getBounds(str);
			int offsetX = (int) (scaleFactor * 0.5f * bounds.getWidth());
			int offsetY = (int) (scaleFactor * 0.5f * bounds.getHeight());
			double radius = 0.43;
			int x = (int) (centerX + radius * size * FastMath.sin(angle) - offsetX);
			int y = (int) (centerY + radius * size * FastMath.cos(angle) - offsetY);

			renderer.draw3D(str, x, y, 0, scaleFactor);
		}

		renderer.endRendering();
	}

	private float getMaxBinValue(int halftoneIndex) {
		float max = 0;
		int baseIndex = binsPerHalftone * halftoneIndex;
		for (int i = 0; i < binsPerHalftone; i++) {
			float value = (float) values[baseIndex + i];
			max = FastMath.max(max, value);
		}
		return max;
	}

	private void drawCentralPupil(GL2 gl) {
		float radius = 0.09f;
		int steps = 30;

		gl.glColor3f(0.25f, 0.25f, 0.25f);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		drawCircle(gl, radius, steps);
		gl.glEnd();

		gl.glColor3f(0.5f, 0.5f, 0.5f);
		gl.glBegin(GL.GL_LINE_LOOP);
		drawCircle(gl, radius, steps);
		gl.glEnd();
	}

	private void setConstantAspectRatio(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		double w = drawable.getWidth();
		double h = drawable.getHeight();
		if (w > h) {
			double aspectRatio = w / h;
			gl.glOrtho(-aspectRatio, aspectRatio, -1, 1, -1, 1);
		} else {
			double aspectRatio = h / w;
			gl.glOrtho(-1, 1, -aspectRatio, aspectRatio, -1, 1);
		}

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	double getAverageValue(int upperIndex) {
		int lowerIndex = (upperIndex + values.length + 1) % values.length;
		upperIndex = upperIndex % values.length;
		return 0.5 * (values[lowerIndex] + values[upperIndex]);
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

		renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 50), true,
			true, null, true);
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
}
