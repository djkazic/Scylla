package org.alopex.scylla.core;

import com.esotericsoftware.kryonet.Connection;
import org.alopex.scylla.net.p2p.Peer;
import org.alopex.scylla.net.packets.Data;
import org.alopex.scylla.net.packets.DataTypes;

import java.util.Scanner;

/**
 * @author Kevin Cai on 9/18/2016.
 */
public class Chat {

	private Connection connection;

	public Chat(Connection connection) {
		this.connection = connection;
	}

	public void init() {
		Scanner scan = new Scanner(System.in);
		String input = "";
		System.out.println();
		while (scan.hasNextLine()) {
			input = scan.nextLine();
			render(Bootstrapper.selfUUID.substring(0, 4), input + "\r\033[K");
			connection.sendTCP(new Data(DataTypes.CHAT_DATA, input));
		}
	}

	public void render(Peer peer, String message) {
		System.out.println("$" + peer.getUuid().substring(0, 4) + "> " + message);
	}

	public void render(String user, String message) {
		System.out.println("$" + user + "> " + message);
	}
}
