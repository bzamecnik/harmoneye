package com.harmoneye.viz;

import java.util.Map;

public interface Visualizer<T> {

	void update(T data);

	Map<String, Object> getConfig();

}