package com.java.mail.domain;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;

import com.java.mail.operation.MailAuthenticator;

/**
 * Basic Information of sending a email.
 * 
 * @author zhontao
 * 
 */
public class MailSendInfo {
	/* the email server's IP. */
	private String host;

	/* the email server's port. */
	private String port = "25";

	/* the username of logining in email server. */
	private String userName;

	/* the password of logining in email server. */
	private String password;

	/* a flag to define whether the email server needs authentication. */
	private boolean validate = false;

	/* a email's subject. */
	private String subject;

	/* a email's content. */
	private String content;

	/* an attachment's file path for a email. */
	private String[] attachFilePath;

	/* the date of sending a email. */
	private Date sendDate;

	/* the address of email sender. */
	private String fromAddress;

	/* the address of email receiver. */
	private String[] toAddresses;

	/* the email's CC address. */
	private String[] ccAddresses;

	/* the email's BCC address. */
	private String[] bccAddresses;

	/* a email's MIME type. */
	private String mimeType;

	/* protocol of sending email. */
	private String sendingProtocol;

	/**
	 * Get the properties of a email session.
	 * 
	 * @return
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		props.put("mail.smtp.host", this.host);
		props.put("mail.smtp.port", this.port);
		props.put("mail.smtp.auth", validate ? "true" : "false");
		if(this.sendingProtocol != null || !this.sendingProtocol.isEmpty()){
			props.put("mail.transport.protocol", this.sendingProtocol);
		}

		return props;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String[] getAttachFilePath() {
		return attachFilePath;
	}

	public void setAttachFilePath(String[] attachFilePath) {
		this.attachFilePath = attachFilePath;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String[] getToAddresses() {
		return toAddresses;
	}

	public void setToAddresses(String[] toAddresses) {
		this.toAddresses = toAddresses;
	}

	public String[] getCcAddresses() {
		return ccAddresses;
	}

	public void setCcAddresses(String[] ccAddresses) {
		this.ccAddresses = ccAddresses;
	}

	public String[] getBccAddresses() {
		return bccAddresses;
	}

	public void setBccAddresses(String[] bccAddresses) {
		this.bccAddresses = bccAddresses;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getSendingProtocol() {
		return sendingProtocol;
	}

	public void setSendingProtocol(String sendingProtocol) {
		this.sendingProtocol = sendingProtocol;
	}
}