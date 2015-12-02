/*
 * LucesMapper.java
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

/**
 * Provides a mapping from Lucene objects to a typed value (for Elasticsearch).
 *
 * @author Doug Schroeder
 * @since 12/2/15.
 */
public interface LucesMapper<MappingTypeT> {

	/**
	 * Specify a mapping JSON object to be able to convert document fields to their proper types
	 *
	 * @param typeName Name of the type. Set to null to reset the mapping file and only output strings for the field
	 *                 values. Throws an error if the typeName doesn't match the root in the mapping, or if the mapping
	 *                 is invalid.
	 * @param mapping  mapping JSON object.
	 * @return this
	 */
	LucesMapper mapping(String typeName, MappingTypeT mapping);

	/**
	 * Flag for setting default values for empty strings. Only used when there is a mapping file to
	 * determine field types. Otherwise empty strings will throw a parsing error
	 * Defaults are:
	 * * 0 for int / long
	 * * 0.0 for float / double
	 * * false for boolean
	 *
	 * @param useDefaults whether to use defaults when an empty string is encountered for a non-string type.
	 *                    If set to true, useNull will be set to false
	 *
	 * @return this
	 */
	LucesMapper useDefaultsForEmpty(boolean useDefaults);

	/**
	 * Use a null value when an empty string is encountered. If set to true, useDefaults will be set to false
	 *
	 * @param useNull whether to use a null value when an empty string is encountered for a non-string type
	 * @return this
	 */
	LucesMapper useNullForEmpty(boolean useNull);
}
