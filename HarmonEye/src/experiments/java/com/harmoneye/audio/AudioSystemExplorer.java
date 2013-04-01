package com.harmoneye.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

public class AudioSystemExplorer {
	public static void main(String[] args) throws Exception {
//		TargetDataLine line = getTargetDataLine();
//		System.out.println(line);
		printMixerInfo();
	}

	private static TargetDataLine getTargetDataLine() {
		TargetDataLine line = null;
		AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			System.err.println("line is not supported");
		}
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
		} catch (LineUnavailableException ex) {
			System.err.println(ex);
		}
		return line;
	}

	private static Port getMicrophonePort() throws LineUnavailableException {
		if (AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
			return (Port) AudioSystem.getLine(Port.Info.MICROPHONE);
		}
		return null;
	}

	private static void printMixerInfo() {
		Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Info info : mixerInfos) {
			System.out.println(info);
			Mixer mixer = AudioSystem.getMixer(info);
			System.out.println(mixer);
			printLineInfo(mixer);
			System.out.println();
		}
	}

	private static void printLineInfo(Mixer mixer) {
		javax.sound.sampled.Line.Info[] sourceLineInfos = mixer
				.getSourceLineInfo();
		for (javax.sound.sampled.Line.Info lineInfo : sourceLineInfos) {
			System.out.println(lineInfo);
		}
	}
}
