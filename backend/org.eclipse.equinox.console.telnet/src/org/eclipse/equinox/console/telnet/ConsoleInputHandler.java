package org.eclipse.equinox.console.telnet;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class performs the actual reading from the socket input stream, then forwarding to the input scanner,
 * which processes the input, handling the command characters. 
 * 
 *
 */
public class ConsoleInputHandler extends Thread {
	private ConsoleInputScanner inputScanner;
	private ConsoleOutputStream out;
	private ConsoleInputStream in;
	private InputStream input;
	private byte[] buffer;
	private static final int MAX_SIZE = 2048;

	public ConsoleInputHandler(InputStream input, ConsoleInputStream in, ConsoleOutputStream out) {
		this.input = input;
		this.in = in;
		this.out = out;

		inputScanner = new ConsoleInputScanner(in, out);
		buffer = new byte[MAX_SIZE];
	}

	public void run() {
		int count;
		try {
			while ((count = input.read(buffer)) > -1) {
				for (int i = 0; i < count; i++) {
					inputScanner.scan(buffer[i]);
				}
			}
			in.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
