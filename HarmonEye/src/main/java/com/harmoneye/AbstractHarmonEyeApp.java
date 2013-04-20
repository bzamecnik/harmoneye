package com.harmoneye;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;

public class AbstractHarmonEyeApp {

	private static final int TIME_PERIOD_MILLIS = 20;
	private static final String WINDOW_TITLE = "HarmonEye";

	private Timer timer;
	protected MusicAnalyzer soundAnalyzer;

	private VisualizerPanel visualizerPanel;
	private JFrame frame;

	private CircleOfFifthsEnabledAction circleOfFifthsEnabledAction;
	private JCheckBoxMenuItem circleOfFifthsEnabledMenuItem;
	private PauseAction pauseAction;
	private JMenuItem pauseMenuItem;
	private AccumulationEnabledAction accumulationEnabledAction;
	private JCheckBoxMenuItem accumulationEnabledMenuItem;

	private ApplicationListener appListener;

	public AbstractHarmonEyeApp() {
		timer = new Timer(TIME_PERIOD_MILLIS, new TimerActionListener());
		timer.setInitialDelay(190);

		visualizerPanel = new VisualizerPanel();

		soundAnalyzer = new MusicAnalyzer(visualizerPanel);

		circleOfFifthsEnabledAction = new CircleOfFifthsEnabledAction("Circle of fifths", null, "", new Integer(
			KeyEvent.VK_F));
		pauseAction = new PauseAction("Pause", null, "", new Integer(KeyEvent.VK_P));
		accumulationEnabledAction = new AccumulationEnabledAction("Accumulate", null, "", new Integer(KeyEvent.VK_A));

		frame = createFrame();

		appListener = new MyApplicationListener();
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame(WINDOW_TITLE);
		frame.add(visualizerPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);

		frame.setJMenuBar(createMenuBar());

		frame.setVisible(true);

		return frame;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("Visualization");

		pauseMenuItem = new JMenuItem(pauseAction);
		pauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(' '));
		menu.add(pauseMenuItem);

		circleOfFifthsEnabledMenuItem = new JCheckBoxMenuItem(circleOfFifthsEnabledAction);
		circleOfFifthsEnabledMenuItem.setAccelerator(KeyStroke.getKeyStroke('f'));
		menu.add(circleOfFifthsEnabledMenuItem);

		accumulationEnabledMenuItem = new JCheckBoxMenuItem(accumulationEnabledAction);
		accumulationEnabledMenuItem.setAccelerator(KeyStroke.getKeyStroke('a'));
		menu.add(accumulationEnabledMenuItem);

		menuBar.add(menu);

		menu = new JMenu("Window");

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

		menuBar.add(menu);

		return menuBar;
	}

	public void start() {
		timer.start();
		pauseMenuItem.setText("Pause");
		frame.setTitle(WINDOW_TITLE);
	}

	public void stop() {
		timer.stop();
		pauseMenuItem.setText("Play");
		frame.setTitle("= " + WINDOW_TITLE + " =");
	}

	private void toggle() {
		if (timer.isRunning()) {
			stop();
		} else {
			start();
		}
	}

	private class VisualizerPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g) {
			super.paintComponent(g);
			soundAnalyzer.paint((Graphics2D) g);
		}
	}

	private final class TimerActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			soundAnalyzer.updateSignal();
			visualizerPanel.repaint();
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
			// boolean fifthsEnabled = circleOfFifthsEnabledMenuItem.getState();
			fifthsEnabled = !fifthsEnabled;
			soundAnalyzer.getVisualizer().setPitchStep(fifthsEnabled ? 7 : 1);
			visualizerPanel.repaint();
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
	public class MyApplicationListener implements ApplicationListener {

		private void handle(ApplicationEvent event, String message) {
			JOptionPane.showMessageDialog(frame, message);
			event.setHandled(true);
		}

		public void handleAbout(ApplicationEvent event) {
			handle(event, "aboutAction");
		}

		public void handleOpenApplication(ApplicationEvent event) {
			// Ok, we know our application started
			// Not much to do about that..
		}

		public void handleOpenFile(ApplicationEvent event) {
			handle(event, "openFileInEditor: " + event.getFilename());
		}

		public void handlePreferences(ApplicationEvent event) {
			handle(event, "preferencesAction");
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
}
