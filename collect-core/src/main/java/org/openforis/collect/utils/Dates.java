package org.openforis.collect.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class Dates {

	private static final String DATE_TIME_FORMAT 	= "yyyy-MM-dd'T'HH:mm:ss";
	private static final String DATE_FORMAT 		= "yyyy-MM-dd";

	public static Date parseDateTime(String dateTime) {
		if ( StringUtils.isBlank(dateTime) ) {
			return null;
		}
		dateTime = dateTime.trim();
		if ( dateTime.length() == DATE_FORMAT.length() ) {
			return parse(dateTime, DATE_FORMAT);
		} else {
			return parse(dateTime, DATE_TIME_FORMAT);
		}
	}
	
	public static Date parseDate(String date) {
		if ( StringUtils.isBlank(date) ) {
			return null;
		}
		date = date.trim();
		if ( date.length() > DATE_FORMAT.length() ) {
			date = date.substring(0, DATE_FORMAT.length());
		}
		return parse(date, DATE_FORMAT);
	}

	private static Date parse(String dateString, String format) {
		if ( dateString == null ) {
			return null;
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			try {
				return formatter.parse(dateString);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String formatDate(Date dateTime) {
		return format(dateTime, DATE_FORMAT);
	}
	
	public static String formatDateTime(Date dateTime) {
		return format(dateTime, DATE_TIME_FORMAT);
	}

	private static String format(Date dateTime, String format) {
		if ( dateTime == null ) {
			return null;
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			String result = formatter.format(dateTime);
			return result;
		}
	}
	
}
