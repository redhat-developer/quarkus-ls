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
package com.redhat.qute.parser.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;

/**
 * Tests for JavaTypeInfoProvider alternative types and label generation.
 *
 * @author Angelo ZERR
 */
public class JavaTypeInfoProviderTest {

	@Test
	public void getAlternativeTypes_whenNull_returnsNull() {
		JavaTypeInfoProvider provider = new TestJavaTypeInfoProvider(null, null);
		assertNull(provider.getAlternativeTypes());
	}

	@Test
	public void getJavaElementTypeLabel_whenNoAlternatives_returnsSimpleType() {
		// Single type: java.lang.String → "String"
		JavaTypeInfoProvider provider = new TestJavaTypeInfoProvider("java.lang.String", null);
		assertEquals("String", provider.getJavaElementTypeLabel());
	}

	@Test
	public void getJavaElementTypeLabel_whenNoAlternatives_withResolvedType() {
		// Single type with resolved type
		ResolvedJavaTypeInfo resolved = new ResolvedJavaTypeInfo();
		resolved.setSignature("java.util.List<E>");
		JavaTypeInfoProvider provider = new TestJavaTypeInfoProvider(null, resolved);
		assertEquals("List<E>", provider.getJavaElementTypeLabel());
	}

	@Test
	public void getJavaElementTypeLabel_whenHasAlternatives_returnsPipeSeparated() {
		// Union type: Integer|Boolean|String
		JavaTypeInfoProvider alt1 = new TestJavaTypeInfoProvider("java.lang.Integer", null);
		JavaTypeInfoProvider alt2 = new TestJavaTypeInfoProvider("java.lang.Boolean", null);
		JavaTypeInfoProvider alt3 = new TestJavaTypeInfoProvider("java.lang.String", null);

		JavaTypeInfoProvider provider = new TestJavaTypeInfoProvider(null, null,
				Arrays.asList(alt1, alt2, alt3));

		assertEquals("Integer|Boolean|String", provider.getJavaElementTypeLabel());
	}

	@Test
	public void getJavaElementTypeLabel_whenHasAlternatives_withComplexTypes() {
		// Union type: List<E>|Map<K,V>|Set<E>
		ResolvedJavaTypeInfo resolved1 = new ResolvedJavaTypeInfo();
		resolved1.setSignature("java.util.List<E>");

		ResolvedJavaTypeInfo resolved2 = new ResolvedJavaTypeInfo();
		resolved2.setSignature("java.util.Map<K,V>");

		ResolvedJavaTypeInfo resolved3 = new ResolvedJavaTypeInfo();
		resolved3.setSignature("java.util.Set<E>");

		JavaTypeInfoProvider alt1 = new TestJavaTypeInfoProvider(null, resolved1);
		JavaTypeInfoProvider alt2 = new TestJavaTypeInfoProvider(null, resolved2);
		JavaTypeInfoProvider alt3 = new TestJavaTypeInfoProvider(null, resolved3);

		JavaTypeInfoProvider provider = new TestJavaTypeInfoProvider(null, null,
				Arrays.asList(alt1, alt2, alt3));

		assertEquals("List<E>|Map<K,V>|Set<E>", provider.getJavaElementTypeLabel());
	}

	@Test
	public void getJavaElementTypeLabel_whenAlternativesEmpty_returnsNull() {
		JavaTypeInfoProvider provider = new TestJavaTypeInfoProvider(null, null, Arrays.asList());
		assertNull(provider.getJavaElementTypeLabel());
	}

	@Test
	public void getJavaElementTypeLabel_whenSingleAlternative() {
		// Edge case: single alternative (should show that one type)
		JavaTypeInfoProvider alt1 = new TestJavaTypeInfoProvider("org.acme.Item", null);
		JavaTypeInfoProvider provider = new TestJavaTypeInfoProvider(null, null, Arrays.asList(alt1));

		assertEquals("Item", provider.getJavaElementTypeLabel());
	}

	// ─── Test Implementation ─────────────────────────────────────────────────

	/**
	 * Test implementation of JavaTypeInfoProvider for testing purposes.
	 */
	private static class TestJavaTypeInfoProvider implements JavaTypeInfoProvider {

		private final String javaType;
		private final ResolvedJavaTypeInfo resolvedType;
		private final List<JavaTypeInfoProvider> alternatives;

		public TestJavaTypeInfoProvider(String javaType, ResolvedJavaTypeInfo resolvedType) {
			this(javaType, resolvedType, null);
		}

		public TestJavaTypeInfoProvider(String javaType, ResolvedJavaTypeInfo resolvedType,
				List<JavaTypeInfoProvider> alternatives) {
			this.javaType = javaType;
			this.resolvedType = resolvedType;
			this.alternatives = alternatives;
		}

		@Override
		public String getJavaType() {
			return javaType;
		}

		@Override
		public ResolvedJavaTypeInfo getResolvedType() {
			return resolvedType;
		}

		@Override
		public List<JavaTypeInfoProvider> getAlternativeTypes() {
			return alternatives;
		}

		@Override
		public Node getJavaTypeOwnerNode() {
			return null;
		}
	}
}
