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

import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Test with #with section
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithWithSectionTest {

	@Test
	public void missingObject() throws Exception {
		String template = "{#with}\r\n" + //
				"{/with}";
		Diagnostic d = d(0, 6, 0, 6, QuteErrorCode.SyntaxError,
				"Parser error on line 1: mandatory section parameters not declared for {#with}: [object]",
				DiagnosticSeverity.Error);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d);
	}

	@Test
	public void undefinedObject() throws Exception {
		String template = "{#with item}\r\n" + //
				"{/with}";

		Diagnostic d1 = d(0, 7, 0, 11, QuteErrorCode.UndefinedObject, "`item` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d1.setData(DiagnosticDataFactory.createUndefinedVariableData("item", false));

		Diagnostic d2 = d(0, 0, 1, 7, QuteErrorCode.NotRecommendedWithSection,
				"`with` is not recommended. Use `let` instead.", DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d1, d2);
		testCodeActionsFor(template, d1, //
				ca(d1, te(0, 0, 0, 0, "{@java.lang.String item}\r\n")));
		testCodeActionsFor(template, d2, //
				ca(d2, te(0, 1, 0, 11, "#let "), te(1, 1, 1, 6, "/let")));
	}

	@Test
	public void singleSection() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#with item}\r\n" + //
				"  <h1>{name}</h1>  \r\n" + //
				"  <p>{price}</p> \r\n" + //
				"{/with}";
		Diagnostic d = d(1, 0, 4, 7, QuteErrorCode.NotRecommendedWithSection,
				"`with` is not recommended. Use `let` instead.", DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 1, 1, 11, "#let price=item.price name=item.name"), te(4, 1, 4, 6, "/let")));
	}

	@Test
	public void nestedParameter() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#with item}\r\n" + //
				"  {#let foo=name} \r\n" + //
				"  {/let} \r\n" + //
				"{/with}";
		Diagnostic d = d(1, 0, 4, 7, QuteErrorCode.NotRecommendedWithSection,
				"`with` is not recommended. Use `let` instead.", DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 1, 1, 11, "#let name=item.name"), te(4, 1, 4, 6, "/let")));
	}

	@Test
	public void nested() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#with item}\r\n" + //
				"  <h1>{name}</h1>  \r\n" + //
				"  <p>{reviews}</p>\r\n" + //
				"  {#with reviews}\r\n" + //
				"	<p>{size}</p>\r\n" + //
				"	<p>{average}</p>\r\n" + // <-- error because reviews is a List
				"	<h1>{name}</h1>  \r\n" + //
				"	<h1>{item.name}</h1>\r\n" + //
				"	<h1>{data:item.name}</h1>\r\n" + //
				"  {/with}\r\n" + //
				"{/with}";

		Diagnostic d1 = d(1, 0, 11, 7, QuteErrorCode.NotRecommendedWithSection,
				"`with` is not recommended. Use `let` instead.", DiagnosticSeverity.Warning);

		Diagnostic d2 = d(4, 2, 10, 9, QuteErrorCode.NotRecommendedWithSection,
				"`with` is not recommended. Use `let` instead.", DiagnosticSeverity.Warning);

		Diagnostic d3 = d(6, 5, 6, 12, QuteErrorCode.UndefinedObject, "`average` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d3.setData(DiagnosticDataFactory.createUndefinedVariableData("average", false));

		testDiagnosticsFor(template, d1, d2, d3);
		testCodeActionsFor(template, d1, //
				ca(d1, te(1, 1, 1, 11, "#let reviews=item.reviews name=item.name"), te(11, 1, 11, 6, "/let")));
		testCodeActionsFor(template, d2, //
				ca(d2, te(4, 3, 4, 16,
						"#let average=reviews.average size=reviews.size name=reviews.name data:item.name=reviews.data:item.name item.name=reviews.item.name"),
						te(10, 3, 10, 8, "/let")));
		testCodeActionsFor(template, d3, //
				ca(d3, te(0, 0, 0, 0, "{@java.lang.String average}\r\n")));

	}
}
