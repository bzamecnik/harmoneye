package com.harmoneye.analysis;

import com.harmoneye.math.cqt.CqtContext;

public class PitchClassProfile {

	private final double[] pitchClassBins;
	private final CqtContext ctx;

	public PitchClassProfile(double[] pitchClassBins, CqtContext ctx) {
		assert pitchClassBins != null;
		assert ctx != null;
		assert pitchClassBins.length == ctx.getTotalBins();

		this.pitchClassBins = pitchClassBins;
		this.ctx = ctx;
	}

	public double[] getPitchClassBins() {
		return pitchClassBins;
	}

	public CqtContext getCtxContext() {
		return ctx;
	}
}
