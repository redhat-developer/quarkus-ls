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
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Test with expressions in case of switch and when sections.
 *
 *
 */
public class QuteDiagnosticsInExpressionWithWhenSectionTest {

	@Test
	public void switchExpressionTypeMismatch() {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{#switch item.name}\r\n" + //
				"		{#case \"Apple\"}\r\n" + //
				"		{#case 123}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template, //
				d(3, 9, 3, 12, QuteErrorCode.UnexpectedMemberTypeInCaseSection,
						"Unexpected type `java.lang.Integer` in `item.name`. Expected `java.lang.String`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void switchExpressionTypeMismatchObjectPart() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#switch name}\r\n" + //
				"		{#case \"Apple\"}\r\n" + //
				"		{#case 123}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template, //
				d(3, 9, 3, 12, QuteErrorCode.UnexpectedMemberTypeInCaseSection,
						"Unexpected type `java.lang.Integer` in `name`. Expected `java.lang.String`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void switchExpressionTypeMismatchObjectPartInvalidPart() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#switch name}\r\n" + //
				"		{#case \"Apple\"}\r\n" + //
				"		{#case XXX}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template, //
				d(3, 9, 3, 12, QuteErrorCode.UnexpectedValueInCaseSection,
						"Unexpected value `XXX` in `name`. Expected value of type `java.lang.String`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void switchExpressionTypeMismatchSectionSwitch() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#switch name}\r\n" + //
				"		{#is \"Apple\"}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template);
	}

	@Test
	public void switchExpressionTypeMismatchSectionWhen() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#when name}\r\n" + //
				"		{#case \"Apple\"}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template);
	}

	@Test
	public void switchExpressionTypeNoParent() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#case \"Apple\"}";
		testDiagnosticsFor(template, //
				d(1, 3, 1, 8, QuteErrorCode.InvalidParentInCaseSection,
						"`case` section must be hosted in a #switch or #when section.", DiagnosticSeverity.Error));
	}

	@Test
	public void switchExpressionTypeMismatchSectionIf() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#if name}\r\n" + //
				"		{#case \"Apple\"}\r\n" + //
				"		{/if}";
		testDiagnosticsFor(template, //
				d(2, 3, 2, 8, QuteErrorCode.InvalidParentInCaseSection,
						"`case` section must be hosted in a #switch or #when section.", DiagnosticSeverity.Error));
	}

	@Test
	public void switchExpressionTypeMismatchInvalidSection() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#switch name}\r\n" + //
				"		{#XXX \"Apple\"}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template, //
				d(2, 3, 2, 7, QuteErrorCode.UndefinedSectionTag, "No section helper found for `XXX`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void switchExpressionTypeMismatchInvalidParent() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#switch XXX}\r\n" + //
				"		{#case \"Apple\"}\r\n" + //
				"		{#case 123}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template, //
				d(1, 11, 1, 14, QuteErrorCode.UndefinedObject, "`XXX` cannot be resolved to an object.",
						DiagnosticSeverity.Warning));
	}

	@Test
	public void whenExpressionTypeMismatch() {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{#when item.name}\r\n" + //
				"		{#is \"Apple\"}\r\n" + //
				"		{#is 123}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(3, 7, 3, 10, QuteErrorCode.UnexpectedMemberTypeInCaseSection,
						"Unexpected type `java.lang.Integer` in `item.name`. Expected `java.lang.String`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionTypeMismatchEnum() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.status}\r\n" + //
				"		{#is ON}\r\n" + //
				"		{#is in OFF BROKEN}\r\n" + //
				"		{#is BAD_ENUM_VALUE}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(4, 7, 4, 21, QuteErrorCode.UnexpectedValueInCaseSection,
						"Unexpected value `BAD_ENUM_VALUE` in `Machine.status`. Expected value of type `org.acme.MachineStatus`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionTypeMismatchEnumIn() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.status}\r\n" + //
				"		{#is ON}\r\n" + //
				"		{#is in OFF BAD_ENUM_VALUE}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(3, 14, 3, 28, QuteErrorCode.UnexpectedValueInCaseSection,
						"Unexpected value `BAD_ENUM_VALUE` in `Machine.status`. Expected value of type `org.acme.MachineStatus`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionTypeMismatchEnumInAsInvalid() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.status}\r\n" + //
				"		{#is ON}\r\n" + //
				"		{#is ni ni}\r\n" + //
				"		{#is ni ON}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(3, 10, 3, 12, QuteErrorCode.UnexpectedValueInCaseSection,
						"Unexpected value `ni` in `Machine.status`. Expected value of type `org.acme.MachineStatus`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void switchExpressionTypeMismatchMethod() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#switch Machine.getCount()}\r\n" + //
				"		{#case 1}\r\n" + //
				"		{#case 2}\r\n" + //
				"		{#case \"Apple\"}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template, //
				d(4, 9, 4, 16, QuteErrorCode.UnexpectedMemberTypeInCaseSection,
						"Unexpected type `java.lang.String` in `Machine.getCount()`. Expected `java.lang.Integer`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionTypeMismatchEnumMethod() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.getMachine()}\r\n" + //
				"		{#is ON}\r\n" + //
				"		{#is OFF}\r\n" + //
				"		{#is BAD_ENUM_VALUE}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(4, 7, 4, 21, QuteErrorCode.UnexpectedValueInCaseSection,
						"Unexpected value `BAD_ENUM_VALUE` in `Machine.getMachine()`. Expected value of type `org.acme.MachineStatus`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionOperatorInvalidOperatorEnum() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.status}\r\n" + //
				"		{#is XXX ON}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(2, 7, 2, 10, QuteErrorCode.InvalidOperator,
						"Invalid `XXX` operator for section `#is`. Allowed operators are `[<=, in, !in, lt, gt, not, ne, le, ni, <, !=, >, ge, >=]`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionOperatorInvalidOperator() {
		String template = "{@java.lang.String name}\r\n" + //
				"		{#switch name}\r\n" + //
				"		{#is XXX \"Apple\"}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template, //
				d(2, 7, 2, 10, QuteErrorCode.InvalidOperator,
						"Invalid `XXX` operator for section `#is`. Allowed operators are `[<=, in, !in, lt, gt, not, ne, le, ni, <, !=, >, ge, >=]`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionOperatorTooManyParamNoOperator() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.getMachine()}\r\n" + //
				"		{#is ON OFF}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(2, 7, 2, 9, QuteErrorCode.InvalidOperator,
						"Invalid `ON` operator for section `#is`. Allowed operators are `[<=, in, !in, lt, gt, not, ne, le, ni, <, !=, >, ge, >=]`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionOperatorTooManyParamSingleOperator() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.getMachine()}\r\n" + //
				"		{#is ne ON OFF}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(2, 13, 2, 16, QuteErrorCode.UnexpectedParameter,
						"Unexpected operand `OFF`. The operator `ne` in the `#is` section expects only one parameter.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionOperatorTooManyParamSingleOperatorMulti() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.getMachine()}\r\n" + //
				"		{#is ne ON OFF BROKEN}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(2, 13, 2, 16, QuteErrorCode.UnexpectedParameter,
						"Unexpected operand `OFF`. The operator `ne` in the `#is` section expects only one parameter.",
						DiagnosticSeverity.Error),
				d(2, 17, 2, 23, QuteErrorCode.UnexpectedParameter,
						"Unexpected operand `BROKEN`. The operator `ne` in the `#is` section expects only one parameter.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionOperatorTooManyParamSameNameAsOperator() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.status}\r\n" + //
				"		{#is ne in in}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(2, 13, 2, 15, QuteErrorCode.UnexpectedParameter,
						"Unexpected operand `in`. The operator `ne` in the `#is` section expects only one parameter.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionOperatorExpectedParam() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.getMachine()}\r\n" + //
				"		{#is in ON OFF}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template);
	}

	@Test
	public void whenExpressionOperatorNoParameters() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.status}\r\n" + //
				"		{#case}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(2, 3, 2, 8, QuteErrorCode.MissingParameter, "A parameter is required in the `#case` section.",
						DiagnosticSeverity.Error));
	}
}
