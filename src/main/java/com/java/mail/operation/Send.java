package com.java.mail.operation;

import java.util.Date;
import java.util.Properties;

import com.java.mail.domain.MailSendInfo;

public class Send {
	public static void main(String[] args) {
		// Initialize
		String username = "smart";
		String password = "smart";
		String smtpServer = "localhost";
		String sendingProtocol = "smtp";
		String[] toAddresses = new String[1];
		toAddresses[0] = "smart@localhost";
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
}
