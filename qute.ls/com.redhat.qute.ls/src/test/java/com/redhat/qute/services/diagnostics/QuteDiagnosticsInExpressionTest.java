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
	public void undefinedUnderscoreVariable() throws Exception {
		String template = "{nested-content}";

		Diagnostic d = d(0, 1, 0, 15, //
				QuteErrorCode.UndefinedVariable, //
				"`nested-content` cannot be resolved to a variable.", //
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("nested-content", false));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String nested-content}" + //
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
						"`sizeXXX` cannot be resolved or is not a field of `java.util.List<E>` Java type.",
						DiagnosticSeverity.Error));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.sizeXXX()}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 14, QuteErrorCode.UnkwownMethod,
						"`sizeXXX` cannot be resolved or is not a method of `java.util.List<E>` Java type.",
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

	@Test
	public void invalidMethodVoid() {
		String template = "{@java.lang.String string}\r\n" + //
				"{string.isEmpty()}\r\n" + //
				"{string.getChars()}\r\n";

		testDiagnosticsFor(template, //
				d(2, 8, 2, 16, QuteErrorCode.InvalidMethodVoid,
						"Invalid `getChars` method of `java.lang.String` : void return is not allowed.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void invalidMethodStatic() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name}\r\n" + //
				"{item.staticMethod(1)}\r\n";

		testDiagnosticsFor(template, //
				d(2, 6, 2, 18, QuteErrorCode.InvalidMethodStatic,
						"Invalid `staticMethod` method of `org.acme.Item` : static method is not allowed.",
						DiagnosticSeverity.Error));
	}

	public void virtualMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{item.discountedPrice}";
		testDiagnosticsFor(template);
	}

	@Test
	public void listGeneric() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.get(0).name}";
		testDiagnosticsFor(template);
	}

	@Test
	public void returnPrimitiveTypeArray() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{foo.getBytes('abcd')}";
		testDiagnosticsFor(template);
	}

	@Test
	public void quoteNoteClosed() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{foo.getBytes('abcd)}";
		testDiagnosticsFor(template, //
				d(1, 22, 1, 22, QuteErrorCode.SyntaxError,
						"Parser error on line 2: unexpected non-text buffer at the end of the template - unterminated string literal: foo.getBytes('abcd)}",
						DiagnosticSeverity.Error));

	}

	@Test
	public void invalidMethodParameters() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.codePointCount()}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 25, QuteErrorCode.InvalidMethodParameter,
						"The method `codePointCount(int, int)` in the type `String` is not applicable for the arguments `()`.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.codePointCount(1)}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 25, QuteErrorCode.InvalidMethodParameter,
						"The method `codePointCount(int, int)` in the type `String` is not applicable for the arguments `(Integer)`.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.codePointCount(1, 2)}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.codePointCount(1, 2, 3)}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 25, QuteErrorCode.InvalidMethodParameter,
						"The method `codePointCount(int, int)` in the type `String` is not applicable for the arguments `(Integer, Integer, Integer)`.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.codePointCount('1',2)}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 25, QuteErrorCode.InvalidMethodParameter,
						"The method `codePointCount(int, int)` in the type `String` is not applicable for the arguments `(String, Integer)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void invalidMethodParametersWithLet() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let a=1 b=2}\r\n" + //
				"	{item.name.codePointCount(a, b)}\r\n" + //
				"{/let}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{#let a='1' b=2}\r\n" + //
				"	{item.name.codePointCount(a, b)}\r\n" + //
				"{/let}";
		testDiagnosticsFor(template, //
				d(2, 12, 2, 26, QuteErrorCode.InvalidMethodParameter,
						"The method `codePointCount(int, int)` in the type `String` is not applicable for the arguments `(String, Integer)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void nestedMethodParameters() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.codePointCount(item.name.codePointCount(1,2), 3)}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.codePointCount(item.name.codePointCount(1,2), '3')}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 25, QuteErrorCode.InvalidMethodParameter,
						"The method `codePointCount(int, int)` in the type `String` is not applicable for the arguments `(int, String)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void inNestedMethodParameters() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.codePointCount(item.name.codePointCount(1,2,3), 4)}";
		testDiagnosticsFor(template, //
				d(1, 36, 1, 50, QuteErrorCode.InvalidMethodParameter,
						"The method `codePointCount(int, int)` in the type `String` is not applicable for the arguments `(Integer, Integer, Integer)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void inNestedMethodParametersWithLet() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let a=1 b=2}\r\n" + //
				"	{item.name.codePointCount(item.name.codePointCount(a,b), 4)}" + //
				"{/let}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{#let a=1 b=2 c=3}\r\n" + //
				"	{item.name.codePointCount(item.name.codePointCount(a,item.name.codePointCount(a,c)), 4)}" + //
				"{/let}";
		testDiagnosticsFor(template);
	}

	@Test
	public void staticVirtualMethodNotApplicable() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.take(0)}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.take(0)}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.InvalidVirtualMethod,
						"The virtual method `take` in the type `null` is not applicable for the base object `Item` type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void dynamicVirtualMethodNotApplicable() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.getByIndex(0)}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.getByIndex(0)}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 16, QuteErrorCode.InvalidVirtualMethod,
						"The virtual method `getByIndex` in the type `CollectionTemplateExtensions` is not applicable for the base object `Item` type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void invalidMethodParametersWithVirtualMethod() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.get(0)}";
		testDiagnosticsFor(template);

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.get('0')}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 10, QuteErrorCode.InvalidMethodParameter,
						"The method `get(int)` in the type `List` is not applicable for the arguments `(String)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void sameMethodNameWithDifferentSignature() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.getBytes()}\r\n" + //
				"{item.name.getBytes('abcd')}\r\n" + //
				"{item.name.getBytes(1)}";
		testDiagnosticsFor(template, //
				d(3, 11, 3, 19, QuteErrorCode.InvalidMethodParameter,
						"The method `getBytes(String)` in the type `String` is not applicable for the arguments `(Integer)`.",
						DiagnosticSeverity.Error));
	}
}
