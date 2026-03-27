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
package com.redhat.qute.project.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;

/**
 * Test with {@link UserTagInfoCollector} which collects parameters (name,
 * required, default value) of a given user tag.
 */
public class UserTagInfoCollectorTest {

	// Object part parameter

	@Test
	public void paramFromObjectPart() {
		assertUserTagParameter("{foo}", //
				p("foo", true));
	}

	@Test
	public void optionalParamFromObjectPart() {
		assertUserTagParameter("{foo??}", //
				p("foo", false));
	}

	// -------------------- Ignore parameters

	@Test
	public void ignoreParametersFromNamespacePart() {
		assertUserTagParameter("{uri:Login}");
	}

	@Test
	public void ignoreParametersFromForSectionItem() {
		assertUserTagParameter("{#for item in items}\r\n" + //
				"    {item}\r\n" + //
				"{/for}", //
				p("items", true)); // <-- item is ignored
	}

	@Test
	public void ignoreParametersLiteral() {
		assertUserTagParameter("{123}");
		assertUserTagParameter("{123d}");
		assertUserTagParameter("{true}");
		assertUserTagParameter("{false}");
	}

	// -------------------- If section (optional parameters)

	@Test
	public void requiredParamFromIfSection() {
		assertUserTagParameter("{#if foo}Hello{/if}", //
				p("foo", true)); // <-- foo is required because no ??
	}

	@Test
	public void optionalParamFromIfSection() {
		assertUserTagParameter("{#if foo??}Hello{/if}", //
				p("foo", false)); // <-- foo is optional because of ??
	}

	@Test
	public void optionalParamFromIfSectionWithElse() {
		assertUserTagParameter("{#if foo??}Hello{#else}World{/if}", //
				p("foo", false)); // <-- foo is optional because of ??
	}

	// -------------------- Let/Set section

	@Test
	public void ignoreParamFromLetSection() {
		assertUserTagParameter("{#let foo='bar'}\n" + //
				"    {foo}\n" + //
				"{/let}"); // <-- foo is ignored, it's a local variable
	}

	@Test
	public void ignoreParamFromSetSection() {
		assertUserTagParameter("{#set foo='bar'}\n" + //
				"    {foo}\n" + //
				"{/set}"); // <-- foo is ignored, it's a local variable
	}

	@Test
	public void optionalParamFromLetSectionWithDefaultValue() {
		assertUserTagParameter("{#let foo?='bar'}\n" + //
				"    {foo}\n" + //
				"{/let}", //
				p("foo", false, "'bar'")); // <-- foo is optional with default value
	}

	// @Test
	public void paramFromLetSectionWithTernaryExpression() {
		assertUserTagParameter("{#let dialogId = (extDialogId ? extDialogId : str:concat('form-dlg-', title))}", //
				p("extDialogId", false), // <-- optional because of ?:
				p("title", true)); // <-- required
		// dialogId is ignored, it's a local variable
	}

	// -------------------- Or operator (?:)

	@Test
	public void optionalParamFromOrOperator() {
		assertUserTagParameter("{foo ?: 'default'}", //
				p("foo", false));
	}

	// -------------------- Multiple parameters

	@Test
	public void multipleParams() {
		assertUserTagParameter("{foo} {bar}", //
				p("foo", true), //
				p("bar", true));
	}

	@Test
	public void multipleParamsMixedRequiredAndOptional() {
		assertUserTagParameter("{foo} {bar??}", //
				p("foo", true), //
				p("bar", false));
	}

	// -------------------- _args

	@Test
	public void hasArgs() {
		Template template = TemplateParser.parse("{_args.skip('readonly')}", "test.qute");
		UserTagInfoCollector collector = new UserTagInfoCollector(null);
		template.accept(collector);
		assertTrue(collector.hasArgs());
		assertEquals(0, collector.getParameters().size());
	}

	@Test
	public void noArgs() {
		Template template = TemplateParser.parse("{foo}", "test.qute");
		UserTagInfoCollector collector = new UserTagInfoCollector(null);
		template.accept(collector);
		assertFalse(collector.hasArgs());
	}

	private static void assertUserTagParameter(String content, UserTagParameter... expected) {
		Template template = TemplateParser.parse(content, "test.qute");
		UserTagInfoCollector collector = new UserTagInfoCollector(null);
		template.accept(collector);

		var actual = collector.getParameters();
		assertEquals(expected.length, actual.size(), "Wrong parameters length");
		for (UserTagParameter expectedParameter : expected) {
			String name = expectedParameter.getName();
			UserTagParameter actualParameter = actual.get(name);
			assertNotNull(actualParameter, "Cannot find parameter with name=" + name);
			assertEquals(expectedParameter.isRequired(), actualParameter.isRequired(),
					"Wrong required for parameter name=" + name);
			assertEquals(expectedParameter.getDefaultValue(), actualParameter.getDefaultValue(),
					"Wrong defaultValue for parameter name=" + name);
		}
	}

	// -------------------- Parameter declarations

	@Test
	public void requiredParamFromParameterDeclaration() {
		assertUserTagParameter("{@java.lang.String title}", //
				p("title", true)); // <-- required because no default value
	}

	@Test
	public void optionalParamFromParameterDeclarationWithEmptyDefaultValue() {
		assertUserTagParameter("{@java.lang.String openButtonText = ''}", //
				p("openButtonText", false, "''")); // <-- optional because of default value
	}

	@Test
	public void optionalParamFromParameterDeclarationWithDefaultValue() {
		assertUserTagParameter("{@java.lang.String submitLabel = 'Submit'}", //
				p("submitLabel", false, "'Submit'")); // <-- optional because of default value
	}

	@Test
	public void multipleParamDeclarationsWithAndWithoutDefaultValue() {
		assertUserTagParameter("{@java.lang.String openButtonText = ''}\n" + //
				"{@java.lang.String submitLabel = 'Submit'}\n" + //
				"{@java.lang.String title}\n" + //
				"{@java.lang.Boolean isOpen}\n" + //
				"{@java.lang.String extDialogId}\n" + //
				"{@java.lang.String class = 'form-dialog dialog--sm'}", //
				p("title", true), // <-- required
				p("isOpen", true), // <-- required
				p("extDialogId", true), // <-- required
				p("openButtonText", false, "''"), // <-- optional with empty default value
				p("submitLabel", false, "'Submit'"), // <-- optional with default value
				p("class", false, "'form-dialog dialog--sm'")); // <-- optional with default value
	}

	private static UserTagParameter p(String name, boolean required) {
		return p(name, required, null);
	}

	private static UserTagParameter p(String name, boolean required, String defaultValue) {
		UserTagParameter p = new UserTagParameter(name);
		p.setRequired(required);
		p.setDefaultValue(defaultValue);
		return p;
	}
}
