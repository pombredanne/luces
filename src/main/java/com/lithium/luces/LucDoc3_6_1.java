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

/**
 * @author Brian Harrington
 */

public class LucDoc3_6_1 {
	private String name;
	private String value;
	private IndexAnalysis indexAnalysis;
	private Stored stored;

	private LucDoc3_6_1(LucDocBuilder builder) {
		this.name = builder.name;
		this.value = builder.value;
		this.indexAnalysis = builder.indexAnalysis;
		this.stored = builder.stored;
	}
	public static class LucDocBuilder {
		private String name;
		private String value;
		private IndexAnalysis indexAnalysis;
		private Stored stored;

		public LucDocBuilder (String name, String value, IndexAnalysis indexAnalysis, Stored stored) {
			this.name = name;
			this.value = value;
			this.indexAnalysis = indexAnalysis;
			this.stored = stored;
		}

		public LucDoc3_6_1 build() {
			return new LucDoc3_6_1(this);
		}
	}
}