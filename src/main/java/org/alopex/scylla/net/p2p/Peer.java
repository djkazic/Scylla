package org.alopex.scylla.net.p2p;

import com.esotericsoftware.kryonet.Connection;
import org.alopex.scylla.core.Bootstrapper;
import org.alopex.scylla.core.Chat;
import org.alopex.scylla.crypto.AES;
import org.alopex.scylla.crypto.RSA;
import org.alopex.scylla.net.packets.Data;
import org.alopex.scylla.net.packets.DataTypes;
import org.alopex.scylla.utils.Utils;

import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class Peer {

	private Connection connection;
	private String uuid;
	private int direction;

	private CountDownLatch pubKeyRecvLatch;
	private CountDownLatch uuidRecvLatch;

	private PublicKey pubKey;
	private AES aes;
	private Chat chat;

	public Peer(Connection connection, int direction) {
		this.connection = connection;
		this.direction = direction;

		pubKeyRecvLatch = new CountDownLatch(1);
		uuidRecvLatch = new CountDownLatch(1);

		addToPeerList();
		bootstrapHandshake();
	}

	private void addToPeerList() {
		Bootstrapper.peers.add(this);
	}

	private void bootstrapHandshake() {
		Runnable bootstrapHandshakeThreadRunnable = new Runnable() {
			public void run() {
				try {
					if (direction == 1) {
						Utils.log(this, "Sending our pubkey first...", false);
						connection.sendTCP(new Data(DataTypes.PUBKEY_DATA, RSA.pubKey));

						Utils.log(this, "Requesting remote peer's pubkey...", false);
						connection.sendTCP(new Data(DataTypes.PUBKEY_REQS, null));

						Utils.log(this, "Waiting for remote peer's pubkey...", false);
						pubKeyRecvLatch.await();

						Thread.sleep(150);
						Utils.log(this, "Requesting remote peer's UUID...", false);
						connection.sendTCP(new Data(DataTypes.UUID_REQS, null));

						Utils.log(this, "Waiting for remote peer's UUID...", false);
						uuidRecvLatch.await();

						Utils.log(this, "Initializing local AES...", false);
						aes = new AES(uuid);
					} else {
						Utils.log(this, "Waiting for remote peer's pubkey...", false);
						pubKeyRecvLatch.await();

						Thread.sleep(150);
						Utils.log(this, "Requesting remote peer's UUID...", false);
						connection.sendTCP(new Data(DataTypes.UUID_REQS, null));

						Utils.log(this, "Waiting for remote peer's UUID...", false);
						uuidRecvLatch.await();

						Utils.log(this, "Initializing local AES...", false);
						aes = new AES(uuid);
					}
					Utils.log(this, "Handshake complete!\n", false);
					Utils.log(this, "Checking UUID...", false);
					if (uuidCheck()) {
						Utils.log(this, "Ready for data Tx/Rx.", false);
						chat = new Chat(connection);
						Utils.log(this, "Booted up chat connection", false);
						Utils.log(this, "Initializing...\n\n", false);
						System.out.println();
						System.out.println("                                      /|\n" +
								"                                     |\\|\n" +
								"                                     |||\n" +
								"                                     |||\n" +
								"                                     |||\n" +
								"      Remember                       |||\n" +
								"      The Sword of                   |||\n" +
								"      Damocles                       |||\n" +
								"                                  ~-[{o}]-~\n" +
								"                                     |/|\n" +
								"                                     |/|\n" +
								"                                     `0'");
						System.out.print("\n$>>> ");
						chat.init();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		Thread bootstrapHandshakeThread = new Thread(bootstrapHandshakeThreadRunnable);
		bootstrapHandshakeThread.start();
	}

	public boolean uuidCheck() {
		if (uuid.equals(Bootstrapper.selfUUID)) {
			Utils.log(this, "Duplicate UUID against self: " + uuid, false);
			disconnect();
			return false;
		} else {
			boolean passed = true;
			for (Peer peer : Bootstrapper.peers) {
				if (peer != this && peer.getUuid() != null && peer.getUuid().equals(uuid)) {
					Utils.log(this, "Duplicate UUID: " + uuid, false);
					passed = false;
					break;
				}
			}

			if (passed) {
				Utils.log(this, "UUID check passed!", false);
				return true;
			} else {
				disconnect();
				return false;
			}
		}
	}

	public void disconnect() {
		Bootstrapper.peers.remove(this);
		chat = null;
		int connNumber = connection.getID();
		connection.close();
		if (uuid != null) {
			Utils.log(this, "Peer " + uuid + " disconnected", false);
		} else {
			Utils.log(this, "Peer disconnected (uuid null for connection [" + connNumber + "])", false);
		}
	}

	public boolean setPubKey(String pubkey) {
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(pubkey));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(keySpec);
			this.pubKey = pk;
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public Connection getConnection() {
		return connection;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getDirection() {
		return direction;
	}

	public PublicKey getPubKey() {
		return pubKey;
	}

	public AES getAES() {
		return aes;
	}

	public Chat getChat() {
		return chat;
	}

	public CountDownLatch getPubKeyRecvLatch() {
		return pubKeyRecvLatch;
	}

	public CountDownLatch getUuidRecvLatch() {
		return uuidRecvLatch;
	}

	public static Peer findPeer(Connection connection) {
		for (Peer peer : Bootstrapper.peers) {
			if (peer.getConnection().equals(connection)) {
				return peer;
			}
		}
		return null;
	}

	public void reinstance() {
		connection.sendTCP(new Data(DataTypes.REINSTANCE_REQS, null));
		Utils.log(this, "Reinstancing with peer " + uuid, false);
		try {
			Bootstrapper.selfUUID = UUID.randomUUID().toString();
			Bootstrapper.aes = new AES(Bootstrapper.selfUUID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
