package org.alopex.scylla.net.p2p;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import org.alopex.scylla.core.Bootstrapper;
import org.alopex.scylla.crypto.RSA;
import org.alopex.scylla.expsocks.org.jdamico.socks.server.impl.ProxyServerInitiator;
import org.alopex.scylla.expsocks.org.jdamico.socks.server.impl.ProxySocketHandler;
import org.alopex.scylla.net.packets.Data;
import org.alopex.scylla.net.packets.DataTypes;
import org.alopex.scylla.utils.Utils;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class DualListener extends Listener {

	public static ExecutorService replyPool;
	private int direction;

	public DualListener(int direction) {
		super();
		this.direction = direction;
		if (replyPool == null) {
			replyPool = Executors.newCachedThreadPool();
		}
	}

	public void connected(Connection connection) {
		if (direction == 1) {
			Utils.log(this, "New incoming peer: " + connection.getRemoteAddressTCP().getHostString(), false);
		} else {
			Utils.log(this, "New outgoing peer: " + connection.getRemoteAddressTCP().getHostString(), false);
		}
		connection.setIdleThreshold(0.3f);
		try {
			if (direction == 1) {
				new Peer(connection, direction);
			} else {
				new Peer(connection, direction);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void received(final Connection connection, Object object) {
		final Peer foundPeer = Peer.findPeer(connection);
		if (object instanceof Data) {
			final Data dataObject = (Data) object;
			final byte type = dataObject.getType();

			if ((int) ((float) type % 2) == 0) {
				Utils.log(this, "DATA RECV from remote peer: CID @ " + foundPeer.getConnection().getID(), false);
				switch (type) {
					case DataTypes.PUBKEY_DATA:
						Utils.log(this, "RECV DATA for PUBKEY_DATA", false);
						replyPool.execute(new Runnable() {
							public void run() {
								String recvPubKey = (String) dataObject.getPayload();
								if (foundPeer.setPubKey(recvPubKey)) {
									foundPeer.getPubKeyRecvLatch().countDown();
								}
							}
						});
						break;

					case DataTypes.UUID_DATA:
						Utils.log(this, "RECV DATA for UUID_DATA", false);
						replyPool.execute(new Runnable() {
							public void run() {
								String encryptedUUID = (String) dataObject.getPayload();
								try {
									String uuid = Bootstrapper.rsa.decrypt(encryptedUUID);
									// Update foundPeer
									Peer bridgeFoundPeer = foundPeer;
									int attemptsToFindPeer = 0;
									while (bridgeFoundPeer == null && attemptsToFindPeer <= 5) {
										bridgeFoundPeer = Peer.findPeer(connection);
										Thread.sleep(100);
										attemptsToFindPeer++;
									}
									bridgeFoundPeer.setUuid(uuid);
									bridgeFoundPeer.getUuidRecvLatch().countDown();
								} catch (Exception ex) {
									Utils.log(this, "Failed to set UUID", false);
									ex.printStackTrace();
								}
							}
						});
						break;

					case DataTypes.ARTICHOKE_DATA:
						Utils.log(this, "RECV DATA for ARTICHOKE_DATA", false);
						replyPool.execute(new Runnable() {
							public void run() {
								try {
									byte[] preByteBuffer = (byte[]) dataObject.getPayload();
									Utils.log(this, "Parsed preByteBuffer data", false);
									// Inject byte buffer to client
									ProxyServerInitiator.mostRecentHandler.sendToClient(preByteBuffer);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						});
						break;

				}
			} else {
				Utils.log(this, "REQ RECV from remote peer: " + foundPeer.getUuid(), false);

				switch (type) {
					case DataTypes.PUBKEY_REQS:
						Utils.log(this, "RECV REQ for PUBKEY_DATA", false);
						replyPool.execute(new Runnable() {
							public void run() {
								connection.sendTCP(new Data(DataTypes.PUBKEY_DATA, RSA.pubKey));
								Utils.log(this, "\tSent pubKey back", false);
							}
						});
						break;

					case DataTypes.UUID_REQS:
						Utils.log(this, "RECV REQ for UUID_DATA", false);
						replyPool.execute(new Runnable() {
							public void run() {
								connection.sendTCP(new Data(DataTypes.UUID_DATA, Bootstrapper.rsa.encrypt(Bootstrapper.selfUUID, foundPeer.getPubKey())));
								Utils.log(this, "\tSent UUID back", false);
							}
						});
						break;

					case DataTypes.ARTICHOKE_REQS:
						Utils.log(this, "RECV REQ for ARTICHOKE_DATA", false);
						replyPool.execute(new Runnable() {
							public void run() {
								try {
									SOCKSRoute socksRoute = (SOCKSRoute) dataObject.getPayload();
									ProxySocketHandler psh = new ProxySocketHandler(null);
                                    System.out.println("HOST: " + socksRoute.getDestinationIP());
									psh.connectToServer(socksRoute.getDestinationIP(), socksRoute.getDestinationPort());
									psh.exitNode = true;
									psh.sendToServer(socksRoute.getSendBuffer(), socksRoute.getSendBuffer().length, false);
                                    psh.relay();
									// Find a SocksClient to hijack
									//SocksClient socksClient = SOCKSProxy.getSocksClient(null);
									//socksClient.setPeer(foundPeer);
									//socksClient.newOutboundData(SOCKSProxy.select, socksRoute.getDestinationIP(), socksRoute.getDestinationPort(), socksRoute.getSendBuffer());

								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						});
						break;
				}
			}
		}
	}

	public void disconnected(Connection connection) {
		Peer foundPeer = Peer.findPeer(connection);
		if (foundPeer != null) {
			foundPeer.disconnect();
		}
	}
}
