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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link JavaTypeInfo}.
 *
 * @author Angelo ZERR
 *
 */
public class JavaTypeInfoTest {

	@Test
	public void simple() {
		String signature = "java.math.BigInteger";
		JavaTypeInfo type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("java.math.BigInteger", type.getName());
		assertFalse(type.isGenericType());
	}

	@Test
	public void generic() {
		String signature = "T";
		JavaTypeInfo type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("T", type.getName());
		assertTrue(type.isGenericType());
		assertTrue(type.getTypeParameters().isEmpty());
	}

	@Test
	public void listOfGeneric() {
		String signature = "java.util.List<T>";
		JavaTypeInfo type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("java.util.List", type.getName());
		assertTrue(type.isGenericType());
		assertEquals(1, type.getTypeParameters().size());
		assertEquals("T", type.getTypeParameters().get(0).getType());
		assertTrue(type.getTypeParameters().get(0).getJavaType().isGenericType());
	}

	@Test
	public void listOItem() {
		String signature = "java.util.List<org.acme.Item>";
		JavaTypeInfo type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("java.util.List", type.getName());
		assertFalse(type.isGenericType());
		assertEquals(1, type.getTypeParameters().size());
		assertEquals("org.acme.Item", type.getTypeParameters().get(0).getType());
		assertFalse(type.getTypeParameters().get(0).getJavaType().isGenericType());
	}

	@Test
	public void primitive() {
		String signature = "boolean";
		JavaTypeInfo type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("boolean", type.getName());
		assertFalse(type.isGenericType());

		signature = "byte";
		type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("byte", type.getName());
		assertFalse(type.isGenericType());

		signature = "double";
		type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("double", type.getName());
		assertFalse(type.isGenericType());

		signature = "float";
		type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("float", type.getName());
		assertFalse(type.isGenericType());

		signature = "int";
		type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("int", type.getName());
		assertFalse(type.isGenericType());

		signature = "long";
		type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("long", type.getName());
		assertFalse(type.isGenericType());
	}

	@Test
	public void string() {
		String signature = "java.lang.String";
		JavaTypeInfo type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("java.lang.String", type.getName());
		assertEquals("String", type.getJavaElementSimpleType());
		assertFalse(type.isArray());
		assertFalse(type.isGenericType());
	}

	@Test
	public void stringArray() {
		String signature = "java.lang.String[]";
		JavaTypeInfo type = new JavaTypeInfo();
		type.setSignature(signature);
		assertEquals("java.lang.String[]", type.getName());
		assertEquals("String[]", type.getJavaElementSimpleType());
		assertTrue(type.isArray());
		assertFalse(type.isGenericType());
	}

}
