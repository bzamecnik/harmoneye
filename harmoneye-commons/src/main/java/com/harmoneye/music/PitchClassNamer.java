package com.harmoneye.music;

public class PitchClassNamer {

	private static PitchClassNamer ENGLISH_FLAT = new PitchClassNamer(
		new String[] { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A",
			"Bb", "B" });

	private static PitchClassNamer ENGLISH_SHARP = new PitchClassNamer(
		new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A",
			"A#", "B" });

	private static PitchClassNamer ROMAN_FLAT = new PitchClassNamer(
		new String[] { "I", "IIb", "II", "IIIb", "III", "IV", "Vb", "V", "VIb",
			"VI", "VIIb", "VII" });

	private static PitchClassNamer INTERVAL_FLAT = new PitchClassNamer(
		new String[] { "1", "b2", "2", "b3", "3", "4", "b5", "5", "b6", "6",
			"b7", "7" });

	private final String[] names;

	public static PitchClassNamer defaultInstance() {
		return ENGLISH_FLAT;
	}

	public static PitchClassNamer romanNumeralFlat() {
		return ROMAN_FLAT;
	}

	public static PitchClassNamer intervalFlat() {
		return INTERVAL_FLAT;
	}

	public PitchClassNamer(String[] names) {
		this.names = names;
	}

	public String getName(int pitchClass) {
		if (pitchClass < 0 || pitchClass >= names.length) {
			throw new IllegalArgumentException("Pitch class out of bounds: "
				+ pitchClass);
		}
		return names[pitchClass];
	}
}
