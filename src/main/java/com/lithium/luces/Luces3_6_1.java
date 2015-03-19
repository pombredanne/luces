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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;


/**
 * Utility class for converting Lucene Documents to a JSON format for consumption by Elasticsearch
 * @author Brian Harrington
 */
public class Luces3_6_1 implements Luces {

	@SuppressWarnings("unused")
	public Luces3_6_1() {
		// check version?
		Document doc = new Document(); // discard after check
	}

	public String documentToJSONStringified(Document doc, boolean setPrettyPrint) {
		Gson gson = setPrettyPrint ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
		return gson.toJson(documentToJSON(doc));
	}

	public JsonElement documentToJSON(Document doc) {
		HashMap<String, Object> fields = new LinkedHashMap<>();
		List<Fieldable> docFields = doc.getFields();
		for (Fieldable field : docFields) {
			fields.put(field.name(), field.stringValue());
		}
		return new Gson().toJsonTree(fields);
	}


}
