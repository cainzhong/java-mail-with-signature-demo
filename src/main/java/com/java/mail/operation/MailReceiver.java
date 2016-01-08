package com.java.mail.operation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.apache.commons.lang.StringUtils;

import com.sun.mail.imap.IMAPFolder;

/**
 * Mail Receiver, call sequence: <code>open-> receive-> close</code>
 * 
 * @author zhontao
 *
 */
public class MailReceiver {

	public static void main(String args[]) throws MessagingException, UnsupportedEncodingException, IOException {
		String host = "webmail.hp.com";
		boolean auth = true;
//		 you must use SSL for incoming mail, SSL ports are 993 for IMAP and 995 for POP3
		// String receivingProtocol = "pop3";
		// SSL
//		String receivingProtocol = "pop3s";
		String port = "993";
		 String receivingProtocol = "imaps";
		String username = "tao.zhong@hpe.com";
		String password = "Cisco01!";
		String attachSavePath = "C:\\saveMail";

		MailReceiver mailReceiver = new MailReceiver(host, port, auth, receivingProtocol, username, password, attachSavePath);
		mailReceiver.open();
		List<Message> msgList = mailReceiver.receive();
		if (msgList.size() == 0) {
			System.out.println("No message!");
		}
		mailReceiver.close();
	}

	private String host;
	private String port;
	private boolean auth;
	private String receivingProtocol;
	private String username;
	private String password;

	/* Storage directory for the attachment. */
	private String attachSavePath;

	private Store store;
	
	/*POP3Folder can only receive the mails in 'INBOX', IMAPFolder can receive the mails in all folders which created by user.*/
	private IMAPFolder inbox;
//	private POP3Folder inbox;
	
	private FetchProfile profile;

	/**
	 * Constructor
	 * 
	 * @param host
	 *            Mail Server Host
	 * @param port
	 *            Mail Server Port
	 * @param auth
	 *            a flag to define whether the email server needs authentication
	 * @param receivingProtocol
	 * @param username
	 *            User Name
	 * @param password
	 *            Password
	 * @param attachSavePath
	 *            save path of attachment, null means that will not save
	 *            attachment
	 */
	public MailReceiver(String host, String port, boolean auth, String receivingProtocol, String username, String password, String attachSavePath) {
		this.host = host;
		this.port = port;
		this.auth = auth;
		this.receivingProtocol = receivingProtocol;
		this.username = username;
		this.password = password;
		this.attachSavePath = attachSavePath;
	}

	/**
	 * Connect to the mail server, open INBOX
	 * 
	 * @throws MessagingException
	 */
	public void open() throws MessagingException {
		Properties props = getProperties();
		Session session = Session.getDefaultInstance(props, null);
		
		// URLName urln = new URLName("pop3","webmail.hp.com",995,null, this.username, this.password);
		// this.store = session.getStore(urln);
		this.store = session.getStore(this.receivingProtocol);
		store.connect(this.host, this.username, this.password);

		this.inbox = (IMAPFolder) store.getFolder("To or Cc me");
//		this.inbox = (POP3Folder) store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);

		this.profile = new FetchProfile();
		profile.add(UIDFolder.FetchProfileItem.UID);
		profile.add(FetchProfile.Item.ENVELOPE);
	}

	/**
	 * Receive emails and save the attachment to specific directory.
	 * 
	 * @return
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public List<Message> receive() throws MessagingException, UnsupportedEncodingException, IOException {
		SearchTerm st = new OrTerm(new FromStringTerm("tao.zhong@hpe.com"), new SubjectTerm("Sign Mail Test"));
		Message[] messages = this.inbox.search(st);
		int mailCounts = messages.length;
		System.out.println("Found: " + mailCounts + " mails matched with the condition.");

		// Get emails and UID
		// Get all the messages from inbox.
		// Message[] messages = inbox.getMessages();
		// inbox.fetch(messages, profile);
		
		List<Message> msgList = new ArrayList<Message>();
		int i = 1;
		for (Message msg : messages) {

			// print out details of each message
			System.out.println("******** Mail " + i + " ********");
			System.out.println("Sent Date: " + msg.getSentDate().toLocaleString());
			System.out.println("From: " + ((InternetAddress) msg.getFrom()[0]).getAddress());
			System.out.println("Subject: " + msg.getSubject());

			// Save attachment
			String contentType = msg.getContentType();
			if (contentType.contains("multipart")) {
				MimeMultipart multi = (MimeMultipart) msg.getContent();
				System.out.println("Multipart Message: " + multi.getBodyPart(0).getContent());
			} else if (contentType.contains("application/pkcs7-mime") || contentType.contains("application/x-pkcs7-mime")) {
				System.out.println("Enveloped Message: " + msg.getContent());
			} else {
				System.out.println("Simple Message: " + msg.getContent());
			}
			if (StringUtils.isNotEmpty(attachSavePath) && contentType.contains("multipart")) {
				saveAttachment(msg);
			}
			msgList.add(msg);
			i++;
		}
		return msgList;
	}

	/**
	 * Close the INBOX and connection.
	 */
	public void close() {
		try {
			inbox.close(false);
			store.close();
		} catch (MessagingException e) {
			// throw new MailException("关闭INBOX及连接出错", e);
		}
	}

	/**
	 * Save attachment
	 * 
	 * @throws Exception
	 * @throws IOException
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void saveAttachment(Part part) throws UnsupportedEncodingException, MessagingException, IOException {
		String filename = "";
		Multipart multiPart = (Multipart) part.getContent();
		for (int i = 0; i < multiPart.getCount(); i++) {
			BodyPart bodyPart = multiPart.getBodyPart(i);
			String disposition = bodyPart.getDisposition();
			if ((disposition != null) && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
				int size = bodyPart.getSize();
				System.out.println("bodyPart Size: " + size);
				filename = bodyPart.getFileName();
				String prefix = filename.substring(0, filename.lastIndexOf(".") + 1);
				String suffix = filename.substring(filename.lastIndexOf(".") + 1);
				filename = prefix + Calendar.getInstance().getTimeInMillis() + "." + suffix;
				System.out.println("\t Attachments: " + filename);
				saveFile(filename, bodyPart.getInputStream());
			}
		}
	}

	/**
	 * Save file to specific directory.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void saveFile(String filename, InputStream in) throws FileNotFoundException, IOException {
		File file = new File(attachSavePath + File.separator + filename);
		if (!file.exists()) {
			// FileCopyUtils.copy(in, new FileOutputStream(file));
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(file));
				byte[] buf = new byte[8192];
				int len;
				while ((len = in.read(buf)) > 0)
					out.write(buf, 0, len);
			} finally {
				// close streams
				try {
					if (in != null)
						in.close();
				} catch (IOException ex) {
				}
				try {
					if (out != null)
						out.close();
				} catch (IOException ex) {
				}
			}
		}
	}

	private Properties getProperties() {
		Properties props = new Properties();
		props.put("mail.smtp.host", this.host);
		props.put("mail.smtp.port", this.port);
		props.put("mail.smtp.auth", this.auth);
		props.put("mail.store.protocol", this.receivingProtocol);

		// To support SSL: 1. using protocol pop3s; 2 using code like below.
		// Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		// final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		// props.put("mail.pop3.socketFactory.class", SSL_FACTORY);
		// props.put("mail.pop3.socketFactory.fallback", "false");
		// props.put("mail.pop3.port", "995");
		// props.put("mail.pop3.socketFactory.port", "995");

		return props;
	}
}