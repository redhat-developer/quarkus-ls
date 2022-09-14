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
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.redhat.qute.project.QuteProjectRegistry;

/**
 * Resolved Java Type tests.
 * 
 * @author Angelo ZERR
 *
 */
public class ResolvedJavaTypeInfoTest {

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

		ResolvedJavaTypeInfo typeWithGenericApply = QuteProjectRegistry.updateJavaType(map, generics);
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
		String signature = "java.util.Set<java.util.Map$Entry<String,org.acme.Item>>";
		ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
		type.setSignature(signature);

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
		ResolvedJavaTypeInfo classAWithGenericApply = QuteProjectRegistry.updateJavaType(classA, generics);
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

}