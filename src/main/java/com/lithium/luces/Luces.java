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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

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

	private enum ParseType {
		BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BOOLEAN, STRING
	}

	private String typeName;
	private Map<String, ParseType> typeMap;
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
	 *                 values. Throws an error if the typeName doesn't match the root in the mapping
	 * @param mapping  mapping JSON object.
	 * @return this
	 */
	public Luces mapping(String typeName, JsonObject mapping) {
		if (null == typeName || null == mapping) {
			this.typeName = null;
			this.typeMap = null;
		} else {
			this.typeName = typeName;
			this.typeMap = new HashMap<>();
			JsonObject mapRoot = mapping.getAsJsonObject(typeName);
			if (null == mapRoot) {
				throw new NoSuchElementException(typeName + " type not present or misnamed in mapping");
			}
			mapRoot = mapRoot.getAsJsonObject("properties"); // TODO account for nesting
			for (Entry<String, JsonElement> fieldDef : mapRoot.entrySet()) {
				JsonElement typeElt = fieldDef.getValue().getAsJsonObject().get("type");
				if (null == typeElt) {
					throw new NoSuchElementException("Invalid mapping: No type defined for " + fieldDef.getKey() + " field.");
				}
				ParseType parseType;
				try {
					parseType = ParseType.valueOf(typeElt.getAsString().toUpperCase());
				} catch (UnsupportedOperationException ex) {
					throw new UnsupportedOperationException("Invalid Mapping: Type defined is not a string: " + typeElt.toString());
				} catch (IllegalArgumentException illegal) {
					throw new UnsupportedOperationException("The " + typeElt.getAsString() + " type is not supported for conversion");
				}
				if (null != parseType) {
					typeMap.put(fieldDef.getKey(), parseType);
				}
			}
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
		if (null != typeMap && null != typeName) {
			for (Fieldable field : docFields) {
				ParseType parseType = typeMap.containsKey(field.name()) ? typeMap.get(field.name()) : ParseType.STRING;
				String fieldValue = field.stringValue().trim();
				Object parsedValue;
				try {
					switch (parseType) {
						case BYTE:
							// FALL THROUGH
						case SHORT:
							// FALL THROUGH
						case INTEGER:
							// FALL THROUGH
						case LONG:
							fieldValue = "".equals(fieldValue) && useDefaults ? "0" : fieldValue;
							parsedValue = Long.parseLong(fieldValue);
							break;
						case FLOAT:
							// FALL THROUGH
						case DOUBLE:
							fieldValue = "".equals(fieldValue) && useDefaults ? "0.0" : fieldValue;
							parsedValue = Double.parseDouble(fieldValue);
							break;
						case BOOLEAN:
							fieldValue = "".equals(fieldValue) && useDefaults ? "false" : fieldValue;
							parsedValue = Boolean.parseBoolean(fieldValue);
							break;
						default: // leave as untrimmed string
							parsedValue = field.stringValue();
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
			if (value instanceof ArrayList) {
				List<Object> values = (List<Object>) value;
				values.add(fieldValue);
				fieldMap.put(fieldName, values);
			} else {
				List<Object> objects = new ArrayList<>();
				objects.add(value);
				objects.add(fieldValue);
				fieldMap.put(fieldName, objects);
			}
		} else {
			fieldMap.put(fieldName, fieldValue);
		}
	}


}
