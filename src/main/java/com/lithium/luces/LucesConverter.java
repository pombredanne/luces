/*
 * FieldValueProvider.java
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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import com.google.gson.JsonElement;

/**
 * Provides a conversion from a Lucene {@link Fieldable#stringValue()} value to a typed value (for Elasticsearch).
 *
 * @author Doug Schroeder
 * @since 12/2/15.
 */
public interface LucesConverter {

	/**
	 * @param field the field to extract the typed value for
	 * @return the value (converted from {@link Fieldable#stringValue()} to the appropriate type for Elasticsearch
	 */
	Object getFieldValue(Fieldable field);

	/**
	 * Convert the document to a JSON representation of a Lucene document for indexing into Elasticsearch
	 *
	 * @param doc the Lucene document
	 * @return the JSON representation of the document
	 */
	JsonElement documentToJSON(Document doc);
}
