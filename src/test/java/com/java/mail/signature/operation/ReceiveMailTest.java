package com.java.mail.signature.operation;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.java.mail.domain.MailMessage;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ReceiveMailTest {

	@Before
	public void setUp() {
	}

	/**
	 * {@link com.java.mail.signature.operation.ReceiveMail#convertJsonToMap(JSONObject)}
	 */
	@Test
	public void testConvertJsonToMap() {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("host", "webmail.hp.com");
		paramMap.put("port", "993");
		paramMap.put("auth", "true");
		paramMap.put("protocol", "imaps");
		paramMap.put("username", "tao.zhong@hpe.com");
		paramMap.put("password", "Cisco01!");
		paramMap.put("proxySet", "true");
		paramMap.put("proxyHost", "");
		paramMap.put("proxyPort", "");
		JSONArray paramJson = JSONArray.fromObject(paramMap);

		JSONObject jsonObject = JSONObject.fromObject(paramJson.toString().substring(1, paramJson.toString().length() - 1));

		Map<String, String> resultMap = ReceiveMail.convertJsonToMap(jsonObject);
		Assert.assertEquals(paramMap, resultMap);
	}

	/**
	 * {@link com.java.mail.signature.operation.ReceiveMail#processMsg(Message[], Folder, String)}
	 * 
	 * @throws MessagingException
	 * @throws CMSException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws OperatorCreationException
	 * @throws Exception
	 */
	@Test
	public void testProcessMsgSimpleMailWithoutAttachment() throws MessagingException, OperatorCreationException, CertificateException, IOException, CMSException {
		Session session = Session.getDefaultInstance(new Properties(), null);
		Address from = new InternetAddress("from");
		Address to = new InternetAddress("to");

		MimeMessage message = new MimeMessage(session);
		message.setFrom(from);
		message.setRecipient(Message.RecipientType.TO, to);
		message.setSubject("subject");
		message.setContent("content", "text/plain");
		message.saveChanges();

		Message[] messages = new Message[1];
		messages[0] = message;

		ReceiveMail receive = new ReceiveMail("host", "port", "true", "protocol", "username", "password", "true", "proxyHost", "proxyPort");
		List<MailMessage> msgList = receive.processMsg(messages, null, "toFolderName");
		Assert.assertEquals("subject", msgList.get(0).getSubject());
		Assert.assertEquals("content", msgList.get(0).getContent());
		Assert.assertEquals("text/plain; charset=us-ascii", msgList.get(0).getContentType());
	}

	/**
	 * {@link com.java.mail.signature.operation.ReceiveMail#processMsg(Message[], Folder, String)}
	 * 
	 * @throws MessagingException
	 * @throws CMSException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws OperatorCreationException
	 * @throws Exception
	 */
	@Test
	public void testProcessMsgSimpleMailWithAttachment() throws MessagingException, OperatorCreationException, CertificateException, IOException, CMSException {
		Session session = Session.getDefaultInstance(new Properties(), null);
		Address from = new InternetAddress("from");
		Address to = new InternetAddress("to");

		MimeMessage message = new MimeMessage(session);
		message.setFrom(from);
		message.setRecipient(Message.RecipientType.TO, to);
		message.setSubject("subject");

		Multipart multiPart = new MimeMultipart();
		BodyPart bodyPart = new MimeBodyPart();
		bodyPart.setContent("attachment content", "multipart/mixed");
		multiPart.addBodyPart(bodyPart);

		message.setContent(multiPart);

		Message[] messages = new Message[1];
		messages[0] = message;

		ReceiveMail receive = new ReceiveMail("host", "port", "true", "protocol", "username", "password", "true", "proxyHost", "proxyPort");
		List<MailMessage> msgList = receive.processMsg(messages, null, "toFolderName");
		Assert.assertEquals("subject", msgList.get(0).getSubject());
		Assert.assertEquals("attachment content", msgList.get(0).getContent());
		Assert.assertEquals("text/plain", msgList.get(0).getContentType());
	}

}
