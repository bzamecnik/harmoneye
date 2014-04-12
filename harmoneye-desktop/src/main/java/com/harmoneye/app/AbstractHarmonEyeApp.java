package com.harmoneye.app;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;

import com.harmoneye.analysis.AnalyzedFrame;
import com.harmoneye.analysis.MusicAnalyzer;
import com.harmoneye.viz.OpenGlCircularVisualizer;
import com.harmoneye.viz.OpenGlLinearVisualizer;
import com.harmoneye.viz.SwingVisualizer;

public class AbstractHarmonEyeApp {

	private static final int TIME_PERIOD_MILLIS = 25;
	private static final String WINDOW_TITLE = "HarmonEye";

	protected static final float AUDIO_SAMPLE_RATE = 44100.0f;
	protected static final int AUDIO_BITS_PER_SAMPLE = 16;

	protected MusicAnalyzer soundAnalyzer;

	private JFrame frame;

	private CircleOfFifthsEnabledAction circleOfFifthsEnabledAction;
	private PauseAction pauseAction;
	private JMenuItem pauseMenuItem;
	private AccumulationEnabledAction accumulationEnabledAction;

	private ApplicationListener appListener;

	private SwingVisualizer<AnalyzedFrame> visualizer;
	private AtomicBoolean initialized = new AtomicBoolean();

	private Timer updateTimer;

	public AbstractHarmonEyeApp() {
		visualizer = new OpenGlCircularVisualizer();
//		visualizer = new OpenGlLinearVisualizer();

		soundAnalyzer = new MusicAnalyzer(visualizer, AUDIO_SAMPLE_RATE, AUDIO_BITS_PER_SAMPLE);

		circleOfFifthsEnabledAction = new CircleOfFifthsEnabledAction("Circle of fifths", null, "", new Integer(
			KeyEvent.VK_F));
		pauseAction = new PauseAction("Pause", null, "", new Integer(KeyEvent.VK_P));
		accumulationEnabledAction = new AccumulationEnabledAction("Accumulate", null, "", new Integer(KeyEvent.VK_A));

		frame = createFrame();

		appListener = new MyApplicationListener(frame);

		frame.setVisible(true);
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame(WINDOW_TITLE);
		frame.add(visualizer.getComponent());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);

		frame.setJMenuBar(createMenuBar());

		return frame;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		menuBar.add(createVisualizationMenu());
		menuBar.add(createWindowMenu());
		menuBar.add(createHelpMenu());

		return menuBar;
	}

	private JMenu createVisualizationMenu() {
		JMenu menu = new JMenu("Visualization");

		pauseMenuItem = new JMenuItem(pauseAction);
		pauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(' '));
		menu.add(pauseMenuItem);

		JCheckBoxMenuItem circleOfFifthsEnabledMenuItem = new JCheckBoxMenuItem(circleOfFifthsEnabledAction);
		circleOfFifthsEnabledMenuItem.setAccelerator(KeyStroke.getKeyStroke('f'));
		menu.add(circleOfFifthsEnabledMenuItem);

		JCheckBoxMenuItem accumulationEnabledMenuItem = new JCheckBoxMenuItem(accumulationEnabledAction);
		accumulationEnabledMenuItem.setAccelerator(KeyStroke.getKeyStroke('a'));
		menu.add(accumulationEnabledMenuItem);
		return menu;
	}

	private JMenu createWindowMenu() {
		JMenu menu = new JMenu("Window");

		final JCheckBoxMenuItem alwaysOnTopMenuItem = new JCheckBoxMenuItem();
		alwaysOnTopMenuItem.setAction(new AbstractAction("Always on top") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setAlwaysOnTop(alwaysOnTopMenuItem.getState());
			}
		});
		alwaysOnTopMenuItem.setAccelerator(KeyStroke.getKeyStroke('t'));
		menu.add(alwaysOnTopMenuItem);
		return menu;
	}

	private JMenu createHelpMenu() {
		JMenu menu = new JMenu("Help");

		final JCheckBoxMenuItem helpMenuItem = new JCheckBoxMenuItem();
		helpMenuItem.setAction(new AbstractAction("Open website") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				WebHelper.openWebpage(WebHelper.HELP_URL);
			}
		});
		menu.add(helpMenuItem);
		return menu;
	}

	public void start() {
		if (!initialized.get()) {
			return;
		}

		updateTimer = new Timer("update timer");
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				soundAnalyzer.updateSignal();
			}
		};
		updateTimer.scheduleAtFixedRate(updateTask, 200, TIME_PERIOD_MILLIS);
		pauseMenuItem.setText("Pause");
		frame.setTitle(WINDOW_TITLE);
	}

	public void stop() {
		if (!initialized.get()) {
			return;
		}

		updateTimer.cancel();
		updateTimer = null;
		pauseMenuItem.setText("Play");
		frame.setTitle("= " + WINDOW_TITLE + " =");
	}

	public void init() {
		soundAnalyzer.init();
		initialized.set(true);
	}

	private void toggle() {
		if (updateTimer != null) {
			stop();
		} else {
			start();
		}
	}

	private class CircleOfFifthsEnabledAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		boolean fifthsEnabled = false;

		public CircleOfFifthsEnabledAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			fifthsEnabled = !fifthsEnabled;
			visualizer.setPitchStep(fifthsEnabled ? 7 : 1);
			//visualizer.getPanel().repaint();
		}
	}

	public class PauseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public PauseAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			toggle();
		}
	}

	public class AccumulationEnabledAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public AccumulationEnabledAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			soundAnalyzer.toggleAccumulatorEnabled();
		}
	}

	// Must be public!!
	public static class MyApplicationListener implements ApplicationListener {

		private JFrame frame;

		public MyApplicationListener(JFrame frame) {
			this.frame = frame;
		}

		private void handle(ApplicationEvent event, String message) {
			JOptionPane.showMessageDialog(frame, message);
			event.setHandled(true);
		}

		public void handleAbout(ApplicationEvent event) {
			String message = prepareAboutMessage();
			JOptionPane.showMessageDialog(frame, message, "About HarmonEye", JOptionPane.INFORMATION_MESSAGE);
			event.setHandled(true);
		}

		private String prepareAboutMessage() {
			Package p = getClass().getPackage();
			String title = p.getImplementationTitle();
			if (title == null) {
				title = WINDOW_TITLE;
			}
			String version = p.getImplementationVersion();
			if (version == null) {
				version = "";
			}

			StringBuilder m = new StringBuilder();
			m.append(title).append("\n");
			m.append("Version: ").append(version).append("\n\n");
			m.append("A software that enables you to see what you hear.\n");
			m.append("Crafted with love by Bohumír Zámečník since 2012.\n\n");
			m.append("http://harmoneye.com/");
			return m.toString();
		}

		public void handleOpenApplication(ApplicationEvent event) {
			// Ok, we know our application started
			// Not much to do about that..
		}

		public void handleOpenFile(ApplicationEvent event) {
			handle(event, "openFileInEditor: " + event.getFilename());
		}

		public void handlePreferences(ApplicationEvent event) {
			// TODO
			handle(event, "For now there are no preferences.");
		}

		public void handlePrintFile(ApplicationEvent event) {
			handle(event, "Sorry, printing not implemented");
		}

		public void handleQuit(ApplicationEvent event) {
			//handle(event, "exitAction");
			System.exit(0);
		}

		public void handleReOpenApplication(ApplicationEvent event) {
			event.setHandled(true);
			frame.setVisible(true);
		}
	}

	public ApplicationListener getApplicationListener() {
		return appListener;
	}
	
	private static class WebHelper {

		// TODO: the help page!
		public static final String HELP_URL = "http://harmoneye.com/?utm_campaign=help&utm_medium=macosxapp";

		public static void openWebpage(String uri) {
			if (!Desktop.isDesktopSupported()) {
				return;
			}
			Desktop desktop = Desktop.getDesktop();
			if (!desktop.isSupported(Desktop.Action.BROWSE)) {
				return;
			}
			try {
				desktop.browse(new URI(uri));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

}
