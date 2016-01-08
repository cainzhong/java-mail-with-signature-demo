package com.java.mail.signature.operation;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.mail.smime.SMIMEUtil;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

import com.java.mail.operation.MailAuthenticator;
import com.java.mail.util.X509CertUtil;

public class SendMailWithSignature {
	public static void main(String args[]) {
		try {
//			String issuePfxPath = "ROOTCA.pfx";
//			String issueCrtPath = "ROOTCA.crt";
//			String issueAlias = "RootCA";
//			String issuePassword = "123456";
//			X509Certificate certificate = X509CertUtil.readX509Certificate(issueCrtPath);
//			PrivateKey privateKey = X509CertUtil.readPrivateKey(issueAlias, issuePfxPath, issuePassword);
			
			String subjectPfxPath = "ISSUE.pfx";
			String subjectCrtPath = "ISSUE.crt";
			String subjectAlias = "HPE";
			String subjectPassword = "hpe";
			X509Certificate certificate = X509CertUtil.readX509Certificate(subjectCrtPath);
			PrivateKey privateKey = X509CertUtil.readPrivateKey(subjectAlias, subjectPfxPath, subjectPassword);
			
			
//			sendEnvelopedMail("Enveloped Mail Subject", "Enveloped Mail Text", certificate);
			sendSignedMail("Signed Mail Subject", "Signed Mail Text", certificate, privateKey);

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private static final String PROVIDER_NAME = "BC";
	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
	// private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

	public static Session getSendMailSession() {
		try {
			String username = "tao.zhong@hpe.com";
			String password = "Cisco01!";
			String smtp = "smtp3.hp.com";
			
//			String smtp = "localhost";
//			String port = "993";
//			String receivingProtocol = "pop3";
//			String username = "smart";
//			String password = "smart";
			
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtp);
			props.put("mail.smtp.auth", "true");
			Authenticator myauth = new MailAuthenticator(username, password);
			Session session = Session.getDefaultInstance(props, myauth);
			return session;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		}
	}

	/**
	 * @param subject
	 * @param text
	 * @param cert
	 * @throws MessagingException
	 * @throws CertificateEncodingException
	 * @throws IllegalArgumentException
	 * @throws SMIMEException
	 * @throws CMSException
	 * @throws IOException
	 */
	public static void sendEnvelopedMail(String subject, String text, X509Certificate cert) throws MessagingException, CertificateEncodingException, IllegalArgumentException, SMIMEException, CMSException, IOException {
		MimeBodyPart dataPart = new MimeBodyPart();
		dataPart.setText(text);
		SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();
		gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(cert).setProvider(PROVIDER_NAME));

		MimeBodyPart envPart = gen.generate(dataPart, new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC, 40).setProvider(PROVIDER_NAME).build());
		Session session = getSendMailSession();
		MimeMessage mail = createMimeMessage(subject, envPart.getContent(), envPart.getContentType(), session);
		Transport.send(mail);
		System.out.println("EnvelopedMail Sent!");
	}

	/**
	 * @param subject
	 * @param text
	 * @param cert
	 * @param key
	 * @throws MessagingException
	 * @throws CertificateEncodingException
	 * @throws CertificateParsingException
	 * @throws OperatorCreationException
	 * @throws SMIMEException
	 */
	public static void sendSignedMail(String subject, String text, X509Certificate cert, PrivateKey key) throws MessagingException, CertificateEncodingException, CertificateParsingException, OperatorCreationException, SMIMEException {
		MimeBodyPart dataPart = new MimeBodyPart();
		dataPart.setText(text);
		MimeMultipart multiPart = createMultipartWithSignature(key, cert, dataPart);
		Session session = getSendMailSession();
		MimeMessage mail = createMimeMessage(subject, multiPart, multiPart.getContentType(), session);
		Transport.send(mail);
		System.out.println("Signed Sent!");
	}

	public static MimeMessage createMimeMessage(String subject, Object content, String contentType, Session session) throws MessagingException {
		Address fromUser = new InternetAddress("tao.zhong@hpe.com");
		Address toUser = new InternetAddress("tao.zhong@hpe.com");
//		Address fromUser = new InternetAddress("smart");
//		Address toUser = new InternetAddress("smart");
		MimeMessage message = new MimeMessage(session);
		message.setFrom(fromUser);
		message.setRecipient(Message.RecipientType.TO, toUser);
		message.setSubject(subject);
		message.setContent(content, contentType);
		message.saveChanges();
		return message;
	}

	/**
	 * @param key
	 * @param cert
	 * @param dataPart
	 * @return
	 * @throws CertificateEncodingException
	 * @throws CertificateParsingException
	 * @throws OperatorCreationException
	 * @throws SMIMEException
	 */
	public static MimeMultipart createMultipartWithSignature(PrivateKey key, X509Certificate cert, MimeBodyPart dataPart) throws CertificateEncodingException, CertificateParsingException, OperatorCreationException, SMIMEException {
		List<X509Certificate> certList = new ArrayList<X509Certificate>();
		certList.add(cert);
		Store certs = new JcaCertStore(certList);
		ASN1EncodableVector signedAttrs = generateSignedAttributes(cert);

		SMIMESignedGenerator gen = new SMIMESignedGenerator();
		gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider(PROVIDER_NAME).setSignedAttributeGenerator(new AttributeTable(signedAttrs)).build(SIGNATURE_ALGORITHM, key, cert));
		gen.addCertificates(certs);
		return gen.generate(dataPart);
	}

	/**
	 * @param cert
	 * @return
	 * @throws CertificateParsingException
	 */
	private static ASN1EncodableVector generateSignedAttributes(X509Certificate cert) throws CertificateParsingException {
		ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
		SMIMECapabilityVector caps = new SMIMECapabilityVector();
		caps.addCapability(SMIMECapability.aES256_CBC);
		caps.addCapability(SMIMECapability.dES_EDE3_CBC);
		caps.addCapability(SMIMECapability.rC2_CBC, 128);
		signedAttrs.add(new SMIMECapabilitiesAttribute(caps));
		signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(SMIMEUtil.createIssuerAndSerialNumberFor(cert)));
		return signedAttrs;
	}
}