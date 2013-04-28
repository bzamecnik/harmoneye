package com.harmoneye;

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
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;

import com.harmoneye.licensing.LicenseDialogs;
import com.harmoneye.licensing.LicenseManager;

public class AbstractHarmonEyeApp {

	private static final int TIME_PERIOD_MILLIS = 25;
	private static final String WINDOW_TITLE = "HarmonEye";

	private Timer timer;
	protected MusicAnalyzer soundAnalyzer;

	private JFrame frame;

	private CircleOfFifthsEnabledAction circleOfFifthsEnabledAction;
	private PauseAction pauseAction;
	private JMenuItem pauseMenuItem;
	private AccumulationEnabledAction accumulationEnabledAction;

	private ApplicationListener appListener;

	private LicenseManager licenseManager;
	private LicenseDialogs licenseDialogs;
	private SwingVisualizer<PitchClassProfile> visualizer;

	public AbstractHarmonEyeApp() {
		licenseManager = new LicenseManager();
		licenseManager.init();

		timer = new Timer(TIME_PERIOD_MILLIS, new TimerActionListener());
		timer.setInitialDelay(190);

//		visualizer = new Java2dCircularVisualizer();
		visualizer = new OpenGlCircularVisualizer();
		
		soundAnalyzer = new MusicAnalyzer(visualizer);

		circleOfFifthsEnabledAction = new CircleOfFifthsEnabledAction("Circle of fifths", null, "", new Integer(
			KeyEvent.VK_F));
		pauseAction = new PauseAction("Pause", null, "", new Integer(KeyEvent.VK_P));
		accumulationEnabledAction = new AccumulationEnabledAction("Accumulate", null, "", new Integer(KeyEvent.VK_A));

		frame = createFrame();

		appListener = new MyApplicationListener(frame);

		licenseDialogs = new LicenseDialogs(frame, licenseManager);

		checkActivation();

		frame.setVisible(true);
	}

	private void checkActivation() {
		// TODO: show a dialog: activate/buy license
		// TOOD: allow a trial mode

		try {
			licenseManager.checkActivation();
			if (!licenseManager.isActivated()) {
				String productKey = licenseDialogs.showActivationDialog();
				if (StringUtils.isNotBlank(productKey)) {
					licenseManager.activate(productKey);
				}
			}
		} catch (Exception ex) {
			String message = ex.getMessage();
			if (ex.getCause() != null) {
				message += "\n" + ex.getCause().getMessage();
			}
			JOptionPane.showMessageDialog(frame, message, "Problem with the activation", JOptionPane.ERROR_MESSAGE);
		}
		if (!licenseManager.isActivated()) {
			System.exit(0);
		}
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

		menuBar.add(menu);

		menu = new JMenu("License");

		JMenuItem showLicenseMenuItem = new JMenuItem();
		showLicenseMenuItem.setAction(new AbstractAction("Show license details") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				licenseDialogs.showLicenseInfoDialog();
			}
		});
		menu.add(showLicenseMenuItem);

		JMenuItem deactivateMenuItem = new JMenuItem();
		deactivateMenuItem.setAction(new AbstractAction("Deactivate") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean deactivate = licenseDialogs.showDeactivateDialog();
				if (deactivate) {
					try {
						licenseManager.deactivate();
						licenseDialogs.showDeactivatedDialog();
						System.exit(0);
					} catch (Exception ex) {
						String message = ex.getMessage();
						if (ex.getCause() != null) {
							message += "\n" + ex.getCause().getMessage();
						}
						JOptionPane.showMessageDialog(frame, message, "Problem with the deactivation",
							JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		menu.add(deactivateMenuItem);

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
		if (!licenseManager.isActivated()) {
			return;
		}

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


	private final class TimerActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			soundAnalyzer.updateSignal();
			//visualizer.getPanel().repaint();
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
			JOptionPane.showMessageDialog(frame, "HarmonEye\n" + "A software that enables you to see what you hear.\n"
				+ "Crafted with love by Bohumír Zámečník since 2012.\n\n" + "http://harmoneye.com/", "About HarmonEye",
				JOptionPane.INFORMATION_MESSAGE);
			event.setHandled(true);
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
