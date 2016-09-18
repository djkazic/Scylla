package org.alopex.scylla.core;

import org.alopex.scylla.crypto.RSA;
import org.alopex.scylla.expsocks.org.jdamico.socks.server.impl.ProxyServerInitiator;
import org.alopex.scylla.net.NetBootstrapper;
import org.alopex.scylla.net.p2p.Peer;
import org.alopex.scylla.utils.Utils;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class Bootstrapper {

	private static final String CLASS_NAME = "Bootstrapper";

	public static ArrayList<Peer> peers;
	public static String selfUUID;
	public static RSA rsa;

	//TODO: change to dynamically loaded Config
	public static Config config = new Config();
	//public static SOCKSProxy socksProxy;

	public static NetBootstrapper netBootstrapper;

	public static void main(String[] args) {

		// Bootstrapper code here
		try {
			// Instance var instantiation segment
			peers = new ArrayList<Peer> ();
			rsa = new RSA();

			Utils.log(CLASS_NAME, "Generating self UUID...", false);
			selfUUID = UUID.randomUUID().toString();

			Utils.log(CLASS_NAME, "Initializing SOCKSProxy instance...", false);
			new ProxyServerInitiator(8888).start();

			Utils.log(CLASS_NAME, "Bootstrapping P2P networking engine...", false);
			netBootstrapper = new NetBootstrapper();
			netBootstrapper.init();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
