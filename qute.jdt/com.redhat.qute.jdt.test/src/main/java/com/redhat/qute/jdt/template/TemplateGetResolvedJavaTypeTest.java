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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.QuteProjectTest.QuteMavenProjectName;

public class TemplateGetResolvedJavaTypeTest {

	@Test
	public void iterable() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.lang.Iterable",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.lang.Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

		params = new QuteResolvedJavaTypeParams("Iterable", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("Iterable", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

		params = new QuteResolvedJavaTypeParams("Iterable<String>", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("Iterable<String>", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("Iterable", result.getIterableType());
		Assert.assertEquals("String", result.getIterableOf());

		params = new QuteResolvedJavaTypeParams("Iterable<java.lang.String>", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("Iterable<java.lang.String>", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.String", result.getIterableOf());

		params = new QuteResolvedJavaTypeParams("java.lang.Iterable<java.lang.String>",
				QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<java.lang.String>", result.getClassName());
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
		Assert.assertEquals("java.util.List", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.util.List", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

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
		Assert.assertEquals("java.util.List<java.lang.String>", result.getClassName());
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
		Assert.assertEquals("org.acme.qute.SomeInterface", result.getClassName());
		Assert.assertFalse(result.isIterable());

		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("getName() : java.lang.String", result.getMethods().get(0).getSignature());
	}

	@Test
	public void methodArray() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("org.acme.qute.Item", result.getClassName());
		Assert.assertFalse(result.isIterable());
		
		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("getDerivedItems() : org.acme.qute.Item[]", result.getMethods().get(0).getSignature());
	}
}
