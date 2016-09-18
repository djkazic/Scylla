package org.alopex.scylla.net.socks;

import org.alopex.scylla.utils.Utils;

import java.nio.channels.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class SOCKSProxy {
	public static ArrayList <SocksClient> clients = new ArrayList<SocksClient>();
	public static Selector select;

	public static void main(String[] args) {
		SOCKSProxy sp = new SOCKSProxy();
		sp.init();
	}

	private ServerSocketChannel socks;

	public void init() {
		try {
			socks = ServerSocketChannel.open();
			socks.socket().bind(new InetSocketAddress(8888));
			socks.configureBlocking(false);
			select = Selector.open();
			final Selector threadSelect = select;
			socks.register(threadSelect, SelectionKey.OP_ACCEPT);

			Utils.log(this, "Starting proxy thread...", false);
			(new Thread(new Runnable() {
				public void run() {
					Utils.log(this, "Proxy thread started.", false);
					while(true) {
						try {
							threadSelect.select(1000);

							Set keys = threadSelect.selectedKeys();
							Iterator iterator = keys.iterator();
							while (iterator.hasNext()) {
								SelectionKey iterSelectionKey = (SelectionKey) iterator.next();

								if (!iterSelectionKey.isValid()) {
									//System.out.println("SelectionKey invalid, skipping");
									continue;
								}

								if (iterSelectionKey.isAcceptable() && iterSelectionKey.channel() == socks) {
									// Attach to new viable socket
									SocketChannel acceptSocket = socks.accept();
									if (acceptSocket == null)
										continue;

									// Register for read events
									acceptSocket.configureBlocking(false);
									acceptSocket.register(threadSelect, SelectionKey.OP_READ);

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
												thisClient.newOutboundData(threadSelect, null, 0, null);
											} else if (iterSelectionKey.channel() == thisClient.remoteSocketChannel
													   || thisClient.remoteSocketChannel.isConnected()) {
												thisClient.newInboundData(null);
											}
											//Utils.log(this, "DEBUG REMOTESC: " + thisClient.remoteSocketChannel.toString(), false);
										} catch (Exception e) {
											//TODO: objectify SocksClient MORE
											thisClient.clientSocketChannel.close();
											if (thisClient.remoteSocketChannel != null)
												thisClient.remoteSocketChannel.close();
											iterSelectionKey.cancel();
											Utils.log(this, "Removing client " + clients.indexOf(thisClient), false);
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
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			})).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Populates pool organically
	public SocksClient addClient(SocketChannel localSocketChannel) {
		SocksClient cl;
		try {
			//TODO: work on Peer assigning logic once we know that there is a purpose built SocksClient
			// Organically generated SocksClient
			Utils.log(this, "Organically generating SocksClient", false);
			cl = new SocksClient(localSocketChannel);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		clients.add(cl);
		return cl;
	}

	public static SocksClient getSocksClient() {
		/**
		 * for (int i=0; i < clients.size(); i++) {
		 if (clients.get(i).getPeer() == null) {
		 return clients.get(i);
		 }
		 }
		 */
		SocksClient sc = new SocksClient(null);
		clients.add(sc);
		return sc;
	}
}
