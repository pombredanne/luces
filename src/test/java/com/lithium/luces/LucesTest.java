/*
 * ConvertTest.java
 * Created on Jan 13, 2015
 *
 * Copyright 2015 Lithium Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lithium.luces;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;

import com.google.gson.JsonElement;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Brian Harrington
 */
public class LucesTest {
	private static final Logger log = LoggerFactory.getLogger(LucesTest.class);

	private static final String TYPE = "testType";
	private static final String LOGIN = "login";
	private static final String FIRST_NAME = "name_first";
	private static final String LAST_NAME = "name_last";
	private static final String EMAIL_FIELD = "email";
	private static final String RATING = "rating";
	private static final String VIEWS = "views";
	private static final String NEG_BYTE = "negByteField";
	private static final String REGISTERED = "registered";
	private static final String EMAIL_1 = "homestar@runner.com";
	private static final String SIGNUP = "signup";
	private static final String GENDER = "gender";
	private static String[] NAMES = {"Alice", "Bob", "Charlie", "David", "Elise" };

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
		Assert.assertEquals(valid, json);
	}

	@Test
	public void testTimedDocumentConversion() {
		Luces luces = new Luces(Version.LUCENE_36);
		luces.mapping(TYPE, createMapping());

		final int iterations = 100000;
		long startTime = System.nanoTime();
		for (int i = 0; i < iterations; ++i) {
			Document doc = createMockFlatUserDocument(true);
			luces.documentToJSONStringified(doc, false);
		}
		long endTime = System.nanoTime();
		long delta = endTime - startTime;
		log.info("\nPerf test took {}ms for {} iterations\n", delta / 1000, iterations);
		log.info("Averaging {}ns/doc", delta / iterations);
		Assert.assertTrue(true);
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
		log.debug(json);
		Assert.assertEquals(valid, json);
	}

	/**
	 * Tests to see if a null mapping is handled correctly. No errors should be thrown
	 */
	@Test
	public void testMappingIsNullified() {
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
		Luces luces = new Luces(Version.LUCENE_36).throwErrorIfMappingIsNull(false);
		luces.mapping(TYPE, createMapping());
		luces.mapping(TYPE, null);
		String json = luces.documentToJSONStringified(doc, true);
		log.debug(json);
		Assert.assertEquals(valid, json);

		luces.mapping(TYPE, createMapping());
		luces.mapping(null, createMapping());
		json = luces.documentToJSONStringified(doc, true);
		log.debug(json);
		Assert.assertEquals(valid, json);
	}

	/**
	 * Tests to see if a null mapping is handled correctly when {@link Luces#getFieldValue(String, String)} is called,
	 * and the mapping is null
	 * No errors should be thrown
	 */
	@Test
	public void testGetFieldValueWhenMappingIsNullified() {
		Luces luces = new Luces(Version.LUCENE_36).throwErrorIfMappingIsNull(false);
		luces.mapping(TYPE, createMapping());
		luces.mapping(TYPE, null);
		Object fieldValue = luces.getFieldValue("views", "655351");
		Assert.assertEquals("655351", fieldValue);

		luces.mapping(TYPE, createMapping());
		luces.mapping(null, createMapping());
		fieldValue = luces.getFieldValue("name_first", "Joe");
		Assert.assertEquals("Joe", fieldValue);
	}

	/**
	 * Tests to see if an exception is thrown when {@link Luces#mapping(String, JsonObject)} is called with a null value
	 */
	@Test
	public void testThrowErrorWhenMappingSetToNull(){
		Luces luces = new Luces(Version.LUCENE_36);
		luces.throwErrorIfMappingIsNull(true);
		luces.mapping(TYPE, createMapping());
		try {
			luces.mapping(TYPE, null);
			Assert.fail("IllegalStateException should have been thrown");
		} catch (Exception ex) {
			log.info(ex.getMessage());
			Assert.assertTrue(ex instanceof IllegalStateException);
		}
	}

	/**
	 * Tests to see if an exception is thrown when {@link Luces#getFieldValue(String, String)} is called with a null
	 * mapping
	 */
	@Test (expected = IllegalStateException.class)
	public void testThrowErrorWhenMappingIsNeverSetAndCallGetFieldValue(){
		Luces luces = new Luces(Version.LUCENE_36);
		luces.throwErrorIfMappingIsNull(true);
		Object fieldValue = luces.getFieldValue("test", "something");
		Assert.assertEquals("something", fieldValue);
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

	@Test (expected = UnsupportedOperationException.class)
	public void testTypeIsNotAString() {
		JsonObject mapping = createMapping();
		mapping.getAsJsonObject(TYPE).getAsJsonObject("properties").getAsJsonObject(LOGIN).remove("type");
		JsonObject typeAsObject = new JsonObject();
		typeAsObject.addProperty("blah", "something");
		typeAsObject.addProperty("meh", "something else");
		mapping.getAsJsonObject(TYPE).getAsJsonObject("properties").getAsJsonObject(LOGIN).add("type", typeAsObject);

		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, mapping);
	}

	@Test (expected = UnsupportedOperationException.class)
	public void testTypeIsNotSupported() {
		JsonObject mapping = createMapping();
		mapping.getAsJsonObject(TYPE).getAsJsonObject("properties").getAsJsonObject(LOGIN).remove("type");
		mapping.getAsJsonObject(TYPE).getAsJsonObject("properties").getAsJsonObject(LOGIN).addProperty("type", "weird_type");

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

	@Test
	public void testWeirdBoolValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(REGISTERED);
		doc.add(new Field(REGISTERED, "boolean", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, createMapping()).useDefaultsForEmpty(true);
		JsonObject converted = luces.documentToJSON(doc).getAsJsonObject();
		Assert.assertEquals("false", converted.get(REGISTERED).getAsString());
	}

	@Test
	public void testUseNullOnEmptyIntValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(VIEWS);
		doc.add(new Field(VIEWS, "   ", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36)
				.useNullForEmpty(true)
				.mapping(TYPE, createMapping());
		JsonObject converted = luces.documentToJSON(doc).getAsJsonObject();
		Assert.assertNull(converted.get(VIEWS));
	}

	@Test
	public void testCantUseNullAndDefaultOnEmptyIntValue() {
		Document doc = createMockFlatUserDocument();
		doc.removeField(VIEWS);
		doc.add(new Field(VIEWS, "   ", Store.NO, Index.ANALYZED));
		Luces luces = new Luces(Version.LUCENE_36)
				.useNullForEmpty(true)
				.mapping(TYPE, createMapping()).useDefaultsForEmpty(true);
		JsonObject converted = luces.documentToJSON(doc).getAsJsonObject();
		Assert.assertNotNull(converted.get(VIEWS));
		Assert.assertEquals("0", converted.get(VIEWS).getAsString());
	}

	@Test
	public void testDocWithDuplicateFieldsConvertsToArray() {
		Document doc = createMockFlatUserDocument();
		String EMAIL2 = "this@that.com";
		String EMAIL3 = "me@you.com";
		doc.add(new Field(EMAIL_FIELD, EMAIL2, Store.NO, Index.ANALYZED)); // second email field
		doc.add(new Field(EMAIL_FIELD, EMAIL3, Store.NO, Index.ANALYZED));    // third
		Luces luces = new Luces(Version.LUCENE_36).mapping(TYPE, createMapping()).useDefaultsForEmpty(true);
		JsonObject converted = luces.documentToJSON(doc).getAsJsonObject();
		JsonArray array = converted.get(EMAIL_FIELD).getAsJsonArray();
		Assert.assertEquals(3, array.size());
		Assert.assertEquals(EMAIL_1, array.get(0).getAsString());
		Assert.assertEquals(EMAIL2, array.get(1).getAsString());
		Assert.assertEquals(EMAIL3, array.get(2).getAsString());
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
		return createMockFlatUserDocument(false);
	}
	private Document createMockFlatUserDocument(boolean randomize) {
		int randomNum = 0;
		if (randomize) {
			randomNum = (int)(Math.random() * 100);
		}
		final String login = randomize ? NAMES[randomNum % NAMES.length] : "Trogdor";
		final String email = randomize ? (NAMES[randomNum % NAMES.length] + "@aol.com") : EMAIL_1;
		final Calendar reg_date = randomize ? Calendar.getInstance()
											: new GregorianCalendar(2014, Calendar.DECEMBER, 23, 13, 24, 56);
		final String name_first = randomize ? NAMES[randomNum % NAMES.length] : "Joe";
		final String name_last = randomize ? NAMES[randomNum % NAMES.length] : "Schmo";
		final String gender = randomize ? (randomNum %2 == 0 ? "female" : "male") : "male";

		final String rating = randomize ? String.valueOf(randomNum / 10.0) : "  4.2453 ";
		final String views = randomize ? String.valueOf(2147483000 + randomNum) : "  655351";
		final String negbyte = randomize ? String.valueOf(-randomNum) : " -12 ";
		final String registered = randomize ? String.valueOf(randomNum % 2 == 0) : " true ";

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		Document doc = new Document();

		doc.add(new Field(LOGIN, login.toLowerCase(), Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field(FIRST_NAME, name_first, Store.NO, Index.ANALYZED));
		doc.add(new Field(LAST_NAME, name_last, Store.NO, Index.ANALYZED));
		doc.add(new Field(EMAIL_FIELD, email.toLowerCase(), Store.NO, Index.ANALYZED));
		doc.add(new Field(SIGNUP, sdf.format(reg_date.getTime()), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field(GENDER, gender, Store.NO, Index.ANALYZED));
		// testing numbers and accounting for whitespace
		doc.add(new Field(RATING, rating, Store.NO, Index.ANALYZED));
		doc.add(new Field(VIEWS, views, Store.NO, Index.ANALYZED));
		doc.add(new Field(NEG_BYTE, negbyte, Store.NO, Index.ANALYZED));
		doc.add(new Field(REGISTERED, registered, Store.NO, Index.ANALYZED));

		return doc;
	}

	@Test
	public void testMultipleValueField() {
		Document doc = createMockFlatUserDocumentWithMultipleValueForGenderField();
		Luces luces = new Luces(Version.LUCENE_36);
		luces.mapping(TYPE, createMapping());
		JsonElement json = luces.documentToJSON(doc);
		Assert.assertEquals(2, json.getAsJsonObject().getAsJsonArray(GENDER).size());
	}

	private Document createMockFlatUserDocumentWithMultipleValueForGenderField() {
		final String login = "Trogdor";
		final String email = EMAIL_1;
		final Calendar reg_date = new GregorianCalendar(2014, Calendar.DECEMBER, 23, 13, 24, 56);
		final String name_first = "Joe";
		final String name_last = "Schmo";
		final String gender1 = "male";
		final String gender2 = "female";

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		Document doc = new Document();

		doc.add(new Field(LOGIN, login.toLowerCase(), Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field(FIRST_NAME, name_first, Store.NO, Index.ANALYZED));
		doc.add(new Field(LAST_NAME, name_last, Store.NO, Index.ANALYZED));
		doc.add(new Field(EMAIL_FIELD, email.toLowerCase(), Store.NO, Index.ANALYZED));
		doc.add(new Field(SIGNUP, sdf.format(reg_date.getTime()), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field(GENDER, gender1, Store.NO, Index.ANALYZED));
		doc.add(new Field(GENDER, gender2, Store.NO, Index.ANALYZED));

		// testing numbers and accounting for whitespace
		doc.add(new Field(RATING, "  4.2453 ", Store.NO, Index.ANALYZED));
		doc.add(new Field(VIEWS, "  655351", Store.NO, Index.ANALYZED));
		doc.add(new Field(NEG_BYTE, " -12 ", Store.NO, Index.ANALYZED));
		doc.add(new Field(REGISTERED, " true ", Store.NO, Index.ANALYZED));

		return doc;
	}

	private static JsonObject createMapping() {
		JsonObject mapping = new JsonObject();
		JsonObject typeObject = new JsonObject();
		JsonObject propertiesObject = new JsonObject();

		JsonObject loginDef = new JsonObject();
		loginDef.addProperty("type", "string");
		propertiesObject.add(LOGIN, loginDef);

		JsonObject firstNameDef = new JsonObject();
		firstNameDef.addProperty("type", "string");
		propertiesObject.add(FIRST_NAME, firstNameDef);

		JsonObject lastNameDef = new JsonObject();
		lastNameDef.addProperty("type", "string");
		propertiesObject.add(LAST_NAME, lastNameDef);

		JsonObject signupDef = new JsonObject();
		signupDef.addProperty("type", "string");
		propertiesObject.add(SIGNUP, signupDef);

		JsonObject gender = new JsonObject();
		gender.addProperty("type", "string");
		propertiesObject.add(GENDER, gender);

		JsonObject emailDef = new JsonObject();
		emailDef.addProperty("type", "string");
		propertiesObject.add(EMAIL_FIELD, emailDef);

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
