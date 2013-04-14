package com.harmoneye;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.DefaultApplication;

public class HarmonEye {
	public static void main(String[] args) throws Exception {
		// Must be before setLookAndFeel
		macSetup("HarmonEye");
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Application app = new DefaultApplication();

				CaptureHarmonEyeApp captureHarmonEyeApp = new CaptureHarmonEyeApp();
				captureHarmonEyeApp.start();

				app.addApplicationListener(captureHarmonEyeApp.getApplicationListener());
				app.addPreferencesMenuItem();
				app.setEnabledPreferencesMenu(true);
			}
		});
	}

	private static void macSetup(String appName) {
		String os = System.getProperty("os.name").toLowerCase();
		boolean isMac = os.startsWith("mac os x");

		if (!isMac) {
			return;
		}

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
	}

}
