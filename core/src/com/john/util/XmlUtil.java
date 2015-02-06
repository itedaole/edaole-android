package com.john.util;

import java.io.InputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class XmlUtil {
	private static String PARSE_ERROR = "Problem parsing API response";

	public static HashMap<String, String> parse(InputStream result) throws ParseException {
		HashMap<String, String>  data = new HashMap<String, String>();
		
		final XmlPullParser parser = Xml.newPullParser();
		
		try {
		    parser.setInput(result, "UTF-8");
			int type;
			String tag = null;
			
			while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
				switch (type) {
				case XmlPullParser.START_TAG:
					if (!parser.getName().equals("root")) {			
						tag = parser.getName().trim();						
					}
					break;
				case XmlPullParser.TEXT:
					if (tag != null){						
						data.put(tag, parser.getText().trim());
					}
					break;
				case XmlPullParser.END_TAG:									
					tag = null;					
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			throw new ParseException(PARSE_ERROR, e);
		}
		
		return data;
	}

	/**
	 * Thrown when there were problems parsing the response to an API call,
	 * either because the response was empty, or it was malformed.
	 */
	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}
	}

}
