/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.console.telnet;

import java.io.*;
import java.net.Socket;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.framework.console.ConsoleSession;

public class TelnetConsoleSession extends ConsoleSession implements CommandProvider{
	private final Socket s;
	private final ConsoleInputStream in;
	private final ConsoleOutputStream out;
	private final ConsoleInputHandler inputHandler;

	public TelnetConsoleSession(Socket s) throws IOException {
		this.in = new ConsoleInputStream();
		this.out = new ConsoleOutputStream(s.getOutputStream());
		this.out.autoSend();
		this.inputHandler = new ConsoleInputHandler(s.getInputStream(), this.in, this.out);
		this.inputHandler.start();
		this.s = s;
	}

	public synchronized InputStream getInput() {
		return in;
	}

	public synchronized OutputStream getOutput() {
		return out;
	}

	public void doClose() {
		if (s != null)
			try {
				s.close();
			} catch (IOException ioe) {
				// do nothing
			}
		if (out != null)
			try {
				out.close();
			} catch (IOException e) {
				// do nothing
			}
		if (in != null)
			try {
				in.close();
			} catch (IOException ioe) {
				// do nothing
			}
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "ADDED MAN!";
	}
	
	

}
