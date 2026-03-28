/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.binary.BinaryTemplateInfo;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;

/**
 * Base class for project which initializes JDK resolved Java types.
 */
public abstract class BaseQuteProject extends MockQuteProject {

	private static class BinaryTemplateInfoLoader {
		List<BinaryTemplateInfo> binaries;
	}

	public BaseQuteProject(ProjectInfo projectInfo, QuteProjectRegistry projectRegistry) {
		super(projectInfo, projectRegistry);
	}

	protected DataModelProject<DataModelTemplate<?>> loadDataModel(String fileName, Class<?> clazz) {
		InputStream in = clazz.getResourceAsStream(fileName);
		return new GsonBuilder().create().fromJson(new InputStreamReader(in), DataModelProject.class);
	}

	protected List<BinaryTemplateInfo> loadDataBinaryTemplates(String fileName, Class<?> clazz) {
		InputStream in = clazz.getResourceAsStream(fileName);
		return new GsonBuilder().create().fromJson(new InputStreamReader(in), BinaryTemplateInfoLoader.class).binaries;
	}

	protected void loadResolvedJavaType(String fileName, List<ResolvedJavaTypeInfo> resolvedJavaTypes, Class<?> clazz) {
		InputStream in = clazz.getResourceAsStream(fileName);
		ResolvedJavaTypeInfo resolvedJavaType = new GsonBuilder().create().fromJson(new InputStreamReader(in),
				ResolvedJavaTypeInfo.class);
		resolvedJavaTypes.add(resolvedJavaType);
	}

	@Override
	protected void fillJavaTypes(List<JavaTypeInfo> cache) {
		createJavaTypeInfo("java.util.List<E>", JavaTypeKind.Interface, cache);
		createJavaTypeInfo("java.util.Map<K,V>", JavaTypeKind.Interface, cache);
	}

	@Override
	protected void fillResolvedJavaTypes(List<ResolvedJavaTypeInfo> resolvedJavaTypes) {
		createBinaryTypes(resolvedJavaTypes);
	}

	private void createBinaryTypes(List<ResolvedJavaTypeInfo> resolvedJavaTypes) {
		// Java type primitives
		createResolvedJavaTypeInfo("java.lang.Object", resolvedJavaTypes, true);
		createResolvedJavaTypeInfo("java.lang.Boolean", resolvedJavaTypes, true);
		createResolvedJavaTypeInfo("java.lang.Double", resolvedJavaTypes, true);
		createResolvedJavaTypeInfo("java.lang.Long", resolvedJavaTypes, true);
		createResolvedJavaTypeInfo("java.lang.Float", resolvedJavaTypes, true);
		createResolvedJavaTypeInfo("java.math.BigDecimal", resolvedJavaTypes, true);

		// Number
		createResolvedJavaTypeInfo("java.lang.Number", resolvedJavaTypes, true);

		// Integer
		ResolvedJavaTypeInfo integer = createResolvedJavaTypeInfo("java.lang.Integer", resolvedJavaTypes, true);
		integer.setExtendedTypes(List.of("java.lang.Number"));
		registerMethod("byteValue() : byte", integer);

		// String
		ResolvedJavaTypeInfo string = createResolvedJavaTypeInfo("java.lang.String", resolvedJavaTypes, true);
		registerField("UTF16 : byte", string);
		registerMethod("isEmpty() : boolean", string);
		registerMethod("codePointCount(beginIndex : int,endIndex : int) : int", string);
		string.setInvalidMethod("getChars", InvalidMethodReason.VoidReturn); // void getChars(int srcBegin, int srcEnd,
																				// char dst[], int dstBegin)
		registerMethod("charAt(index : int) : char", string);
		registerMethod("getBytes(charsetName : java.lang.String) : byte[]", string);
		registerMethod("getBytes() : byte[]", string);

		// BigInteger
		ResolvedJavaTypeInfo bigInteger = createResolvedJavaTypeInfo("java.math.BigInteger", resolvedJavaTypes, true);
		registerMethod("divide(val : java.math.BigInteger) : java.math.BigInteger", bigInteger);

		// Iterator
		ResolvedJavaTypeInfo iterator = createResolvedJavaTypeInfo("java.util.Iterator<E>", resolvedJavaTypes, true);
		registerMethod("hasNext() : boolean", iterator);
		registerMethod("next() : E", iterator);

		// Iterable
		ResolvedJavaTypeInfo iterable = createResolvedJavaTypeInfo("java.lang.Iterable<T>", resolvedJavaTypes, true);
		registerMethod("iterator() : java.util.Iterator<T>", iterable);

		// Collection
		ResolvedJavaTypeInfo collection = createResolvedJavaTypeInfo("java.util.Collection<E>", resolvedJavaTypes,
				true);
		collection.setExtendedTypes(Arrays.asList("java.lang.Iterable<E>"));
		registerMethod("addAll(arg0 : java.util.Collection<? extends E>) : boolean", collection);

		// AbstractCollection
		ResolvedJavaTypeInfo abstractCollection = createResolvedJavaTypeInfo("java.util.AbstractCollection<E>",
				resolvedJavaTypes, true);
		abstractCollection.setExtendedTypes(Arrays.asList("java.lang.Object", "java.util.Collection<E>"));

		// SequencedCollection
		ResolvedJavaTypeInfo sequencedCollection = createResolvedJavaTypeInfo("java.util.SequencedCollection<E>",
				resolvedJavaTypes, true);
		sequencedCollection.setExtendedTypes(Arrays.asList("java.util.Collection<E>"));

		// List
		ResolvedJavaTypeInfo list = createResolvedJavaTypeInfo("java.util.List<E>", resolvedJavaTypes, true);
		list.setExtendedTypes(Arrays.asList("java.util.Object", "java.util.SequencedCollection<E>"));
		registerMethod("size() : int", list);
		registerMethod("get(index : int) : E", list);
		registerMethod("subList(fromIndex : int, toIndex: int) : java.util.List<E>", list);
		registerMethod("containsAll(arg0 : java.util.Collection<?>) : boolean", list);

		// AbstractList
		ResolvedJavaTypeInfo abstractList = createResolvedJavaTypeInfo("java.util.AbstractList<E>", resolvedJavaTypes,
				true);
		abstractList.setExtendedTypes(Arrays.asList("java.util.AbstractCollection<E>", "java.util.List<E>"));

		// ArrayList
		ResolvedJavaTypeInfo arrayList = createResolvedJavaTypeInfo("java.util.ArrayList<E>", resolvedJavaTypes, true);
		arrayList.setExtendedTypes(Arrays.asList("java.util.AbstractList<E>", "java.util.List<E>"));

		// Set
		ResolvedJavaTypeInfo set = createResolvedJavaTypeInfo("java.util.Set<E>", resolvedJavaTypes, true);
		set.setExtendedTypes(Arrays.asList("java.lang.Iterable<E>"));

		// Map
		ResolvedJavaTypeInfo map = createResolvedJavaTypeInfo("java.util.Map<K,V>", resolvedJavaTypes, true);
		registerMethod("keySet() : java.util.Set<K>", map);
		registerMethod("values() : java.util.Collection<V>", map);
		registerMethod("entrySet() : java.util.Set<java.util.Map$Entry<K,V>>", map);
		registerMethod("get(key : K) : V", map);

		// Map.Entry
		ResolvedJavaTypeInfo mapEntry = createResolvedJavaTypeInfo("java.util.Map$Entry<K,V>", resolvedJavaTypes, true);
		registerMethod("getKey() : K", mapEntry);
		registerMethod("getValue() : V", mapEntry);

		// AbstractMap
		ResolvedJavaTypeInfo abstractMap = createResolvedJavaTypeInfo("java.util.AbstractMap<K,V>", resolvedJavaTypes,
				true);
		abstractMap.setExtendedTypes(Arrays.asList("java.util.Map<K,V>"));

		// HashMap
		ResolvedJavaTypeInfo hashMap = createResolvedJavaTypeInfo("java.util.HashMap<K,V>", resolvedJavaTypes, true);
		hashMap.setExtendedTypes(Arrays.asList("java.util.AbstractMap<K,V>", "java.util.Map<K,V>"));

		// https://quarkus.io/guides/qute-reference#evaluation-of-completionstage-and-uni-objects
		createResolvedJavaTypeInfo("java.util.concurrent.CompletionStage<T>", resolvedJavaTypes, true);
		ResolvedJavaTypeInfo completableFuture = createResolvedJavaTypeInfo("java.util.concurrent.CompletableFuture<T>",
				resolvedJavaTypes, true);
		completableFuture.setExtendedTypes(Arrays.asList("java.util.concurrent.CompletionStage<T>"));
		createResolvedJavaTypeInfo("io.smallrye.mutiny.Uni<T>", resolvedJavaTypes, true);
		ResolvedJavaTypeInfo asyncResultUni = createResolvedJavaTypeInfo("io.smallrye.mutiny.vertx.AsyncResultUni<T>",
				resolvedJavaTypes, true);
		asyncResultUni.setExtendedTypes(Arrays.asList("io.smallrye.mutiny.Uni<T>"));

		// RawString for raw and safe resolver tests
		ResolvedJavaTypeInfo rawString = createResolvedJavaTypeInfo("io.quarkus.qute.RawString", resolvedJavaTypes,
				true);
		registerMethod("getValue() : java.lang.String", rawString);
		registerMethod("toString() : java.lang.String", rawString);

		// Load Java classes from JDK
		loadResolvedJavaType("ZoneDateTime.json", resolvedJavaTypes, BaseQuteProject.class);
		loadResolvedJavaType("Temporal.json", resolvedJavaTypes, BaseQuteProject.class);

		// Load JsonObject from vertx
		loadResolvedJavaType("JsonObject.json", resolvedJavaTypes, BaseQuteProject.class);

	}

	/*
	 * @Override public Collection<QuteTextDocument> getBinaryDocuments() { if
	 * (!binaryDoumentsLoaded) { binaryDoumentsLoaded = true;
	 * super.registerBinaryTemplates(loadBinaryTemplatesSync()); } return
	 * super.getBinaryDocuments(); }
	 */

}
