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
package com.redhat.qute.services.inlayhint.sections.let;

import static com.redhat.qute.QuteAssert.ih;
import static com.redhat.qute.QuteAssert.ihLabel;
import static com.redhat.qute.QuteAssert.p;
import static com.redhat.qute.QuteAssert.testInlayHintFor;

import org.eclipse.lsp4j.Command;
import org.junit.jupiter.api.Test;

import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.services.inlayhint.InlayHintASTVistor;
import com.redhat.qute.settings.QuteInlayHintSettings;

/**
 * Tests for Qute inlay hint with #let section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteInlayHintWithLetTest {

	@Test
	public void parameterLetSection() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let name=item.name price=item.price bad=item.XXXX}\r\n" + // name[:String]=item.name
																				// price[:BigInteger]=item.price
																				// bad=item.XXXX
				"  \r\n" + //
				"{/let}";
		testInlayHintFor(template, //
				ih(p(1, 10), ihLabel(":"),
						ihLabel("String", "Open `java.lang.String` Java type.", cd("java.lang.String"))), //
				ih(p(1, 26), ihLabel(":"),
						ihLabel("BigInteger", "Open `java.math.BigInteger` Java type.", cd("java.math.BigInteger"))));

		// enabled=false
		QuteInlayHintSettings settings = new QuteInlayHintSettings();
		settings.setEnabled(false);
		testInlayHintFor(template, //
				settings);

		// showSectionParameterType=false
		settings = new QuteInlayHintSettings();
		settings.setShowSectionParameterType(false);
		testInlayHintFor(template, //
				settings);
	}

	@Test
	public void parameterLetSectionWithSpaces() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let name    =item.name price   =  item.price   bad=  item.XXXX}\r\n" + // name[:String]=item.name
				// price[:BigInteger]=item.price
				// bad=item.XXXX
				"  \r\n" + //
				"{/let}";
		testInlayHintFor(template, //
				ih(p(1, 10), ihLabel(":"),
						ihLabel("String", "Open `java.lang.String` Java type.", cd("java.lang.String"))), //
				ih(p(1, 30), ihLabel(":"),
						ihLabel("BigInteger", "Open `java.math.BigInteger` Java type.", cd("java.math.BigInteger"))));

		// enabled=false
		QuteInlayHintSettings settings = new QuteInlayHintSettings();
		settings.setEnabled(false);
		testInlayHintFor(template, //
				settings);

		// showSectionParameterType=false
		settings = new QuteInlayHintSettings();
		settings.setShowSectionParameterType(false);
		testInlayHintFor(template, //
				settings);
	}

	@Test
	public void ifTruthy() throws Exception {
		String template = "{@java.lang.Boolean multiply}\r\n" + //
				"{#let name=multiply.ifTruthy(\"arg\")}";
		testInlayHintFor(template, //
				ih(p(1, 10), ihLabel(":"),
						ihLabel("String", "Open `java.lang.String` Java type.", cd("java.lang.String"))));
	}

	@Test
	public void parameterLetSectionWithComma() throws Exception {
		String template = "{@java.lang.Boolean multiply}\r\n" + //
				"	{#let bool=(multiply && multiply)\r\n" + //
				"	      string=(multiply ? 'foo' : 'bar')}\r\n" + //
				"	{/let}";

		testInlayHintFor(template, //
				ih(p(1, 11), ihLabel(":"),
						ihLabel("Boolean", "Open `java.lang.Boolean` Java type.", cd("java.lang.Boolean"))), //
				ih(p(2, 13), ihLabel(":"),
						ihLabel("String", "Open `java.lang.String` Java type.", cd("java.lang.String"))));

		// enabled=false
		QuteInlayHintSettings settings = new QuteInlayHintSettings();
		settings.setEnabled(false);
		testInlayHintFor(template, //
				settings);

		// showSectionParameterType=false
		settings = new QuteInlayHintSettings();
		settings.setShowSectionParameterType(false);
		testInlayHintFor(template, //
				settings);
	}

	@Test
	public void parameterLetSectionWithTernary() throws Exception {
		String template = "{@java.lang.Boolean multiply}\r\n" + //
				"	{#let int=(multiply ? 'foo' : 123)}\r\n" + //
				"	{/let}";

		testInlayHintFor(template, //
				ih(p(1, 10), ihLabel(":"),
						ihLabel("Integer", "Open `java.lang.Integer` Java type.", cd("java.lang.Integer"))));

		// enabled=false
		QuteInlayHintSettings settings = new QuteInlayHintSettings();
		settings.setEnabled(false);
		testInlayHintFor(template, //
				settings);

		// showSectionParameterType=false
		settings = new QuteInlayHintSettings();
		settings.setShowSectionParameterType(false);
		testInlayHintFor(template, //
				settings);
	}

	@Test
	public void parameterLetSectionWithTernaryInvalidType() throws Exception {
		String template = "{#let wrong=(multiply ? 'foo' : tr)}\r\n" + //
				"	{/let}";

		testInlayHintFor(template);

		// enabled=false
		QuteInlayHintSettings settings = new QuteInlayHintSettings();
		settings.setEnabled(false);
		testInlayHintFor(template, //
				settings);

		// showSectionParameterType=false
		settings = new QuteInlayHintSettings();
		settings.setShowSectionParameterType(false);
		testInlayHintFor(template, //
				settings);
	}
	
	private static Command cd(String javaType) {
		return InlayHintASTVistor.createOpenJavaTypeCommand(javaType, QuteQuickStartProject.PROJECT_URI);
	}
}
