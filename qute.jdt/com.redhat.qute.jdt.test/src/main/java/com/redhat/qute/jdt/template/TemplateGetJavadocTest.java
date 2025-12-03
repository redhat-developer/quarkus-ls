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
package com.redhat.qute.jdt.template;

import static com.redhat.qute.jdt.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.QuteProjectTest.loadMavenProject;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import com.redhat.qute.commons.DocumentFormat;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.QuteSupportForTemplate;

/**
 * Tests for getting the formatted Javadocs for Java members
 * 
 * @author datho7561
 */
public class TemplateGetJavadocTest {

	@Test
	public void getFieldJavadoc() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavadocParams params = new QuteJavadocParams(//
				"org.acme.qute.Item", //
				QuteMavenProjectName.qute_quickstart, //
				"name", //
				"name : java.lang.String", //
				DocumentFormat.Markdown);

		String actual = QuteSupportForTemplate.getInstance().getJavadoc(params, getJDTUtils(), new NullProgressMonitor());
		String expected = "The name of the item";
		assertEquals(expected, actual);
	}
	
	@Test
	public void getMethodJavadoc() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavadocParams params = new QuteJavadocParams(//
				"org.acme.qute.Item", //
				QuteMavenProjectName.qute_quickstart, //
				"getDerivedItems", //
				"getDerivedItems() : org.acme.qute.Item[]", //
				DocumentFormat.Markdown);

		String actual = QuteSupportForTemplate.getInstance().getJavadoc(params, getJDTUtils(), new NullProgressMonitor());
		String expected = "Returns the derived items.\n\n *  **Returns:**\n    \n     *  the derived items";
		assertEquals(expected, actual);
	}
	
	@Test
	public void getFieldJavadocPlainText() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavadocParams params = new QuteJavadocParams(//
				"org.acme.qute.Item", //
				QuteMavenProjectName.qute_quickstart, //
				"name", //
				"name : java.lang.String", //
				DocumentFormat.PlainText);

		String actual = QuteSupportForTemplate.getInstance().getJavadoc(params, getJDTUtils(), new NullProgressMonitor());
		String expected = " The name of the item ";
		assertEquals(expected, actual);
	}
	
	@Test
	public void getMethodJavadocPlainText() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavadocParams params = new QuteJavadocParams(//
				"org.acme.qute.Item", //
				QuteMavenProjectName.qute_quickstart, //
				"getDerivedItems", //
				"getDerivedItems() : org.acme.qute.Item[]", //
				DocumentFormat.PlainText);

		String actual = QuteSupportForTemplate.getInstance().getJavadoc(params, getJDTUtils(), new NullProgressMonitor());
		String expected = " Returns the derived items.   Returns:\nthe derived items\n";// Updated jdt.ls JavaDoc2PlainTextConverter doesn't yield great results anymore
		assertEquals(expected, actual);
	}
	
	// @Test
	// Re-enable test when https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4672 will be fixed.
	public void getMethodJavadocCyclic() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavadocParams params = new QuteJavadocParams(//
				"org.acme.qute.cyclic.ClassC", //
				QuteMavenProjectName.qute_quickstart, //
				"convert", //
				"convert() : java.lang.String", //
				DocumentFormat.PlainText);

		String actual = QuteSupportForTemplate.getInstance().getJavadoc(params, getJDTUtils(), new NullProgressMonitor());
		String expected = " cyclic documentation ";
		assertEquals(expected, actual);
	}
	
	@Test
	public void getMethodJavadocMethodTypeParams() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavadocParams params = new QuteJavadocParams(//
				"org.acme.qute.generic.B", //
				QuteMavenProjectName.qute_quickstart, //
				"get", //
				"get(param : B2) : B1", //
				DocumentFormat.PlainText);

		String actual = QuteSupportForTemplate.getInstance().getJavadoc(params, getJDTUtils(), new NullProgressMonitor());
		String expected = " some docs ";
		assertEquals(expected, actual);
	}

}
