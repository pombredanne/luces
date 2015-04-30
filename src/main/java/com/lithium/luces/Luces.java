/*
 * Luces.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.util.Version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/**
 * Utility class for converting Lucene Documents to a JSON format for consumption by Elasticsearch
 *
 * @author Brian Harrington
 */
public class Luces {

	private String typeName;
	private JsonObject mapping;
	private boolean useDefaults;

	@SuppressWarnings("unused")
	public Luces(Version version) {
		if (!version.equals(Version.LUCENE_36)) {
			throw new UnsupportedOperationException("This library does not support Lucene version " + version.name());
		}
	}

	/**
	 * Specify a mapping JSON object to be able to convert document fields to their proper types
	 *
	 * @param typeName Name of the type. Set to null to reset the mapping file and only output strings for the field
	 *                 values
	 * @param mapping  mapping JSON object.
	 * @return this
	 */
	public Luces mapping(String typeName, JsonObject mapping) {
		if (null == typeName || null == mapping) {
			this.typeName = null;
			this.mapping = null;
		} else {
			this.typeName = typeName;
			this.mapping = mapping;
		}
		return this;
	}

	/**
	 * Flag for setting default values for empty strings. Only used when there is a mapping file to
	 * determine field types. Otherwise empty strings will throw a parsing error
	 * Defaults are:
	 * <li>0 for int / long</li>
	 * <li>0.0 for float / double</li>
	 * <li>false for boolean</li>
	 *
	 * @param useDefaults whether to use defaults when an empty string is encountered
	 * @return this
	 */
	public Luces useDefaultsForEmpty(boolean useDefaults) {
		this.useDefaults = useDefaults;
		return this;
	}

	/**
	 * Gets a string representation of the JSON object that the document has been converted to
	 *
	 * @param doc            the Lucene document to convert
	 * @param setPrettyPrint pretty print the JSON string
	 * @return the String version of the JSON version of a document
	 */
	public String documentToJSONStringified(Document doc, boolean setPrettyPrint) {
		Gson gson = setPrettyPrint ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
		return gson.toJson(documentToJSON(doc));
	}

	/**
	 * Convert the document to a JSON representation of a Lucene document for indexing into Elasticsearch
	 *
	 * @param doc the Lucene document
	 * @return the JSON representation of the document
	 */
	public JsonElement documentToJSON(Document doc) {
		Map<String, Object> fields = new LinkedHashMap<>();
		List<Fieldable> docFields = doc.getFields();
		if (null != mapping && null != typeName) {
			JsonObject propertiesJson = mapping.getAsJsonObject(typeName).getAsJsonObject("properties");
			for (Fieldable field : docFields) {
				JsonObject fieldDef = propertiesJson.getAsJsonObject(field.name());
				if (null == fieldDef) {
					putOrAppend(fields, field.name(), field.stringValue());
					continue;
				}
				JsonElement fieldType = fieldDef.get("type");
				String fieldValue = field.stringValue().trim();
				Object parsedValue;
				try {
					switch (fieldType.getAsString()) {
						case "byte":
							// FALL THROUGH
						case "short":
							// FALL THROUGH
						case "integer":
							// FALL THROUGH
						case "long":
							fieldValue = "".equals(fieldValue) && useDefaults ? "0" : fieldValue;
							parsedValue = Long.parseLong(fieldValue);
							break;
						case "float":
							// FALL THROUGH
						case "double":
							fieldValue = "".equals(fieldValue) && useDefaults ? "0.0" : fieldValue;
							parsedValue = Double.parseDouble(fieldValue);
							break;
						case "boolean":
							fieldValue = "".equals(fieldValue) && useDefaults ? "false" : fieldValue;
							parsedValue = Boolean.parseBoolean(fieldValue);
							break;
						default: // leave as string
							parsedValue = fieldValue;
							break;
					}
				} catch (NumberFormatException ex) {
					throw new NumberFormatException("Error parsing " + field.name() + " field: " + ex.getMessage());
				}
				putOrAppend(fields, field.name(), parsedValue);
			}
		} else {
			for (Fieldable field : docFields) {
				putOrAppend(fields, field.name(), field.stringValue());
			}
		}
		return new Gson().toJsonTree(fields);
	}


	private void putOrAppend(Map<String, Object> fieldMap, String fieldName, Object fieldValue) {
		Object value = fieldMap.get(fieldName);
		if (value != null) {
			if (value instanceof Collections) {
				List<Object> values = (List<Object>) value;
				values.add(fieldValue);
				fieldMap.put(fieldName, values);
			} else {
				Collection<Object> objects = new ArrayList<>(3);
				objects.add(value);
				objects.add(fieldValue);
				fieldMap.put(fieldName, objects);
			}
		} else {
			fieldMap.put(fieldName, fieldValue);
		}
	}


}
