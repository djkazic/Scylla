package org.alopex.scylla.net.p2p;

import com.esotericsoftware.kryonet.Connection;
import org.alopex.scylla.core.Bootstrapper;
import org.alopex.scylla.crypto.AES;
import org.alopex.scylla.crypto.RSA;
import org.alopex.scylla.utils.Utils;

public class Peer {

	private Connection connection;
	private String mutex;
	private int direction;

	private RSA rsa;
	private AES aes;

	public Peer(Connection connection, int direction) {
		this.connection = connection;
		this.direction = direction;
		addToPeerList();
	}

	private void addToPeerList() {
		Bootstrapper.peers.add(this);
	}

	public boolean mutexCheck() {
		if (mutex.equals(Bootstrapper.selfMutex)) {
			Utils.log(this, "Duplicate mutex against self: " + mutex, false);
			//disconnect();
			return false;
		} else {
			boolean passed = true;
			for (Peer peer : Bootstrapper.peers) {
				if (peer != this && peer.getMutex() != null && peer.getMutex().equals(mutex)) {
					Utils.log(this, "Duplicate mutex: " + mutex, false);
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
		if (mutex != null) {
			Utils.log(this, "Peer " + mutex + " disconnected", false);
		} else {
			Utils.log(this, "Peer disconnected (mutex null for connection [" + connNumber + "])", false);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public String getMutex() {
		return mutex;
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
}
