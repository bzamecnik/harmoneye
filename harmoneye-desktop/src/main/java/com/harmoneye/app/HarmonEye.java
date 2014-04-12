package com.harmoneye.app;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.DefaultApplication;

/**
 * Application launcher for Mac OS X.
 * 
 */
public class HarmonEye {

	private static final String MAC_APP_NAME = "HarmonEye";

	public static void main(String[] args) throws Exception {
		// must be called before setLookAndFeel()
		macSetup();

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		SwingUtilities.invokeLater(new AppThread());
	}

	private static void macSetup() {
		if (isMac()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", MAC_APP_NAME);
		}
	}

	private static boolean isMac() {
		String os = System.getProperty("os.name");
		return os.toLowerCase().startsWith("mac os x");
	}

	private static final class AppThread implements Runnable {
		@Override
		public void run() {
			Application app = new DefaultApplication();

			final CaptureHarmonEyeApp captureHarmonEyeApp = new CaptureHarmonEyeApp();
			class Initializer extends SwingWorker<String, Object> {
				@Override
				public String doInBackground() {
					captureHarmonEyeApp.init();
					captureHarmonEyeApp.start();
					return null;
				}
			}

			new Initializer().execute();

			app.addApplicationListener(captureHarmonEyeApp.getApplicationListener());
			app.addPreferencesMenuItem();
			app.setEnabledPreferencesMenu(true);
		}
	}
}
