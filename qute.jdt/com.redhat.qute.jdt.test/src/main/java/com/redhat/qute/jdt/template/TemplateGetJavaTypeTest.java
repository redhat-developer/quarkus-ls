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
package com.redhat.qute.jdt.template;

import static com.redhat.qute.jdt.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.QuteProjectTest.loadMavenProject;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;

/**
 * Tests for
 * {@link QuteSupportForTemplate#getJavaTypes(QuteJavaTypesParams, com.redhat.qute.jdt.utils.IJDTUtils, org.eclipse.core.runtime.IProgressMonitor)}
 *
 * @author Angelo ZERR
 *
 */
public class TemplateGetJavaTypeTest {

	@Test
	public void packages() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaTypesParams params = new QuteJavaTypesParams("java.", QuteMavenProjectName.qute_quickstart);
		List<JavaTypeInfo> actual = QuteSupportForTemplate.getInstance().getJavaTypes(params, getJDTUtils(),
				new NullProgressMonitor());

		assertJavaTypes(actual, //
				t("java.util", JavaTypeKind.Package), //
				t("java.lang", JavaTypeKind.Package));
	}

	@Test
	public void list() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaTypesParams params = new QuteJavaTypesParams("List", QuteMavenProjectName.qute_quickstart);
		List<JavaTypeInfo> actual = QuteSupportForTemplate.getInstance().getJavaTypes(params, getJDTUtils(),
				new NullProgressMonitor());

		assertJavaTypes(actual, //
				t("java.util.List<E>", JavaTypeKind.Interface));
	}

	@Test
	public void item() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaTypesParams params = new QuteJavaTypesParams("Item", QuteMavenProjectName.qute_quickstart);
		List<JavaTypeInfo> actual = QuteSupportForTemplate.getInstance().getJavaTypes(params, getJDTUtils(),
				new NullProgressMonitor());

		assertJavaTypes(actual, //
				t("org.acme.qute.Item", JavaTypeKind.Class), //
				t("org.acme.qute.ItemResource", JavaTypeKind.Class));
	}

	@Test
	public void nested() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaTypesParams params = new QuteJavaTypesParams("org.acme.qute.NestedClass.",
				QuteMavenProjectName.qute_quickstart);
		List<JavaTypeInfo> actual = QuteSupportForTemplate.getInstance().getJavaTypes(params, getJDTUtils(),
				new NullProgressMonitor());

		assertJavaTypes(actual, //
				t("org.acme.qute.NestedClass.Foo", JavaTypeKind.Class), //
				t("org.acme.qute.NestedClass.Bar", JavaTypeKind.Class));
	}

	public static JavaTypeInfo t(String typeName, JavaTypeKind kind) {
		JavaTypeInfo javaType = new JavaTypeInfo();
		javaType.setSignature(typeName);
		javaType.setKind(kind);
		return javaType;
	}

	/**
	 * Assert Java types.
	 *
	 * @param actual   the actual java types
	 * @param expected the expected Java types.
	 */
	public static void assertJavaTypes(List<JavaTypeInfo> actual, JavaTypeInfo... expected) {
		assertJavaTypes(actual, null, expected);
	}

	/**
	 * Assert Java types.
	 *
	 * @param actual        the actual java types.
	 * @param expectedCount Java types expected count.
	 * @param expected      the expected Java types.
	 */
	public static void assertJavaTypes(List<JavaTypeInfo> actual, Integer expectedCount, JavaTypeInfo... expected) {
		if (expectedCount != null) {
			Assert.assertEquals(expectedCount.intValue(), actual.size());
		}
		for (JavaTypeInfo javaType : expected) {
			assertJavaType(actual, javaType);
		}
	}

	/**
	 * Assert Java type.
	 *
	 * @param actual   the actual java types.
	 * @param expected the expected Java type.
	 */
	private static void assertJavaType(List<JavaTypeInfo> actualTypes, JavaTypeInfo expected) {
		List<JavaTypeInfo> matches = actualTypes.stream().filter(completion -> {
			return expected.getSignature().equals(completion.getSignature());
		}).collect(Collectors.toList());

		Assert.assertEquals(expected.getSignature() + " should only exist once: Actual: " //
				+ actualTypes.stream().map(c -> c.getSignature()).collect(Collectors.joining(",")), //
				1, matches.size());

		JavaTypeInfo actual = matches.get(0);
		Assert.assertEquals("Test 'type name' for '" + expected.getSignature() + "'", expected.getSignature(),
				actual.getSignature());
		Assert.assertEquals("Test 'kind' for '" + expected.getSignature() + "'", expected.getJavaElementKind(),
				actual.getJavaElementKind());
	}

}
