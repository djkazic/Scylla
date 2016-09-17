package org.alopex.scylla.utils;

import com.esotericsoftware.minlog.Log;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class Utils {

	public static void log(Object someClass, String msg, boolean debug) {
		String output = "[" + someClass.getClass().getSimpleName() + "]: " + msg;
		if (debug) {
			Log.debug(output);
		} else {
			Log.info(output);
		}
	}

	public static void log(String someClass, String msg, boolean debug) {
		String output = "[" + someClass + "]: " + msg;
		if (debug) {
			Log.debug(output);
		} else {
			Log.info(output);
		}
	}
}
