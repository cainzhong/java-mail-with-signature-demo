package com.java.mail.signature.operation;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

public class ReceiveMailEWS {
	public static void main(String args[]) throws MessagingException {
		String host = "webmail.hp.com";
		String auth = "true";
		String username = "cainzhong@outlook.com";
		String password = "Cisco01!";

		String fromStringTerm = "tao.zhong@hpe.com";
		String subjectTerm = "Simple Mail without attachment";
		// String subjectTerm = "Simple Mail with attachment";
		// String subjectTerm = "Sign Mail without attachment";
		// String subjectTerm = "Sign Mail with attachment";

		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", auth);

		// Initalize a session with no properties
		// Can also be initalized with System.getProperties();
		Session session = Session.getDefaultInstance(new Properties());

		// Get the EWS store implementation
		Store store = session.getStore("ewsstore");

		// Connect to the Exchange server - No port required.
		// Also connect() might be used if the session is initalized with the known mail.* properties
		store.connect("outlook.office365.com", username, password);

		Folder folder = store.getDefaultFolder();
		folder.open(Folder.READ_ONLY);

		// Message[] messages = folder.getMessages();

		SearchTerm st = new OrTerm(new FromStringTerm(fromStringTerm), new SubjectTerm(subjectTerm));
		Message[] messages = folder.search(st);

		System.out.println("Found: " + messages.length + " mails matched with the condition.");
	}

}
