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

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
		String valid = "{\n" +
				"  \"login\": \"trogdor\",\n" +
				"  \"name_first\": \"Joe\",\n" +
				"  \"name_last\": \"Schmo\",\n" +
				"  \"email\": \"homestar@runner.com\",\n" +
				"  \"signup\": \"12/23/2014\",\n" +
				"  \"gender\": \"male\"\n" +
				"}";
		Document doc = createMockFlatUserDocument();
		String json = Convert.documentToJSON(doc, true);
		System.out.println(json);
		assertEquals(valid, json);
	}

	private Document createMockFlatUserDocument() {
		final String login = "Trogdor";
		final String email = "homestar@runner.com";
		final Calendar reg_date = new GregorianCalendar(2014, Calendar.DECEMBER, 23, 13, 24, 56);
		final String name_first = "Joe";
		final String name_last = "Schmo";
		final String gender = "male";
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		Document doc = new Document();

		doc.add(new Field("login", login.toLowerCase(), Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field("name_first", name_first, Store.NO, Index.ANALYZED));
		doc.add(new Field("name_last", name_last, Store.NO, Index.ANALYZED));
		doc.add(new Field("email", email.toLowerCase(), Store.NO, Index.ANALYZED));
		doc.add(new Field("signup", sdf.format(reg_date.getTime()), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("gender", gender, Store.NO, Index.ANALYZED));
		return doc;
	}
}
