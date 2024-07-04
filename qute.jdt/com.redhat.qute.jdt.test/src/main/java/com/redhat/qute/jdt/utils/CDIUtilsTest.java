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
package com.redhat.qute.jdt.utils;

import static com.redhat.qute.jdt.QuteProjectTest.loadMavenProject;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.internal.ls.JDTUtilsLSImpl;

/**
 * Tests for {@link CDIUtils}.
 *
 * @author Angelo ZERR
 *
 */
public class CDIUtilsTest {

	@Test
	public void namedWithType() {
		String javaType = "MyClass";

		String name = CDIUtils.getSimpleName(javaType, null, IJavaElement.TYPE);
		Assert.assertEquals("myClass", name);

		String named = "foo";
		name = CDIUtils.getSimpleName(javaType, named, IJavaElement.TYPE);
		Assert.assertEquals("foo", name);
	}

	@Test
	public void namedWithField() {
		String javaType = "MyField";

		String name = CDIUtils.getSimpleName(javaType, null, IJavaElement.FIELD);
		Assert.assertEquals("MyField", name);

		String named = "foo";
		name = CDIUtils.getSimpleName(javaType, named, IJavaElement.FIELD);
		Assert.assertEquals("foo", name);
	}

	@Test
	public void namedWithMethod() {
		String javaType = "MyMethod";

		String name = CDIUtils.getSimpleName(javaType, null, IJavaElement.METHOD);
		Assert.assertEquals("MyMethod", name);

		String named = "foo";
		name = CDIUtils.getSimpleName(javaType, named, IJavaElement.METHOD);
		Assert.assertEquals("foo", name);
	}

	@Test
	public void namedWithGetterMethod() {
		String javaType = "getMethod";

		String name = CDIUtils.getSimpleName(javaType, null, IJavaElement.METHOD, () -> true);
		Assert.assertEquals("method", name);

		String named = "foo";
		name = CDIUtils.getSimpleName(javaType, named, IJavaElement.METHOD, () -> true);
		Assert.assertEquals("foo", name);
	}

	@Test
	public void isBeanQuarkus3() throws Exception {
		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.quarkus3);

		IType notBean1 = getCompilationUnit("src/main/java/org/acme/NotBean1.java", javaProject);
		// @Decorator annotated class is not a bean
		assertFalse(CDIUtils.isValidBean(notBean1));

		IType notBean2 = getCompilationUnit("src/main/java/org/acme/NotBean2.java", javaProject);
		// @Vetoed annotated class is not a bean
		assertFalse(CDIUtils.isValidBean(notBean2));

		IType bean1 = getCompilationUnit("src/main/java/org/acme/Bean1.java", javaProject);
		// Empty class is a bean
		assertTrue(CDIUtils.isValidBean(bean1));

		// Class with constructor is a bean
		IType bean3 = getCompilationUnit("src/main/java/org/acme/Bean3.java", javaProject);
		assertTrue(CDIUtils.isValidBean(bean3));
	}

	private static IType getCompilationUnit(String javaFilePath, IJavaProject javaProject) {
		IFile javaFile = javaProject.getProject().getFile(new Path(javaFilePath));
		return JDTUtilsLSImpl.getInstance().resolveCompilationUnit(javaFile.getLocationURI().toString()).findPrimaryType();
	}
}
