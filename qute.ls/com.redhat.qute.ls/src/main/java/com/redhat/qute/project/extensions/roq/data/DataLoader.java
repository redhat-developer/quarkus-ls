/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions.roq.data;

/**
 * Strategy interface for loading data files in different formats.
 * 
 * <p>
 * This interface defines the contract for data loaders that convert various
 * file formats (YAML, JSON, etc.) into JavaFieldInfo structures that provide
 * IDE support (autocomplete, validation) in Qute templates.
 * </p>
 * 
 * <p>
 * Each implementation is responsible for:
 * <ul>
 * <li>Reading the file content from disk</li>
 * <li>Parsing the content using an appropriate parser (Jackson, Gson,
 * etc.)</li>
 * <li>Extracting the data structure and inferring Java types</li>
 * <li>Populating the RoqDataFile with discovered fields</li>
 * <li>Handling errors gracefully (malformed files, IO errors, etc.)</li>
 * </ul>
 * </p>
 * 
 * <h3>Implementations:</h3>
 * <ul>
 * <li>{@code YamlDataLoader} - Uses Jackson's YAMLMapper for .yaml/.yml
 * files</li>
 * <li>{@code JsonDataLoader} - Uses Gson's JsonParser for .json files</li>
 * </ul>
 * 
 * <h3>Example Usage:</h3>
 * 
 * <pre>
 * RoqDataFile dataFile = new RoqDataFile(path, "authors");
 * DataLoader loader = new YamlDataLoader();
 * loader.load(dataFile);
 * 
 * // Now dataFile contains:
 * // - List of JavaFieldInfo (discovered fields)
 * // - Type signatures for each field
 * // - Nested structures for complex objects
 * </pre>
 * 
 * <h3>Type Inference:</h3>
 * <p>
 * Implementations should infer Java types from data file values:
 * <ul>
 * <li>Scalars → String, Integer, Long, Double, Boolean</li>
 * <li>Objects/Maps → java.lang.Object with nested fields</li>
 * <li>Arrays/Lists → java.util.Collection&lt;T&gt; with inferred item type</li>
 * <li>Null values → java.lang.Object (unknown type)</li>
 * </ul>
 * </p>
 * 
 * @see RoqDataFile
 * @see com.redhat.qute.commons.JavaFieldInfo
 * @see com.redhat.qute.project.extensions.roq.data.yaml.YamlDataLoader
 * @see com.redhat.qute.project.extensions.roq.data.json.JsonDataLoader
 */
public interface DataLoader {

	/**
	 * Loads and parses a data file, populating the RoqDataFile with discovered
	 * field information.
	 * 
	 * <p>
	 * This method should:
	 * <ol>
	 * <li>Read the file content from {@code roqDataFile.getFilePath()}</li>
	 * <li>Parse the content using an appropriate parser</li>
	 * <li>Extract all fields and their types recursively</li>
	 * <li>Call {@code roqDataFile.setFields()} with discovered fields</li>
	 * <li>Call {@code roqDataFile.setSignature()} with the file signature</li>
	 * <li>Handle errors gracefully (set empty fields on failure)</li>
	 * </ol>
	 * </p>
	 * 
	 * <h3>Error Handling:</h3>
	 * <p>
	 * Implementations should NOT throw exceptions. Instead, on any error
	 * (IOException, parse error, etc.), they should initialize the RoqDataFile with
	 * empty/default values:
	 * </p>
	 * 
	 * <pre>
	 * try {
	 * 	// parsing logic
	 * } catch (Exception e) {
	 * 	roqDataFile.setFields(List.of());
	 * 	roqDataFile.setSignature(roqDataFile.getName() + " : Object");
	 * }
	 * </pre>
	 * 
	 * <h3>Thread Safety:</h3>
	 * <p>
	 * Implementations should be stateless and thread-safe. Multiple threads may
	 * call this method concurrently on different RoqDataFile instances.
	 * </p>
	 * 
	 * @param roqDataFile The data file to load and populate with field information.
	 *                    Must not be null. The file path must be set via
	 *                    {@code roqDataFile.getFilePath()}.
	 */
	void load(RoqDataFile roqDataFile);

}