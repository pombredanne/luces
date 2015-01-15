/*
 * ConvertTest.java
 * Created on Jan 13, 2015
 *
 * Copyright 2015 Lithium Technologies, Inc.
 * San Francisco, California, U.S.A.  All Rights Reserved.
 *
 * This software is the  confidential and proprietary information
 * of  Lithium  Technologies,  Inc.  ("Confidential Information")
 * You shall not disclose such Confidential Information and shall
 * use  it  only in  accordance  with  the terms of  the  license
 * agreement you entered into with Lithium.
 */

package com.lithium.luces;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import junit.framework.TestCase;

/**
 * @author Brian Harrington
 */
public class ConvertTest extends TestCase{

	public void testConvertOneField() {
		Document doc = createMockUserDocument();
		String json = Convert.documentToJSON(doc, true);
		System.out.println(json);
		assertNotNull(json);
	}

	private Document createMockUserDocument() {
		final String login = "Brian";
		final String email = "brian@acme.com";
		final int ranking = 20;
		final String rank_name = "Frequent Visitor";
		final Calendar reg_date = new GregorianCalendar(2014, 12, 23, 13, 24, 56);
		final Calendar visit_date = new GregorianCalendar(2015, 1, 14, 11, 24, 56);
		final String name_first = "Brian";
		final String name_last = "Harrington";
		final boolean user_email_verified = true;
		final String user_last_visit_ipaddress = "12.34.56.78";
		final String privacy = "none"; // try enum?
		final String roles = "1 2 13"; // in lucene, this is a space-delimited list of numbers
		final String posts = "000000";
		final int kudos_received = 34;
		final int kudos_given = 42;
		final String public_image_upload_count = "0000000001";

		Document doc = new Document();

		// readUsers
		doc.add(new Field("login", login.toLowerCase(), Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field("login_sort", login.toLowerCase(), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("*login*", login.toLowerCase(), Store.NO, Index.ANALYZED));
		doc.add(new Field("email", email.toLowerCase(), Store.NO, Index.ANALYZED));
		doc.add(new Field("email_sort", email.toLowerCase(), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("ranking", "" + ranking, Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("rank_name", rank_name, Store.YES, Index.ANALYZED));
		doc.add(new Field("rank_name_sort", rank_name.toLowerCase(), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("rank_name_not_analyzed", rank_name, Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field("all", "all", Store.NO, Index.NOT_ANALYZED));
		// dates for registration
		addDateFieldToDocument("reg_date", doc, reg_date.getTime());
		// dates for last visit
		addDateFieldToDocument("visit_date", doc, visit_date.getTime());

		// readUserProfiles
		doc.add(new Field("name_first", name_first, Store.NO, Index.ANALYZED));
		doc.add(new Field("name_last", name_last, Store.NO, Index.ANALYZED));
		doc.add(new Field("user.email_verified", String.valueOf(user_email_verified), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("user.last_visit_ipaddress", user_last_visit_ipaddress, Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("privacy", privacy, Store.NO, Index.NOT_ANALYZED));

		// readUserRoles
		doc.add(new Field("roles", roles, Store.NO, Index.ANALYZED));

		// readUserMetrics
		doc.add(new Field("posts", posts, Store.NO, Index.NOT_ANALYZED));
		// TODO: kudos given, received, image upload count
		doc.add(new Field("kudos_given", String.valueOf(kudos_given), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("kudos_received", String.valueOf(kudos_received), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("public_image_upload_count", public_image_upload_count, Store.NO, Index.NOT_ANALYZED));

		return doc;
	}

	private static final String MILLISECONDS = ".ms";
	private static final String SECONDS = ""; // sorting defaults to seconds
	private static final String MINUTES = ".minutes";
	private static final String HOURS = ".hours";
	private static final String DAYS = ".days";
	private static final String YEARS = ".years";
	private static long MILLISEC_IN_SECOND = 1000L;
	private static long MILLISEC_IN_MINUTE = MILLISEC_IN_SECOND * 60;
	private static long MILLISEC_IN_HOUR = MILLISEC_IN_MINUTE * 60;
	private static long MILLISEC_IN_DAY = MILLISEC_IN_HOUR * 24;
	private static long MILLISEC_IN_YEAR = MILLISEC_IN_DAY* 365;

	private void addDateFieldToDocument(String fieldName, Document doc, Date date) {
		String[] modifiers = {MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS, YEARS};
		if (date != null) {
			for (String element : modifiers) {
				doc.add((new Field(fieldName + element, String.valueOf(getDateToUnit(date, element)), Store.NO,
						Index.NOT_ANALYZED)));
			}
		}
	}
	private long getDateToUnit(Date date, String modifier) {
		long value = date.getTime();
		switch (modifier) {
			case SECONDS:
				value = value/ MILLISEC_IN_SECOND;
				break;
			case MINUTES:
				value = value/ MILLISEC_IN_MINUTE;
				break;
			case HOURS:
				value = value / MILLISEC_IN_HOUR;
				break;
			case DAYS:
				value = value / MILLISEC_IN_DAY;
				break;
			case YEARS:
				value = value / MILLISEC_IN_YEAR;
				break;
		}
		return value;
	}

}
