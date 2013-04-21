package com.harmoneye;

import java.awt.Component;

public interface SwingVisualizer<T> extends Visualizer<T> {

	Component getComponent();
	
}
