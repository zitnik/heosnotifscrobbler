package com.github.zitnik.heosnotifscrobbler;

public class Utils {
	public static String capitalizeWords(String a){
		StringBuilder sb = new StringBuilder(a.length());
		boolean wordStart = true;
		for (int i = 0; i < a.length(); i++) {
			int c = a.codePointAt(i);
			sb.appendCodePoint(wordStart ? Character.toUpperCase(c) : Character.toLowerCase(c));
			wordStart = !Character.isLetter(c);
		}
		return sb.toString();
	}
}
