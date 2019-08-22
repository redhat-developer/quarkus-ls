/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.quarkus.jdt.core.JavaUtils;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusUtils;

/**
 * Test with {@link QuarkusClasspathListenerManager}
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusClasspathListenerManagerTest {

	final Set<String> projects = new HashSet<>();
	private IClasspathChangedListener listener = p -> {
		projects.addAll(p);
	};

	@Before
	public void init() {
		projects.clear();
		QuarkusClasspathListenerManager manager = QuarkusClasspathListenerManager.getInstance();
		manager.addClasspathChangedListener(listener);
	}

	@After
	public void destroy() {
		QuarkusClasspathListenerManager manager = QuarkusClasspathListenerManager.getInstance();
		manager.removeClasspathChangedListener(listener);
	}

	@Test
	public void classpathChanged() throws Exception {
		Assert.assertEquals(0, projects.size());

		// Create a Java project -> classpath changed
		IJavaProject project = JavaUtils.createJavaProject("test-classpath-changed", new String[] { "/test.jar" });
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(JDTQuarkusUtils.getProjectURI(project), projects.iterator().next());

		projects.clear();
		Assert.assertEquals(0, projects.size());

		// Update classpath -> classpath changed
		project.setRawClasspath(new IClasspathEntry[] {}, new NullProgressMonitor());
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(JDTQuarkusUtils.getProjectURI(project), projects.iterator().next());
	}
}
