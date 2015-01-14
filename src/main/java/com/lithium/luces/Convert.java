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

enum IndexAnalysis {
	NO, ANALYZED, NOT_ANALYZED, NOT_ANALYZED_NO_NORMS, ANALYZED_NO_NORMS
}
enum Stored {
	YES, NO
}

/**
 * @author Brian Harrington
 */
public class Convert {

	public static String documentToJSON(Document doc) {
		// convert to a more friendly, controllable object
		LucDoc3_6_1 doc361 = new LucDoc3_6_1.LucDocBuilder("name", "Value", IndexAnalysis.ANALYZED, Stored.YES).build();


		// then serialize
		return "";
	}


}
