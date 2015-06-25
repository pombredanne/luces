/*
 * Luces.java
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
import com.google.gson.JsonNull;
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
	private boolean useNull;

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
	 *                 values. Throws an error if the typeName doesn't match the root in the mapping, or if the mapping
	 *                 is invalid.
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
			JsonObject workingJson = mapping.getAsJsonObject(typeName);
			if (null == workingJson) {
				throw new NoSuchElementException(typeName + " type not present or misnamed in mapping");
			}
			// TODO account for nesting
			workingJson = workingJson.getAsJsonObject("properties");
			for (Entry<String, JsonElement> entry : workingJson.entrySet()) {
				JsonElement typeElt = entry.getValue().getAsJsonObject().get("type");
				if (null == typeElt) {
					throw new NoSuchElementException("Invalid mapping: No type defined for " + entry.getKey() + " field.");
				}
				ParseType parseType;
				try {
					parseType = ParseType.valueOf(typeElt.getAsString().toUpperCase());
				} catch (UnsupportedOperationException ex) {
					throw new UnsupportedOperationException("Invalid Mapping: Type defined is not a string: " + typeElt.toString());
				} catch (IllegalArgumentException illegal) {
					throw new UnsupportedOperationException("The " + typeElt.getAsString() + " type is not supported for conversion");
				}
				if (null != parseType && !ParseType.STRING.equals(parseType)) { // don't need to store info on strings
					typeMap.put(entry.getKey(), parseType);
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
	 * @param useDefaults whether to use defaults when an empty string is encountered for a non-string type.
	 *                    If set to true, useNull will be set to false
	 *
	 * @return this
	 */
	public Luces useDefaultsForEmpty(boolean useDefaults) {
		this.useDefaults = useDefaults;
		if (useDefaults) {
			useNull = false;
		}
		return this;
	}

	/**
	 * Use a null value when an empty string is encountered. If set to true, useDefaults will be set to false
	 *
	 * @param useNull whether to use a null value when an empty string is encountered for a non-string type
	 * @return this
	 */
	public Luces useNullForEmpty(boolean useNull) {
		this.useNull = useNull;
		if (useNull) {
			useDefaults = false;
		}
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
				String fieldValue = field.stringValue();
				if (null == fieldValue || (useNull && "".equals(fieldValue.trim()))) {
					putOrAppend(fields, field.name(), JsonNull.INSTANCE);
					continue;
				}
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
							fieldValue = fieldValue.trim();
							fieldValue = (useDefaults && "".equals(fieldValue)) ? "0" : fieldValue;
							parsedValue = Long.parseLong(fieldValue);
							break;
						case FLOAT:
							// FALL THROUGH
						case DOUBLE:
							fieldValue = fieldValue.trim();
							fieldValue = (useDefaults && "".equals(fieldValue)) ? "0.0" : fieldValue;
							parsedValue = Double.parseDouble(fieldValue);
							break;
						case BOOLEAN:
							fieldValue = fieldValue.trim();
							// anything that doesn't match the string "true" ignoring case evaluates to false
							parsedValue = Boolean.parseBoolean(fieldValue);
							break;
						default: // leave as untrimmed string
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
