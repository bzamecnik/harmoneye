package com.harmoneye.audio;

/*
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in 
 * source and binary code form, provided that i) this copyright
 * notice and license appear on all copies of the software; and 
 * ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty
 * of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS
 * AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE 
 * HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT
 * WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS
 * OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY
 * TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES.

 This software is not designed or intended for use in on-line
 control of aircraft, air traffic, aircraft navigation or
 aircraft communications; or in the design, construction,
 operation or maintenance of any nuclear facility. Licensee 
 represents and warrants that it will not use or redistribute 
 the Software for such purposes.
 */

/*  The above copyright statement is included because this 
 * program uses several methods from the JavaSoundDemo
 * distributed by SUN. In some cases, the sound processing methods
 * unmodified or only slightly modified.
 * All other methods copyright Steve Potts, 2002
 */

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import com.harmoneye.app.Config;

/**
 * SimpleSoundCapture Example. This is a simple program to record sounds and
 * play them back. It uses some methods from the CapturePlayback program in the
 * JavaSoundDemo. For licensizing reasons the disclaimer above is included.
 * 
 * @author Steve Potts
 */
public class SimpleFilePlayer {

	private static final int BUFFER_SIZE = 16384;

	private String inputFileName;

	public SimpleFilePlayer(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public void play() throws Exception {
		File file = new File(inputFileName);
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);

		AudioFormat format = audioInputStream.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			throw new Exception("Line matching " + info + " not supported.");
		}

		SourceDataLine playbackLine = (SourceDataLine) AudioSystem.getLine(info);
		playbackLine.open(format, BUFFER_SIZE);

		int frameSizeInBytes = format.getFrameSize();
		int bufferLengthInFrames = playbackLine.getBufferSize() / 8;
		int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		byte[] buffer = new byte[bufferLengthInBytes];

		playbackLine.start();

		int readBytesCount = 0;
		while (readBytesCount >= 0) {
			int bytesToWriteCount = readBytesCount;
			while (bytesToWriteCount > 0) {
				int writtenBytesCount = playbackLine.write(buffer, 0, bytesToWriteCount);
				bytesToWriteCount -= writtenBytesCount;
			}
			readBytesCount = audioInputStream.read(buffer);
		}

		playbackLine.drain();
		playbackLine.stop();
		playbackLine.close();
	}

	public static void main(String s[]) throws Exception {
		Config config = Config.fromDefault();
		String inputFileName = config.get("inputFile");
		SimpleFilePlayer player = new SimpleFilePlayer(inputFileName);
		player.play();
	}
}