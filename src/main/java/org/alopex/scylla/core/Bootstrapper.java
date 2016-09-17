package org.alopex.scylla.core;

import org.alopex.scylla.net.p2p.Peer;
import org.alopex.scylla.net.socks.SOCKSProxy;

import java.util.ArrayList;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class Bootstrapper {

	public static ArrayList<Peer> peers;
	public static String selfMutex;

	public static SOCKSProxy socksProxy;

	public static void main(String[] args) {
		// Instance var instantiation segment
		peers = new ArrayList<Peer> ();

		// Bootstrapper code here
		try {
			socksProxy = new SOCKSProxy();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
