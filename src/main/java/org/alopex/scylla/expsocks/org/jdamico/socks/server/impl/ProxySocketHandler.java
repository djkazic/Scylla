package org.alopex.scylla.expsocks.org.jdamico.socks.server.impl;

import org.alopex.scylla.core.Bootstrapper;
import org.alopex.scylla.expsocks.org.jdamico.socks.server.commons.Constants;
import org.alopex.scylla.expsocks.org.jdamico.socks.server.commons.DebugLog;
import org.alopex.scylla.net.p2p.DualListener;
import org.alopex.scylla.net.p2p.SOCKSRoute;
import org.alopex.scylla.net.packets.Data;
import org.alopex.scylla.net.packets.DataTypes;
import org.alopex.scylla.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ProxySocketHandler implements Runnable {
	public static int proxyHandlerCount = 0;

	protected Object lock;

	protected Thread proxyThread = null;

	public Socket clientSocket = null;
	public Socket serverSocket = null;

	public byte[] buffer = null;

	public InputStream clientInputStream = null;
	public OutputStream clientOutputStream = null;
	public InputStream serverInputStream = null;
	public OutputStream serverOutputStream = null;

	public ProxySocketHandler(Socket clientSocket) {
		lock = this;
		this.clientSocket = clientSocket;
		if (this.clientSocket != null) {
			try {
				this.clientSocket.setSoTimeout(Constants.DEFAULT_PROXY_TIMEOUT);
			} catch (SocketException e) {
				DebugLog.getInstance().error("Socket Exception during seting Timeout.");
			}
		}
		buffer = new byte[Constants.DEFAULT_BUF_SIZE];
		DebugLog.getInstance().println("Proxy Created.");
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}

	public void start() {
		proxyThread = new Thread(this);
		proxyThread.start();
		proxyHandlerCount++;
		Utils.log(this, "Proxy started: " + proxyHandlerCount, false);
	}

	public void run() {
		setLock(this);

		if (!prepareClient()) {
			DebugLog.getInstance().error("Proxy - client socket is null !");
			return;
		}

		socksHandshake();
	}

	public void close() {
		try {
			if (clientOutputStream != null) {
				clientOutputStream.flush();
				clientOutputStream.close();
			}
		} catch (IOException e) {}
		try {
			if (serverOutputStream != null) {
				serverOutputStream.flush();
				serverOutputStream.close();
			}
		} catch (IOException e) {}

		try {
			if (clientSocket != null) {
				clientSocket.close();
			}
		} catch (IOException e) {}

		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {}

		serverSocket = null;
		clientSocket = null;

		Utils.log(this, "PROXY CLOSED", false);
	}

	public void sendToClient(byte[] buffer) {
		sendToClient(buffer, buffer.length);
	}

	public void sendToClient(byte[] buffer, int len) {
		if (clientOutputStream == null) return;
		if (len <= 0 || len > buffer.length) return;

		try {
			clientOutputStream.write(buffer, 0, len);
			clientOutputStream.flush();
		} catch (IOException e) {
			DebugLog.getInstance().error("Sending data to client");
		}
	}

	public void sendToServer(byte[] buffer, int len) {
		if (serverOutputStream == null) return;
		if (len <= 0 || len > buffer.length) return;

		try {
			Utils.log(this, "Intercepted data for serverOutputStream!", false);
			final SOCKSRoute socksRoute = new SOCKSRoute(serverSocket.getInetAddress().toString(), serverSocket.getPort(), buffer);
			DualListener.replyPool.execute(new Runnable() {
				public void run() {
					Bootstrapper.peers.get(0).getConnection().sendTCP(new Data(DataTypes.ARTICHOKE_REQS, socksRoute));
				}
			});

			//serverOutputStream.write(buffer, 0, len);
			//serverOutputStream.flush();
		} catch (Exception e) {
			DebugLog.getInstance().error("Sending data to server");
		}
	}

	public boolean isActive() {
		return (clientSocket != null && serverSocket != null);
	}

	public void connectToServer(String server, int port) throws IOException {
		if (server.equals("")) {
			close();
			Utils.log(this, "Invalid Remote Host Name - Empty String !!!", false);
			return;
		}

		serverSocket = new Socket(server, port);
		serverSocket.setSoTimeout(Constants.DEFAULT_PROXY_TIMEOUT);

		DebugLog.getInstance().println("Connected to " + DebugLog.getInstance().getSocketInfo(serverSocket));
		prepareServer();
	}

	protected void prepareServer() throws IOException {
		synchronized(lock) {
			serverInputStream = serverSocket.getInputStream();
			serverOutputStream = serverSocket.getOutputStream();
		}
	}

	public boolean prepareClient() {
		if (clientSocket == null) return false;

		try {
			clientInputStream = clientSocket.getInputStream();
			clientOutputStream = clientSocket.getOutputStream();
		} catch (IOException e) {
			DebugLog.getInstance().error("Proxy - can't get I/O streams!");
			DebugLog.getInstance().error(e);
			return false;
		}
		return true;
	}

	Socks4Impl comm = null;

	public void socksHandshake() {
		try {
			byte socksVersion = getByteFromClient();

			switch (socksVersion) {
				case Constants.SOCKS4_Version:
					comm = new Socks4Impl(this);
					break;
				case Constants.SOCKS5_Version:
					comm = new Socks5Impl(this);
					break;
				default:
					DebugLog.getInstance().error("Invalid SOKCS version : " + socksVersion);
					return;
			}

			Utils.log(this, "\tAccepted SOCKS " + socksVersion + " request.", false);

			comm.authenticate(socksVersion);
			comm.getClientCommand();

			switch (comm.socksCommand) {
				case Constants.SC_CONNECT:
					comm.connect();
					relay();
					break;

				case Constants.SC_BIND:
					comm.bind();
					relay();
					break;

				case Constants.SC_UDP:
					comm.udp();
					break;
			}
		} catch (Exception e) {
			DebugLog.getInstance().error(e);
		}
	}

	public byte getByteFromClient() throws Exception {
		int b;
		while (clientSocket != null) {

			try {
				b = clientInputStream.read();
			} catch (InterruptedIOException e) {
				Thread.yield();
				continue;
			}

			return (byte) b; // return loaded byte

		} // while...
		throw new Exception("Interrupted Reading GetByteFromClient()");
	}

	public void relay() {
		boolean isActive = true;
		int dlen = 0;

		while (isActive) {
			//---> Check for client data <---
			dlen = checkClientData(null);

			if (dlen < 0) isActive = false;
			if (dlen > 0) {
				logClientData(dlen);
				sendToServer(buffer, dlen);
			}

			//---> Check for Server data <---
			dlen = checkServerData(null);

			if (dlen < 0) isActive = false;
			if (dlen > 0) {
				logServerData(dlen);
				sendToClient(buffer, dlen);
			}

			Thread.currentThread();
			Thread.yield();
		} // while
	}

	public int checkClientData(byte[] overBuf) {
		synchronized(lock) {
			if (overBuf == null) {
				//	The client side is not opened.
				if (clientInputStream == null) return -1;

				int dlen = 0;

				try {
					dlen = clientInputStream.read(buffer, 0, Constants.DEFAULT_BUF_SIZE);
				} catch (InterruptedIOException e) {
					return 0;
				} catch (IOException e) {
					Utils.log(this, "Client connection Closed!", false);
					close(); //	Close the server on this exception
					return -1;
				}

				if (dlen < 0) {
					Utils.log(this, "Nothing to read!", false);
					close();
				}
				return dlen;
			} else {
				buffer = overBuf;
				sendToServer(buffer, buffer.length);
				return 0;
			}
		}
	}

	public int checkServerData(byte[] overBuf) {
		synchronized(lock) {
			if (overBuf == null) {
				//	The client side is not opened.
				if (serverInputStream == null) return -1;
				int dlen = 0;

				try {
					dlen = serverInputStream.read(buffer, 0, Constants.DEFAULT_BUF_SIZE);
				} catch (InterruptedIOException e) {
					return 0;
				} catch (IOException e) {
					return -1;
				}

				if (dlen < 0) close();
				return dlen;
			} else {
				buffer = overBuf;
				sendToClient(buffer, buffer.length);
				return 0;
			}
		}
	}

	public void logServerData(int traffic) {
		DebugLog.getInstance().println("Srv data : " +
				DebugLog.getInstance().getSocketInfo(clientSocket) +
				" << <" +
				comm.m_ServerIP.getHostName() + "/" +
				comm.m_ServerIP.getHostAddress() + ":" +
				comm.m_nServerPort + "> : " +
				traffic + " bytes.");
	}

	public void logClientData(int traffic) {
		DebugLog.getInstance().println("Cli data : " +
				DebugLog.getInstance().getSocketInfo(clientSocket) +
				" >> <" +
				comm.m_ServerIP.getHostName() + "/" +
				comm.m_ServerIP.getHostAddress() + ":" +
				comm.m_nServerPort + "> : " +
				traffic + " bytes.");
	}

	public Socket getSocksServer() {
		return serverSocket;
	}
}