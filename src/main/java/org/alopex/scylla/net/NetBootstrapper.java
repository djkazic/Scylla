package org.alopex.scylla.net;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import org.alopex.scylla.core.Bootstrapper;
import org.alopex.scylla.utils.Utils;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class NetBootstrapper {

	private Server server;

	public Client createClient() {
		Client client = new Client(512000 * 6, 512000 * 5);
		registerClientListeners(client);
		return client;
	}

	public void createServer() {
		try {
			server = new Server(512000 * 6, 512000 * 5);
			registerServerListeners(server);

			Utils.log(this, "Starting server component", false);
			server.bind(Bootstrapper.config.tcpPort);
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void registerClientListeners(Client client) {

	}

	public void registerServerListeners(Server server) {

	}

	public Server getServer() {
		return server;
	}
}
