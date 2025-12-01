/*******************************************************************************
* Copyright (c) 2025 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in text node to generate #for, #each in smart mode.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionsForSmartIterableTest {

	@Test
	public void smartListWithEmptyText() throws Exception {
		String template = "{@java.util.List<org.acme.Order> orders}\r\n" + //
				"|";
		testCompletionFor(template, //
				true, // snippet support
				c("#each orders", //
						"{#each orders}" + System.lineSeparator() + //
								"\t{it}$0" + System.lineSeparator() + //
								"{/each}", //
						r(1, 0, 1, 0)));
		testCompletionFor(template, //
				true, // snippet support
				c("#for orders", //
						"{#for ${1:order} in orders}" + System.lineSeparator() + //
								"\t{${1:order}}$0" + System.lineSeparator() + //
								"{/for}", //
						r(1, 0, 1, 0)));
	}

	@Test
	public void smartListWithText() throws Exception {
		String template = "{@java.util.List<org.acme.Order> orders}\r\n" + //
				"or|d";
		testCompletionFor(template, //
				true, // snippet support
				c("#each orders", //
						"{#each orders}" + System.lineSeparator() + //
								"\t{it}$0" + System.lineSeparator() + //
								"{/each}", //
						r(1, 0, 1, 2)));
		testCompletionFor(template, //
				true, // snippet support
				c("#for orders", //
						"{#for ${1:order} in orders}" + System.lineSeparator() + //
								"\t{${1:order}}$0" + System.lineSeparator() + //
								"{/for}", //
						r(1, 0, 1, 2)));
	}

	@Test
	public void smartListWithTextInsideHTMLTag() throws Exception {
		String template = "{@java.util.List<org.acme.Order> orders}\r\n" + //
				"<p>or|d</p>";
		testCompletionFor(template, //
				true, // snippet support
				c("#each orders", //
						"{#each orders}" + System.lineSeparator() + //
								"\t{it}$0" + System.lineSeparator() + //
								"{/each}", //
						r(1, 3, 1, 6)));
		testCompletionFor(template, //
				true, // snippet support
				c("#for orders", //
						"{#for ${1:order} in orders}" + System.lineSeparator() + //
								"\t{${1:order}}$0" + System.lineSeparator() + //
								"{/for}", //
						r(1, 3, 1, 6)));
	}

	@Test
	public void smartListWithCountries() throws Exception {
		String template = "{@java.util.List<org.acme.Country> countries}\r\n" + //
				"<p>co|n</p>";
		testCompletionFor(template, //
				true, // snippet support
				c("#each countries", //
						"{#each countries}" + System.lineSeparator() + //
								"\t{it}$0" + System.lineSeparator() + //
								"{/each}", //
						r(1, 3, 1, 6)));
		testCompletionFor(template, //
				true, // snippet support
				c("#for countries", //
						"{#for ${1:country} in countries}" + System.lineSeparator() + //
								"\t{${1:country}}$0" + System.lineSeparator() + //
								"{/for}", //
						r(1, 3, 1, 6)));
	}

	@Test
	public void smartListWithPeople() throws Exception {
		String template = "{@java.util.List<java.lang.String> people}\r\n" + //
				"<p>pe|o</p>";
		testCompletionFor(template, //
				true, // snippet support
				c("#each people", //
						"{#each people}" + System.lineSeparator() + //
								"\t{it}$0" + System.lineSeparator() + //
								"{/each}", //
						r(1, 3, 1, 6)));
		testCompletionFor(template, //
				true, // snippet support
				c("#for people", //
						"{#for ${1:person} in people}" + System.lineSeparator() + //
								"\t{${1:person}}$0" + System.lineSeparator() + //
								"{/for}", //
						r(1, 3, 1, 6)));
	}

	@Test
	public void smartListWithMoose() throws Exception {
		String template = "{@java.util.List<java.lang.String> moose}\r\n" + //
				"<p>mo|o</p>";
		testCompletionFor(template, //
				true, // snippet support
				c("#each moose", //
						"{#each moose}" + System.lineSeparator() + //
								"\t{it}$0" + System.lineSeparator() + //
								"{/each}", //
						r(1, 3, 1, 6)));
		testCompletionFor(template, //
				true, // snippet support
				c("#for moose", //
						"{#for ${1:string} in moose}" + System.lineSeparator() + //
								"\t{${1:string}}$0" + System.lineSeparator() + //
								"{/for}", //
						r(1, 3, 1, 6)));
	}

	@Test
	public void smartListWithBar() throws Exception {
		String template = "{@java.util.List<java.lang.String> bar}\r\n" + //
				"<p>ba|r</p>";
		testCompletionFor(template, //
				true, // snippet support
				c("#each bar", //
						"{#each bar}" + System.lineSeparator() + //
								"\t{it}$0" + System.lineSeparator() + //
								"{/each}", //
						r(1, 3, 1, 6)));
		testCompletionFor(template, //
				true, // snippet support
				c("#for bar", //
						"{#for ${1:string} in bar}" + System.lineSeparator() + //
								"\t{${1:string}}$0" + System.lineSeparator() + //
								"{/for}", //
						r(1, 3, 1, 6)));
	}

}