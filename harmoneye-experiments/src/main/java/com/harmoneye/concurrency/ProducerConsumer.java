package com.harmoneye.concurrency;

import com.harmoneye.audio.ByteConverter;
import com.harmoneye.audio.SoundCapture;

/**
 * 1 producer and 3 consumers producing/consuming 10 items
 * 
 * @author pt
 * 
 */
public class ProducerConsumer {

	private static final int INPUT_BUFFER_SAMPLE_COUNT = 2048;

	private byte[] rawSamples;
	private Object lock = new Object();

	public static void main(String args[]) throws Exception {
		ProducerConsumer pc = new ProducerConsumer();
		Thread t1 = new Thread(pc.new Producer());
		Thread t2 = new Thread(pc.new MaxAmplitudeConsumer());
		Thread t3 = new Thread(pc.new MinAmplitudeConsumer());
		t1.start();
		t2.start();
		t3.start();
		try {
			t1.join();
			t2.join();
			//			t3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class Producer implements Runnable {

		private SoundCapture soundCapture;

		public Producer() throws Exception {
			soundCapture = SoundCapture.createWithDefaultAudioFormat(INPUT_BUFFER_SAMPLE_COUNT);
		}

		public void produce() {
			rawSamples = soundCapture.next();
		}

		public void run() {
			long start = System.nanoTime();
			soundCapture.start();
			while (soundCapture.hasNext()) {
				long now = System.nanoTime();
				if (now - start > 10 * 1e9) {
					break;
				}
				synchronized (lock) {
					produce();
					lock.notifyAll();
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	abstract class AbstractConsumer implements Runnable {

		public abstract void consume();

		private boolean isStarving() {
			return rawSamples == null;
		}

		public void run() {
			while (isStarving()) {
				synchronized (lock) {
					while (isStarving()) {
						try {
							lock.wait(10);
						} catch (InterruptedException e) {
							Thread.interrupted();
						}
					}
					consume();
				}
			}
		}
	}

	class MaxAmplitudeConsumer extends AbstractConsumer {

		private double[] samples = new double[2 * INPUT_BUFFER_SAMPLE_COUNT];

		private int count = 0;

		public void consume() {
			ByteConverter.littleEndianBytesToDoubles(rawSamples, samples);
			System.out.println("MAX [" + count + "]: " + maxAmplitude(samples));
			rawSamples = null;
			count++;
		}

		private double maxAmplitude(double[] samples) {
			double max = Double.MIN_VALUE;
			for (int i = 0; i < samples.length; i++) {
				max = Math.max(max, samples[i]);
			}
			return max;
		}
	}

	class MinAmplitudeConsumer extends AbstractConsumer {

		double[] samples = new double[2 * INPUT_BUFFER_SAMPLE_COUNT];
		private int count = 0;

		public void consume() {
			ByteConverter.littleEndianBytesToDoubles(rawSamples, samples);
			System.out.println("MIN [" + count + "]: " + minAmplitude(samples));
			rawSamples = null;
			count++;
		}

		private double minAmplitude(double[] samples) {
			double max = Double.MAX_VALUE;
			for (int i = 0; i < samples.length; i++) {
				max = Math.min(max, samples[i]);
			}
			return max;
		}
	}
}