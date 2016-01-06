package com.java.mail.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertAndKeyGen;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class X509CertUtilTest {

	/**
	 * 读取公钥证书中的公钥（字符串形式）
	 * 
	 * @param crtPath
	 * @return
	 * @throws CertificateException
	 * @throws IOException
	 */
	public String readX509CertificatePublicKey(String crtPath) throws CertificateException, IOException {
		
		X509Certificate x509Certificate = X509CertUtil.readX509Certificate(crtPath);

		PublicKey publicKey = x509Certificate.getPublicKey();

		return publicKey.toString().replace(" ", "");

	}

	/**
	 * 读取KeyStore里面的私钥（字符串形式）
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public String readPrivateKeyStr(String alias, String pfxPath, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

		PrivateKey privateKey = X509CertUtil.readPrivateKey(alias, pfxPath, password);

		return privateKey.toString().replace(" ", "");

	}

	/**
	 * 根据证书读取 读取模数N
	 * 
	 * @param crtPath
	 * @return
	 */
	public String getModulusByCrt(String crtPath) {
		String crt = "";
		try {
			crt = readX509CertificatePublicKey(crtPath);
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = crt.substring(crt.indexOf("modulus:") + "modulus:".length(), crt.indexOf("publicexponent:"));
		return modulus.trim().replace(" ", "");
	}

	/**
	 * 根据证书读取公钥e
	 * 
	 * @param crtPath
	 * @return
	 */
	public String getPubExponentByCrt(String crtPath) {

		String crt = "";
		try {
			crt = readX509CertificatePublicKey(crtPath);
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String pubExponent = crt.substring(crt.indexOf("publicexponent:") + "publicexponent:".length(), crt.length());
		return pubExponent.trim().replace(" ", "");

	}

	/**
	 * 根据KeyStore读取模数N
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 */
	public String getModulusByPfx(String alias, String pfxPath, String password) {

		String pfx = "";
		try {
			pfx = readPrivateKeyStr(alias, pfxPath, password);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = pfx.substring(pfx.indexOf("modulus:") + "modulus:".length(), pfx.indexOf("publicexponent:"));

		return modulus.trim().replace(" ", "");

	}

	/**
	 * 根据KeyStore读取公钥e
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 */
	public String getPubExponentByPfx(String alias, String pfxPath, String password) {

		String pfx = "";
		try {
			pfx = readPrivateKeyStr(alias, pfxPath, password);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = pfx.substring(pfx.indexOf("publicexponent:") + "publicexponent:".length(), pfx.indexOf("privateexponent:"));

		return modulus.trim().replace(" ", "");

	}

	/**
	 * 根据KeyStore读取私钥d
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 */
	public String getPriExponentByPfx(String alias, String pfxPath, String password) {

		String pfx = "";
		try {
			pfx = readPrivateKeyStr(alias, pfxPath, password);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = pfx.substring(pfx.indexOf("privateexponent:") + "privateexponent:".length(), pfx.indexOf("primep:"));

		return modulus.trim().replace(" ", "");

	}

	/**
	 * 根据KeyStore读取p
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 */
	public String getpByPfx(String alias, String pfxPath, String password) {

		String pfx = "";
		try {
			pfx = readPrivateKeyStr(alias, pfxPath, password);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = pfx.substring(pfx.indexOf("primep:") + "primep:".length(), pfx.indexOf("primeq:"));

		return modulus.trim().replace(" ", "");
	}

	/**
	 * 根据KeyStore读取q
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 */
	public String getqByPfx(String alias, String pfxPath, String password) {

		String pfx = "";
		try {
			pfx = readPrivateKeyStr(alias, pfxPath, password);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = pfx.substring(pfx.indexOf("primeq:") + "primeq:".length(), pfx.indexOf("primeexponentp:"));

		return modulus.trim().replace(" ", "");

	}

	/**
	 * 根据KeyStore读取dp
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 */
	public String getdpByPfx(String alias, String pfxPath, String password) {

		String pfx = "";
		try {
			pfx = readPrivateKeyStr(alias, pfxPath, password);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = pfx.substring(pfx.indexOf("primeexponentp:") + "primeexponentp:".length(), pfx.indexOf("primeexponentq:"));

		return modulus.trim().replace(" ", "");

	}

	/**
	 * 根据KeyStore读取dq
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 */
	public String getdqByPfx(String alias, String pfxPath, String password) {

		String pfx = "";
		try {
			pfx = readPrivateKeyStr(alias, pfxPath, password);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = pfx.substring(pfx.indexOf("primeexponentq:") + "primeexponentq:".length(), pfx.indexOf("crtcoefficient:"));

		return modulus.trim().replace(" ", "");

	}

	/**
	 * 根据KeyStore读取qInv
	 * 
	 * @param alias
	 * @param pfxPath
	 * @param password
	 * @return
	 */
	public String getqInvByPfx(String alias, String pfxPath, String password) {

		String pfx = "";
		try {
			pfx = readPrivateKeyStr(alias, pfxPath, password);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modulus = pfx.substring(pfx.indexOf("crtcoefficient:") + "crtcoefficient:".length(), pfx.length());

		return modulus.trim().replace(" ", "");

	}

	public static void main(String args[]) throws IOException {

		// CN commonName 一般名字
		// L localityName 地方名
		// ST stateOrProvinceName 州省名
		// O organizationName 组织名
		// OU organizationalUnitName 组织单位名
		// C countryName 国家
		// STREET streetAddress 街道地址
		// DC domainComponent 领域
		// UID user id 用户ID
		X500Name issue = new X500Name("CN=RootCA,OU=ISI,O=BenZeph,L=CD,ST=SC,C=CN");

		X500Name subject = new X500Name("CN=subject,OU=ISI,O=BenZeph,L=CD,ST=SC,C=CN");

		String issuePfxPath = "ROOTCA.pfx";
		String issueCrtPath = "ROOTCA.crt";

		String subjectPfxPath = "ISSUE.pfx";
		String subjectCrtPath = "ISSUE.crt";

		String issueAlias = "RootCA";
		String subjectAlias = "subject";

		String issuePassword = "123456";
		String subjectPassword = "123456";

		X509CertUtilTest test = new X509CertUtilTest();

		try {
			System.out.println(test.readX509CertificatePublicKey(issueCrtPath));
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		System.out.println("");
		System.out.println(test.getModulusByCrt(issueCrtPath));
		System.out.println(test.getPubExponentByCrt(issueCrtPath));
		System.out.println("");
		try {
			System.out.println(test.readPrivateKeyStr(issueAlias, issuePfxPath, issuePassword));
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		System.out.println("");
		System.out.println(test.getModulusByPfx(issueAlias, issuePfxPath, issuePassword));
		System.out.println(test.getPubExponentByPfx(issueAlias, issuePfxPath, issuePassword));
		System.out.println(test.getPriExponentByPfx(issueAlias, issuePfxPath, issuePassword));
		System.out.println(test.getpByPfx(issueAlias, issuePfxPath, issuePassword));
		System.out.println(test.getqByPfx(issueAlias, issuePfxPath, issuePassword));
		System.out.println(test.getdpByPfx(issueAlias, issuePfxPath, issuePassword));
		System.out.println(test.getdqByPfx(issueAlias, issuePfxPath, issuePassword));
		System.out.println(test.getqInvByPfx(issueAlias, issuePfxPath, issuePassword));
	}
}
