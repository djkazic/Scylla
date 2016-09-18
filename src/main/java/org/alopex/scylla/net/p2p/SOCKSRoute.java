package org.alopex.scylla.net.p2p;

import java.nio.channels.SocketChannel;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class SOCKSRoute {

	//private SocketChannel clientSocketChannel;
	private String destinationIP;
	private int destinationPort;
	byte[] sendBuffer;

	public SOCKSRoute() {}

	public SOCKSRoute(String destinationIP, int destinationPort, byte[] sendBuffer) {
		//this.clientSocketChannel = clientSocketChannel;
		this.destinationIP = destinationIP;
		this.destinationPort = destinationPort;
		this.sendBuffer = sendBuffer;
	}

	//public SocketChannel getClientSocketChannel() {
		//return clientSocketChannel;
	//}

	public String getDestinationIP() {
		return destinationIP;
	}

	public int getDestinationPort() {
		return destinationPort;
	}

	public byte[] getSendBuffer() {
		return sendBuffer;
	}
}
