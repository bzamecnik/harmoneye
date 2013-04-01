package com.harmoneye.cqt;

import org.apache.commons.math3.complex.Complex;

public interface Cqt {

	Complex[] transform(double[] signal);

}
