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
package com.redhat.qute.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;

/**
 * Tests with
 * {@link QuteProject#isMatchType(ResolvedJavaTypeInfo, JavaTypeInfo)}
 */
public class MatchTypeTest {

	// -------------------------------------------------------------------------
	// Exact match
	// -------------------------------------------------------------------------

	@Test
	public void testExactMatch() {
		assertMatchType("java.lang.String", "java.lang.String");
	}

	@Test
	public void testExactMatchInteger() {
		assertMatchType("java.lang.Integer", "java.lang.Integer");
	}

	// -------------------------------------------------------------------------
	// Match primitive type
	// -------------------------------------------------------------------------

	@Test
	public void testInt() {
		assertMatchType("java.lang.Integer", "int");
		assertMatchType("int", "java.lang.Integer");
		assertMatchType("int", "Integer");
	}

	// -------------------------------------------------------------------------
	// Match without lang
	// -------------------------------------------------------------------------

	@Test
	public void testMatchStringWithoutLang() {
		assertMatchType("java.lang.String", "String");
	}

	@Test
	public void testExactMatchIntegerWithoutLang() {
		assertMatchType("java.lang.Integer", "Integer");
	}

	// -------------------------------------------------------------------------
	// Incompatible types (expected false)
	// -------------------------------------------------------------------------

	@Test
	public void testIncompatibleTypes() {
		assertNoMatchType("java.lang.String", "java.lang.Integer");
	}

	@Test
	public void testIncompatibleTypesNumberAndString() {
		assertNoMatchType("java.lang.Number", "java.lang.String");
	}

	@Test
	public void testIncompatibleTypesListAndString() {
		assertNoMatchType("java.util.List", "java.lang.String");
	}

	// -------------------------------------------------------------------------
	// Inheritance / subtypes
	// -------------------------------------------------------------------------

	@Test
	public void testSubTypeIntegerExtendsNumber() {
		// Integer extends Number
		assertMatchType("java.lang.Integer", "java.lang.Number");
	}

	@Test
	public void testSubTypeArrayListExtendsList() {
		// ArrayList extends AbstractList, implements List
		assertMatchType("java.util.ArrayList", "java.util.List");
	}

	@Test
	public void testParentTypeDoesNotMatchChild() {
		// Number does NOT extend Integer — inverse relationship must be false
		assertNoMatchType("java.lang.Number", "java.lang.Integer");
	}

	// -------------------------------------------------------------------------
	// Generic types
	// -------------------------------------------------------------------------

	@Test
	public void testGenericListString() {
		assertMatchType("java.util.List<java.lang.String>", "java.util.List<java.lang.String>");
	}

	@Test
	public void testGenericListIncompatibleTypeParam() {
		assertNoMatchType("java.util.List<java.lang.String>", "java.util.List<java.lang.Integer>");
	}

	@Test
	public void testGenericMapStringInteger() {
		assertMatchType("java.util.Map<java.lang.String,java.lang.Integer>",
				"java.util.Map<java.lang.String,java.lang.Integer>");
	}

	@Test
	public void testGenericRawTypeVsParameterized() {
		// Raw List should match List<String> due to type erasure
		assertMatchType("java.util.List", "java.util.List<java.lang.String>");
	}

	@Test
	public void testGenericParameterizedVsRawType() {
		// List<String> should also match raw List due to type erasure
		assertMatchType("java.util.List<java.lang.String>", "java.util.List");
	}

	// -------------------------------------------------------------------------
	// Primitive types / wrapper types
	// -------------------------------------------------------------------------

	@Test
	public void testPrimitiveInt() {
		assertMatchType("int", "int");
	}

	@Test
	public void testPrimitiveBoolean() {
		assertMatchType("boolean", "boolean");
	}

	@Test
	public void testPrimitiveIntMatchesWrapperInteger() {
		// Autoboxing: int <-> Integer
		assertMatchType("int", "java.lang.Integer");
	}

	@Test
	public void testWrapperIntegerMatchesPrimitiveInt() {
		assertMatchType("java.lang.Integer", "int");
	}

	@Test
	public void testPrimitiveBooleanMatchesWrapperBoolean() {
		assertMatchType("boolean", "java.lang.Boolean");
	}

	@Test
	public void testPrimitiveDoesNotMatchUnrelatedWrapper() {
		assertNoMatchType("int", "java.lang.String");
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private static void assertMatchType(String type1, String type2) {
		boolean result = resolveAndMatch(type1, type2);
		assertTrue(result, "Expected types to match: " + type1 + " <-> " + type2);
	}

	private static void assertNoMatchType(String type1, String type2) {
		boolean result = resolveAndMatch(type1, type2);
		assertFalse(result, "Expected types NOT to match: " + type1 + " <-> " + type2);
	}

	private static boolean resolveAndMatch(String type1, String type2) {
		MockQuteProjectRegistry registry = new MockQuteProjectRegistry();
		QuteProject project = registry
				.getProject(new ProjectInfo(QuteQuickStartProject.PROJECT_URI, null, null, null, null, null));

		ResolvedJavaTypeInfo javaType1 = project.resolveJavaTypeSync(type1);
		JavaTypeInfo javaType2 = new JavaTypeInfo();
		javaType2.setSignature(type2);

		return project.isMatchType(javaType1, javaType2);
	}
}