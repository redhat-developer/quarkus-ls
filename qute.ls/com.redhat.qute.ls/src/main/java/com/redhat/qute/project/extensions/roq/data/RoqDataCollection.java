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

import com.redhat.qute.commons.ResolvedJavaTypeInfo;

/**
 * Represents a collection/array type from data files (YAML, JSON).
 * 
 * <p>
 * This class wraps another type to indicate it's iterable, enabling Qute
 * templates to use loop constructs like {@code #for} and {@code #each}.
 * </p>
 * 
 * <h3>Purpose:</h3>
 * <p>
 * When a data file contains an array/list:
 * 
 * <pre>
 * books:
 *   - title: "Book 1"
 *     author: "John"
 *   - title: "Book 2"
 *     author: "Jane"
 * </pre>
 * </p>
 * 
 * <p>
 * The "books" field is represented as:
 * <ul>
 * <li><b>Outer type:</b> ArrayDataMapping (indicates it's iterable)</li>
 * <li><b>Inner type:</b> ResolvedJavaTypeInfo with fields [title, author]</li>
 * </ul>
 * </p>
 * 
 * <h3>Template Usage:</h3>
 * 
 * <pre>
 * {#for book in authors.books}
 *   {book.title} by {book.author}
 * {/for}
 * </pre>
 * 
 * <h3>Type Mapping:</h3>
 * <ul>
 * <li>YAML/JSON array → {@code java.util.Collection<T>}</li>
 * <li>Item type T → stored as resolvedType</li>
 * <li>Signature → {@code "java.util.Collection<ItemType>"}</li>
 * </ul>
 * 
 * <h3>Alternative Names:</h3>
 * <p>
 * Consider renaming to make the purpose clearer:
 * <ul>
 * <li>{@code RoqDataCollection} - emphasizes it's from data files</li>
 * <li>{@code IterableDataType} - emphasizes the iterable behavior</li>
 * <li>{@code DataArrayType} - shorter, clearer than "Mapping"</li>
 * <li>{@code CollectionTypeWrapper} - describes the wrapping pattern</li>
 * </ul>
 * </p>
 * 
 * @see ResolvedJavaTypeInfo
 * @see RoqDataField
 * @see RoqDataFile
 */
public class RoqDataCollection extends ResolvedJavaTypeInfo {

	/**
	 * Creates a new array/collection type wrapper.
	 * 
	 * <p>
	 * Wraps an item type to indicate it's iterable in templates.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * // For array of strings: ["a", "b", "c"]
	 * ResolvedJavaTypeInfo stringType = new ResolvedJavaTypeInfo();
	 * stringType.setSignature("java.lang.String");
	 * ArrayDataMapping arrayType = new ArrayDataMapping(stringType);
	 * 
	 * // Now arrayType.isIterable() returns true
	 * // And arrayType.getResolvedType() returns stringType
	 * </pre>
	 * 
	 * <h3>Example with complex objects:</h3>
	 * 
	 * <pre>
	 * // For array of objects: [{ name: "John" }, { name: "Jane" }]
	 * ResolvedJavaTypeInfo objectType = new ResolvedJavaTypeInfo();
	 * objectType.setFields([nameField]);
	 * objectType.setSignature("java.lang.Object");
	 * ArrayDataMapping arrayType = new ArrayDataMapping(objectType);
	 * 
	 * // Template can now iterate and access fields:
	 * // {#for person in people}{person.name}{/for}
	 * </pre>
	 * 
	 * @param iterableType The type of items in the collection. This is the "T" in
	 *                     Collection<T>. For example: String, Integer, or a complex
	 *                     object type
	 */
	public RoqDataCollection(ResolvedJavaTypeInfo iterableType) {
		// Store the item type - used by template engine to resolve item properties
		super.setResolvedType(iterableType);

		// Empty signature - actual signature set by caller
		// (e.g., "fieldName : java.util.Collection<String>")
		super.setSignature("");
	}

	/**
	 * Indicates this type is iterable in templates.
	 * 
	 * <p>
	 * Enables Qute template loop constructs:
	 * <ul>
	 * <li>{@code #for item in collection}</li>
	 * <li>{@code #each collection}</li>
	 * <li>{@code collection.size}</li>
	 * <li>{@code collection.isEmpty}</li>
	 * </ul>
	 * </p>
	 * 
	 * @return true - always iterable
	 */
	@Override
	public boolean isIterable() {
		return true;
	}

	/**
	 * Returns the iteration type name.
	 * 
	 * <p>
	 * Currently returns empty string. The actual item type is retrieved via
	 * {@code getResolvedType()}.
	 * </p>
	 * 
	 * <p>
	 * This method might be used for type hints in IDEs or error messages. Consider
	 * returning a meaningful value like:
	 * <ul>
	 * <li>{@code "java.util.Collection"}</li>
	 * <li>{@code getResolvedType().getSignature()}</li>
	 * </ul>
	 * </p>
	 * 
	 * @return Empty string (could be enhanced to return actual type)
	 */
	@Override
	public String getIterableOf() {
		return "";
	}

}