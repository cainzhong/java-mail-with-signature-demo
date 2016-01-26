package com.java.mail.signature.operation;

import java.net.URI;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

public class ReceiveMailEWS1 {
	public static void main(String args[]) throws Exception {
		ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
		ExchangeCredentials credentials = new WebCredentials("cainzhong@cainzhong.win", "Cisco01!");
		service.setCredentials(credentials);
		try {
			service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
			EmailMessage msg = new EmailMessage(service);
			msg.setSubject("Hello world!");
			msg.setBody(MessageBody.getMessageBodyFromText("Sent using the EWS Java API."));
			msg.getToRecipients().add("cainzhong@cainzhong.win");
			msg.send();
			System.out.println("end");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * FindFoldersResults findResults = service.findFolders(WellKnownFolderName.Inbox, new FolderView(Integer.MAX_VALUE));
		 * 
		 * for (Folder folder : findResults.getFolders()) {
		 * System.out.println("Count======" + folder.getChildFolderCount());
		 * System.out.println("Name=======" + folder.getDisplayName());
		 * }
		 * 
		 * Folder folder = new Folder(service);
		 * 
		 * ItemView view = new ItemView(10);
		 * FindItemsResults<Item> findResults1 = service.findItems(folder.getId(), view);
		 * 
		 * for (Item item : findResults1.getItems()) {
		 * // Do something with the item as shown
		 * System.out.println("id==========" + item.getId());
		 * System.out.println("sub==========" + item.getSubject());
		 * }
		 */

	}
}
