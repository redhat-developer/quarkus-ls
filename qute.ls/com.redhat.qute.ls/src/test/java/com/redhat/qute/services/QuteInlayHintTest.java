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
package com.redhat.qute.services;

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
 * Tests for Qute inlay hint.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteInlayHintTest {

	@Test
	public void aliasForSection() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + // item[:Item]
				"  {item.name}\r\n" + //
				"{/for}";
		testInlayHintFor(template, //
				ih(p(1, 10), ihLabel(":"), ihLabel("Item", "Open `org.acme.Item` Java type.", cd("org.acme.Item"))));

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

	private static Command cd(String javaType) {
		return InlayHintASTVistor.createJavaDefinitionCommand(javaType, QuteQuickStartProject.PROJECT_URI);
	}

	@Test
	public void parameterCustomSection() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#form name=item.name item.price bad=item.XXXX}\r\n" + // name[:String]=item.name
																		// item.price[:BigInteger]
																		// bad=item.XXXX
				"  \r\n" + //
				"{/form}";
		testInlayHintFor(template, //
				ih(p(1, 11), ihLabel(":"),
						ihLabel("String", "Open `java.lang.String` Java type.", cd("java.lang.String"))), //
				ih(p(1, 32), ihLabel(":"),
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
	public void optionalParameterIfSection() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#if foo?? and item??}\r\n" + // item??[:Item]
				"  \r\n" + //
				"{/if}";
		testInlayHintFor(template, //
				ih(p(1, 10), ":?"), //
				ih(p(1, 21), ihLabel(":"), ihLabel("Item", "Open `org.acme.Item` Java type.", cd("org.acme.Item"))));

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

}
