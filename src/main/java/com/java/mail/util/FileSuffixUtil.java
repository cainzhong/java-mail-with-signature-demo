package com.java.mail.util;

import java.util.HashMap;
import java.util.Map;

public final class FileSuffixUtil {
	public final static String FILE_SUFFIX_TXT = "TXT";

	private static Map<String, String> fileSuffixMap = new HashMap<String, String>();

	static {
		fileSuffixMap.put(FILE_SUFFIX_TXT, FILE_SUFFIX_TXT);
	}

	public static boolean validateFileSuffix(String suffix) {
		if (fileSuffixMap.containsValue(suffix)) {
			return true;
		} else {
			return false;
		}
	}
}
