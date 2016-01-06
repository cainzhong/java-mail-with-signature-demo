package com.java.mail.operation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;

import com.sun.mail.pop3.POP3Folder;

/**
 * Mail Receiver, call sequence: <code>open-> receive-> close</code>
 * 
 * @author zhontao
 *
 */
public class MailReceiver {
	public static final String RECEIVING_PROTOCOL = "pop3";
	public static final String INBOX = "INBOX";

	private String host;
	private String port;
	private boolean auth;
	private String receivingProtocol;
	private String username;
	private String password;

	/* Storage directory after downloading the attachment. */
	private String attachSavePath;

	private Store store;
	private POP3Folder inbox;
	private FetchProfile profile;

	/**
	 * Constructor(Will not save attachment)
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
	 */
	public MailReceiver(String host, String port, boolean auth, String receivingProtocol, String username, String password) {
		this(host, port, auth, receivingProtocol, username, password, null);
	}

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
		Properties props = new Properties();
		// smtp server
		props.put("mail.smtp.host", this.host);
		// smtp port
		props.put("mail.smtp.port", this.port);
		props.put("mail.smtp.auth", this.auth);
		// protocol of receiving email
		props.put("mail.store.protocol", this.receivingProtocol);

		Session session = Session.getDefaultInstance(props, null);

		this.store = session.getStore(receivingProtocol);
		store.connect(this.host, this.username, this.password);

		this.inbox = (POP3Folder) store.getFolder(INBOX);
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
		// Get emails and UID
		Message[] messages = inbox.getMessages();
		inbox.fetch(messages, profile);

		List<Message> msgList = new ArrayList<Message>();
		int i = 1;
		for (Message msg : messages) {

			// print out details of each message
			System.out.println("******** Mail " + i + " ********");
			System.out.println("Sent Date: " + msg.getSentDate().toLocaleString());
			System.out.println("From: " + ((InternetAddress) msg.getFrom()[0]).getAddress());
			System.out.println("Subject: " + msg.getSubject());
			System.out.println("Message: " + msg.getContent());

			// Save attachment
			String contentType = msg.getContentType();
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
}