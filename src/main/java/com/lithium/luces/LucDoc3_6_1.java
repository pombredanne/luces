/*
 * LucDoc3_6_1.java
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

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;

/**
 * @author Brian Harrington
 */

public class LucDoc3_6_1 {
	private final float boost;
	private final List<Fieldable> fields;

	private LucDoc3_6_1(LucDocBuilder builder) {
		this.boost = builder.boost;
		this.fields = builder.fields;
	}
	public static class LucDocBuilder {
		private float boost;
		private List<Fieldable> fields;

		public LucDocBuilder (Document doc) {
			this.boost = doc.getBoost();
			this.fields = doc.getFields();
		}



		public LucDoc3_6_1 build() {
			return new LucDoc3_6_1(this);
		}
	}
//	public static class MockUserDocumentBuilder {
//
//	}
}