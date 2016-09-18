package org.alopex.scylla.net;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import org.alopex.scylla.core.Bootstrapper;
import org.alopex.scylla.net.p2p.DualListener;
import org.alopex.scylla.net.p2p.SOCKSRoute;
import org.alopex.scylla.net.packets.Data;
import org.alopex.scylla.utils.Utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class NetBootstrapper {

	public static List<InetAddress> foundHosts;

	private Server server;

	public void init() {
		try {
			Utils.log(this, "Creating server / starting listeners...", false);
			createServer();

			Utils.log(this, "Creating test connections...", false);
			foundHosts = new ArrayList<InetAddress>();
			foundHosts.add(InetAddress.getByName("18.22.8.10"));

			Utils.log(this, "Attempting to open test connections...", false);
			attemptConnections();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void attemptConnections() {
		try {
			Client newConnection = null;
			for (int i=0; i < foundHosts.size(); i++) {
				InetAddress ia = foundHosts.get(i);
				try {
					Utils.log(this, "Attempting connect to " + ia.getHostAddress(), false);
					newConnection = createClient();
					newConnection.connect(8000, ia, Bootstrapper.config.tcpPort);
				} catch (Exception ex) {
					Utils.log(this, "Connection to " + ia.getHostAddress() + " failed", false);
					newConnection.close();
					System.gc();
				}
				Thread.sleep(1000);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

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
		registerClasses(client.getKryo());
		Utils.log(this, "Registering client listeners...", false);
		client.addListener(new DualListener(0));
		client.start();
	}

	public void registerServerListeners(Server server) {
		registerClasses(server.getKryo());
		Utils.log(this, "Registering server listeners...", false);
		server.addListener(new DualListener(1));
		// DO NOT CALL server.start()
	}

	private void registerClasses(Kryo kryo) {
		// Main Data object
		kryo.register(Data.class);
		kryo.register(SOCKSRoute.class);
	}

	public Server getServer() {
		return server;
	}
}
