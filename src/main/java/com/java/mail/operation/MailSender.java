package com.java.mail.operation;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.java.mail.domain.MailSendInfo;
import com.java.mail.util.FileSuffixUtil;

/**
 * A class which can send 2 kinds of email.
 * <p>
 * 1. Text
 * </p>
 * <p>
 * 2. With attachment
 * </p>
 * 
 * @author zhontao
 * 
 */
public class MailSender {
	
	public static void main(String[] args) {
		// Initialize
		String username = "tao.zhong@hpe.com";
		String password = "Cisco01!";
		String smtpServer = "smtp3.hp.com";
		String sendingProtocol = "smtp";
		String[] toAddresses = new String[1];
		toAddresses[0] = "tao.zhong@hpe.com";
		String subject = "Java Mail Subject";
		String content = "Java Mail Content";
		String[] attachFilePaths = new String[1];
		attachFilePaths[0] = "c:/mailAttachment.txt";
		Date sendDate = new Date();

		MailSendInfo mailInfo = new MailSendInfo();
		mailInfo.setFromAddress(username);
		mailInfo.setHost(smtpServer);
		mailInfo.setPort("25");
		mailInfo.setValidate(true);
		mailInfo.setUserName(username);
		mailInfo.setPassword(password);
		mailInfo.setToAddresses(toAddresses);
		mailInfo.setSubject(subject);
		mailInfo.setContent(content);
		mailInfo.setSendDate(sendDate);
		mailInfo.setAttachFilePath(attachFilePaths);
		mailInfo.setSendingProtocol(sendingProtocol);
		MailSender sender = new MailSender();
		/* send a email with attachment. */
		System.out.println("***************Send a email with attachment.***************");
		mailInfo.setMimeType(MIMEType.TEXT_PLAIN_GBK);
		System.out.println("The email has been sent: " + sender.sendAttachmentMail(mailInfo));
		/*send a email without attachment.*/
		System.out.println("***************Send a email without attachment.***************");
		System.out.println("The email has been sent: " + sender.sendTextMail(mailInfo));
		
	}

	private static Logger logger = Logger.getLogger(MailSender.class);

	/**
	 * Sending mail in text format.
	 * 
	 * @param mailInfo
	 *            email information which will be sent
	 */
	public boolean sendTextMail(MailSendInfo mailInfo) {
		Session sendMailSession = getSendMailSession(mailInfo);
		try {
			Message mailMessage = new MimeMessage(sendMailSession);
			
			mailMessage.setFrom(new InternetAddress(mailInfo.getFromAddress()));
			mailMessage.setSubject(mailInfo.getSubject());
			mailMessage.setSentDate(mailInfo.getSendDate());
			
			mailMessage.setRecipients(Message.RecipientType.TO, getInternetAddresses(mailInfo.getToAddresses()));
			if (mailInfo.getCcAddresses() != null && mailInfo.getCcAddresses().length > 0) {
				mailMessage.setRecipients(Message.RecipientType.CC, getInternetAddresses(mailInfo.getCcAddresses()));
			}
			if (mailInfo.getBccAddresses() != null && mailInfo.getBccAddresses().length > 0) {
				mailMessage.setRecipients(Message.RecipientType.BCC, getInternetAddresses(mailInfo.getBccAddresses()));
			}
			
			mailMessage.setText(mailInfo.getContent());
			// Send a email.
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			logger.error(ex);
		}
		return false;
	}

	/**
	 * Sending mail with attachment.
	 * 
	 * @param mailInfo
	 *            email information which will be sent
	 */
	public boolean sendAttachmentMail(MailSendInfo mailInfo) {
		Session sendMailSession = getSendMailSession(mailInfo);
		MailAuthenticator authenticator = new MailAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
		sendMailSession.setPasswordAuthentication(new URLName(mailInfo.getHost()), authenticator.getPasswordAuthentication());

		try {
			Message mailMessage = new MimeMessage(sendMailSession);

			mailMessage.setFrom(new InternetAddress(mailInfo.getFromAddress()));
			mailMessage.setSubject(mailInfo.getSubject());
			mailMessage.setSentDate(mailInfo.getSendDate());
			
			mailMessage.setRecipients(Message.RecipientType.TO, getInternetAddresses(mailInfo.getToAddresses()));
			if (mailInfo.getCcAddresses() != null && mailInfo.getCcAddresses().length > 0) {
				mailMessage.setRecipients(Message.RecipientType.CC, getInternetAddresses(mailInfo.getCcAddresses()));
			}
			if (mailInfo.getBccAddresses() != null && mailInfo.getBccAddresses().length > 0) {
				mailMessage.setRecipients(Message.RecipientType.BCC, getInternetAddresses(mailInfo.getBccAddresses()));
			}

			// construct the body of email
			Multipart multiPart = createMultipart(mailInfo);
			mailMessage.setContent(multiPart);
			
			// Send a email.
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			logger.error(ex);
		}
		return false;
	}

	/**
	 * Get the session of sending a email.
	 * 
	 * @param mailInfo
	 *            email information which will be sent
	 * @return
	 */
	private Session getSendMailSession(MailSendInfo mailInfo) {
		// Determine whether the identity authentication is needed.
		MailAuthenticator authenticator = null;
		Properties pro = mailInfo.getProperties();
		if (mailInfo.isValidate()) {
			// If need the identity authentication, then create a
			// MailAuthenticator.
			authenticator = new MailAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
		}
		// Construct a sending email session according to the session properties
		// and MailAuthenticator.
		Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
		return sendMailSession;
	}

	/**
	 * Create Multipart for MimeMessage.
	 * 
	 * @param mailInfo
	 * @return
	 * @throws MessagingException
	 */
	private Multipart createMultipart(MailSendInfo mailInfo) throws MessagingException {
		// Class MiniMultipart is a Container Class which contains Object
		// MimeBodyPart.
		Multipart multiPart = new MimeMultipart();
		// Create a MimeBodyPart which contains HTML content.
		BodyPart bodyPart = new MimeBodyPart();

		// content of email
		bodyPart.setContent(mailInfo.getContent(), mailInfo.getMimeType());
		
		// handle the email attachment
		String[] attachFilePaths = mailInfo.getAttachFilePath();
		if (attachFilePaths != null && attachFilePaths.length > 0) {
			for (String attachFileName : attachFilePaths) {
				DataSource source = new FileDataSource(attachFileName);
				String name = source.getName();
				// Validate the file suffix, the illegal file will not
				// be sent.
				String suffix = name.substring(name.lastIndexOf(".") + 1);
				if (FileSuffixUtil.validateFileSuffix(suffix.toUpperCase())) {
					bodyPart.setDataHandler(new DataHandler(source));
					bodyPart.setFileName(name);
					multiPart.addBodyPart(bodyPart);
				}
			}
		}
		return multiPart;
	}

	/**
	 * Format the String type of Address to InternetAddress.
	 * 
	 * @param addresses
	 *            String type of Address
	 * @return InternetAddress type of Address
	 * @throws AddressException
	 */
	private InternetAddress[] getInternetAddresses(String[] addresses) throws AddressException {
		int length = addresses.length;
		InternetAddress[] internetAddresses = new InternetAddress[length];
		if (addresses != null && length > 0) {
			for (int i = 0; i < length; i++) {
				internetAddresses[i] = new InternetAddress(addresses[i]);
			}
		}
		return internetAddresses;
	}
}
