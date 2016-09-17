package org.alopex.scylla.net.socks;

import java.nio.channels.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class SOCKSProxy {
	public static ArrayList <SocksClient> clients = new ArrayList<SocksClient>();

	public void init() {
		try {
			ServerSocketChannel socks = ServerSocketChannel.open();
			socks.socket().bind(new InetSocketAddress(8888));
			socks.configureBlocking(false);
			Selector select = Selector.open();
			socks.register(select, SelectionKey.OP_ACCEPT);

			while(true) {
				select.select(1000);

				Set keys = select.selectedKeys();
				Iterator iterator = keys.iterator();
				while (iterator.hasNext()) {
					SelectionKey iterSelectionKey = (SelectionKey) iterator.next();

					if (!iterSelectionKey.isValid()) {
						System.out.println("SelectionKey invalid, skipping");
						continue;
					}

					if (iterSelectionKey.isAcceptable() && iterSelectionKey.channel() == socks) {
						// Attach to new viable socket
						SocketChannel acceptSocket = socks.accept();
						if (acceptSocket == null)
							continue;

						// Register for read events
						acceptSocket.configureBlocking(false);
						acceptSocket.register(select, SelectionKey.OP_READ);

						addClient(acceptSocket);
					} else if (iterSelectionKey.isReadable()) {
						// Is a known socket we've registered for read events
						for (int i = 0; i < clients.size(); i++) {
							SocksClient thisClient = clients.get(i);
							try {
								// Find SocksClient that matches
								// Execute operations for either incoming / outgoing data
								if (iterSelectionKey.channel() == thisClient.clientSocketChannel) {
									//TODO: implement outbound EXIT node call of this method
									thisClient.newOutboundData(select, iterSelectionKey, null, 0, null);
								} else if (iterSelectionKey.channel() == thisClient.remoteSocketChannel) {
									thisClient.newInboundData(select, iterSelectionKey, null);
								}
							} catch (Exception e) {
								//TODO: objectify SocksClient MORE
								thisClient.clientSocketChannel.close();
								if (thisClient.remoteSocketChannel != null)
									thisClient.remoteSocketChannel.close();
								iterSelectionKey.cancel();
								clients.remove(thisClient);
							}
						}
					}
				}

				// clientSocketChannel timeout check
				for (int i = 0; i < clients.size(); i++) {
					SocksClient cl = clients.get(i);
					if ((System.currentTimeMillis() - cl.lastData) > 30000L) {
						//TODO: objectify SocksClient MORE
						cl.clientSocketChannel.close();
						if (cl.remoteSocketChannel != null)
							cl.remoteSocketChannel.close();
						clients.remove(cl);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public SocksClient addClient(SocketChannel s) {
		SocksClient cl;
		try {
			cl = new SocksClient(s);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		clients.add(cl);
		return cl;
	}
}
