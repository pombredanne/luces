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


/**
 * @author Brian Harrington
 */
public class LucesTest {

	private String TYPE = "testType";
	private String LOGIN = "login";
	private String FIRST_NAME = "name_first";
	private String EMAIL_FIELD = "email";
	private String RATING = "rating";
	private String VIEWS = "views";
	private String NEG_BYTE = "negByteField";
	private String REGISTERED = "registered";
	private String EMAIL_1 = "homestar@runner.com";
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
		System.out.println("\nPerf test took " + delta / 1000 + "ms for " + iterations + " iterations\n" +
				"Averaging " + delta / iterations + "ns/doc");
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
//		System.out.println(json);
		Assert.assertEquals(valid, json);
	}

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
		Luces luces = new Luces(Version.LUCENE_36);
		luces.mapping(TYPE, createMapping());
		luces.mapping(TYPE, null);
		String json = luces.documentToJSONStringified(doc, true);
//		System.out.println(json);
		Assert.assertEquals(valid, json);

		luces.mapping(TYPE, createMapping());
		luces.mapping(null, createMapping());
		json = luces.documentToJSONStringified(doc, true);
//		System.out.println(json);
		Assert.assertEquals(valid, json);
	}

	@Test
	public void testGetFieldValueWhenMappingIsNullified() {
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
		Luces luces = new Luces(Version.LUCENE_36);
		luces.mapping(TYPE, createMapping());
		luces.mapping(TYPE, null);
		Object fieldValue = luces.getFieldValue("views", "655351");
//		System.out.println(json);
		Assert.assertEquals("655351", fieldValue);

		luces.mapping(TYPE, createMapping());
		luces.mapping(null, createMapping());
		fieldValue = luces.getFieldValue("name_first", "Joe");
//		System.out.println(json);
		Assert.assertEquals("Joe", fieldValue);
	}

	@Test
	public void testThrowErrorWhenMappingSetToNull(){
		Luces luces = new Luces(Version.LUCENE_36);
//		luces.throwErrorIfMappingIsNull(true);
		luces.mapping(TYPE, createMapping());
		try {
			luces.mapping(TYPE, null);
			Assert.fail("IllegalStateException should have been thrown");
		} catch (Exception ex) {
			Assert.assertTrue(ex instanceof IllegalStateException);
		}
	}

	@Test (expected = IllegalStateException.class)
	public void testThrowErrorWhenMappingIsNeverSetAndCallGetFieldValue(){
		Luces luces = new Luces(Version.LUCENE_36);
//		luces.throwErrorIfMappingIsNull(true);
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
		doc.add(new Field("name_last", name_last, Store.NO, Index.ANALYZED));
		doc.add(new Field(EMAIL_FIELD, email.toLowerCase(), Store.NO, Index.ANALYZED));
		doc.add(new Field("signup", sdf.format(reg_date.getTime()), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("gender", gender, Store.NO, Index.ANALYZED));
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
		Assert.assertEquals(2, json.getAsJsonObject().getAsJsonArray("gender").size());
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
		doc.add(new Field("name_last", name_last, Store.NO, Index.ANALYZED));
		doc.add(new Field(EMAIL_FIELD, email.toLowerCase(), Store.NO, Index.ANALYZED));
		doc.add(new Field("signup", sdf.format(reg_date.getTime()), Store.NO, Index.NOT_ANALYZED));
		doc.add(new Field("gender", gender1, Store.NO, Index.ANALYZED));
		doc.add(new Field("gender", gender2, Store.NO, Index.ANALYZED));

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
