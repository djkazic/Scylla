package org.alopex.scylla.net.p2p;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import org.alopex.scylla.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class DualListener extends Listener {

	private static ExecutorService replyPool;
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

	public void disconnected(Connection connection) {
		Peer foundPeer = Peer.findPeer(connection);
		if (foundPeer != null) {
			foundPeer.disconnect();
		}
	}
}
