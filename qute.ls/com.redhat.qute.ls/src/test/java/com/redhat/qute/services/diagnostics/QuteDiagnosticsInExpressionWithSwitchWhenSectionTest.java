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
public class QuteDiagnosticsInExpressionWithSwitchWhenSectionTest {

	@Test
	public void switchExpressionTypeMismatchSwitch() {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{#switch item.name}\r\n" + //
				"		{#case \"Apple\"}\r\n" + //
				"		{#case 123}\r\n" + //
				"		{/switch}";
		testDiagnosticsFor(template, //
				d(3, 9, 3, 12, QuteErrorCode.UnexpectedMemberType,
						"Unexpected type `java.lang.Integer` in `name`. Expected `java.lang.String`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void switchExpressionTypeMismatchWhen() {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{#when item.name}\r\n" + //
				"		{#is \"Apple\"}\r\n" + //
				"		{#is 123}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(3, 7, 3, 10, QuteErrorCode.UnexpectedMemberType,
						"Unexpected type `java.lang.Integer` in `name`. Expected `java.lang.String`.",
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
				d(4, 7, 4, 21, QuteErrorCode.UnexpectedEnumValue,
						"Unexpected value `BAD_ENUM_VALUE` in `status`. Expected value of type `org.acme.MachineStatus`.",
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
				d(3, 14, 3, 28, QuteErrorCode.UnexpectedEnumValue,
						"Unexpected value `BAD_ENUM_VALUE` in `status`. Expected value of type `org.acme.MachineStatus`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void whenExpressionTypeMismatchEnumInAsInvalid() {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"		{#when Machine.status}\r\n" + //
				"		{#is ON}\r\n" + //
				"		{#is in in}\r\n" + //
				"		{/when}";
		testDiagnosticsFor(template, //
				d(3, 10, 3, 12, QuteErrorCode.UnexpectedEnumValue,
						"Unexpected value `in` in `status`. Expected value of type `org.acme.MachineStatus`.",
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
				d(4, 9, 4, 16, QuteErrorCode.UnexpectedMemberType,
						"Unexpected type `java.lang.String` in `getCount`. Expected `java.lang.Integer`.",
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
				d(4, 7, 4, 21, QuteErrorCode.UnexpectedEnumValue,
						"Unexpected value `BAD_ENUM_VALUE` in `getMachine`. Expected value of type `org.acme.MachineStatus`.",
						DiagnosticSeverity.Error));
	}
}
