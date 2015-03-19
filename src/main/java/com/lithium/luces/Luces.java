/*
 * Luces.java
 * Created on Jan 28, 2015
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

import com.google.gson.JsonElement;

/**
 * @author Brian Harrington
 */
public interface Luces {

	String documentToJSONStringified(Document doc, boolean setPrettyPrint);
	JsonElement documentToJSON(Document doc);
}
