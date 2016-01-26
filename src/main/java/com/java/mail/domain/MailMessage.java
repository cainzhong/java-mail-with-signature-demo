package com.java.mail.domain;

import java.util.Date;
import java.util.List;

import javax.mail.Address;

public class MailMessage {
	/* the address of email sender. */
	private Address[] from;

	/* the address of email receiver. */
	private Address[] to;

	/* the email's CC address. */
	private Address[] cc;

	/* the email's BCC address. */
	private Address[] bcc;

	/* a email's subject. */
	private String subject;

	/* a email's content */
	private String content;

	/* the date of sending a email. */
	private Date sendDate;

	/* a list of attachments */
	private List<Attachment> attachList;

	/* verify the email has a digital signature or not */
	private boolean hasSignature;

	/* verify the signature is valid and trusted or not */
	private boolean signaturePassed;

	private String contentType;

	public Address[] getFrom() {
		return this.from;
	}

	public void setFrom(Address[] from) {
		this.from = from;
	}

	public Address[] getTo() {
		return this.to;
	}

	public void setTo(Address[] to) {
		this.to = to;
	}

	public Address[] getCc() {
		return this.cc;
	}

	public void setCc(Address[] cc) {
		this.cc = cc;
	}

	public Address[] getBcc() {
		return this.bcc;
	}

	public void setBcc(Address[] bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getSendDate() {
		return this.sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public List<Attachment> getAttachList() {
		return this.attachList;
	}

	public void setAttachList(List<Attachment> attachList) {
		this.attachList = attachList;
	}

	public boolean isHasSignature() {
		return this.hasSignature;
	}

	public void setHasSignature(boolean hasSignature) {
		this.hasSignature = hasSignature;
	}

	public boolean isSignaturePassed() {
		return this.signaturePassed;
	}

	public void setSignaturePassed(boolean signaturePassed) {
		this.signaturePassed = signaturePassed;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
