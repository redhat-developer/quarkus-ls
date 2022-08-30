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

import com.redhat.qute.project.MockQuteProjectRegistry;

/**
 * Test Enum definition in #when section.
 * 
 */
public class QuteDefinitionInWhenSectionTest {

	@Test
	public void definitionInEnum() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ON }\r\n" + //
				"  {#is OF|F }\r\n" + //
				"{/when}";
		testDefinitionFor(template, //
				ll("org/acme/MachineStatus.java", r(3, 7, 3, 10), MockQuteProjectRegistry.JAVA_FIELD_RANGE));
	}

	@Test
	public void definitionInEnumList() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ON }\r\n" + //
				"  {#is OFF BROK|EN}\r\n" + //
				"{/when}";
		testDefinitionFor(template, //
				ll("org/acme/MachineStatus.java", r(3, 11, 3, 17), MockQuteProjectRegistry.JAVA_FIELD_RANGE));
	}

	@Test
	public void definitionInEnumInvalidEnum() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ON }\r\n" + //
				"  {#is OF|FXXX }\r\n" + //
				"{/when}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInEnumInvalidParentSection() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when XXX}\r\n" + //
				"  {#is OFF BROK|EN}\r\n" + //
				"{/when}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInEnumNoParentSection() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"  {#is O|N }";
		testDefinitionFor(template);
	}
	
	@Test
	public void definitionInEnumNoEnum() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ON }\r\n" + //
				"  {#is |}\r\n" + //
				"{/when}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInEnumMismatchSection() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#let O|N}\r\n" + //
				"{/when}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInEnumWithOperator() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is in O|N}\r\n" + //
				"{/when}";
		testDefinitionFor(template, //
				ll("org/acme/MachineStatus.java", r(2, 10, 2, 12), MockQuteProjectRegistry.JAVA_FIELD_RANGE));
	}

	@Test
	public void definitionInEnumOnOperator() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is i|n ON}\r\n" + //
				"{/when}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInEnumOnOperatorAsEnum() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is in i|n}\r\n" + //
				"{/when}";
		testDefinitionFor(template, //
				ll("org/acme/MachineStatus.java", r(2, 10, 2, 12), MockQuteProjectRegistry.JAVA_FIELD_RANGE));
	}

	@Test
	public void definitionInEnumString() throws Exception {
		String template = "{@java.lang.String string}\r\n" + //
				"{#switch string}\r\n" + //
				"  {#case UTF|16}\r\n" + //
				"{/switch}";
		testDefinitionFor(template);
	}
	
	@Test
	public void definitionInEnumNullValueParameter() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when}\r\n" + //
				"  {#is in O|N}\r\n" + //
				"{/when}";
		testDefinitionFor(template);
	}
}
