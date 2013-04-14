package com.harmoneye;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.Timer;

public class AbstractHarmonEyeApp extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final int TIME_PERIOD_MILLIS = 20;

	private Timer timer;
	protected MusicAnalyzer soundAnalyzer;

	private CircleOfFifthsEnabledAction circleOfFifthsEnabledAction;
	private JCheckBoxMenuItem circleOfFifthsEnabledMenuItem;

	public AbstractHarmonEyeApp() {
		timer = new Timer(TIME_PERIOD_MILLIS, this);
		timer.setInitialDelay(190);
		soundAnalyzer = new MusicAnalyzer(this);

		JFrame frame = new JFrame("HarmonEye");
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);

		circleOfFifthsEnabledAction = new CircleOfFifthsEnabledAction(
				"Circle of fifths", null, "", new Integer(KeyEvent.VK_F));

		frame.setJMenuBar(createMenuBar());

		frame.setVisible(true);

		// enable pausing
		this.addMouseListener(new MouseClickListener());
	}

	private JMenuBar createMenuBar() {
		JMenu mainMenu = new JMenu("Settings");

		circleOfFifthsEnabledMenuItem = new JCheckBoxMenuItem(
				circleOfFifthsEnabledAction);
		circleOfFifthsEnabledMenuItem.setSelected(false);
		circleOfFifthsEnabledMenuItem.setIcon(null);
		mainMenu.add(circleOfFifthsEnabledMenuItem);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(mainMenu);
		return menuBar;
	}

	public void start() {
		timer.start();
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		soundAnalyzer.paint(g2);
	}

	public void actionPerformed(ActionEvent e) {
		soundAnalyzer.updateSignal();
		repaint();
	}

	private final class MouseClickListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (timer.isRunning()) {
				timer.stop();
			} else {
				timer.restart();
			}
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

		public CircleOfFifthsEnabledAction(String text, ImageIcon icon,
				String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			boolean fifthsEnabled = circleOfFifthsEnabledMenuItem.getState();
			soundAnalyzer.getVisualizer().setPitchStep(fifthsEnabled ? 7 : 1);
			repaint();
		}
	}
}