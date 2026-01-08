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
package com.redhat.qute.jdt.debug;

import static com.redhat.qute.jdt.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.QuteProjectTest.loadMavenProject;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.QuteSupportForDebug;

public class DebugGetStartLineTest {

	@Test
	public void validStartLine() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_record);

		// Simple string test with record
		// @TemplateContents(value = "Hello {name}!")
		// record Hello(String name) implements TemplateInstance {}
		assertStartLine(loc("org.acme.sample.TemplateContentsResource$Hello", "io.quarkus.qute.TemplateContents"), 11);

		// Text block test with record
		// @TemplateContents("""
		// Hello {name}!
		// """)
		// record Hello2(String name) implements TemplateInstance {}
		assertStartLine(loc("org.acme.sample.TemplateContentsResource$Hello2", "io.quarkus.qute.TemplateContents"), 15);

		// Simple string test with @CheckedTemplate
		// @CheckedTemplate
		// public static class Templates {
		// @TemplateContents("Item is {item}")
		// public static native TemplateInstance item(String item);
		// }
		assertStartLine(
				loc("org.acme.sample.TemplateContentsResource$Templates", "item", "io.quarkus.qute.TemplateContents"),
				21);
	}

	@Test
	public void invalidStartLine() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_record);

		// Invalid recod type
		assertStartLine(loc("org.acme.sample.TemplateContentsResource$HelloXXX", "io.quarkus.qute.TemplateContents"),
				null);

		// Invalid annotation
		assertStartLine(loc("org.acme.sample.TemplateContentsResource$Hello", "io.quarkus.qute.TemplateContentsXXX"),
				null);
	}

	private static void assertStartLine(JavaSourceLocationArguments args, Integer expected) {
		JavaSourceLocationResponse response = QuteSupportForDebug.getInstance().resolveJavaSource(args, getJDTUtils(),
				new NullProgressMonitor());
		Integer actual = response != null ? response.getStartLine() : null;
		assertEquals(expected, actual);
	}

	private static JavaSourceLocationArguments loc(String typeName, String annotation) {
		return loc(typeName, null, annotation);
	}

	private static JavaSourceLocationArguments loc(String typeName, String method, String annotation) {
		JavaSourceLocationArguments args = new JavaSourceLocationArguments();
		args.setTypeName(typeName);
		args.setMethod(method);
		args.setAnnotation(annotation);
		return args;
	}
}
