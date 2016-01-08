package com.java.mail.signature.operation;

import java.security.PrivateKey;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.KeyTransRecipient;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMEUtil;

import com.java.mail.util.X509CertUtil;

public class ReceiveMailWithSignature {
	public static void main(String args[]) {
		try {
			// String issuePfxPath = "ROOTCA.pfx";
			// String issueCrtPath = "ROOTCA.crt";
			// String issueAlias = "RootCA";
			// String issuePassword = "123456";
			String subjectPfxPath = "ISSUE.pfx";
			String subjectCrtPath = "ISSUE.crt";
			String subjectAlias = "HPE";
			String subjectPassword = "hpe";

			// X509Certificate certificate = X509CertUtil.readX509Certificate("outlook.der.encoded.cer");
			X509Certificate certificate = X509CertUtil.readX509Certificate("outlook.base64.encoded.cer");
			// X509Certificate certificate = X509CertUtil.readX509Certificate(subjectCrtPath);
			PrivateKey privateKey = X509CertUtil.readPrivateKey(subjectAlias, subjectPfxPath, subjectPassword);

			// Folder folder = getFolder("INBOX");
			String inbox ="To or Cc me";
			Folder folder = getFolder(inbox);
			// Message messages[] = folder.getMessages();

			// SearchTerm st = new OrTerm(new FromStringTerm("tao.zhong@hpe.com"), new SubjectTerm("Signed Mail Subject"));
			SearchTerm st = new OrTerm(new FromStringTerm("tao.zhong@hpe.com"), new SubjectTerm("Sign Mail Test"));
			Message[] messages = folder.search(st);
			int mailCounts = messages.length;
			System.out.println("Found: " + mailCounts + " mails matched with the condition.");

			if (messages.length == 0) {
				System.out.println("No Message!");
			}
			for (int i = 0; i < messages.length; i++) {
				System.out.println("the " + (i + 1) + " mail" + "------------------------------------------------");
				MimeMessage mail = (MimeMessage) messages[i];
				String out_from = ((InternetAddress) messages[i].getFrom()[0]).getAddress();
				System.out.println("From:" + out_from);
				System.out.println("Subject:" + messages[i].getSubject());
				System.out.println("Type: " + mail.getContentType());
				if (mail.isMimeType("multipart/signed")) {
					System.out.println("a signed mail");
					receiveSignedMail(mail, certificate);
				} else if (mail.isMimeType("application/pkcs7-mime") || mail.isMimeType("application/x-pkcs7-mime")) {
					System.out.println("a enveloped mail");
					receiveEnveloped(mail, privateKey, certificate);
				} else {
					System.out.println("not a identified mail");
				}

				// message[i].setFlag(Flags.Flag.DELETED, true);
			}
			folder.close(true);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static void receiveEnveloped(Message mail, PrivateKey key, X509Certificate cert) {

		try {
			SMIMEEnveloped enveloped = new SMIMEEnveloped((MimeMessage) mail);

			KeyTransRecipientId recId = new JceKeyTransRecipientId(cert.getIssuerX500Principal(), cert.getSerialNumber());
			RecipientInformationStore recipientInfos = enveloped.getRecipientInfos();
			RecipientInformation recipientInfo = recipientInfos.get(recId);

			if (recipientInfo != null) {
				KeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(key);
				MimeBodyPart recoveredPart = SMIMEUtil.toMimeBodyPart(recipientInfo.getContent(recipient));

				System.out.println("ContentType: " + recoveredPart.getContentType());
				if (recoveredPart.isMimeType("multipart/alternative")) {
					Multipart mp = (Multipart) recoveredPart.getContent();
					int index = 0;
					if (mp.getCount() > 1)
						index = 1;
					Part tmp = mp.getBodyPart(index);
					System.out.println("Content: " + tmp.getContent());
				} else {
					System.out.println("Content: " + recoveredPart.getContent());
				}
			} else {
				System.out.println("decrypt error");
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static void receiveSignedMail(Message msg, X509Certificate cert) throws MessagingException {
		
		try {

			MimeMessage newmsg = new MimeMessage((MimeMessage) msg);
//			newmsg.setHeader("micalg", "multipart/signed; protocol=application/x-pkcs7-signature; micalg=2.16.840.1.101.3.4.2.1;boundary="----=_NextPart_000_0109_01D1496F.83293B50"");
			newmsg.saveChanges();
			System.out.println("newmsg ContentType(): " + newmsg.getContentType());
			// SMIMESigned signed = new SMIMESigned((MimeMultipart) newmsg.getContent());
			SMIMESigned signed = new SMIMESigned((MimeMultipart) newmsg.getContent());

			////
			/*
			 * SignerInformationStore signers = signed.getSignerInfos();
			 * 
			 * org.bouncycastle.util.Store certs = (org.bouncycastle.util.Store)signed.getCertificates();
			 * 
			 * Collection c = signers.getSigners();
			 * Iterator it = c.iterator();
			 * while (it.hasNext()) {
			 * SignerInformation signer = (SignerInformation) it.next();
			 * Collection certCollection = ((org.bouncycastle.util.Store) certs).getMatches(signer.getSID());
			 * 
			 * Iterator certIt = certCollection.iterator();
			 * X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
			 * X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
			 * 
			 * if (signer.verify((SignerInformationVerifier) certificate.getPublicKey())) {
			 * // verified++;
			 * System.out.println("verification succeeded");
			 * }else {
			 * System.out.println("verification failed");
			 * }
			 * }
			 */
			// org.bouncycastle.util.Store store = (org.bouncycastle.util.Store) signedData.getCertificates();
			// Collection certCollection = ((org.bouncycastle.util.Store) store).getMatches(signer.getSID());
			// Iterator certIt = certCollection.iterator();
			// X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
			// X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
			///

			if (isValid(signed, cert)) {
				System.out.println("verification succeeded");
			} else {
				System.out.println("verification failed");
			}
			signed = new SMIMESigned((MimeMultipart) msg.getContent());
			MimeBodyPart content = signed.getContent();

			System.out.println("Content: " + content.getContent());
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static Folder getFolder(String folderName) {
		try {
			String host = "webmail.hp.com";
			boolean auth = true;
			/* You must use SSL for incoming mail, SSL ports are 993 for IMAP and 995 for POP3 */
			/* To support SSL, using protocl 'pop3s' or 'imaps' */
			String port = "993";
			String receivingProtocol = "imaps";
			String username = "tao.zhong@hpe.com";
			String password = "Cisco01!";
			boolean proxySet = true;
			String proxyHost = "172.28.2.1";
			String proxyPort = "85";

			// String host = "localhost";
			// boolean auth = true;
			// /* You must use SSL for incoming mail, SSL ports are 993 for IMAP and 995 for POP3 */
			// /* To support SSL, using protocl 'pop3s' or 'imaps' */
			// String port = "993";
			// String receivingProtocol = "pop3";
			// String username = "smart";
			// String password = "smart";

			Properties props = System.getProperties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.auth", auth);
			props.put("mail.store.protocol", receivingProtocol);
			//Proxy
			props.put("proxySet", proxySet);
			props.put("http.proxyHost",proxyHost); 
			props.put("http.proxyPort",proxyPort); 
//			props.put("socksProxyHost", "192.168.1.1");
//			props.put("socksProxyPort", "1080");
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore(receivingProtocol);
			store.connect(host, username, password);
			Folder folder = store.getFolder(folderName);
			folder.open(Folder.READ_WRITE);
			return folder;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		}
	}

	public static boolean isValid(CMSSignedData signedData, X509Certificate cert) {
		try {
			SignerInformationStore signers = signedData.getSignerInfos();
			Iterator<SignerInformation> it = signers.getSigners().iterator();

			if (it.hasNext()) {
				SignerInformation signer = it.next();
				// org.bouncycastle.util.Store store = (org.bouncycastle.util.Store) signedData.getCertificates();
				// Collection certCollection = ((org.bouncycastle.util.Store) store).getMatches(signer.getSID());
				// Iterator certIt = certCollection.iterator();
				// X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
				// X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
				return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert));
			}

			return false;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		}
	}
}
