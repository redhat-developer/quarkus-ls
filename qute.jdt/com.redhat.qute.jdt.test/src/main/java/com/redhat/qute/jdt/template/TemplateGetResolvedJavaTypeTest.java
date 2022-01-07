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

import static com.redhat.qute.jdt.internal.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.internal.QuteProjectTest.loadMavenProject;

import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.QuteProjectTest.QuteMavenProjectName;

/**
 * Tests for
 * {@link QuteSupportForTemplate#getResolvedJavaType(QuteResolvedJavaTypeParams, com.redhat.qute.jdt.utils.IJDTUtils, org.eclipse.core.runtime.IProgressMonitor)}
 *
 * @author Angelo ZERR
 *
 */
public class TemplateGetResolvedJavaTypeTest {

	@Test
	public void iterable() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.lang.Iterable",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<T>", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.lang.Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

		params = new QuteResolvedJavaTypeParams("Iterable", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<T>", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.lang.Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

		params = new QuteResolvedJavaTypeParams("Iterable<String>", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<java.lang.String>", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.lang.Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.String", result.getIterableOf());

		params = new QuteResolvedJavaTypeParams("Iterable<java.lang.String>", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<java.lang.String>", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.lang.Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.String", result.getIterableOf());

		params = new QuteResolvedJavaTypeParams("java.lang.Iterable<java.lang.String>",
				QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<java.lang.String>", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.lang.Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.String", result.getIterableOf());
	}

	@Test
	public void list() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.util.List",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.util.List<E>", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.util.List", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

		// Invalid method void clear();
		JavaMethodInfo clearMethod = findMethod(result, "clear");
		Assert.assertNull(clearMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("clear");
		Assert.assertEquals(InvalidMethodReason.VoidReturn, reason);

		params = new QuteResolvedJavaTypeParams("List", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNull(result);

		params = new QuteResolvedJavaTypeParams("List<String>", QuteMavenProjectName.qute_quickstart);
		Assert.assertNull(result);

		params = new QuteResolvedJavaTypeParams("List<java.lang.String>", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNull(result);

		params = new QuteResolvedJavaTypeParams("java.util.List<java.lang.String>",
				QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.util.List<java.lang.String>", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.util.List", result.getIterableType());
		Assert.assertEquals("java.lang.String", result.getIterableOf());
	}

	@Test
	public void someInterface() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.SomeInterface",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("org.acme.qute.SomeInterface", result.getSignature());
		Assert.assertFalse(result.isIterable());

		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("getName() : java.lang.String", result.getMethods().get(0).getSignature());
	}

	@Test
	public void item() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("org.acme.qute.Item", result.getSignature());
		Assert.assertFalse(result.isIterable());

		// Fields
		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(2, result.getFields().size());
		Assert.assertEquals("name", result.getFields().get(0).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(0).getType());
		Assert.assertEquals("price", result.getFields().get(1).getName());
		Assert.assertEquals("java.math.BigDecimal", result.getFields().get(1).getType());

		// Methods
		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("getDerivedItems() : org.acme.qute.Item[]", result.getMethods().get(0).getSignature());

		// Invalid methods(static method)
		JavaMethodInfo discountedPriceMethod = findMethod(result, "staticMethod");
		Assert.assertNull(discountedPriceMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("staticMethod");
		Assert.assertEquals(InvalidMethodReason.Static, reason);

	}

	@Test
	public void itemArray() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.Item[]",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("org.acme.qute.Item[]", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertNull(result.getIterableType());
		Assert.assertTrue(result.isArray());
		Assert.assertEquals("org.acme.qute.Item", result.getIterableOf());

		// Fields
		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(1, result.getFields().size());
		Assert.assertEquals("length", result.getFields().get(0).getName());
		Assert.assertEquals("int", result.getFields().get(0).getType());
	}

	@Test
	public void stringArray() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("String[]",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("java.lang.String[]", result.getSignature());
		Assert.assertTrue(result.isIterable());
		Assert.assertNull(result.getIterableType());
		Assert.assertTrue(result.isArray());
		Assert.assertEquals("java.lang.String", result.getIterableOf());
		
		// Fields
		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(1, result.getFields().size());
		Assert.assertEquals("length", result.getFields().get(0).getName());
		Assert.assertEquals("int", result.getFields().get(0).getType());
	}

	@Test
	public void record() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_java17);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.RecordItem",
				QuteMavenProjectName.qute_java17);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("org.acme.qute.RecordItem", result.getSignature());
		Assert.assertFalse(result.isIterable());

		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(2, result.getFields().size());
		Assert.assertEquals("name", result.getFields().get(0).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(0).getType());
		Assert.assertEquals("price", result.getFields().get(1).getName());
		Assert.assertEquals("java.math.BigDecimal", result.getFields().get(1).getType());

	}

	@Test
	public void object() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.lang.Object",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("java.lang.Object", result.getSignature());
		Assert.assertFalse(result.isIterable());

		// None valid methods
		Assert.assertNotNull(result.getMethods());
		Assert.assertTrue(result.getMethods().isEmpty());

		// Invalid method codePointAt(int index)
		JavaMethodInfo waitMethod = findMethod(result, "wait");
		Assert.assertNull(waitMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("wait");
		Assert.assertEquals(InvalidMethodReason.FromObject, reason);
	}

	@Test
	public void string() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.lang.String",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("java.lang.String", result.getSignature());
		Assert.assertFalse(result.isIterable());

		Assert.assertNotNull(result.getMethods());

		// Valid method isEmpty()
		JavaMethodInfo isEmptyMethod = findMethod(result, "isEmpty");
		Assert.assertNotNull(isEmptyMethod);

		// Invalid method void getChars(int srcBegin, int srcEnd, char dst[], int
		// dstBegin) {
		JavaMethodInfo getCharsMethod = findMethod(result, "getChars");
		Assert.assertNull(getCharsMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("getChars");
		Assert.assertEquals(InvalidMethodReason.VoidReturn, reason);
	}

	private static JavaMethodInfo findMethod(ResolvedJavaTypeInfo javaType, String methodName) {
		Optional<JavaMethodInfo> result = javaType.getMethods().stream() //
				.filter(m -> methodName.equals(m.getName())) //
				.findFirst();
		return result.isPresent() ? result.get() : null;
	}

}
