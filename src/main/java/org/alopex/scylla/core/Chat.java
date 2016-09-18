package org.alopex.scylla.core;

import com.esotericsoftware.kryonet.Connection;
import org.alopex.scylla.net.p2p.Peer;
import org.alopex.scylla.net.packets.Data;
import org.alopex.scylla.net.packets.DataTypes;
import org.alopex.scylla.utils.ASCII;

import java.util.Scanner;

/**
 * @author Kevin Cai on 9/18/2016.
 */
public class Chat {

	private Connection connection;
	private Scanner scan;

	public Chat(Connection connection) {
		this.connection = connection;
	}

	public void init() {
		scan = new Scanner(System.in);
		String input = "";
		//System.out.print("$" + Bootstrapper.selfUUID.substring(0, 4) + "> ");
		while (connection.isConnected() && scan.hasNextLine()) {
			System.out.print("$>>> ");
			input = scan.nextLine();

			if (input.startsWith(":")) {
				processMacros(input);
			} else if (input.startsWith("/")) {
				processCommands(input);
			} else {
				connection.sendTCP(new Data(DataTypes.CHAT_DATA, Bootstrapper.aes.encrypt(input)));
			}
			//render(Bootstrapper.selfUUID.substring(0, 4), input + "\n");
		}
	}

	public void render(Peer peer, String message) {
		System.out.print("\n$" + peer.getUuid().substring(0, 4) + "> " + message + "\n$>>> ");
	}

	public void render(String uuid, String message) {
		System.out.print("\n$" + uuid.substring(0, 4) + "> " + message);
	}

	private void processCommands(String input) {
		switch(input) {
			case "/dc":
				// Classic XKCD
				System.out.println("\n+-------------------------------------------+  \t       \t       \t       \t       \t\n" +
						"|                                           | \t\t\t\t      \n" +
						"|                                           |                                   \n" +
						"|                   I wonder where I'll     +----------------------------------+\n" +
						"|                      float next?          |                                  |\n" +
						"|                       /                   |                                  |\n" +
						"|                      |                    |                                  |\n" +
						"|                                           |                                  |\n" +
						"|               , , | / /                   |                                  |\n" +
						"|              / / | \\ \\                    |__________________________________|\n" +
						"|                     .                     |  ~    o                          |\n" +
						"|__________   | , ,   |  ___________________|      |@|    ~                    |\n" +
						"|             \\  __  /                      |         ~             ~       ~  |\n" +
						"|   -^-._      \\____/                       |                                  |\n" +
						"|              ,^U-^.           -~~-        +----------------------------------+\n" +
						"|        _.---/      \\--^>                  |                                   \n" +
						"|       |`--.|________|./|                  |                                   \n" +
						"|~~     |                |   /\\,-^-         |                                   \n" +
						"|  ~    |                |                  |                                   \n" +
						"|       |                |         ~~       |                                   \n" +
						"|       |_-^~-.________/~'                  |                                   \n" +
						"|_/^-._                     ~               |                                   \n" +
						"|                                           |                                   \n" +
						"+-------------------------------------------+                                   ");
				Peer.findPeer(connection).disconnect();
				break;

			case "/rc":
				Peer.findPeer(connection).reinstance();
				break;
		}
	}

	private void processMacros(String input) {
		switch(input) {
			case ":ayylmao:":
				input = ASCII.ayylmao;
				break;

			case ":floppy:":
				input = ASCII.floppy;
				break;

			case ":fatcat:":
				input = ASCII.fatcat;
				break;

			case ":fsociety:":
				input = ASCII.fsociety;
				break;

			case ":cake:":
				input = ASCII.cake;
				break;

			case ":atari:":
				input = ASCII.atari;
				break;
		}
		System.out.println(input);
		connection.sendTCP(new Data(DataTypes.CHAT_DATA, Bootstrapper.aes.encrypt(input)));
		System.out.print("$>>> ");
	}

	public void shutdown() {
		scan.close();
	}
}
