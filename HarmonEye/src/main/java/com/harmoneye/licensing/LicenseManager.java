package com.harmoneye.licensing;

import java.util.EnumSet;

import com.wyday.turboactivate.IsGenuineResult;
import com.wyday.turboactivate.TurboActivate;
import com.wyday.turboactivate.TurboActivateException;

public class LicenseManager {

	private static final String PRODUCT_VERSION_GUID = "1c9b70455169ca3a9b2056.49075694";

	private static final int ONLINE_CHECK_INTERVAL_IN_DAYS = 90;
	private static final int OFFLINE_GRACE_PERIOD_IN_DAYS = 14;

	//@formatter:off
	private static final EnumSet<IsGenuineResult> ACTIVATED = EnumSet.of(
		IsGenuineResult.Genuine,
		IsGenuineResult.GenuineFeaturesChanged,
		// an Internet error means the user is activated but
		// TurboActivate failed to contact the LimeLM servers
		IsGenuineResult.InternetError);
	//@formatter:on

	private boolean isActivated;

	public void init() {
		TurboActivate.VersionGUID = PRODUCT_VERSION_GUID;

		try {
			// "SetPDetsLocation" loads the TurboActivate.dat file from the TurboActivateFolder directory
			TurboActivate.SetPDetsLocation();
		} catch (TurboActivateException e) {
			throw new RuntimeException("Failed to initialize the licensing library.", e);
		}
	}

	public void checkActivation() {
		// Check if we're activated, and every 90 days verify it with the activation servers
		// In this example we won't show an error if the activation was done offline
		// (see the 3rd parameter of the IsGenuine() function)
		// http://wyday.com/limelm/help/offline-activation/

		try {
			IsGenuineResult result = TurboActivate.IsGenuine(ONLINE_CHECK_INTERVAL_IN_DAYS,
				OFFLINE_GRACE_PERIOD_IN_DAYS, true, false);

			isActivated = ACTIVATED.contains(result);

			if (result == IsGenuineResult.InternetError) {
				//TODO: give the user the option to retry the genuine checking immediately
				//      For example a dialog box. In the dialog call IsGenuine() to retry immediately
			}
		} catch (TurboActivateException e) {
			throw new RuntimeException("Failed to check the license activation.", e);
		}
	}

	public boolean isActivated() {
		return isActivated;
	}

	public void setActivated(boolean isActivated) {
		this.isActivated = isActivated;
	}

	public String getExistingProductKey() {
		try {
			if (TurboActivate.IsProductKeyValid()) {
				return TurboActivate.GetPKey();
			}
		} catch (Exception ex) {
			// TODO
		}
		return "";
	}

	public void activate(String productKey) {
		try {
			saveProductKey(productKey);
			TurboActivate.Activate();
			isActivated = TurboActivate.IsActivated();
		} catch (Exception ex) {
			throw new RuntimeException("Could not activate the product", ex);
		}
	}

	public void deactivate() {
		try {
			// do not forget the product key
			TurboActivate.Deactivate(false);
			isActivated = false;
		} catch (Exception ex) {
			throw new RuntimeException("Could not deactivate the product", ex);
		}
	}

	private void saveProductKey(String productKey) throws TurboActivateException, Exception {
		String existingKey = getExistingProductKey();
		if (!productKey.equals(existingKey)) {
			boolean saved = TurboActivate.CheckAndSavePKey(productKey);
			if (!saved) {
				throw new Exception("The product key is not valid.");
			}
		}
	}
}
