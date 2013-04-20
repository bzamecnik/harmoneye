package com.harmoneye.licensing;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LicenseDialogs {

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
		JOptionPane.showMessageDialog(frame, "Your product key is:\n" + getExistingProductKey(), "License information",
			JOptionPane.PLAIN_MESSAGE);
	}

	public boolean showDeactivateDialog() {
		int returnCode = JOptionPane.showConfirmDialog(frame, "Would you really like to deactivate this license?\n"
			+ "After deactivation you will be able to activate\nit again on this or another computer.",
			"Deactivate HarmonEye?", JOptionPane.YES_NO_OPTION);
		return returnCode == JOptionPane.YES_OPTION;
	}

	public void showDeactivatedDialog() {
		JOptionPane.showConfirmDialog(frame, "This license has been deactivated.\n"
			+ "You can activate it again on this or another computer.", "License deactivated", JOptionPane.DEFAULT_OPTION);
	}

	private String getExistingProductKey() {
		return licenseManager.getExistingProductKey();
	}
}
