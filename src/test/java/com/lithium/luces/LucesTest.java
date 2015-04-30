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
import java.util.NoSuchElementException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;


/**
 * @author Brian Harrington
 */
public class LucesTest {

	private String TYPE = "testType";
	private String LOGIN = "login";
	private String FIRST_NAME = "name_first";
	private String EMAIL = "email";
	private String RATING = "rating";
	private String VIEWS = "views";
	private String NEG_BYTE = "negByteField";
	private String REGISTERED = "registered";

	@Test
	public void testConvertOneDocumentWithMapping() {
		String valid = "{\n" +
				"  \"login\": \"trogdor\",\n" +
				"  \"name_first\": \"Joe\",\n" +
				"  \"name_last\": \"Schmo\",\n" +
				"  \"email\": \"homestar@runner.com\",\n" +
				"  \"signup\": \"12/23/2014\",\n" +
				"  \"gender\": \"male\",\n" +
				"  \"rating\": 4.2453,\n" +
				"  \"views\": 655351,\n" +
				"  \"negByteField\": -12,\n" +
				"  \"registered\": true\n" +
				"}";
		Document doc = createMockFlatUserDocument();
		Luces luces = new Luces(Version.LUCENE_36);
		luces.mapping(TYPE, createMapping());
		String json = luces.documentToJSONStringified(doc, true);
//		System.out.println(json);
		Assert.assertEquals(valid, json);
	}

	@Test
	public void testConvertOneDocumentWithoutMapping() {
		String valid = "{\n" +
				"  \"login\": \"trogdor\",\n" +
				"  \"name_first\": \"Joe\",\n" +
				"  \"name_last\": \"Schmo\",\n" +
				"  \"email\": \"homestar@runner.com\",\n" +
				"  \"signup\": \"12/23/2014\",\n" +
				"  \"gender\": \"male\",\n" +
				"  \"rating\": \"  4.2453 \",\n" +
				"  \"views\": \"  655351\",\n" +
				"  \"negByteField\": \" -12 \",\n" +
				"  \"registered\": \" true \"\n" +
				"}";
		Document doc = createMockFlatUserDocument();
		Luces luces = new Luces(Version.LUCENE_36);
		String json = luces.documentToJSONStringified(doc, true);
//		System.out.println(json);
		Assert.assertEquals(valid, json);
	}

	@Test (expected = NoSuchElementException.class)
	public void testTypeMappingMismatch() {
		Luces luces = new Luces(Version.LUCENE_36).mapping("typo", createMapping());
	}

	@Test (expected = NoSuchElementException.class)
	public void testMappingFieldMissingType() {
		JsonObject mapping = createMapping();
		mapping.getAsJsonObject(TYPE).getAsJsonObject("properties").getAsJsonObject(LOGIN).remove("type");

		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, mapping);
	}

	@Test (expected = NumberFormatException.class)
	public void testErrorOnEmptyFloatValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(RATING);
		doc.add(new Field(RATING, "   ", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, createMapping());
		luces.documentToJSONStringified(doc, true);
	}

	@Test (expected = NumberFormatException.class)
	public void testErrorOnEmptyIntValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(VIEWS);
		doc.add(new Field(VIEWS, "   ", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, createMapping());
		luces.documentToJSONStringified(doc, true);
	}

	/**
	 * for similar tests, this would throw a format error, but since empty strings get parsed to false, no error is
	 * thrown
	 */
	@Test
	public void testEmptyBoolValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(REGISTERED);
		doc.add(new Field(REGISTERED, "   ", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, createMapping());
		JsonObject converted = luces.documentToJSON(doc).getAsJsonObject();
		Assert.assertEquals("false", converted.get(REGISTERED).getAsString());
	}

	@Test
	public void testDefaultOnEmptyFloatValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(RATING);
		doc.add(new Field(RATING, "   ", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, createMapping()).useDefaultsForEmpty(true);
		JsonObject converted = luces.documentToJSON(doc).getAsJsonObject();
		Assert.assertEquals("0.0", converted.get(RATING).getAsString());
	}

	@Test
	public void testDefaultOnEmptyIntValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(VIEWS);
		doc.add(new Field(VIEWS, "   ", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, createMapping()).useDefaultsForEmpty(true);
		JsonObject converted = luces.documentToJSON(doc).getAsJsonObject();
		Assert.assertEquals("0", converted.get(VIEWS).getAsString());
	}

	@Test
	public void testDefaultOnEmptyBoolValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(REGISTERED);
		doc.add(new Field(REGISTERED, "   ", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, createMapping()).useDefaultsForEmpty(true);
		JsonObject converted = luces.documentToJSON(doc).getAsJsonObject();
		Assert.assertEquals("false", converted.get(REGISTERED).getAsString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnsupportedVersion() {
		Luces luces = new Luces(Version.LUCENE_30);
	}

	@Test
	public void testSupportedVersion() {
		Luces luces = new Luces(Version.LUCENE_36);
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

		doc.add(new Field(LOGIN, login.toLowerCase(), Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field(FIRST_NAME, name_first, Store.NO, Index.ANALYZED));
		doc.add(new Field("name_last", name_last, Store.NO, Index.ANALYZED));
		doc.add(new Field("email", email.toLowerCase(), Store.NO, Index.ANALYZED));
		doc.add(new Field("signup", sdf.format(reg_date.getTime()), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("gender", gender, Store.NO, Index.ANALYZED));
		// testing numbers and accounting for whitespace
		doc.add(new Field(RATING, "  4.2453 ", Store.NO, Index.ANALYZED));
		doc.add(new Field(VIEWS, "  655351", Store.NO, Index.ANALYZED));
		doc.add(new Field(NEG_BYTE, " -12 ", Store.NO, Index.ANALYZED));
		doc.add(new Field(REGISTERED, " true ", Store.NO, Index.ANALYZED));

		return doc;
	}

	private JsonObject createMapping() {
		JsonObject mapping = new JsonObject();
		JsonObject typeObject = new JsonObject();
		JsonObject propertiesObject = new JsonObject();

		JsonObject loginDef = new JsonObject();
		loginDef.addProperty("type", "string");
		propertiesObject.add(LOGIN, loginDef);

		JsonObject nameDef = new JsonObject();
		nameDef.addProperty("type", "string");
		propertiesObject.add(FIRST_NAME, nameDef);

		JsonObject ratingDef = new JsonObject();
		ratingDef.addProperty("type", "float");
		propertiesObject.add(RATING, ratingDef);

		JsonObject viewsDef = new JsonObject();
		viewsDef.addProperty("type", "integer");
		propertiesObject.add(VIEWS, viewsDef);

		JsonObject byteDef = new JsonObject();
		byteDef.addProperty("type", "byte");
		propertiesObject.add(NEG_BYTE, byteDef);

		JsonObject boolDef = new JsonObject();
		boolDef.addProperty("type", "boolean");
		propertiesObject.add(REGISTERED, boolDef);

		typeObject.add("properties", propertiesObject);
		mapping.add(TYPE, typeObject);
		return mapping;
	}
}
