package com.harmoneye;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class StaticVisualizerApp extends JPanel {

	private static final long serialVersionUID = 1L;

	private static double[] pcProfile = { 0.3041186413764153,
			0.488348077814431, 0.6338482229286416, 0.6114733708636751,
			0.5433162346858984, 0.41503266425616264, 0.29313939965109137,
			0.35303742456077936, 0.3049398800457355, 0.288978939092745,
			0.3307008122044611, 0.3027356610219062, 0.4064024450950668,
			0.34323988086491874, 0.3538055405451057, 0.30909087232294574,
			0.26472754325066794, 0.2569321672755722, 0.3351718314077321,
			0.3957545815145435, 0.44945015795745663, 0.4882667376561512,
			0.46036751026268136, 0.4655163037848545, 0.42795346759565145,
			0.3636904552123416, 0.28950009826089984, 0.27017868161459035,
			0.3156513910710033, 0.38022688226259665, 0.30986678517499616,
			0.2821646115797838, 0.31137674549146044, 0.38482359429421104,
			0.3264560344453969, 0.31773358812212005, 0.40748855376260085,
			0.7514696649257182, 0.7735895691740594, 0.5408170376471453,
			0.41426633615283703, 0.356720780266001, 0.28131923502442385,
			0.330370551065478, 0.27159649944956277, 0.29323633889224193,
			0.2381518228210138, 0.25165875705452717, 0.3185602598405816,
			0.30450767645578214, 0.33478783886068525, 0.22487476007145793,
			0.2265049814004759, 0.29075221898898546, 0.2685338912283162,
			0.296836946481297, 0.3394988783623299, 0.37399693845130627,
			0.39056981482573594, 0.3127457237708192, 0.3580843366608671, };

	protected AbstractVisualizer visualizer;

	public StaticVisualizerApp() {
		visualizer = new CircularVisualizer(this);
		visualizer.setBinsPerHalftone(5);
		visualizer.setPitchBinCount(12 * 5);
		visualizer.setPitchClassProfile(pcProfile);

		JFrame frame = new JFrame("HarmonEye");
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHints(rh);

		setBackground(Color.DARK_GRAY);
		visualizer.paint(g2);
	}

	public static void main(String[] args) {
		new StaticVisualizerApp();
	}
}
