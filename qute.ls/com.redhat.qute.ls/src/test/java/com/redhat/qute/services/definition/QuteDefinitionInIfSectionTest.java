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
package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

/**
 * Test definition with #if section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInIfSectionTest {

	@Test
	public void definedObject() throws Exception {
		String template = "{#let value=123}\r\n" + //
				"  {#if val|ue}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 7, 1, 12), r(0, 6, 0, 11)));
	}

	@Test
	public void undefinedObject() throws Exception {
		String template = "{#if val|ue}";
		testDefinitionFor(template);
	}

	@Test
	public void noOptionalParameterInIfBlock() throws Exception {
		String template = "{#if foo}\r\n" + //
				"	{fo|o}\r\n" + //
				"{/if}";
		testDefinitionFor(template);
	}

	@Test
	public void optionalParameterInIfBlock() throws Exception {
		String template = "{#if foo??}\r\n" + //
				"	{fo|o}\r\n" + //
				"{/if}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 2, 1, 5), r(0, 5, 0, 8)));

		template = "{#if f|oo??}\r\n" + //
				"	{foo}\r\n" + //
				"{/if}";
		testDefinitionFor(template);
	}

	@Test
	public void optionalParameterInIfBlockDeclaredInParentlet() throws Exception {
		String template = "{#let foo='bar'}\r\n" + //
				"  {#if foo??}\r\n" + //
				"        {f|oo}\r\n" + // find definition here --> {#let foo|
				"    {/if}\r\n" + //
				"{/let}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 9, 2, 12), r(0, 6, 0, 9)));

		template = "{#let foo='bar'}\r\n" + //
				"  {#if fo|o??}\r\n" + // find definition here --> {#let foo|
				"        {foo}\r\n" + "    {/if}\r\n" + //
				"{/let}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 7, 1, 10), r(0, 6, 0, 9)));
	}
}
