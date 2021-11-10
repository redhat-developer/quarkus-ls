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
package com.redhat.qute.jdt;

import static com.redhat.qute.jdt.internal.QuteProjectTest.loadMavenProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.qute.jdt.internal.QuteProjectTest.QuteMavenProjectName;

/**
 * Test classpath.
 * 
 * @author Angelo ZERR
 *
 */
public class ClasspathTest {

	@Test
	public void findType() throws CoreException, Exception {
		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		// Find Java source class
		IType typeFromSource = javaProject.findType("org.acme.qute.HelloResource");
		Assert.assertNotNull("Test find 'org.acme.qute.HelloResource' from Java source.", typeFromSource);
		// Find JDK class
		IType typeFromJDK = javaProject.findType("java.lang.String");
		Assert.assertNotNull("Test find 'java.lang.String' from JDK.", typeFromJDK);
		// Find Qute Maven dependency
		IType typeFromDep = javaProject.findType("io.quarkus.runtime.ApplicationConfig");
		Assert.assertNotNull("Test find 'io.quarkus.runtime.ApplicationConfig' from Maven dependency.", typeFromDep);
		// Find Qute Maven dependency (class)
		IType quteTypeFromDep = javaProject.findType("io.quarkus.qute.EngineBuilder");
		Assert.assertNotNull("Test find 'io.quarkus.qute.EngineBuilder' from Maven dependency.", quteTypeFromDep);
		// Find Qute Maven dependency (interface)
		quteTypeFromDep = javaProject.findType("io.quarkus.qute.Template");
		Assert.assertNotNull("Test find 'io.quarkus.qute.Template' from Maven dependency.", quteTypeFromDep);
	}
}
