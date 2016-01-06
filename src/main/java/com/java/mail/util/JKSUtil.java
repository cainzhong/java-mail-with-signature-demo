package com.java.mail.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

public class JKSUtil {

	private static Logger logger = Logger.getLogger(JKSUtil.class);

	/* 指定加密算法为RSA */
	private static final String PUBLIC_KEY_ALGORITHM = "RSA";
	private static final String SIGNATURE_ALGORITHM = "SHA1WithRSA";
	/* 密钥长度，用来初始化 */
	private static final int KEYSIZE = 1024;
	/* 指定公钥存放文件 */
	private static String PUBLIC_KEY_FILE = "PublicKey";
	/* 指定私钥存放文件 */
	private static String PRIVATE_KEY_FILE = "PrivateKey";

	public static void createKeyStore() {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);
			keyStore.store(new FileOutputStream("myKey.jks"), "password".toCharArray());
		} catch (KeyStoreException e) {
			logger.error(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		} catch (CertificateException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	/**
	 * 创建根证书（证书有效期1年，私钥保存密码“123456”，公钥算法“RSA”，签名算法“SHA1WithRSA”）
	 * 
	 * @param args
	 */
	public static void saveCertificate() {
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("myKey.jks"), "password".toCharArray());
			CertAndKeyGen gen = new CertAndKeyGen(PUBLIC_KEY_ALGORITHM, SIGNATURE_ALGORITHM);
			gen.generate(KEYSIZE);
			String name = "CN=country,ST=state,L=Locality,OU=OrganizationUnit,O=Organization";
			// 生成自签名证书
			X509Certificate cert = gen.getSelfCertificate(new X500Name(name), (long) 365 * 24 * 3600);
			keyStore.setCertificateEntry("single_cert", cert);
			keyStore.store(new FileOutputStream("myKey.jks"), "password".toCharArray());
			System.out.println("End");
		} catch (KeyStoreException e) {
			logger.error(e);
		} catch (InvalidKeyException e) {
			logger.error(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		} catch (CertificateException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (SignatureException e) {
			logger.error(e);
		} catch (NoSuchProviderException e) {
			logger.error(e);
		}
	}

	public static void savePrivateKey() {
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("myKey.jks"), "password".toCharArray());

			CertAndKeyGen gen = new CertAndKeyGen("RSA", "SHA1WithRSA");

			gen.generate(1024);

			Key key = gen.getPrivateKey();
			X509Certificate cert = gen.getSelfCertificate(new X500Name("CN=ROOT"), (long) 365 * 24 * 3600);
			X509Certificate[] chain = new X509Certificate[1];
			chain[0] = cert;
			keyStore.setKeyEntry("mykey", key, "password".toCharArray(), chain);

			keyStore.store(new FileOutputStream("myKey.jks"), "password".toCharArray());
			System.out.println("End");

		} catch (KeyStoreException e) {
			logger.error(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		} catch (CertificateException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (InvalidKeyException e) {
			logger.error(e);
		} catch (SignatureException e) {
			logger.error(e);
		} catch (NoSuchProviderException e) {
			logger.error(e);
		}
	}

	public static void loadCertificate() {
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("myKey.jks"), "password".toCharArray());
			java.security.cert.Certificate cert = keyStore.getCertificate("single_cert");
			System.out.println(cert.toString());
		} catch (KeyStoreException e) {
			logger.error(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		} catch (CertificateException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public static void loadPrivateKey() {
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("myKey.jks"), "password".toCharArray());
			Key key = keyStore.getKey("myKey", "password".toCharArray());
			System.out.println("Private key : " + key.toString());
			Certificate[] chain = keyStore.getCertificateChain("myKey");
			for (Certificate cert : chain) {
				System.out.println("cert : " + cert.toString());
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		} catch (CertificateException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (UnrecoverableKeyException e) {
			logger.error(e);
		} catch (KeyStoreException e) {
			logger.error(e);
		}

	}

	public static void main(String args[]) {
		JKSUtil.createKeyStore();
		JKSUtil.saveCertificate();
		JKSUtil.savePrivateKey();
		JKSUtil.loadCertificate();
		JKSUtil.loadPrivateKey();
	}
}
