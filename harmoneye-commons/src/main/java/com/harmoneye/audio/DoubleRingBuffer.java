package com.harmoneye.audio;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock-free ring buffer.
 * 
 * Two concurrent threads might overwrite each other's data.
 */
public class DoubleRingBuffer {

	private final int bufferSize;
	private double[] buffer;
	private volatile int writeIndex = 0;
	private volatile int readIndex = 0;

	private Lock lock = new ReentrantLock();
	private Condition notEmptyBuffer = lock.newCondition();

	public DoubleRingBuffer(int bufferSize) {
		this.bufferSize = bufferSize;
		this.buffer = new double[bufferSize];
	}

	/**
	 * Appends the given data to the end of the buffer.
	 * 
	 * Older data get overwritten.
	 * 
	 * @param samples
	 */
	public void write(double[] values) {
		// println("wrappedDestCopy(values, 0, buffer, writeIndex="+writeIndex+", values.length="+values.length+")");
		wrappedDestCopy(values, 0, buffer, writeIndex, values.length);
		writeIndex = mod(writeIndex + values.length);
		lock.lock();
		try {
			notEmptyBuffer.signal();
		} finally {
			lock.unlock();
		}
	}

	/** Reads data from the read index. The read index itself is not updated. */
	public double[] read(int length, double[] result) {
		read(length, readIndex, result);
		return result;
	}

	/**
	 * Reads last {@code length} elements appended to the buffer (from the end)
	 * into the provided {@code result} array.
	 * 
	 * @param length can be lower or equal to result.length
	 * @param result
	 */
	public void readLast(int length, double[] result) {
		read(length, mod(writeIndex - length), result);
	}

	private double[] read(int length, int fromIndex, double[] result) {
		// println("wrappedSrcCopy(buffer, readIndex="+fromIndex+", result, 0, length="+length+");");
		wrappedSrcCopy(buffer, fromIndex, result, 0, length);
		return result;
	}

	public void incrementReadIndex(int increment) {
		readIndex = mod(readIndex + increment);
	}

	public int getCapacityForRead() {
		return mod(writeIndex - readIndex);
	}

	public void awaitNotEmpty() throws InterruptedException {
		lock.lock();
		try {
			notEmptyBuffer.await();
		} finally {
			lock.unlock();
		}
	}

	private void wrappedSrcCopy(double[] src, int srcPos, double[] dest,
		int destPos, int length) {
		int firstBlockLength = Math.min(length, src.length - srcPos);
		int secondBlockLength = length - firstBlockLength;
		// println("first: srcPos=" + srcPos + ", destPos=" + destPos +
		// " length=" + firstBlockLength);
		System.arraycopy(src, srcPos, dest, destPos, firstBlockLength);
		if (secondBlockLength > 0) {
			// println("second: srcPos=" + 0 + ", destPos=" + firstBlockLength +
			// ", length=" + secondBlockLength);
			System.arraycopy(src, 0, dest, firstBlockLength, secondBlockLength);
		}
	}

	private void wrappedDestCopy(double[] src, int srcPos, double[] dest,
		int destPos, int length) {
		int firstBlockLength = Math.min(length, dest.length - destPos);
		int secondBlockLength = length - firstBlockLength;
		// println("first: srcPos=" + srcPos + ", destPos=" + destPos +
		// ", length=" + firstBlockLength);
		System.arraycopy(src, srcPos, dest, destPos, firstBlockLength);
		if (secondBlockLength > 0) {
			// println("second: srcPos=" + firstBlockLength + ", destPos=" + 0 +
			// ", length=" + secondBlockLength);
			System.arraycopy(src, firstBlockLength, dest, 0, secondBlockLength);
		}
	}

	private int mod(int value) {
		return ((value % bufferSize) + bufferSize) % bufferSize;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append("size: ").append(bufferSize);
		sb.append(", write: ").append(writeIndex);
		sb.append(", read: ").append(readIndex);
		sb.append(", read capacity: ").append(getCapacityForRead());
		sb.append(", buffer: ").append(Arrays.toString(buffer));
		sb.append("]");
		return sb.toString();
	}

}
