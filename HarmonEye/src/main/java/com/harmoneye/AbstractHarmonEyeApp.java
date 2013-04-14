package com.harmoneye;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;

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

	public AbstractHarmonEyeApp() {
		timer = new Timer(TIME_PERIOD_MILLIS, new TimerActionListener());
		timer.setInitialDelay(190);

		visualizerPanel = new VisualizerPanel();
		// enable pausing
		visualizerPanel.addMouseListener(new MouseClickListener());

		soundAnalyzer = new MusicAnalyzer(visualizerPanel);

		circleOfFifthsEnabledAction = new CircleOfFifthsEnabledAction("Circle of fifths", null, "", new Integer(
			KeyEvent.VK_F));
		pauseAction = new PauseAction("Pause", null, "", new Integer(KeyEvent.VK_P));

		createFrame();
	}

	private void createFrame() {
		frame = new JFrame(WINDOW_TITLE);
		frame.add(visualizerPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);

		frame.setJMenuBar(createMenuBar());

		frame.setVisible(true);
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("Analysis");

		pauseMenuItem = new JMenuItem(pauseAction);
		pauseMenuItem.setIcon(null);
		menu.add(pauseMenuItem);

		menuBar.add(menu);

		menu = new JMenu("Settings");

		circleOfFifthsEnabledMenuItem = new JCheckBoxMenuItem(circleOfFifthsEnabledAction);
		circleOfFifthsEnabledMenuItem.setSelected(false);
		circleOfFifthsEnabledMenuItem.setIcon(null);
		menu.add(circleOfFifthsEnabledMenuItem);

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

	private final class MouseClickListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			pauseAction.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, "Pause",
				new Date().getTime(), 0));
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
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
}