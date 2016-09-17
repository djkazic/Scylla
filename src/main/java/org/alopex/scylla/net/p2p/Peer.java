package org.alopex.scylla.net.p2p;

import com.esotericsoftware.kryonet.Connection;
import org.alopex.scylla.core.Bootstrapper;
import org.alopex.scylla.crypto.AES;
import org.alopex.scylla.crypto.RSA;
import org.alopex.scylla.utils.Utils;

public class Peer {

	private Connection connection;
	private String uuid;
	private int direction;

	private RSA rsa;
	private AES aes;

	public Peer(Connection connection, int direction) {
		this.connection = connection;
		this.direction = direction;
		uuidCheck();
		addToPeerList();
	}

	private void addToPeerList() {
		Bootstrapper.peers.add(this);
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
				return true;
			} else {
				disconnect();
				return false;
			}
		}
	}

	public void disconnect() {
		Bootstrapper.peers.remove(this);
		int connNumber = connection.getID();
		connection.close();
		if (uuid != null) {
			Utils.log(this, "Peer " + uuid + " disconnected", false);
		} else {
			Utils.log(this, "Peer disconnected (uuid null for connection [" + connNumber + "])", false);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public String getUuid() {
		return uuid;
	}

	public int getDirection() {
		return direction;
	}

	public RSA getRSA() {
		return rsa;
	}

	public AES getAES() {
		return aes;
	}

	public static Peer findPeer(Connection connection) {
		for (Peer peer : Bootstrapper.peers) {
			if (peer.getConnection().equals(connection)) {
				return peer;
			}
		}
		return null;
	}
}
