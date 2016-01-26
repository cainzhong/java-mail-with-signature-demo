package com.java.mail.domain;

import java.io.FileOutputStream;

public class Attachment {
	/* an attachment's file for a email. */
	private FileOutputStream fileStream;

	/* an attachment's file name for a email. */
	private String fileName;

	/* an attachment's file type for a email. */
	private String fileType;

	public FileOutputStream getFileStream() {
		return this.fileStream;
	}

	public void setFileStream(FileOutputStream fileStream) {
		this.fileStream = fileStream;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return this.fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
}
