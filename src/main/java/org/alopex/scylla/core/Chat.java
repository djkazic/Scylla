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

			case "/rc":
				Peer.findPeer(connection).reinstance();
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

			case ":fatcat:":
				input = "\n    /\\_____/\\\n" +
						"   /  o   o  \\\n" +
						"  ( ==  ^  == )\n" +
						"   )         (\n" +
						"  (           )\n" +
						" ( (  )   (  ) )\n" +
						"(__(__)___(__)__)";
				break;

			case ":fsociety:":
				input = "\ncddddddddddddddddddddddddddddddddddddddddddd;\n" +
						"0Mo..........':ldkO0KKXXKK0kxoc,..........kMd\n" +
						"0Ml......;d0WMMMMMMMMMMMMMMMMMMMWKx:......kMd\n" +
						"0Ml...cOWMMMMMMMMMMMMMMMMMMMMMMMMMMMWO:...kMd\n" +
						"0Ml.lNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNc.kMd\n" +
						"0MdKMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM0OMd\n" +
						"0MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMd\n" +
						"0MxcxWMMMMMNXXNMMMMMMMMMMMMMMMNXXNMMMMMWkcKMd\n" +
						"0Md..lMKo,.,'...:kWMMMMMMMNx;...',.;dXMl.'XMd\n" +
						"0Mx'.,O;dXMMMXl....:dWMNo;....oXMMMKd;0,.'KMd\n" +
						"0MO;.,NMWMMMMMMWk;...XMK...:OWMMMMMMWMN,.cNMd\n" +
						"0MxxNMX;KMMKdcclkWN0WMMMN0WNxc:lxXMMk;WMXdKMd\n" +
						"0MMMMMO;MMl.......KMXOMNkMk.......xMM.NMMMMMd\n" +
						"0MMMMMMXKoclddl;.oWMdkMN,MN:.:ldolcdXNMMMMMMd\n" +
						"0MMMMMMWXMMMMMMMW0KdoNMMdox0MMMMMMMMXMMMMMMMd\n" +
						"0MMMMXc'WMMMMMMMMkcWMMMMMMkcMMMMMMMMN'lXMMMMd\n" +
						"0MMMd..cMMMMMMMMNdoKMMMMM0x:XMMMMMMMM:..kMMMd\n" +
						"0MM0....d0KKOd:.....c0Kx'.....:d0NX0l....NMMd\n" +
						"0MMO.....................................WMMd\n" +
						"0Mdkc...................................0kOMd\n" +
						"0Ml.:Ol;........';;.......;,........':oX:.kMd\n" +
						"0Ml..,WMMMMWWWo...';;:c::;'...:WWMMMMMW;..kMd\n" +
						"0Ml...dMMMMMMMMKl...........c0MMMMMMMMd...kMd\n" +
						"0Ml...cMMMMMMMMMMMXOxdddk0NMMMMMMMMMMM'...kMd\n" +
						"0Ml....KMMMMMMMMMMMMMMMMMMMMMMMMMMMMMO....kMd\n" +
						"0Ml.....OMMMMMMMMMMMMMMMMMMMMMMMMMMMK.....kMd\n" +
						"0Ml......:XMMMMMMMMMMMMMMMMMMMMMMMNl......kMd\n" +
						"0Ml........lXMMMMMMMMMMMMMMMMMMMKc........kMd\n" +
						"0Ml..........:KMMMMMMMMMMMMMMM0,..........kMd\n" +
						"oO:............xOOOx:'';dOOOOd............lOc";
				break;
		}
		System.out.println(input);
		connection.sendTCP(new Data(DataTypes.CHAT_DATA, input));
		System.out.print("$>>> ");
	}
}
