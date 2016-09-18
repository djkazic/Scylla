package org.alopex.scylla.net.socks;

import org.alopex.scylla.net.p2p.Peer;
import org.alopex.scylla.utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SocksClient {
	SocketChannel clientSocketChannel;
	SocketChannel remoteSocketChannel;
	boolean connected;
	long lastData = 0;
	Peer peer;

	public SocksClient(SocketChannel cs) throws IOException {
		clientSocketChannel = cs;
		clientSocketChannel.configureBlocking(false);
		peer = null;
		lastData = System.currentTimeMillis();
	}

	public void newInboundData() throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(1024);
		if (remoteSocketChannel.read(buf) == -1) {
			throw new IOException("Reached EOF / read timeout");
		}
		lastData = System.currentTimeMillis();
		buf.flip();

		//TODO: change from overBuf to Peer based
		if (peer != null) {
			//TODO: exit node implementation calls this modified method
			Utils.log(this, "HOOK FOR PACKAGING ORGANIC RESPONSE TO PEER: " + peer, false);
			//System.out.println("newInboundData running in [EXIT] mode");
			//clientSocketChannel.write(overBuf);
		} else {
			// Simple local testing call
			//System.out.println("newInboundData running in [LOCAL] mode");
			clientSocketChannel.write(buf);
			//System.out.println(DatatypeConverter.printHexBinary((buf.array())));
		}
	}

	public void newOutboundData(Selector selector, SelectionKey sk, String overAddr, int overPort, ByteBuffer overBuf) throws Exception {
		if (!connected) {
			// Allocate initial buffer
			ByteBuffer inbuf = ByteBuffer.allocate(512);

			// Check to see if local channel has data -- if so, write it to the intial buffer
			if (clientSocketChannel != null && clientSocketChannel.read(inbuf) < 1) {
				return;
			}

			System.out.println("\nWriting data out to new pipe");
			if (overAddr == null || overBuf == null) {
				/** Read SOCKS message and send back verification */

				// Flip the buffer for decoding
				inbuf.flip();

				// Read socks header
				System.out.println("Checking SOCKS header");
				int ver = inbuf.get();
				int cmd = inbuf.get();

				if (ver != 4 || cmd != 1) {
					throw new Exception("Incompatible SOCKS version [" + ver + ", " + cmd + "]");
				}

				final int port = inbuf.getShort();
				final byte ip[] = new byte[4];
				inbuf.get(ip);

				// Skip the rest of the packets
				while ((inbuf.get()) != 0) ;

				// Remote address / hostname handling
				InetAddress remoteAddr = InetAddress.getByAddress(ip);
				if (ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] != 0) {
					String host = "";
					byte b;
					while ((b = inbuf.get()) != 0) {
						host += b;
					}
					remoteAddr = InetAddress.getByName(host);
				}

				//TODO: instead of opening a connection to remoteSocketChannel directly, have this handled by a client
				System.out.println("Opening remoteSocketChannel @ " + remoteAddr + ":" + port);
				remoteSocketChannel = SocketChannel.open(new InetSocketAddress(remoteAddr, port));

				//TODO: move away from exceptions
				if (!remoteSocketChannel.isConnected())
					throw new IOException("connect failed");

				ByteBuffer out = ByteBuffer.allocate(8);

				System.out.println("Formatting response status packets");
				out.put((byte) 0);
				out.put((byte) (remoteSocketChannel.isConnected() ? 0x5a : 0x5b));
				for (int i=0; i < 6; i++) {
					out.put((byte) 0);
				}
				out.flip();

				System.out.println("Writing response status packets to clientSocketChannel, len = " + out.array().length);
				clientSocketChannel.write(out);
			} else {
				System.out.println("Opening override remoteSocketChannel @ " + overAddr + ":" + overPort);
				remoteSocketChannel = SocketChannel.open(new InetSocketAddress(overAddr, overPort));

				//TODO: move away from exceptions
				if (!remoteSocketChannel.isConnected())
					throw new IOException("connect failed");

				if (overBuf != null) {
					System.out.println("newOutBound data running in [EXIT] mode");
					//TODO: intercept buf, have it sent to a client elsewhere
					remoteSocketChannel.write(overBuf);
				}
			}

			remoteSocketChannel.configureBlocking(false);
			remoteSocketChannel.register(selector, SelectionKey.OP_READ);

			connected = true;
		} else {
			if (overBuf == null) {
				ByteBuffer buf = ByteBuffer.allocate(1024);
				if (clientSocketChannel.read(buf) == -1)
					throw new IOException("Client disconnected");
				lastData = System.currentTimeMillis();
				buf.flip();
				//Utils.log(this, "HOOKED FOR OUTBOUND DATA: " + Arrays.toString(buf.array()), false);
				remoteSocketChannel.write(buf);
			} else {
				System.out.println("Invalid override call for newOutboundData()");
			}
			// Write 1024 byte block to remoteSocketChannel
		}
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}
}
