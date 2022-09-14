/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link JavaMethodInfo}.
 *
 * @author Angelo ZERR
 *
 */
public class JavaMethodInfoTest {

	@Test
	public void map() {
		String signature = "find(query : java.lang.String, params : java.util.Map<java.lang.String,java.lang.Object>) : io.quarkus.hibernate.orm.panache.PanacheQuery<T>";
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature(signature);
		assertEquals("find", method.getName());

		assertTrue(method.hasParameters());
		assertEquals(2, method.getParameters().size());

		JavaParameterInfo parameter = method.getParameters().get(0);
		assertEquals("query", parameter.getName());
		assertEquals("java.lang.String", parameter.getType());

		parameter = method.getParameters().get(1);
		assertEquals("params", parameter.getName());
		assertEquals("java.util.Map<java.lang.String,java.lang.Object>", parameter.getType());

		assertEquals("io.quarkus.hibernate.orm.panache.PanacheQuery<T>", method.getReturnType());
		assertEquals("find(query : String, params : Map<String,Object>) : PanacheQuery<T>",
				method.getSimpleSignature());
	}

	@Test
	public void mapEntry() {
		String signature = "entrySet() : java.util.Set<java.util.Map$Entry<K,V>>";
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature(signature);
		assertEquals("entrySet", method.getName());

		assertFalse(method.hasParameters());
		
		assertEquals("java.util.Set<java.util.Map$Entry<K,V>>", method.getReturnType());
		assertEquals("entrySet() : Set<Map$Entry<K,V>>",
				method.getSimpleSignature());

	}

	@Test
	public void list() {
		ResolvedJavaTypeInfo list = new ResolvedJavaTypeInfo();
		list.setSignature("java.util.List<E>");

		String signature = "get(index : int) : E";
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature(signature);
		method.setJavaType(list);

		ResolvedJavaTypeInfo listOfItem = new ResolvedJavaTypeInfo();
		listOfItem.setSignature("java.util.List<org.acme.Item>");

		String resolved = method.resolveJavaElementType(listOfItem);
		assertEquals("org.acme.Item", resolved);
	}

	@Test
	public void getter() {
		// getFoo()
		JavaMethodInfo getFooMethod = new JavaMethodInfo();
		getFooMethod.setSignature("getFoo() : int");
		assertEquals("foo", getFooMethod.getGetterName());
		assertEquals("int", getFooMethod.getReturnType());
		assertEquals("getFoo() : int", getFooMethod.getSimpleSignature());

		// get()
		JavaMethodInfo getMethod = new JavaMethodInfo();
		getMethod.setSignature("get() : int");
		assertNull(getMethod.getGetterName());
		assertEquals("int", getMethod.getReturnType());
		assertEquals("get() : int", getMethod.getSimpleSignature());

		// isFoo()
		JavaMethodInfo isFooMethod = new JavaMethodInfo();
		isFooMethod.setSignature("isFoo() : boolean");
		assertEquals("foo", isFooMethod.getGetterName());
		assertEquals("boolean", isFooMethod.getReturnType());
		assertEquals("isFoo() : boolean", isFooMethod.getSimpleSignature());

		// is()
		JavaMethodInfo isMethod = new JavaMethodInfo();
		isMethod.setSignature("is() : boolean");
		assertNull(isMethod.getGetterName());
		assertEquals("boolean", isMethod.getReturnType());
		assertEquals("is() : boolean", isMethod.getSimpleSignature());
	}

	@Test
	public void varargs() {
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("pretty(item : org.acme.Item, elements : java.lang.String...) : java.lang.String");
		assertEquals("pretty", method.getName());

		assertTrue(method.hasParameters());
		assertEquals(2, method.getParameters().size());

		JavaParameterInfo parameter = method.getParameters().get(0);
		assertEquals("item", parameter.getName());
		assertEquals("org.acme.Item", parameter.getType());

		parameter = method.getParameters().get(1);
		assertEquals("elements", parameter.getName());
		assertEquals("java.lang.String...", parameter.getType());

		assertEquals("java.lang.String", method.getReturnType());
		assertEquals("pretty(item : Item, elements : String...) : String", method.getSimpleSignature());
	}
}
