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
 * Test with expressions.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionTest {

	@Test
	public void invalidIdentifiers() throws Exception {

		Diagnostic d = d(0, 1, 0, 4, QuteErrorCode.UndefinedVariable, "`foo` cannot be resolved to a variable.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("foo", false));		
		testDiagnosticsFor("{foo}", d);

		d = d(0, 1, 0, 5, QuteErrorCode.UndefinedVariable, "`_foo` cannot be resolved to a variable.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("_foo", false));
		testDiagnosticsFor("{_foo}", d);
		
		testDiagnosticsFor("{ foo}");
		testDiagnosticsFor("{{foo}}");
		testDiagnosticsFor("{\"foo\":true}");
	}
	
	@Test
	public void booleanLiteral() throws Exception {
		String template = "{true}";

		testDiagnosticsFor(template);

		template = "{trueX}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.UndefinedVariable, "`trueX` cannot be resolved to a variable.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("trueX", false));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String trueX}" + //
						System.lineSeparator())));

		template = "{false}";
		testDiagnosticsFor(template);

		template = "{falseX}";
		d = d(0, 1, 0, 7, QuteErrorCode.UndefinedVariable, "`falseX` cannot be resolved to a variable.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("falseX", false));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String falseX}" + //
						System.lineSeparator())));
	}

	@Test
	public void nullLiteral() throws Exception {
		String template = "{null}";
		testDiagnosticsFor(template);

		template = "{nullX}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.UndefinedVariable, "`nullX` cannot be resolved to a variable.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("nullX", false));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String nullX}" + //
						System.lineSeparator())));
	}

	@Test
	public void stringLiteral() throws Exception {
		String template = "{\"abcd\"}"; // it's not an expression
		testDiagnosticsFor(template);

		template = "{'abcd'}"; // it's not an expression
		testDiagnosticsFor(template);
	}

	@Test
	public void integerLiteral() throws Exception {
		String template = "{123}";
		testDiagnosticsFor(template);

		template = "{123X}";
		Diagnostic d = d(0, 1, 0, 5, QuteErrorCode.UndefinedVariable, "`123X` cannot be resolved to a variable.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("123X", false));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String 123X}" + //
						System.lineSeparator())));
	}

	@Test
	public void longLiteral() throws Exception {
		String template = "{123L}";
		testDiagnosticsFor(template);

		template = "{123LX}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.UndefinedVariable, "`123LX` cannot be resolved to a variable.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("123LX", false));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String 123LX}" + //
						System.lineSeparator())));
	}

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{item}";

		Diagnostic d = d(0, 1, 0, 5, //
				QuteErrorCode.UndefinedVariable, //
				"`item` cannot be resolved to a variable.", //
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("item", false));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String item}" + //
						System.lineSeparator())));
	}

	@Test
	public void definedVariableWithParameterDeclaration() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownProperty() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnkwownProperty,
						"`XXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX.YYYY}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnkwownProperty,
						"`XXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.YYYY}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 15, QuteErrorCode.UnkwownProperty,
						"`YYYY` cannot be resolved or is not a field of `java.lang.String` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void kwownProperty() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.UTF16}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownMethod() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX()}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnkwownMethod,
						"`XXXX` cannot be resolved or is not a method of `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX().YYYY}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnkwownMethod,
						"`XXXX` cannot be resolved or is not a method of `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.YYYY()}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 15, QuteErrorCode.UnkwownMethod,
						"`YYYY` cannot be resolved or is not a method of `java.lang.String` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void kwownMethod() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview2()}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview2().average}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview2}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.review2}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.review2.average}";
		testDiagnosticsFor(template);
	}

	@Test
	public void getterWithBoolean() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.isAvailable()}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.isAvailable}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.available}";
		testDiagnosticsFor(template);
	}

	@Test
	public void kwownMethodForIterable() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size}";
		testDiagnosticsFor(template);

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size()}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownMethodForIterable() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.sizeXXX}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 14, QuteErrorCode.UnkwownProperty,
						"`sizeXXX` cannot be resolved or is not a field of `java.util.List` Java type.",
						DiagnosticSeverity.Error));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.sizeXXX()}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 14, QuteErrorCode.UnkwownMethod,
						"`sizeXXX` cannot be resolved or is not a method of `java.util.List` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void unkwownMethodForPrimitive() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size.XXX}";
		testDiagnosticsFor(template, d(1, 12, 1, 15, QuteErrorCode.UnkwownProperty,
				"`XXX` cannot be resolved or is not a field of `int` Java type.", DiagnosticSeverity.Error));
	}

	@Test
	public void elvisOperator() throws Exception {
		String template = "{person.name ?: 'John'}";

		Diagnostic d = d(0, 1, 0, 7, QuteErrorCode.UndefinedVariable, "`person` cannot be resolved to a variable.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("person", false));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String person}" + //
						System.lineSeparator())));
	}

	@Test
	public void definedNamespace() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{data:item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefineNamespace() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{dataXXX:item}";
		testDiagnosticsFor(template, //
				d(1, 1, 1, 8, QuteErrorCode.UndefinedNamespace, "No namespace resolver found for: `dataXXX`",
						DiagnosticSeverity.Warning));
	}

	@Test
	public void objectPartEndsWithDot() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.}";
		testDiagnosticsFor(template, d(1, 6, 1, 7, QuteErrorCode.SyntaxError, //
				"Syntax error: `Unexpected '.' token.`.", DiagnosticSeverity.Error));
	}

	@Test
	public void propertyPartEndsWithDot() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size.}";
		testDiagnosticsFor(template, d(1, 11, 1, 12, QuteErrorCode.SyntaxError, //
				"Syntax error: `Unexpected '.' token.`.", DiagnosticSeverity.Error));
	}
}
