package com.harmoneye.viz;

import java.awt.Component;

public interface SwingVisualizer<T> extends Visualizer<T> {

	Component getComponent();
	
}
