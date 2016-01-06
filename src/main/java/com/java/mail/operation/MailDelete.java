package com.java.mail.operation;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class MailDelete {
	public static void main(String args[]) {
		MailDelete mailDelete = new MailDelete();
		try {
			mailDelete.deleteMail();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteMail() throws MessagingException {
		Folder folder = getFolder("INBOX");
		Message message[] = folder.getMessages();
		for (int i = 0; i < message.length; i++) {
			message[i].setFlag(Flags.Flag.DELETED, true);
		}
		folder.close(true);
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
}
