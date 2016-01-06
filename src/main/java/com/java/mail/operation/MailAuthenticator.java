package com.java.mail.operation;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * @author zhontao
 *
 */
public class MailAuthenticator extends Authenticator {

	private String username;
	private String password;

	public MailAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
