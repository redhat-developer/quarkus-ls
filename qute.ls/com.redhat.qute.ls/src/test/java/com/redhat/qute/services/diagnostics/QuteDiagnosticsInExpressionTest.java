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
import static com.redhat.qute.QuteAssert.testCodeActionsWithConfigurationUpdateFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Test with expressions.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionTest {

	@Test
	public void invalidIdentifiers() throws Exception {

		Diagnostic d = d(0, 1, 0, 4, QuteErrorCode.UndefinedObject, "`foo` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor("{foo}", d);

		d = d(0, 1, 0, 5, QuteErrorCode.UndefinedObject, "`_foo` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
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
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.UndefinedObject, "`trueX` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String trueX}" + //
						System.lineSeparator())), //
				ca(d, te(0, 6, 0, 6, "??")));

		template = "{false}";
		testDiagnosticsFor(template);

		template = "{falseX}";
		d = d(0, 1, 0, 7, QuteErrorCode.UndefinedObject, "`falseX` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String falseX}" + //
						System.lineSeparator())), //
				ca(d, te(0, 7, 0, 7, "??")));
	}

	@Test
	public void nullLiteral() throws Exception {
		String template = "{null}";
		testDiagnosticsFor(template);

		template = "{nullX}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.UndefinedObject, "`nullX` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String nullX}" + //
						System.lineSeparator())), //
				ca(d, te(0, 6, 0, 6, "??")));
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
		Diagnostic d = d(0, 1, 0, 5, QuteErrorCode.UndefinedObject, "`123X` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String 123X}" + //
						System.lineSeparator())), //
				ca(d, te(0, 5, 0, 5, "??")));
	}

	@Test
	public void longLiteral() throws Exception {
		String template = "{123L}";
		testDiagnosticsFor(template);

		template = "{123LX}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.UndefinedObject, "`123LX` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String 123LX}" + //
						System.lineSeparator())), //
				ca(d, te(0, 6, 0, 6, "??")));
	}

	@Test
	public void undefinedObject() throws Exception {
		String template = "{item}";

		Diagnostic d = d(0, 1, 0, 5, //
				QuteErrorCode.UndefinedObject, //
				"`item` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String item}" + //
						System.lineSeparator())), //
				ca(d, te(0, 5, 0, 5, "??")));
	}

	@Test
	public void undefinedUnderscoreObject() throws Exception {
		String template = "{nested-content}";

		Diagnostic d = d(0, 1, 0, 15, //
				QuteErrorCode.UndefinedObject, //
				"`nested-content` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String nested-content}" + //
						System.lineSeparator())), //
				ca(d, te(0, 15, 0, 15, "??")));
	}

	@Test
	public void definedObjectWithParameterDeclaration() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unknownProperty() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnknownProperty,
						"`XXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						new JavaBaseTypeOfPartData("org.acme.Item"), DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX.YYYY}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnknownProperty,
						"`XXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						new JavaBaseTypeOfPartData("org.acme.Item"), DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.YYYY}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 15, QuteErrorCode.UnknownProperty,
						"`YYYY` cannot be resolved or is not a field of `java.lang.String` Java type.",
						new JavaBaseTypeOfPartData("java.lang.String"), DiagnosticSeverity.Error));
	}

	@Test
	public void knownProperty() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.UTF16}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unknownMethod() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX()}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnknownMethod,
						"`XXXX` cannot be resolved or is not a method of `org.acme.Item` Java type.",
						new JavaBaseTypeOfPartData("org.acme.Item"), //
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.XXXX().YYYY}";
		testDiagnosticsFor(template, //
				d(1, 6, 1, 10, QuteErrorCode.UnknownMethod,
						"`XXXX` cannot be resolved or is not a method of `org.acme.Item` Java type.",
						new JavaBaseTypeOfPartData("org.acme.Item"), //
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.YYYY()}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 15, QuteErrorCode.UnknownMethod,
						"`YYYY` cannot be resolved or is not a method of `java.lang.String` Java type.",
						new JavaBaseTypeOfPartData("java.lang.String"), //
						DiagnosticSeverity.Error));
	}

	@Test
	public void knownMethod() {
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
	public void knownMethodForIterable() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size}";
		testDiagnosticsFor(template);

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size()}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unknownMethodForIterable() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.sizeXXX}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 14, QuteErrorCode.UnknownProperty,
						"`sizeXXX` cannot be resolved or is not a field of `java.util.List<org.acme.Item>` Java type.",
						new JavaBaseTypeOfPartData("java.util.List<org.acme.Item>"), DiagnosticSeverity.Error));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.sizeXXX()}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 14, QuteErrorCode.UnknownMethod,
						"`sizeXXX` cannot be resolved or is not a method of `java.util.List<org.acme.Item>` Java type.",
						new JavaBaseTypeOfPartData("java.util.List<org.acme.Item>"), //
						DiagnosticSeverity.Error));
	}

	@Test
	public void unknownMethodForPrimitive() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size.XXX}";
		testDiagnosticsFor(template,
				d(1, 12, 1, 15, QuteErrorCode.UnknownProperty,
						"`XXX` cannot be resolved or is not a field of `int` Java type.",
						new JavaBaseTypeOfPartData("int"), DiagnosticSeverity.Error));
	}

	@Test
	public void elvisOperator() throws Exception {
		String template = "{person.name ?: 'John'}";

		Diagnostic d = d(0, 1, 0, 7, QuteErrorCode.UndefinedObject, "`person` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String person}" + //
						System.lineSeparator())), //
				ca(d, te(0, 7, 0, 7, "??")));
	}

	@Test
	public void definedNamespace() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{data:item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefineNamespace() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{dataXXX:item}";
		Diagnostic d = d(1, 1, 1, 8, QuteErrorCode.UndefinedNamespace, "No namespace resolver found for: `dataXXX`.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d);
		testCodeActionsWithConfigurationUpdateFor(template, d, //
				ca(d, c("Ignore `UndefinedNamespace` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedNamespace.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)),
				ca(d, c("Exclude this file from validation.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.excluded", //
						"test.qute", //
						ConfigurationItemEditType.add, "test.qute", //
						d)),
				ca(d, c("Disable Qute validation for the `qute-quickstart` project.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.enabled", //
						"test.qute", //
						ConfigurationItemEditType.update, false, //
						d)));
	}

	@Test
	public void invalidMethodVoid() {
		String template = "{@java.lang.String string}\r\n" + //
				"{string.isEmpty()}\r\n" + //
				"{string.getChars()}\r\n";

		testDiagnosticsFor(template, //
				d(2, 8, 2, 16, QuteErrorCode.InvalidMethodVoid,
						"Invalid `getChars` method of `java.lang.String` : void return is not allowed.",
						new JavaBaseTypeOfPartData("java.lang.String"), //
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
						new JavaBaseTypeOfPartData("org.acme.Item"), //
						DiagnosticSeverity.Error));
	}

	@Test
	public void virtualMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{item.discountedPrice}";
		testDiagnosticsFor(template);
	}

	@Test
	public void varargs() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{item.pretty()}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"		{item.pretty('a')}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"		{item.pretty('a','b')}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"		{item.pretty(0)}";
		testDiagnosticsFor(template, //
				d(1, 8, 1, 14, QuteErrorCode.InvalidMethodParameter,
						"The method `pretty(String...)` in the type `ItemResource` is not applicable for the arguments `(Integer)`.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"		{item.pretty(0,'a')}";
		testDiagnosticsFor(template, //
				d(1, 8, 1, 14, QuteErrorCode.InvalidMethodParameter,
						"The method `pretty(String...)` in the type `ItemResource` is not applicable for the arguments `(Integer, String)`.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"		{item.pretty('a',0)}";
		testDiagnosticsFor(template, //
				d(1, 8, 1, 14, QuteErrorCode.InvalidMethodParameter,
						"The method `pretty(String...)` in the type `ItemResource` is not applicable for the arguments `(String, Integer)`.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Item item}\r\n" + //
				"		{item.pretty('a',0,'b')}";
		testDiagnosticsFor(template, //
				d(1, 8, 1, 14, QuteErrorCode.InvalidMethodParameter,
						"The method `pretty(String...)` in the type `ItemResource` is not applicable for the arguments `(String, Integer, String)`.",
						DiagnosticSeverity.Error));
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
						"Parser error: unexpected non-text buffer at the end of the template - unterminated string literal: foo.getBytes('abcd)}",
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

	@Test
	public void methodSuperType() {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{item.convert(item)}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"		{item.convert(1)}";
		testDiagnosticsFor(template, //
				d(1, 8, 1, 15, QuteErrorCode.InvalidMethodParameter,
						"The method `convert(AbstractItem)` in the type `AbstractItem` is not applicable for the arguments `(Integer)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void fieldSuperType() {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{item.abstractName}"; // from super class org.acme.AbstractItem#abstractName
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"		{item.XXXX}";
		testDiagnosticsFor(template, //
				d(1, 8, 1, 12, QuteErrorCode.UnknownProperty,
						"`XXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						new JavaBaseTypeOfPartData("org.acme.Item"), DiagnosticSeverity.Error));
	}

	@Test
	public void parameterSuperType() {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{@org.acme.BaseItem baseItem}\r\n" + //
				"		{@org.acme.AbstractItem abstractItem}\r\n" + //
				"\r\n" + //
				"		{abstractItem.convert(1)}\r\n" + //
				"		{abstractItem.convert(item)}\r\n" + //
				"		{abstractItem.convert(baseItem)}\r\n" + //
				"		{abstractItem.convert(item)}";
		testDiagnosticsFor(template, //
				d(4, 16, 4, 23, QuteErrorCode.InvalidMethodParameter,
						"The method `convert(AbstractItem)` in the type `AbstractItem` is not applicable for the arguments `(Integer)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void optionalObject() {
		String template = "{foo??}";
		testDiagnosticsFor(template);
	}

	@Test
	public void globalVariables() throws Exception {
		String template = "{GLOBAL}";
		testDiagnosticsFor(template);

		template = "{GLOBAL.isEmpty()}";
		testDiagnosticsFor(template);

		template = "{GLOBAL.XXXX}";
		testDiagnosticsFor(template, //
				d(0, 8, 0, 12, QuteErrorCode.UnknownProperty,
						"`XXXX` cannot be resolved or is not a field of `java.lang.String` Java type.",
						new JavaBaseTypeOfPartData("java.lang.String"), DiagnosticSeverity.Error));
	}

	@Test
	public void undefinedObjectPartWithKwownMethodResolver() throws Exception {
		String template = "{items.getByIndex(0)}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 6, QuteErrorCode.UndefinedObject, "`items` cannot be resolved to an object.",
						DiagnosticSeverity.Warning));
	}

	@Test
	public void escape() throws Exception {
		String template = "function gtag()\\{dataLayer.push(arguments);\\}";
		testDiagnosticsFor(template);
	}
}
