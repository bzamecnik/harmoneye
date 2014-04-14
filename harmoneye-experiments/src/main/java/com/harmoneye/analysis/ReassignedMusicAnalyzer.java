package com.harmoneye.analysis;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.analysis.StreamingReassignedSpectrograph.OutputFrame;
import com.harmoneye.audio.DoubleRingBuffer;
import com.harmoneye.audio.SoundConsumer;
import com.harmoneye.math.L1Norm;
import com.harmoneye.math.L2Norm;
import com.harmoneye.math.cqt.CqtContext;
import com.harmoneye.math.filter.ExpSmoother;
import com.harmoneye.viz.Visualizer;

public class ReassignedMusicAnalyzer implements SoundConsumer {

	/** [0.0; 1.0] 1.0 = no smoothing */
	private static final double SMOOTHING_FACTOR = 0.25;

	private CqtContext ctx;

	// private FastCqt cqt;
	// private MultiRateRingBufferBank ringBufferBank;
	private DoubleRingBuffer ringBuffer;
	// private DecibelCalculator dbCalculator;
	private HarmonicPatternPitchClassDetector pcDetector;
	private Visualizer<AnalyzedFrame> visualizer;
	// private MovingAverageAccumulator accumulator;
	private ExpSmoother accumulator;
	private ExpSmoother allBinSmoother;
	private ExpSmoother octaveBinSmoother;
	private NoiseGate noiseGate;
	private PercussionSuppressor percussionSuppressor;
	private SpectralEqualizer spectralEqualizer;
	private Median medianFilter;

	private StreamingReassignedSpectrograph chromagraph;

	private double[] samples;
	private double[] chromagram;
	private double[] wrappedChromagram;

	private AtomicBoolean initialized = new AtomicBoolean();
	private AtomicBoolean accumulatorEnabled = new AtomicBoolean();

	private static final boolean BIN_SMOOTHER_ENABLED = false;
	private static final boolean OCTAVE_BIN_SMOOTHER_ENABLED = false;
	private static final boolean HARMONIC_DETECTOR_ENABLED = false;
	private static final boolean PERCUSSION_SUPPRESSOR_ENABLED = false;
	private static final boolean SPECTRAL_EQUALIZER_ENABLED = false;
	private static final boolean NOISE_GATE_ENABLED = false;
	private static final boolean NOISE_GATE_MEDIAN_THRESHOLD_ENABLED = false;

	AtomicBoolean bufferLocked = new AtomicBoolean();

	private double[] outputWrappedChromagram;

	private AnalyzedFrame frame;

	private AtomicBoolean ringBufferLock = new AtomicBoolean();

	private PeakFilter peakFilter;

	private double[] accumulatedOctaveBins;

	private KeyDetector keyDetector;
	
	public ReassignedMusicAnalyzer(Visualizer<AnalyzedFrame> visualizer,
		float sampleRate, int bitsPerSample) {
		this.visualizer = visualizer;

		//@formatter:off
		ctx = CqtContext.create()
			.samplingFreq(sampleRate)
//			.maxFreq((2 << 6) * 65.4063913251)
//			.octaves(2)
			.kernelOctaves(1)
			.binsPerHalftone(10)
			.build();
		//@formatter:on

		int windowSize = 4096;

		samples = new double[windowSize];
		// chromagram = new double[ctx.getTotalBins()];
		// wrappedChromagram = new double[ctx.getBinsPerOctave()];

		// ringBufferBank = new
		// MultiRateRingBufferBank(ctx.getSignalBlockSize(),
		// ctx.getOctaves());
		ringBuffer = new DoubleRingBuffer(windowSize);
		// dbCalculator = new DecibelCalculator(bitsPerSample);
		pcDetector = new HarmonicPatternPitchClassDetector(ctx);
		octaveBinSmoother = new ExpSmoother(ctx.getBinsPerOctave(),
			SMOOTHING_FACTOR);
		allBinSmoother = new ExpSmoother(ctx.getTotalBins(), SMOOTHING_FACTOR);
		// accumulator = new MovingAverageAccumulator(ctx.getBinsPerOctave());
		accumulator = new ExpSmoother(ctx.getBinsPerOctave(), 0.005);
		if (NOISE_GATE_ENABLED) {
			noiseGate = new NoiseGate(ctx.getBinsPerOctave());
			if (NOISE_GATE_MEDIAN_THRESHOLD_ENABLED) {
				medianFilter = new Median();
			}
		}
		percussionSuppressor = new PercussionSuppressor(ctx.getTotalBins(), 7);
		spectralEqualizer = new SpectralEqualizer(ctx.getTotalBins(), 30);
//		peakFilter = new PeakFilter(ctx.getBinsPerHalftone() * ctx.getHalftonesPerOctave(), 1-1.0/30);
		peakFilter = new PeakFilter(ctx.getBinsPerHalftone() * ctx.getHalftonesPerOctave(), 1-1.0/60);

		keyDetector = new KeyDetector(ctx.getBinsPerHalftone(), ctx.getHalftonesPerOctave());
		
		// cqt = new FastCqt(ctx);

		chromagraph = new StreamingReassignedSpectrograph(windowSize, sampleRate);
	}

	public void init() {
		// cqt.init();
		initialized.set(true);
	}

	@Override
	public void consume(double[] samples) {
//		if (ringBufferLock.compareAndSet(false, true)) {
			//System.out.println("update():" + Arrays.toString(samples));
			ringBuffer.write(samples);
//			ringBufferLock.set(false);
//		}
	}

	public void updateSignal() {
		if (!initialized.get()) {
			return;
		}
//		if (ringBufferLock.compareAndSet(false, true)) {
		ringBuffer.readLast(samples.length, samples);
//		System.out.println(Arrays.toString(samples));
		
//		System.out.println(System.nanoTime() + " compute spectrogram - begin");
		OutputFrame outputFrame = chromagraph.computeChromagram(samples);
		chromagram = outputFrame.getChromagram();
		wrappedChromagram = outputFrame.getWrappedChromagram();
		
		wrappedChromagram = peakFilter.smooth(wrappedChromagram);
		
		wrappedChromagram = smooth(wrappedChromagram);
		
		int estimatedKey = keyDetector.detectKey(accumulatedOctaveBins);
		
		// ugly hack
		if (frame == null) {
			outputWrappedChromagram = new double[wrappedChromagram.length];
		}
		frame = new AnalyzedFrame(ctx, outputWrappedChromagram, outputWrappedChromagram, null, estimatedKey);
		// here is still a place for a race condition...
		System.arraycopy(wrappedChromagram, 0, outputWrappedChromagram, 0, wrappedChromagram.length); 
		visualizer.update(frame);
//		System.out.println(System.nanoTime() + " compute spectrogram - end");
//		ringBufferLock.set(false);
//		}
	}

	private AnalyzedFrame analyzeFrame(double[] allBins) {
		double[] detectedPitchClasses = null;

		if (BIN_SMOOTHER_ENABLED) {
			allBins = allBinSmoother.smooth(allBins);
		}
		if (PERCUSSION_SUPPRESSOR_ENABLED) {
			allBins = percussionSuppressor.filter(allBins);
		}

		if (HARMONIC_DETECTOR_ENABLED) {
			detectedPitchClasses = pcDetector.detectPitchClasses(allBins);
			if (SPECTRAL_EQUALIZER_ENABLED) {
				detectedPitchClasses = spectralEqualizer
					.filter(detectedPitchClasses);
			}
			wrappedChromagram = aggregateIntoOctaves(detectedPitchClasses,
				wrappedChromagram);
		} else {
			wrappedChromagram = aggregateIntoOctaves(allBins, wrappedChromagram);
		}

		double[] smoothedOctaveBins = smooth(wrappedChromagram);

		if (NOISE_GATE_ENABLED) {
			if (NOISE_GATE_MEDIAN_THRESHOLD_ENABLED) {
				double medianValue = medianFilter.evaluate(smoothedOctaveBins);
				noiseGate.setOpenThreshold(medianValue * 1.25);
			}
			noiseGate.filter(smoothedOctaveBins);
		}

		AnalyzedFrame pcProfile = new AnalyzedFrame(ctx, allBins,
			smoothedOctaveBins, detectedPitchClasses, 0);
		return pcProfile;
	}

	private double[] aggregateIntoOctaves(double[] bins, double[] octaveBins) {
		int binsPerOctave = ctx.getBinsPerOctave();
		for (int i = 0; i < binsPerOctave; i++) {
			// maximum over octaves:
			double value = 0;
			for (int j = i; j < bins.length; j += binsPerOctave) {
				value = FastMath.max(value, bins[j]);
			}
			octaveBins[i] = value;
		}

		return octaveBins;
	}

	private double[] smooth(double[] octaveBins) {
		double[] smoothedOctaveBins = null;
		accumulatedOctaveBins = accumulator.smooth(octaveBins);
		if (accumulatorEnabled.get()) {
			// accumulator.add(octaveBins);
			// smoothedOctaveBins = accumulator.getAverage();
			smoothedOctaveBins = accumulatedOctaveBins;
		} else if (OCTAVE_BIN_SMOOTHER_ENABLED) {
			smoothedOctaveBins = octaveBinSmoother.smooth(octaveBins);
		} else {
			smoothedOctaveBins = octaveBins;
		}
		return smoothedOctaveBins;
	}

	public void toggleAccumulatorEnabled() {
		accumulatorEnabled.set(!accumulatorEnabled.get());
		if (accumulatorEnabled.get()) {
			// accumulator.reset();
		}
	}

}
