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

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;

/**
 * @author Brian Harrington
 */

public class LucDoc3_6_1 {
	private String name;
	private String value;
	private Index index;
	private Store stored;
	private List<Fieldable> fields;

	private LucDoc3_6_1(LucDocBuilder builder) {
		this.name = builder.name;
		this.value = builder.value;
		this.index = builder.index;
		this.stored = builder.store;
	}
	public static class LucDocBuilder {
		private String name;
		private String value;
		private Index index;
		private Store store;

		public LucDocBuilder (String name, String value, Index index, Store store) {
			this.name = name;
			this.value = value;
			this.index = index;
			this.store = store;
		}



		public LucDoc3_6_1 build() {
			return new LucDoc3_6_1(this);
		}
	}
}