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
import static com.redhat.qute.QuteAssert.p;
import static com.redhat.qute.QuteAssert.testInlayHintFor;

import org.junit.jupiter.api.Test;

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
				ih(p(1, 10), ":Item"));

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
				ih(p(1, 10), ":String"), //
				ih(p(1, 26), ":BigInteger"));

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
	public void parameterCustomSection() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#form name=item.name item.price bad=item.XXXX}\r\n" + // name[:String]=item.name
																		// item.price[:BigInteger]
																		// bad=item.XXXX
				"  \r\n" + //
				"{/form}";
		testInlayHintFor(template, //
				ih(p(1, 11), ":String"), //
				ih(p(1, 32), ":BigInteger"));

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
				ih(p(1, 21), ":Item"));

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
