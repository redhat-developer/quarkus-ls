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
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Test with #for section
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithForSectionTest {

	@Test
	public void definedObject() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedObjectInElseBlock() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"{#else}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}}";

		Diagnostic d = d(4, 2, 4, 6, QuteErrorCode.UndefinedObject, //
				"`item` cannot be resolved to an object.", DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("item", false));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String item}\r\n")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)), //
				ca(d, te(4, 6, 4, 6, "??")));

	}

	@Test
	public void undefinedObject() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in itemsXXX}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}";

		Diagnostic d = d(2, 14, 2, 22, QuteErrorCode.UndefinedObject, //
				"`itemsXXX` cannot be resolved to an object.", DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("itemsXXX", true));

		testDiagnosticsFor(template, d, //
				d(3, 2, 3, 6, QuteErrorCode.UnknownType, //
						"`item` cannot be resolved to a type.", //
						DiagnosticSeverity.Error));
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.util.List itemsXXX}\r\n")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)), //
				ca(d, te(2, 22, 2, 22, "??")));
	}

	@Test
	public void unknownProperty() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.nameXXX}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template, //
				d(3, 7, 3, 14, QuteErrorCode.UnknownProperty,
						"`nameXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						new UnknownPropertyData("org.acme.Item", "nameXXX"),
						DiagnosticSeverity.Error));
	}

	@Test
	public void noIterable() throws Exception {
		String template = "{@org.acme.Item items}\r\n" + // <-- here items is not an iterable Class
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template, //
				d(2, 14, 2, 19, QuteErrorCode.IterationError,
						"Iteration error: {items} resolved to [org.acme.Item] which is not iterable.",
						DiagnosticSeverity.Error),
				d(3, 2, 3, 6, QuteErrorCode.UnknownType, "`org.acme.Item` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void iterableWith2Parts() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#for review in item.reviews}\r\n" + // <- here 2 part in expression
				"		{review.average}    \r\n" + //
				"	{/for}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void noIterableWith2Parts() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#for review in item.name}\r\n" + // <- here 2 part in expression
				"		{review.average}    \r\n" + //
				"	{/for}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template, //
				d(3, 22, 3, 26, QuteErrorCode.IterationError,
						"Iteration error: {item.name} resolved to [java.lang.String] which is not iterable.",
						DiagnosticSeverity.Error),
				d(4, 3, 4, 9, QuteErrorCode.UnknownType, "`java.lang.String` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#expression_resolution
	 *
	 * @throws Exception
	 */
	@Test
	public void derived() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"<html>\r\n" + //
				"{item.name} \r\n" + //
				"<ul>\r\n" + //
				"{#for item in item.derivedItems} \r\n" + //
				"  <li>\r\n" + //
				"  {item.name} \r\n" + //
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + //
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testDiagnosticsFor(template);
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#expression_resolution
	 *
	 * @throws Exception
	 */
	@Test
	public void derivedWithObjectArray() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"<html>\r\n" + //
				"{item.name} \r\n" + //
				"<ul>\r\n" + //
				"{#for item in item.derivedItemArray} \r\n" + // derivedItemArray is an org.acme.Item item[]
				"  <li>\r\n" + //
				"  {item.name} \r\n" + //
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + //
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testDiagnosticsFor(template);
	}

	@Test
	public void integers() throws Exception {
		// integer
		String template = "{#for i in 3}\r\n" + //
				"	{i}:\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);

		// double
		template = "{#for i in 3d}\r\n" + //
				"	{i}:\r\n" + //
				"{/for}";
		testDiagnosticsFor(template, //
				d(0, 11, 0, 13, QuteErrorCode.IterationError,
						"Iteration error: {3d} resolved to [java.lang.Double] which is not iterable.",
						DiagnosticSeverity.Error),
				d(1, 2, 1, 3, QuteErrorCode.UnknownType, "`java.lang.Double` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void integersWithLet() throws Exception {
		// total = integer
		String template = "{#let total=3}\r\n" + //
				"	{#for i in total}\r\n" + //
				"		{i}:\r\n" + //
				"	{/for}	\r\n" + //
				"{/let}";
		testDiagnosticsFor(template);

		// total = double
		template = "{#let total=3d}\r\n" + //
				"	{#for i in total}\r\n" + //
				"		{i}:\r\n" + //
				"	{/for}	\r\n" + //
				"{/let}";
		testDiagnosticsFor(template, //
				d(1, 12, 1, 17, QuteErrorCode.IterationError,
						"Iteration error: {total} resolved to [java.lang.Double] which is not iterable.",
						DiagnosticSeverity.Error),
				d(2, 3, 2, 4, QuteErrorCode.UnknownType, "`java.lang.Double` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void integersWithMethodPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"	{#for i in items.size}\r\n" + //
				"		{i}\r\n" + //
				"	{/for}";
		testDiagnosticsFor(template);
	}

}
