package org.eclipse.equinox.console.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * This class serves as an input stream, which wraps the actual input from the telnet and buffers the lines. 
 * 
 *
 */
public class ConsoleInputStream extends InputStream {
	//	private final ArrayList<byte[]> buffer = new ArrayList<byte[]>();
	private final ArrayList buffer = new ArrayList();
	private byte[] current;
	private int pos;
	private boolean isClosed;

	public synchronized int read() {
		while (current == null && buffer.isEmpty() && !isClosed) {
			try {
				wait();
			} catch (InterruptedException e) {
				return -1;
			}
		}
		if (isClosed) {
			return -1;
		}
		if (current == null) {
			current = (byte[]) buffer.remove(0);
			return current[pos++] & 0xFF;
		} else {
			try {
				return current[pos++] & 0xFF;
			} finally {
				if (pos == current.length) {
					current = null;
					pos = 0;
				}
			}
		}
	}

	public int read(byte b[], int off, int len) throws IOException {
		if (len == 0) {
			return len;
		}
		int i = read();
		if (i == -1) {
			return -1;
		}
		b[off] = (byte) i;
		return 1;
	}

	public synchronized void close() throws IOException {
		isClosed = true;
		notifyAll();
	}

	public synchronized void add(byte[] data) {
		if (data.length > 0) {
			notify();
			buffer.add(data);
		}
	}

}
