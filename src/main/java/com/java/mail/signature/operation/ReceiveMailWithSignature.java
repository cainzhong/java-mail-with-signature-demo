package com.java.mail.signature.operation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
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
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMEUtil;

import com.java.mail.domain.MailMessage;
import com.java.mail.util.X509CertUtil;

import net.sf.json.JSONArray;

public class ReceiveMailWithSignature {

	private static final String PROVIDER_NAME = "BC";

	public static void main(String args[]) {
		ReceiveMailWithSignature receiveMail = new ReceiveMailWithSignature();

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		try {
			String subjectPfxPath = "ISSUE.pfx";
			String subjectCrtPath = "ISSUE.crt";
			String subjectAlias = "HPE";
			String subjectPassword = "hpe";

			// X509Certificate certificate = X509CertUtil.readX509Certificate("outlook.der.encoded.cer");
			// X509Certificate certificate = X509CertUtil.readX509Certificate("outlook.base64.encoded.cer");
			X509Certificate certificate = X509CertUtil.readX509Certificate(subjectCrtPath);
			PrivateKey privateKey = X509CertUtil.readPrivateKey(subjectAlias, subjectPfxPath, subjectPassword);

			// Folder folder = getFolder("INBOX");
			String inbox = "To or Cc me";
			Folder folder = getFolder(inbox);
			// Message messages[] = folder.getMessages();

			SearchTerm st = new OrTerm(new FromStringTerm("tao.zhong@hpe.com"), new SubjectTerm("Sign Mail Test"));
			Message[] messages = folder.search(st);
			int mailCounts = messages.length;
			System.out.println("************Found: " + mailCounts + " mails matched with the condition.************");

			if (messages.length == 0) {
				System.out.println("No Message!");
			}
			for (int i = 0; i < messages.length; i++) {
				System.out.println("************The " + (i + 1) + " mail" + "************");
				MimeMessage msg = (MimeMessage) messages[i];
				String from = ((InternetAddress) messages[i].getFrom()[0]).getAddress();
				String subject = messages[i].getSubject();
				System.out.println("From:" + from);
				System.out.println("Subject:" + subject);
				System.out.println("Type: " + msg.getContentType());
				if (msg.isMimeType("multipart/signed")) {
					System.out.println("a signed mail");
					receiveMail.receiveSignedMail(msg);
					MailMessage mailMsg = receiveMail.getMailMsg(msg);
					JSONArray jsonArray = JSONArray.fromObject(mailMsg);
					System.out.println("jsonArray: " + jsonArray.toString());
				} else if (msg.isMimeType("application/pkcs7-mime") || msg.isMimeType("application/x-pkcs7-mime")) {
					System.out.println("a enveloped mail");
					receiveMail.receiveEnveloped(msg, privateKey, certificate);
				} else {
					System.out.println("not a identified mail");
				}
			}
			folder.close(true);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public Object receiveEnveloped(Message mail, PrivateKey key, X509Certificate cert) {
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
					if (mp.getCount() > 1) {
						index = 1;
					}
					Part tmp = mp.getBodyPart(index);
					return tmp.getContent();
				} else {
					return recoveredPart.getContent();
				}
			} else {
				System.out.println("decrypt error");
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return null;
	}

	public MimeBodyPart receiveSignedMail(Message msg) throws MessagingException {
		try {
			/*
			 * Add a header to make a new message in order to fix the issue of Outlook
			 * 
			 * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
			 * 
			 * @see http://stackoverflow.com/questions/8590426/s-mime-verification-with-x509-certificate
			 */
			MimeMessage newmsg = new MimeMessage((MimeMessage) msg);
			newmsg.setHeader("Add a header for verifying signature only.", "nothing");
			newmsg.saveChanges();
			SMIMESigned signed = new SMIMESigned((MimeMultipart) newmsg.getContent());
			if (isValid(signed)) {
				System.out.println("verification succeeded");
				// this.moveDeleteMessage("To or Cc me", "Deleted Items", msg);
			} else {
				System.out.println("verification failed");
			}
			MimeMultipart multi = (MimeMultipart) msg.getContent();
			System.out.println("Multipart Message: " + multi.getBodyPart(0).getContent().toString());

			signed = new SMIMESigned((MimeMultipart) msg.getContent());
			MimeBodyPart content = signed.getContent();

			System.out.println("Content: " + content.getContent());

			// JSONArray jsonArray = JSONArray.fromObject(msg);
			return null;
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return null;
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
			// Proxy
			props.put("proxySet", proxySet);
			props.put("http.proxyHost", proxyHost);
			props.put("http.proxyPort", proxyPort);
			// props.put("socksProxyHost", "192.168.1.1");
			// props.put("socksProxyPort", "1080");
			/*
			 * General Issues with Multiparts
			 * 
			 * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
			 */
			// props.put("mail.mime.cachemultipart", false);
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore(receivingProtocol);
			store.connect(host, username, password);
			Folder[] folders = store.getPersonalNamespaces();
			Folder folder = store.getFolder(folderName);
			folder.open(Folder.READ_WRITE);
			return folder;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		}
	}

	public static boolean isValid(CMSSignedData signedData) {
		try {
			SignerInformationStore signers = signedData.getSignerInfos();
			Iterator<SignerInformation> it = signers.getSigners().iterator();
			boolean verify = false;
			while (it.hasNext()) {
				SignerInformation signer = it.next();
				org.bouncycastle.util.Store store = signedData.getCertificates();
				Collection certCollection = store.getMatches(signer.getSID());
				Iterator certIt = certCollection.iterator();
				X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
				X509Certificate certificate = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(certHolder);
				verify = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(PROVIDER_NAME).build(certificate));
				/*
				 * SignerInformation signer = it.next();
				 * // String certPath = "ISSUE.crt";
				 * String certPath = "outlook.base64.encoded.cer";
				 * verify = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(X509CertUtil.readX509Certificate(certPath)));
				 */
			}
			return verify;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		}
	}

	public void moveDeleteMessage(String sourceMailFolder, String toMailFolder, Message msg) throws MessagingException {
		Folder sourceFolder = getFolder(sourceMailFolder);
		Folder toFolder = getFolder(toMailFolder);
		// Move message
		if (null != msg) {
			Message[] needCopyMsgs = new Message[1];
			needCopyMsgs[0] = msg;
			// Copy the msg to the specific folder
			sourceFolder.copyMessages(needCopyMsgs, toFolder);
			// delete the original msg
			msg.setFlag(Flags.Flag.DELETED, true);
			System.out.println("Move and delete the message successfully!");
		}
		if (sourceFolder != null && sourceFolder.isOpen()) {
			sourceFolder.close(true);
		}
		if (toFolder != null && toFolder.isOpen()) {
			toFolder.close(true);
		}
	}

	public MailMessage getMailMsg(Message msg) throws IOException, MessagingException {
		MailMessage mailMsg = new MailMessage();

		Address[] from = msg.getFrom();
		Address[] to = msg.getRecipients(RecipientType.TO);
		Address[] cc = msg.getRecipients(RecipientType.CC);
		Address[] bcc = msg.getRecipients(RecipientType.BCC);
		String subject = msg.getSubject();
		Date sendDate = msg.getSentDate();

		mailMsg.setFrom(from);
		mailMsg.setTo(to);
		mailMsg.setCc(cc);
		mailMsg.setBcc(bcc);
		mailMsg.setSubject(subject);
		mailMsg.setSendDate(sendDate);


		// 获取邮件的内容, 就一个大包裹, MultiPart包含所有邮件内容(正文+附件)
		Multipart multipart = (Multipart) msg.getContent();
		System.out.println("邮件共有" + multipart.getCount() + "部分组成");
		// 依次处理各个部分
		for (int i = 0, n = multipart.getCount(); i < n; i++) {
			System.out.println("处理第" + i + "部分");
			// 解包, 取出 MultiPart的各个部分, 每部分可能是邮件内容,也可能是另一个小包裹(MultipPart)
			Part part = multipart.getBodyPart(i);
			// 判断此包裹内容是不是一个小包裹, 一般这一部分是 正文 Content-Type: multipart/alternative
			if (part.getContent() instanceof Multipart) {
				// 转成小包裹
				Multipart p = (Multipart) part.getContent();
				System.out.println("小包裹中有" + p.getCount() + "部分");
				// 列出小包裹中所有内容
				for (int k = 0; k < p.getCount(); k++) {
					System.out.println("小包裹内容:" + p.getBodyPart(k).getContent());
					System.out.println("内容类型:" + p.getBodyPart(k).getContentType());
					if (p.getBodyPart(k).getContentType().startsWith("text/plain")) {
						// 处理文本正文
						mailMsg.setContent(p.getBodyPart(k).getContent().toString());
					} else {
						// 处理 HTML 正文
						mailMsg.setContent(p.getBodyPart(k).getContent().toString());
					}
				}
			}
			// Content-Disposition: attachment; filename="String2Java.jpg"
			// 处理是否为附件信息
			String disposition = part.getDisposition();
			if (disposition != null) {
				System.out.println("发现附件: " + part.getFileName());
				System.out.println("内容类型: " + part.getContentType());
				System.out.println("附件内容:" + part.getContent());
				java.io.InputStream in = part.getInputStream();
				// 打开附件的输入流
				// 读取附件字节并存储到文件中
				java.io.FileOutputStream out = new FileOutputStream(part.getFileName());
				int data;
				while ((data = in.read()) != -1) {
					out.write(data);
				}

				in.close();
				out.close();
			}
		}
		return mailMsg;
	}
}
