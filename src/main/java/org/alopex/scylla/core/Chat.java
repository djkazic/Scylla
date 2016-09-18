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
		//System.out.print("$" + Bootstrapper.selfUUID.substring(0, 4) + "> ");
		while (scan.hasNextLine()) {
			System.out.print("$>>> ");
			input = scan.nextLine();

			if (input.startsWith(":")) {
				processMacros(input);
			} else if (input.startsWith("/")) {
				processCommands(input);
			} else {
				connection.sendTCP(new Data(DataTypes.CHAT_DATA, input));
			}
			//render(Bootstrapper.selfUUID.substring(0, 4), input + "\n");
		}
	}

	public void render(Peer peer, String message) {
		System.out.print("\n$" + peer.getUuid().substring(0, 4) + "> " + message + "\n");
	}

	public void render(String uuid, String message) {
		System.out.print("\n$" + uuid.substring(0, 4) + "> " + message);
	}

	private void processCommands(String input) {
		switch(input) {
			case "/dc":
				Peer.findPeer(connection).disconnect();
				break;
		}
	}

	private void processMacros(String input) {
		switch(input) {
			case ":ayylmao:":
				input = "                        .   *        .       .                \n" +
						"         *      -0-\n" +
						"            .                .  *       - )-\n" +
						"         .      *       o       .       * \n" +
						"   o                |\n" +
						"             .     -O-    \n" +
						"  .                 |        *      .     -0-\n" +
						"         *  o     .    '       *      .        o\n" +
						"                .         .        |      *\n" +
						"     *             *              -O-          .\n" +
						"           .             *         |     ,\n" +
						"                  .           o\n" +
						"          .---.\n" +
						"    =   _/__~0_\\_     .  *            o       ' \n" +
						"   = = (_________)             .            \n" +
						"                   .                        *\n" +
						"         *               - ) -       *      ";
				break;

			case ":floppy:":
				input = "   __________\n" +
						"     .'`   |     |`'.\n" +
						"     |     '-----'  |\n" +
						"     |              |\n" +
						"     |  .--------.  |\n" +
						"     |  |--------|  |\n" +
						"     |  |--------|  |\n" +
						"     |  |--------|  |\n" +
						"     |  ;--------;  |\n" +
						"     |__:________:__|";
				break;
		}
		System.out.println(input);
		connection.sendTCP(new Data(DataTypes.CHAT_DATA, input));
		System.out.print("$>>> ");
	}
}
