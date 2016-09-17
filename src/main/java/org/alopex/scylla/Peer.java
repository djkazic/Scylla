package org.alopex.scylla;

import com.esotericsoftware.kryonet.Connection;

public class Peer {

	private Connection connection;
	private String mutex;
	private int direction;

	public Peer(Connection connection, int direction) {
		this.connection = connection;
		this.direction = direction;
	}
}
