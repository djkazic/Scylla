package org.alopex.scylla.net;

import com.esotericsoftware.kryonet.Connection;
import org.alopex.scylla.core.Bootstrapper;

public class Peer {

	private Connection connection;
	private String mutex;
	private int direction;

	public Peer(Connection connection, int direction) {
		this.connection = connection;
		this.direction = direction;
		addToPeerList();
	}

	private void addToPeerList() {
		Bootstrapper.peers.add(this);
	}
}
