package org.alopex.scylla.expsocks.org.jdamico.socks.server.impl;

import org.alopex.scylla.expsocks.org.jdamico.socks.server.commons.Constants;
import org.alopex.scylla.expsocks.org.jdamico.socks.server.commons.DebugLog;
import org.alopex.scylla.utils.Utils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServerInitiator implements Runnable {

	protected Object lock;
	protected Thread initThread = null;
	protected ServerSocket serverListenSocket = null;
	protected int port = 0;

	public static ProxySocketHandler mostRecentHandler = null;

	public ProxyServerInitiator(int listenPort) {
		lock = this;
		port = listenPort;
		DebugLog.getInstance().println("SOCKS Server Created.");
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}

	public void start() {
		initThread = new Thread(this);
		initThread.start();
		DebugLog.getInstance().println("SOCKS Server Started.");
	}

	public void stop() {
		DebugLog.getInstance().println("SOCKS Server Stopped.");
		initThread.interrupt();
	}

	public void run() {
		setLock(this);
		listen();
		close();
	}

	public void close() {
		if (serverListenSocket != null) {
			try {
				serverListenSocket.close();
			} catch (IOException e) {}
		}
		serverListenSocket = null;
		DebugLog.getInstance().println("SOCKS Server Closed.");
	}

	public boolean isActive() {
		return (serverListenSocket != null);
	}

	private void prepareToListen() throws BindException, IOException {
		synchronized(lock) {
			serverListenSocket = new ServerSocket(port);
			serverListenSocket.setSoTimeout(Constants.LISTEN_TIMEOUT);

			if (port == 0) {
				port = serverListenSocket.getLocalPort();
			}
			DebugLog.getInstance().println("SOCKS Server Listen at Port : " + port);
		}
	}

	protected void listen() {
		try {
			prepareToListen();
		} catch (java.net.BindException e) {
			DebugLog.getInstance().error("The Port " + port + " is in use !");
			DebugLog.getInstance().error(e);
			return;
		} catch (IOException e) {
			DebugLog.getInstance().error("IO Error Binding at port : " + port);
			return;
		}

		while (isActive()) {
			checkClientConnection();
			Thread.yield();
		}
	}

	public void checkClientConnection() {
		synchronized(lock) {
			//	Close() method was probably called.
			if (serverListenSocket == null) return;

			try {
				Socket clientSocket = serverListenSocket.accept();
				clientSocket.setSoTimeout(Constants.DEFAULT_SERVER_TIMEOUT);
				Utils.log(this, "Connection from : " + DebugLog.getInstance().getSocketInfo(clientSocket), false);
				ProxySocketHandler proxy = new ProxySocketHandler(clientSocket);
				proxy.start();
			} catch (InterruptedIOException e) {
				//	This exception is thrown when accept timeout is expired
			} catch (Exception e) {
				DebugLog.getInstance().error(e);
			}
		} // synchronized
	}
}