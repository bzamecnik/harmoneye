package com.harmoneye.concurrency;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.harmoneye.audio.DoubleRingBuffer;

public class ConsumerWithCondition {

	private static final int RING_BUFFER_SIZE = 30;

	public static void main(String[] args) throws InterruptedException {
		DoubleRingBuffer buffer = new DoubleRingBuffer(RING_BUFFER_SIZE);

		Producer producer = new Producer(buffer);
		Consumer consumer = new Consumer(buffer);

		producer.run();

		Thread.sleep(500);
		
		Thread consumerThread = new Thread(consumer);
		consumerThread.start();

		Thread.sleep(5000);

		producer.stop();
		System.out.println("producer stopped");
	}

	private static class Producer implements Runnable {
		private static final int INPUT_BUFFER_SIZE = 5;
		private double[] values = new double[INPUT_BUFFER_SIZE];
		private int counter = 0;
		private DoubleRingBuffer ringBuffer;
		private Timer timer = new Timer();

		public Producer(DoubleRingBuffer ringBuffer) {
			this.ringBuffer = ringBuffer;
		}

		public void run() {
			System.out.println("producer started");

			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					produce();
				}
			}, 0, 1000);
			System.out.println("producer timer launched");
		}

		public void stop() {
			timer.cancel();
		}

		private void produce() {
			for (int i = 0; i < INPUT_BUFFER_SIZE; i++) {
				values[i] = counter;
				counter++;
			}
			System.out.println("producing: " + Arrays.toString(values));
			ringBuffer.write(values);
			System.out.println("produced");
		}
	}

	private static class Consumer implements Runnable {

		private DoubleRingBuffer ringBuffer;
		private int windowSize = 15;
		private int hopSize = 3;

		public Consumer(DoubleRingBuffer ringBuffer) {
			this.ringBuffer = ringBuffer;
		}

		@Override
		public void run() {
			System.out.println("consumer started");

			double[] window = new double[windowSize];

			while (!Thread.currentThread().isInterrupted()) {
				System.out.println("capacityForRead: "
					+ ringBuffer.getCapacityForRead());
				try {
					System.out.println("consumer: waiting");
					while (!canConsume()) {
						ringBuffer.awaitNotEmpty();
						System.out.println("consumer: signalled");
					}
					while (canConsume()) {
						System.out.println("consumer: consuming");
						ringBuffer.read(windowSize, window);
						ringBuffer.incrementReadIndex(hopSize);

						consume(window);
						System.out.println("consumer: consumed");
					}
					System.out.println("consumer: nothing to consume");
				} catch (InterruptedException e) {
					continue;
				}
			}

			System.out.println("consumer died");
		}

		private boolean canConsume() {
			return ringBuffer.getCapacityForRead() >= windowSize;
		}

		private void consume(double[] window) {
			System.out.println("consume: " + Arrays.toString(window));
		}
	}
}
