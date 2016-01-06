package com.java.mail.operation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

public class Receive {
	public static void main(String args[]) throws MessagingException, UnsupportedEncodingException, IOException {
		String host = "localhost";
		String port = "25";
		boolean auth = true;
		String receivingProtocol = "pop3";
		String username = "smart";
		String password = "smart";
		String attachSavePath = "C:\\saveMail";

		// MailReceiver mailReceiver = new MailReceiver(host, port, auth,
		// receivingProtocol, username, password);
		// mailReceiver.open();
		// mailReceiver.receive();
		// mailReceiver.close();

		MailReceiver mailReceiverSaveAttach = new MailReceiver(host, port, auth, receivingProtocol, username, password, attachSavePath);
		mailReceiverSaveAttach.open();
		List<Message> msgList = mailReceiverSaveAttach.receive();
		if (msgList.size() == 0) {
			System.out.println("No message!");
		}
		mailReceiverSaveAttach.close();
	}
}
