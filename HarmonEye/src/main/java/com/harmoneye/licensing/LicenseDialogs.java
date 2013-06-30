package com.harmoneye.licensing;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.harmoneye.WebHelper;

public class LicenseDialogs {

	private static final String WINDOW_TITLE = "HarmonEye";

	private JFrame frame;
	private LicenseManager licenseManager;

	public LicenseDialogs(JFrame frame, LicenseManager licenseManager) {
		this.frame = frame;
		this.licenseManager = licenseManager;
	}

	/**
	 * @return productKey
	 */
	public String showActivationDialog() {
		return (String) JOptionPane.showInputDialog(frame, "Please enter your product key:", "Activate HarmonEye",
			JOptionPane.PLAIN_MESSAGE, null, null, getExistingProductKey());
	}

	public void showLicenseInfoDialog() {
		if (licenseManager.isActivated()) {
			JOptionPane.showMessageDialog(frame, "Your product key is:\n" + getExistingProductKey(), WINDOW_TITLE
				+ " - License information", JOptionPane.PLAIN_MESSAGE);
		} else {
			int days = licenseManager.getRemainingTrialDays();
			String message = "You can try the application for free for another " + days + " days.";
			JOptionPane.showMessageDialog(frame, message, WINDOW_TITLE + " - License information",
				JOptionPane.PLAIN_MESSAGE);
		}
	}

	private String getExistingProductKey() {
		return licenseManager.getExistingProductKey();
	}

	public boolean showDeactivateDialog() {
		int returnCode = JOptionPane.showConfirmDialog(frame, "Would you really like to deactivate this license?\n"
			+ "After deactivation you will be able to activate\nit again on this or another computer.",
			"Deactivate HarmonEye?", JOptionPane.YES_NO_OPTION);
		return returnCode == JOptionPane.YES_OPTION;
	}

	public void showDeactivatedDialog() {
		JOptionPane.showConfirmDialog(frame, "This license has been deactivated.\n"
			+ "You can activate it again on this or another computer.\n" + "The application will be closed.",
			WINDOW_TITLE + " - License deactivated", JOptionPane.DEFAULT_OPTION);
	}

	public boolean offerOptionsWithoutActivation() {
		int option;
		boolean trialExpired = licenseManager.isTrialPeriodExpired();
		String title = WINDOW_TITLE + " - Not activated";
		if (!trialExpired) {
			int days = licenseManager.getRemainingTrialDays();
			String message = "You can try the application for free for another " + days + " days.";
			Object[] options = { "Activate", "Buy", "Try" };
			option = JOptionPane.showOptionDialog(frame, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
		} else {
			String message = "The trial period while you could try the application has ended.\nYou would need buy a license to continue using it.";
			Object[] options = { "Activate", "Buy", "Close" };
			option = JOptionPane.showOptionDialog(frame, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		}

		boolean canContinue = false;
		if (option == 0) {
			boolean activated = activate();
			canContinue = activated;
		} else if (option == 1) {
			buy();
			canContinue = !trialExpired;
		} else if (option == 2) {
			canContinue = !trialExpired;
		}
		return canContinue;
	}

	public void buy() {
		WebHelper.openWebpage(WebHelper.BUY_APP_URL);
	}

	public boolean activate() {
		String productKey = showActivationDialog();
		licenseManager.activate(productKey);
		boolean activated = licenseManager.isActivated();
		if (activated) {
			JOptionPane.showMessageDialog(frame, "Thanks a lot and enjoy!", WINDOW_TITLE + " - Activated",
				JOptionPane.PLAIN_MESSAGE);
		}
		return activated;
	}

	public void deactivate() {
		boolean deactivate = showDeactivateDialog();
		if (deactivate) {
			try {
				licenseManager.deactivate();
				showDeactivatedDialog();
				System.exit(0);
			} catch (Exception ex) {
				showErrorMessage(ex, "Problem with the deactivation");
			}
		}
	}

	private void showErrorMessage(Exception ex, String title) {
		String message = ex.getMessage();
		if (ex.getCause() != null) {
			message += "\n" + ex.getCause().getMessage();
		}
		JOptionPane.showMessageDialog(frame, message, WINDOW_TITLE + " - " + title, JOptionPane.ERROR_MESSAGE);
	}
}
