package org.eclipse.equinox.console.telnet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.framework.console.ConsoleSession;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator ,CommandProvider{
	private ConsoleSocketGetter csg;
	private short socketPort;
	static BundleContext  context;
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	
	public void setSocketPort(short socketPort){
		this.socketPort = socketPort;
		System.out.print("socket port set");
	}
	
	
	public void start(BundleContext context) throws Exception {
	
	
	this.context = context;
	
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		csg.shutdown();
	}
	

	public void startUp() throws Exception {
		
		csg = new ConsoleSocketGetter(new ServerSocket(socketPort,0, InetAddress.getByName("localhost")), context);
		context.registerService(CommandProvider.class.getName(), this, null);

		
	}
	
	public void shutDown(){
		
		
		
		
	}

	/**
	 * ConsoleSocketGetter - provides a Thread that listens on the port
	 * for FrameworkConsole.
	 */
	class ConsoleSocketGetter implements Runnable {

		/** The ServerSocket to accept connections from */
		private final ServerSocket server;
		private final BundleContext context;
		private volatile boolean shutdown = false;

		/**
		 * Constructor - sets the server and starts the thread to
		 * listen for connections.
		 *
		 * @param server a ServerSocket to accept connections from
		 */
		ConsoleSocketGetter(ServerSocket server, BundleContext context) {
			this.server = server;
			this.context = context;
			try {
				Method reuseAddress = server.getClass().getMethod("setReuseAddress", new Class[] {boolean.class}); //$NON-NLS-1$
				reuseAddress.invoke(server, new Object[] {Boolean.TRUE});
			} catch (Exception ex) {
				// try to set the socket re-use property, it isn't a problem if it can't be set
			}
			Thread t = new Thread(this, "ConsoleSocketGetter"); //$NON-NLS-1$
			t.setDaemon(true);
			t.start();
		}

		public void run() {
			// Print message containing port console actually bound to..
			System.out.println("Listening on port: " + Integer.toString(server.getLocalPort()));
			while (!shutdown) {
				try {
					Socket socket = server.accept();
					
					if (socket == null)
						throw new IOException("No socket available.  Probably caused by a shutdown."); //$NON-NLS-1$
					TelnetConsoleSession session = new TelnetConsoleSession(socket);
					context.registerService(ConsoleSession.class.getName(), session, null);
				} catch (Exception e) {
					if (!shutdown)
						e.printStackTrace();
				}

			}
		}
		
	

		public void shutdown() {
			if (shutdown)
				return;
			shutdown = true;
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getHelp() {
		// TODO Auto-generated method stub
		return "TEST";
	}
	
	public void  _okw(CommandInterpreter ci){
		ci.print( "this is the main unit\n");
		String s ="";
		ci.print("The arguments are : ");
		while ((s = ci.nextArgument()) != null){
			
			ci.print(s);
		}
		
		
	}
	
	
	
	public void _ssa(CommandInterpreter ci){
		ci.print("I overrode ss and disabled it ");
	}
	
	public String getName(){
		
		return "commandInterface";
	}

}
