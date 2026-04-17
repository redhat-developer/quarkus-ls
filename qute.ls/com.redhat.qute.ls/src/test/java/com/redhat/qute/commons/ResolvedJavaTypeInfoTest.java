/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.redhat.qute.project.JavaDataModelCache;

/**
 * Resolved Java Type tests.
 * 
 * @author Angelo ZERR
 *
 */
public class ResolvedJavaTypeInfoTest {

	private final static JavaTypeResolver MOCK_TYPE_RESOLVER = new JavaTypeResolver() {
		
		@Override
		public ResolvedJavaTypeInfo resolveJavaTypeSync(String className) {
			return null;
		}
	};
	
	@Test
	public void applyGenericForMap() {
		ResolvedJavaTypeInfo map = new ResolvedJavaTypeInfo();
		map.setSignature("java.util.Map<K,V>");
		map.setMethods(new ArrayList<JavaMethodInfo>());

		// keySet method
		JavaMethodInfo keySetMethod = new JavaMethodInfo();
		keySetMethod.setSignature("keySet() : java.util.Set<K>");
		map.getMethods().add(keySetMethod);

		// values method
		JavaMethodInfo valuesMethod = new JavaMethodInfo();
		valuesMethod.setSignature("values() : java.util.Collection<V>");
		map.getMethods().add(valuesMethod);

		// Before apply of generic
		assertEquals("keySet() : java.util.Set<K>", keySetMethod.getSignature());
		assertEquals("keySet() : Set<K>", keySetMethod.getSimpleSignature());

		assertEquals("values() : java.util.Collection<V>", valuesMethod.getSignature());
		assertEquals("values() : Collection<V>", valuesMethod.getSimpleSignature());

		// After apply of generic
		Map<String, String> generics = map.createGenericMap("java.util.Map<java.lang.String,org.acme.Item>");
		assertNotNull(generics);
		assertEquals(2, generics.size());
		assertTrue(generics.containsKey("K"));
		assertEquals("java.lang.String", generics.get("K"));
		assertTrue(generics.containsKey("V"));
		assertEquals("org.acme.Item", generics.get("V"));

		ResolvedJavaTypeInfo typeWithGenericApply = JavaDataModelCache.updateJavaType(map, generics);
		assertEquals("java.util.Map<java.lang.String,org.acme.Item>", typeWithGenericApply.getSignature());
		assertEquals("Map<String,Item>", typeWithGenericApply.getJavaElementSimpleType());

		List<JavaMethodInfo> methods = typeWithGenericApply.getMethods();
		assertEquals(2, methods.size());

		assertEquals("keySet() : java.util.Set<java.lang.String>", methods.get(0).getSignature());
		assertEquals("keySet() : Set<String>", methods.get(0).getSimpleSignature());

		assertEquals("values() : java.util.Collection<org.acme.Item>", methods.get(1).getSignature());
		assertEquals("values() : Collection<Item>", methods.get(1).getSimpleSignature());
	}

	@Test
	public void embedTypeParameters() {
		ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
		type.setSignature("java.util.Set<java.util.Map$Entry<String,org.acme.Item>>");

		List<JavaParameterInfo> setTypeParameters = type.getTypeParameters();
		assertEquals(1, setTypeParameters.size());

		JavaParameterInfo firstSetTypeParam = setTypeParameters.get(0);
		assertEquals("java.util.Map$Entry<String,org.acme.Item>", firstSetTypeParam.getType());
		assertEquals("Map$Entry<String,Item>", firstSetTypeParam.getJavaElementSimpleType());

		List<JavaParameterInfo> mapEntryTypeParameters = firstSetTypeParam.getJavaType().getTypeParameters();
		assertEquals(2, mapEntryTypeParameters.size());

		assertEquals("String", mapEntryTypeParameters.get(0).getType());
		assertEquals("org.acme.Item", mapEntryTypeParameters.get(1).getType());
	}

	@Test
	public void twoManyTypeParameter() {
		ResolvedJavaTypeInfo list = new ResolvedJavaTypeInfo();
		list.setSignature("java.util.List<E>");
		list.setMethods(new ArrayList<JavaMethodInfo>());

		Map<String, String> generics = list.createGenericMap("java.util.List<java.lang.String,org.acme.Item>");
		assertNotNull(generics);

		assertEquals(1, generics.size());
		assertTrue(generics.containsKey("E"));
		assertEquals("java.lang.String", generics.get("E"));
	}

	@Test
	public void complexGenericType() {
		ResolvedJavaTypeInfo classA = new ResolvedJavaTypeInfo();
		classA.setSignature("org.acme.A<B,C,D>");
		classA.setFields(new ArrayList<JavaFieldInfo>());
		classA.setMethods(new ArrayList<JavaMethodInfo>());

		JavaFieldInfo field = new JavaFieldInfo();
		field.setSignature("fieldB : B");
		classA.getFields().add(field);

		field = new JavaFieldInfo();
		field.setSignature("fieldB1 : java.util.List<B>");
		classA.getFields().add(field);

		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("getB() : B");
		classA.getMethods().add(method);

		method = new JavaMethodInfo();
		method.setSignature("getB1() : java.util.Set<B>");
		classA.getMethods().add(method);

		method = new JavaMethodInfo();
		method.setSignature("getD() : java.util.Map<java.util.List<D>,B>");
		classA.getMethods().add(method);

		Map<String, String> generics = classA
				.createGenericMap("org.acme.A<java.lang.String,java.util.Set<java.lang.String>,org.acme.Item>");
		assertNotNull(generics);
		assertEquals(3, generics.size());
		assertTrue(generics.containsKey("B"));
		assertEquals("java.lang.String", generics.get("B"));
		assertTrue(generics.containsKey("C"));
		assertEquals("java.util.Set<java.lang.String>", generics.get("C"));
		assertTrue(generics.containsKey("D"));
		assertEquals("org.acme.Item", generics.get("D"));

		// Java type apply
		ResolvedJavaTypeInfo classAWithGenericApply = JavaDataModelCache.updateJavaType(classA, generics);
		assertEquals("org.acme.A<java.lang.String,java.util.Set<java.lang.String>,org.acme.Item>",
				classAWithGenericApply.getSignature());
		assertEquals("A<String,Set<String>,Item>", classAWithGenericApply.getJavaElementSimpleType());

		// Java field apply
		List<JavaFieldInfo> fields = classAWithGenericApply.getFields();
		assertEquals(2, fields.size());

		assertEquals("fieldB : java.lang.String", fields.get(0).getSignature());
		assertEquals("fieldB : String", fields.get(0).getSimpleSignature());

		assertEquals("fieldB1 : java.util.List<java.lang.String>", fields.get(1).getSignature());
		assertEquals("fieldB1 : List<String>", fields.get(1).getSimpleSignature());

		// Java method apply
		List<JavaMethodInfo> methods = classAWithGenericApply.getMethods();
		assertEquals(3, methods.size());

		assertEquals("getB() : java.lang.String", methods.get(0).getSignature());
		assertEquals("getB() : String", methods.get(0).getSimpleSignature());

		assertEquals("getB1() : java.util.Set<java.lang.String>", methods.get(1).getSignature());
		assertEquals("getB1() : Set<String>", methods.get(1).getSimpleSignature());

		assertEquals("getD() : java.util.Map<java.util.List<org.acme.Item>,java.lang.String>",
				methods.get(2).getSignature());
		assertEquals("getD() : Map<List<Item>,String>", methods.get(2).getSimpleSignature());
	}

	// ─── Case 1: T arg → T (direct resolution) ────────────────────────────────

	@Test
	public void resolveReturnType_simpleT() {
		// get(arg : T) : T + [org.acme.Item] → org.acme.Item
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(arg : T) : T");

		ResolvedJavaTypeInfo item = new ResolvedJavaTypeInfo();
		item.setSignature("org.acme.Item");

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(item), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_simpleT_primitive() {
		// get(arg : T) : T + [int] → int
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(arg : T) : T");

		ResolvedJavaTypeInfo intType = new ResolvedJavaTypeInfo();
		intType.setSignature("int");

		assertEquals("int", method.resolveReturnType(List.of(intType), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_simpleT_noArgs_fallbackToObject() {
		// get(arg : T) : T + []
		// T unresolved → java.lang.Object
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(arg : T) : T");

		assertEquals("java.lang.Object", method.resolveReturnType(Collections.emptyList(), MOCK_TYPE_RESOLVER));
	}

	// ─── Case 2: List<T> arg → T (inference from parameterized type) ──────────

	@Test
	public void resolveReturnType_listT_returnsT() {
		// get(arg : java.util.List<T>) : T + [java.util.List<org.acme.Item>] →
		// org.acme.Item
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(arg : java.util.List<T>) : T");

		ResolvedJavaTypeInfo listOfItem = new ResolvedJavaTypeInfo();
		listOfItem.setSignature("java.util.List<org.acme.Item>");

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(listOfItem), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_listT_returnsT_withExtendedTypes() {
		// get(arg : java.util.List<T>) : T + [org.acme.Items] →
		// org.acme.Item
		// where Items extends java.util.List<org.acme.Item>
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(arg : java.util.List<T>) : T");

		ResolvedJavaTypeInfo items = new ResolvedJavaTypeInfo();
		items.setSignature("org.acme.Items");
		items.setExtendedTypes(List.of("java.util.List<org.acme.Item>"));

		// Create a resolver that can resolve the extended type
		JavaTypeResolver resolver = new JavaTypeResolver() {
			@Override
			public ResolvedJavaTypeInfo resolveJavaTypeSync(String className) {
				if ("java.util.List<org.acme.Item>".equals(className)) {
					ResolvedJavaTypeInfo listOfItem = new ResolvedJavaTypeInfo();
					listOfItem.setSignature("java.util.List<org.acme.Item>");
					return listOfItem;
				}
				return null;
			}
		};

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(items), resolver));
	}

	@Test
	public void resolveReturnType_listT_returnsT_withExtendedTypes_multipleLevel() {
		// get(arg : java.util.List<T>) : T + [org.acme.Items] →
		// org.acme.Item
		// where Items extends org.acme.MyList
		// and MyList extends java.util.ArrayList<org.acme.Item>
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(arg : java.util.List<T>) : T");

		ResolvedJavaTypeInfo items = new ResolvedJavaTypeInfo();
		items.setSignature("org.acme.Items");
		items.setExtendedTypes(List.of("org.acme.MyList"));

		// Create a resolver that can resolve the extended types recursively
		JavaTypeResolver resolver = new JavaTypeResolver() {
			@Override
			public ResolvedJavaTypeInfo resolveJavaTypeSync(String className) {
				if ("org.acme.MyList".equals(className)) {
					ResolvedJavaTypeInfo myList = new ResolvedJavaTypeInfo();
					myList.setSignature("org.acme.MyList");
					myList.setExtendedTypes(List.of("java.util.ArrayList<org.acme.Item>"));
					return myList;
				} else if ("java.util.ArrayList<org.acme.Item>".equals(className)) {
					ResolvedJavaTypeInfo arrayListOfItem = new ResolvedJavaTypeInfo();
					arrayListOfItem.setSignature("java.util.ArrayList<org.acme.Item>");
					// ArrayList extends AbstractList, but for simplicity we can also say it "is-a" List
					arrayListOfItem.setExtendedTypes(List.of("java.util.List<org.acme.Item>"));
					return arrayListOfItem;
				} else if ("java.util.List<org.acme.Item>".equals(className)) {
					ResolvedJavaTypeInfo listOfItem = new ResolvedJavaTypeInfo();
					listOfItem.setSignature("java.util.List<org.acme.Item>");
					return listOfItem;
				}
				return null;
			}
		};

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(items), resolver));
	}

	@Test
	public void resolveReturnType_listT_returnsListT() {
		// filter(arg : java.util.List<T>) : java.util.List<T> + [List<Item>] →
		// java.util.List<org.acme.Item>
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("filter(arg : java.util.List<T>) : java.util.List<T>");

		ResolvedJavaTypeInfo listOfItem = new ResolvedJavaTypeInfo();
		listOfItem.setSignature("java.util.List<org.acme.Item>");

		assertEquals("java.util.List<org.acme.Item>", method.resolveReturnType(List.of(listOfItem), MOCK_TYPE_RESOLVER));
	}

	// ─── Case 3: Class<T> arg → T (CDI/find pattern) ─────────────────────────

	@Test
	public void resolveReturnType_classT_returnsT() {
		// find(clazz : java.lang.Class<T>) : T + [java.lang.Class<org.acme.Item>] →
		// org.acme.Item
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("find(clazz : java.lang.Class<T>) : T");

		ResolvedJavaTypeInfo classOfItem = new ResolvedJavaTypeInfo();
		classOfItem.setSignature("java.lang.Class<org.acme.Item>");

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(classOfItem), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_classT_returnsListT() {
		// findAll(clazz : java.lang.Class<T>) : java.util.List<T> + [Class<Item>] →
		// java.util.List<org.acme.Item>
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("findAll(clazz : java.lang.Class<T>) : java.util.List<T>");

		ResolvedJavaTypeInfo classOfItem = new ResolvedJavaTypeInfo();
		classOfItem.setSignature("java.lang.Class<org.acme.Item>");

		assertEquals("java.util.List<org.acme.Item>", method.resolveReturnType(List.of(classOfItem), MOCK_TYPE_RESOLVER));
	}

	// ─── Case 4: K key, V value → Map<K,V> (multi-generics) ──────────────────

	@Test
	public void resolveReturnType_multiGenerics_mapKV() {
		// put(key : K, value : V) : java.util.Map<K,V> + [String, Item] →
		// java.util.Map<java.lang.String,org.acme.Item>
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("put(key : K, value : V) : java.util.Map<K,V>");

		ResolvedJavaTypeInfo string = new ResolvedJavaTypeInfo();
		string.setSignature("java.lang.String");
		ResolvedJavaTypeInfo item = new ResolvedJavaTypeInfo();
		item.setSignature("org.acme.Item");

		assertEquals("java.util.Map<java.lang.String,org.acme.Item>", method.resolveReturnType(List.of(string, item), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_multiGenerics_partialArgs_partialResolution() {
		// put(key : K, value : V) : java.util.Map<K,V> + [String]
		// K resolved → java.lang.String, V unresolved → stays V
		// result: java.util.Map<java.lang.String,V>
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("put(key : K, value : V) : java.util.Map<K,V>");

		ResolvedJavaTypeInfo string = new ResolvedJavaTypeInfo();
		string.setSignature("java.lang.String");

		assertEquals("java.util.Map<java.lang.String,java.lang.Object>", method.resolveReturnType(List.of(string), MOCK_TYPE_RESOLVER));
	}

	// ─── No generics: returned as-is ──────────────────────────────────────────

	@Test
	public void resolveReturnType_noGeneric_primitive() {
		// size() : int → int (nothing to resolve)
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("size() : int");

		assertEquals("int", method.resolveReturnType(Collections.emptyList(), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_noGeneric_concreteType() {
		// getName() : java.lang.String → java.lang.String
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("getName() : java.lang.String");

		assertEquals("java.lang.String", method.resolveReturnType(Collections.emptyList(), MOCK_TYPE_RESOLVER));
	}

	// ─── Void method ──────────────────────────────────────────────────────────

	@Test
	public void resolveReturnType_voidMethod_returnsVoid() {
		// save(item : T) : void → "void"
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("save(item : T) : void");

		ResolvedJavaTypeInfo item = new ResolvedJavaTypeInfo();
		item.setSignature("org.acme.Item");

		assertEquals("void", method.resolveReturnType(List.of(item), MOCK_TYPE_RESOLVER));
	}

	// ─── Case 5: baseType resolution ──────────────────────────────────────────

	@Test
	public void resolveReturnType_baseType_array_get() {
		// get(base : T[], index : int) : T + baseType=org.acme.Item[] → org.acme.Item
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(base : T[], index : int) : T");

		ResolvedJavaTypeInfo itemArray = new ResolvedJavaTypeInfo();
		itemArray.setSignature("org.acme.Item[]");
		itemArray.setIterableOf("org.acme.Item");

		ResolvedJavaTypeInfo intType = new ResolvedJavaTypeInfo();
		intType.setSignature("int");

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(itemArray, intType), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_baseType_list_get() {
		// get(base : java.util.List<T>, index : int) : T +
		// baseType=java.util.List<org.acme.Item> → org.acme.Item
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(base : java.util.List<T>, index : int) : T");

		ResolvedJavaTypeInfo listOfItem = new ResolvedJavaTypeInfo();
		listOfItem.setSignature("java.util.List<org.acme.Item>");

		ResolvedJavaTypeInfo intType = new ResolvedJavaTypeInfo();
		intType.setSignature("int");

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(listOfItem, intType), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_baseType_map_get() {
		// get(base : java.util.Map<K,V>, key : K) : V +
		// baseType=java.util.Map<java.lang.String,org.acme.Item> → org.acme.Item
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("get(base : java.util.Map<K,V>, key : K) : V");

		ResolvedJavaTypeInfo mapType = new ResolvedJavaTypeInfo();
		mapType.setSignature("java.util.Map<java.lang.String,org.acme.Item>");

		ResolvedJavaTypeInfo string = new ResolvedJavaTypeInfo();
		string.setSignature("java.lang.String");

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(mapType, string), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void resolveReturnType_baseType_noArgs() {
		// first(base : java.util.List<T>) : T + baseType=java.util.List<org.acme.Item>,
		// args=[] → org.acme.Item
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("first(base : java.util.List<T>) : T");

		ResolvedJavaTypeInfo listOfItem = new ResolvedJavaTypeInfo();
		listOfItem.setSignature("java.util.List<org.acme.Item>");

		assertEquals("org.acme.Item", method.resolveReturnType(List.of(listOfItem), MOCK_TYPE_RESOLVER));
	}

	@Test
	public void iterable() {
		ResolvedJavaTypeInfo iterable = new ResolvedJavaTypeInfo();
		iterable.setSignature("java.lang.Iterable<T>");
		assertTrue(iterable.isIterable(), "java.lang.Iterable<T>");

		ResolvedJavaTypeInfo iterator = new ResolvedJavaTypeInfo();
		iterator.setSignature("java.util.Iterator<E>");
		assertTrue(iterator.isIterable(), "java.util.Iterator<E>");

	}
}