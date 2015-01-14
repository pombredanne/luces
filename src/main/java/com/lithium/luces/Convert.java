/*
 * Convert.java
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



/**
 * Utility class for converting Lucene Documents to a JSON format for consumption by Elasticsearch
 * @author Brian Harrington
 */
public class Convert {

	public static String documentToJSON(Document doc) {
		return documentToJSON(doc, false);
	}
	public static String documentToJSON(Document doc, boolean prettyPrint) {
		LucDoc3_6_1 doc361 = new LucDoc3_6_1.LucDocBuilder(doc).build();

		Gson gson = prettyPrint ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
		return gson.toJson(doc361);
	}


}
