package org.alopex.scylla.net.p2p;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.util.TcpIdleSender;
import org.alopex.scylla.net.packets.Data;

import java.util.HashMap;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class ArtichokeListener extends TcpIdleSender {
	public static HashMap<Connection, Data> sendQueue;
	public static long lastSent = 0;

	public ArtichokeListener() {
		if (sendQueue == null) {
			sendQueue = new HashMap<Connection, Data>();
		}
		lastSent = System.currentTimeMillis();
	}

	public void idle(Connection connection) {
		if (!sendQueue.isEmpty()) {
			Data sendObj = sendQueue.get(connection);
			if (sendObj != null && (System.currentTimeMillis() - lastSent) > 250) {
				lastSent = System.currentTimeMillis();
				connection.sendTCP(sendObj);
				sendQueue.remove(connection);
			}
		}
	}

	public void received(final Connection connection, Object object) {
		/**
		 * final Peer foundPeer = Peer.findPeer(connection);
		 if (object instanceof Data) {
		 Data artichokeObject = (Data) object;
		 final byte type = artichokeObject.getType();
		 if (type == DataTypes.ARTICHOKE_REQS) {

		 }
		 }
		 */
	}

	@Override
	protected Object next() {
		return null;
	}
}
