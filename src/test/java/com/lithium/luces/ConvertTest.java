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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import junit.framework.TestCase;

/**
 * @author Brian Harrington
 */
public class ConvertTest extends TestCase{

	public void testConvertOneField() {
		Document doc = new Document();
		doc.add(new Field("Trogdor", "Homestar", Field.Store.NO,
				Field.Index.NOT_ANALYZED));
		String json = Convert.documentToJSON(doc, true);
		System.out.println(json);
		assertNotNull(json);
	}

}
