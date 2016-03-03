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
 * Utility class for converting Lucene Documents to a JSON format for consumption by Elasticsearch. This class is NOT threadsafe.
 *
 * @author Brian Harrington
 */
public class Luces implements LucesConverter, LucesMapper<JsonObject> {

	private enum ParseType {
		BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BOOLEAN, STRING
	}

	private String typeName;
	private Map<String, ParseType> typeMap;
	private boolean useDefaults;
	private boolean useNull;
	private boolean errIfMappingNull;

	@SuppressWarnings("unused")
	public Luces(Version version) {
		if (!(version == Version.LUCENE_36)) {
			throw new UnsupportedOperationException("This library does not support Lucene version " + version.name());
		}
	}

	@Override
	public Luces mapping(String typename, JsonObject mapping) {
		if (null == typename || null == mapping) {
			if (errIfMappingNull) {
				throw new IllegalStateException(String.format("%1$s cannot be set to null", typename == null ? "Type" : "Mapping"));
			}
			typeName = null;
			typeMap = null;
		} else {
			typeName = typename;
			typeMap = new HashMap<>();
			JsonObject workingJson = mapping.getAsJsonObject(typename);
			if (null == workingJson) {
				throw new NoSuchElementException(typename + " type not present or misnamed in mapping");
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
				if (! (ParseType.STRING == parseType)) { // don't need to store info on strings
					typeMap.put(entry.getKey(), parseType);
				}
			}
		}
		return this;
	}

	@Override
	public Luces useDefaultsForEmpty(boolean usedefaults) {
		useDefaults = usedefaults;
		if (usedefaults) {
			useNull = false;
		}
		return this;
	}

	@Override
	public Luces useNullForEmpty(boolean usenull) {
		useNull = usenull;
		if (usenull) {
			useDefaults = false;
		}
		return this;
	}

	@Override
	public LucesMapper<JsonObject> throwErrorIfMappingIsNull(boolean throwError) {
		errIfMappingNull = throwError;
		return this;
	}

	@Override
	public String documentToJSONStringified(Document doc, boolean setPrettyPrint) {
		Gson gson = setPrettyPrint ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
		return gson.toJson(documentToJSON(doc));
	}

	@Override
	public Object getFieldValue(Fieldable field) {
		return getFieldValue(field.name(), field.stringValue());
	}

	@Override
	public Object getFieldValue(String name, String value) {
		if (typeMap == null && errIfMappingNull) {
			throw new IllegalStateException(String.format("Mapping is null, but required. [name = %1$s, value = %2$s]",
					name, value));
		}
		final ParseType parseType;
		ParseType temp;
		if (typeMap == null || (temp = typeMap.get(name)) == null) {
			parseType = ParseType.STRING;
		} else {
			parseType = temp;
		}
		String fieldValue = value;
		if (null == fieldValue || (useNull && fieldValue.trim().isEmpty())) {
			return JsonNull.INSTANCE;
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
					fieldValue = (useDefaults && fieldValue.isEmpty()) ? "0" : fieldValue;
					parsedValue = Long.parseLong(fieldValue);
					break;
				case FLOAT:
					// FALL THROUGH
				case DOUBLE:
					fieldValue = fieldValue.trim();
					fieldValue = (useDefaults && fieldValue.isEmpty()) ? "0.0" : fieldValue;
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
			throw new NumberFormatException("Error parsing " + name + " field: " + ex.getMessage());
		}

		return parsedValue;
	}

	@Override
	public JsonElement documentToJSON(Document doc) {
		Map<String, Object> fields = new LinkedHashMap<>();
		List<Fieldable> docFields = doc.getFields();
		if (null != typeMap && null != typeName) {
			for (Fieldable field : docFields) {
				putOrAppend(fields, field.name(), getFieldValue(field));
			}
		} else {
			for (Fieldable field : docFields) {
				putOrAppend(fields, field.name(), field.stringValue());
			}
		}
		return new Gson().toJsonTree(fields);
	}

	@SuppressWarnings("unchecked")
	private static List<Object> toObjectList(Object value) {
		return (List<Object>) value;
	}

	private void putOrAppend(Map<String, Object> fieldMap, String fieldName, Object fieldValue) {
		Object value = fieldMap.get(fieldName);
		if (value != null) {
			if (value instanceof ArrayList) {
				List<Object> values = toObjectList(value);
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
