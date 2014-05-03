package com.harmoneye.ng;

/**
 * @param <T> feature set type
 */
interface FeatureExtractor<T> {
	T extract(double[] samples);
}