package com.harmoneye.app.spectrogram;

import com.harmoneye.analysis.MagnitudeSpectrogram;

public interface MagnitudeSpectrograph {
	MagnitudeSpectrogram computeMagnitudeSpectrogram(SampledAudio audio);
}
