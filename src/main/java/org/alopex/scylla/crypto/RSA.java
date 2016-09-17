package org.alopex.scylla.crypto;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author Kevin Cai on 9/17/2016.
 */
public class RSA {

	private KeyPairGenerator kpg;
	public static String pubKey;
	public KeyPair myPair;

	public RSA(byte[] pubBytes, byte[] privBytes) {
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubBytes);
			PublicKey publicKey = kf.generatePublic(pubKeySpec);
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privBytes);
			PrivateKey privKey = kf.generatePrivate(privKeySpec);
			myPair = new KeyPair(publicKey, privKey);
			byte[] pubKeyBytes = publicKey.getEncoded();
			RSA.pubKey = new String(DatatypeConverter.printBase64Binary(pubKeyBytes));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public RSA() throws NoSuchAlgorithmException {
		kpg = KeyPairGenerator.getInstance("RSA");
		myPair = kpg.generateKeyPair();
		byte[] pubKeyBytes = myPair.getPublic().getEncoded();
		RSA.pubKey = new String(DatatypeConverter.printBase64Binary(pubKeyBytes));
	}

	public String encrypt(String str, PublicKey pk) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pk);
			return new String(cipher.doFinal(str.getBytes()), "ISO-8859-1");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public String decrypt(String in) {
		try {
			Cipher decipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			decipher.init(Cipher.DECRYPT_MODE, myPair.getPrivate());
			String output = new String(decipher.doFinal(in.getBytes("ISO-8859-1")));
			return output;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public PublicKey getRawPublicKey() {
		return myPair.getPublic();
	}

	public byte[] getPublicKeyBytes() {
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(myPair.getPublic().getEncoded());
		return x509.getEncoded();
	}

	public byte[] getPrivateKeyBytes() {
		PKCS8EncodedKeySpec pkc = new PKCS8EncodedKeySpec(myPair.getPrivate().getEncoded());
		return pkc.getEncoded();
	}
}
