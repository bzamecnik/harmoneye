package com.harmoneye.music;

import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoneye.math.Modulo;

public class PitchClassSetNamer {

	private Map<Integer, Map<String, Object>> names;
	private PitchClassNamer pitchClassNamer;

	private PitchClassSetNamer(Map<Integer, Map<String, Object>> names,
		PitchClassNamer pitchClassNamer) {
		this.names = names;
		this.pitchClassNamer = pitchClassNamer;
	}

	@SuppressWarnings("unchecked")
	public static PitchClassSetNamer fromJson(InputStream inputStream,
		PitchClassNamer pitchClassNamer) {
		try {
			JsonFactory f = new JsonFactory();
			f.enable(JsonParser.Feature.ALLOW_COMMENTS);
			ObjectMapper objectMapper = new ObjectMapper(f);
			Object n = objectMapper.readValue(inputStream,
				new TypeReference<Map<Integer, Map<String, Object>>>() {
				});
			return new PitchClassSetNamer(
				(Map<Integer, Map<String, Object>>) n, pitchClassNamer);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	public String getName(PitchClassSet tones) {
		Map<String, Object> details = names.get(tones.getCanonic().getIndex());
		if (details == null) {
			return "";
		}
		Integer templateRoot = (Integer) details.get("root");
		if (templateRoot == null) {
			templateRoot = 0;
		}
		int root = Modulo.modulo(templateRoot + tones.getRoot(),
			PitchClassSet.OCTAVE_SIZE);
		String rootName = pitchClassNamer.getName(root);
		String chordName = (String) details.get("chord");
		if (chordName == null) {
			chordName = "";
		}
		chordName = chordName.replace("X", rootName);
		return chordName;
	}
}