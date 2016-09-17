package org.alopex.scylla.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class Config {

	public int tcpPort = 23450;

	public byte[] rsaPubKeyAsBytes;
	public byte[] rsaPrivKeyAsBytes;

	public void writeToDisk() {
		try {
			File configFile = new File(".config/config.dat");
			if (configFile.exists()) {
				configFile.delete();
			}
			configFile.createNewFile();

			Kryo kryo = new Kryo();
			FileOutputStream fos = new FileOutputStream(configFile);
			Output out = new Output(fos);
			kryo.writeObject(out, this);
			out.close();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public byte[] getRSAPubKeyAsBytes() {
		return rsaPubKeyAsBytes;
	}

	public byte[] getRSAPrivKeyAsBytes() {
		return rsaPrivKeyAsBytes;
	}
}
