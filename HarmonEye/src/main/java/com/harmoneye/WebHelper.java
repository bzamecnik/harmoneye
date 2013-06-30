package com.harmoneye;

import java.awt.Desktop;
import java.net.URI;

public class WebHelper {

	// TODO: utm params
	// TODO: the buy page
	public static final String BUY_APP_URL = "http://harmoneye.com/#buy?utm_campaign=buy_from_app&utm_medium=macosxapp";

	public static void openWebpage(String uri) {
		if (!Desktop.isDesktopSupported()) {
			return;
		}
		Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.BROWSE)) {
			return;
		}
		try {
			desktop.browse(new URI(uri));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
