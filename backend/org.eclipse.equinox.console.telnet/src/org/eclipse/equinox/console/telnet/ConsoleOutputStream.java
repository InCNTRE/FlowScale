package org.eclipse.equinox.console.telnet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class wraps the socket output stream of the telnet console. It is responsible for
 * buffering and flushing the characters to the socket output stream.
 * 
 *
 */

public class ConsoleOutputStream extends OutputStream {

	/**
	   * A size of the used buffer.
	   */
	public static final int BUFFER_SIZE = 2048;
	public static final byte[] autoMessage = new byte[] {(byte) 255, (byte) 251, (byte) 1, // IAC WILL ECHO
			(byte) 255, (byte) 251, (byte) 3, // IAC WILL SUPRESS GO_AHEAD
			(byte) 255, (byte) 253, (byte) 31};
	public final static byte CR = (byte) '\r';
	public final static byte LF = (byte) '\n';

	OutputStream out;

	private boolean isEcho = true;
	private boolean queueing = false;
	private byte prevByte;
	private byte[] buffer;
	private int pos;

	/**
	 * Initiates with instance of the output stream to which it will send data. Here it writes to
	 * a socket output stream.
	 *
	 * 
	 */
	public ConsoleOutputStream(OutputStream out) {
		this.out = out;
		buffer = new byte[BUFFER_SIZE];
		pos = 0;
	}

	/**
	 * Sends the options which a server wants to negotiate with a telnet client.
	 *
	 */
	public synchronized void autoSend() throws IOException {
		write(autoMessage);
	}

	/**
	 * An implementation of the corresponding abstract method in OutputStream.
	 *
	 * @param   i
	 * @exception   IOException
	 */
	public synchronized void write(int i) throws IOException {
		if (isEcho) {
			if (!queueing) {
				if (i == '\r' || i == '\0') {
					queueing = true;
					prevByte = (byte) i;
				} else if (i == '\n') {
					add(CR);
					add(LF);
				} else {
					add(i);
				}
			} else { // awaiting '\n' AFTER '\r', and '\0' AFTER '\b'
				if (prevByte == '\r' && i == '\n') {
					add(CR);
					add(LF);
				} else if (prevByte == '\0' && i == '\b') {
					isEcho = !isEcho;
				} else {
					add(CR);
					add(LF);
					add(i);
				}

				queueing = false;
				flush();
			}
		}
	}

	/**
	 * Empties the buffer and sends data to the socket output stream.
	 *
	 * @exception   IOException
	 */
	public synchronized void flush() throws IOException {
		if (pos > 0) {
			out.write(buffer, 0, pos);
			pos = 0;
		}
	}

	/**
	 * Adds a variable of type integer to the buffer.
	 *
	 * @param   i
	 */
	private void add(int i) throws IOException {
		buffer[pos] = (byte) i;
		pos++;

		if (pos == buffer.length) {
			flush();
		}
	}

	/**
	 * Closes this OutputStream.
	 *
	 * @exception   IOException
	 */
	public void close() throws IOException {
		out.close();
	}
}
