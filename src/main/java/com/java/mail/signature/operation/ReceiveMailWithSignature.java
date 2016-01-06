package com.java.mail.signature.operation;

import java.security.PrivateKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.KeyTransRecipient;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
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
			
//			String issuePfxPath = "ROOTCA.pfx";
//			String issueCrtPath = "ROOTCA.crt";
//			String issueAlias = "RootCA";
//			String issuePassword = "123456";
			String subjectPfxPath = "ISSUE.pfx";
			String subjectCrtPath = "ISSUE.crt";
			String subjectAlias = "HPE";
			String subjectPassword = "hpe";
			X509Certificate certificate = X509CertUtil.readX509Certificate(subjectCrtPath);
			PrivateKey privateKey = X509CertUtil.readPrivateKey(subjectAlias, subjectPfxPath, subjectPassword);

			Folder folder = getFolder("INBOX");
			Message message[] = folder.getMessages();
			if (message.length == 0) {
				System.out.println("No Message!");
			}
			for (int i = 0; i < message.length; i++) {
				System.out.println("the " + (i + 1) + " mail" + "------------------------------------------------");
				MimeMessage mail = (MimeMessage) message[i];
				String out_from = ((InternetAddress) message[i].getFrom()[0]).getAddress();
				System.out.println("From:" + out_from);
				System.out.println("Subject:" + message[i].getSubject());
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

	public static void receiveSignedMail(Message mail, X509Certificate cert) {

		try {
			SMIMESigned signed = new SMIMESigned((MimeMultipart) mail.getContent());
			if (isValid(signed, cert)) {
				System.out.println("verification succeeded");
			} else {
				System.out.println("verification failed");
			}

			MimeBodyPart content = signed.getContent();

			System.out.println("Content: " + content.getContent());
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static Folder getFolder(String folderName) {
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", "127.0.0.1");
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("pop3");
			store.connect("127.0.0.1", "smart", "smart");
			Folder folder = store.getFolder(folderName);
			folder.open(Folder.READ_WRITE);
			return folder;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		}
	}

	/*public static boolean isValid(CMSSignedData signedData) {
		try {
			org.bouncycastle.util.Store store = (org.bouncycastle.util.Store) signedData.getCertificates();
			SignerInformationStore signers = signedData.getSignerInfos();
//			Collection c = signers.getSigners();
			Iterator it = signers.getSigners().iterator();
			if (it.hasNext()) {
				SignerInformation signer = (SignerInformation) it.next();
				ASN1EncodableVector attributeVector = signer.getSignedAttributes().toASN1EncodableVector();
				for (int i = 0; i < attributeVector.size(); i++) {
					System.out.println(attributeVector.get(i));
				}
				Collection certCollection = ((org.bouncycastle.util.Store) store).getMatches(signer.getSID());
				Iterator certIt = certCollection.iterator();
				X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
				X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
				return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert));
			}

			return false;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		}
	}*/
	
	public static boolean isValid(CMSSignedData signedData, X509Certificate cert) {
		try {
			SignerInformationStore signers = signedData.getSignerInfos();
			Iterator it = signers.getSigners().iterator();
			if (it.hasNext()) {
				SignerInformation signer = (SignerInformation) it.next();
				return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert));
			}

			return false;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		}
	}
}
