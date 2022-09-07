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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in expression for #switch and #when section.
 *
 */
public class QuteCompletionInExpressionWithWhenSectionTest {

	@Test
	public void propertyPartWhenNoExisting() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is | }\r\n" + // <-- completion here
				"{/when}";
		testCompletionFor(template, 18, //
				c("> : Operator", ">", r(2, 7, 2, 7)), //
				c("gt : Operator", "gt", r(2, 7, 2, 7)), //
				c(">= : Operator", ">=", r(2, 7, 2, 7)), //
				c("ge : Operator", "ge", r(2, 7, 2, 7)), //
				c("< : Operator", "<", r(2, 7, 2, 7)), //
				c("lt : Operator", "lt", r(2, 7, 2, 7)), //
				c("<= : Operator", "<=", r(2, 7, 2, 7)), //
				c("le : Operator", "le", r(2, 7, 2, 7)), //
				c("!= : Operator", "!=", r(2, 7, 2, 7)), //
				c("ne : Operator", "ne", r(2, 7, 2, 7)), //
				c("not : Operator", "not", r(2, 7, 2, 7)), //
				c("in : Operator", "in", r(2, 7, 2, 7)), //
				c("!in : Operator", "!in", r(2, 7, 2, 7)), //
				c("ni : Operator", "ni", r(2, 7, 2, 7)), //
				c("ON : MachineStatus", "ON", r(2, 7, 2, 7)), //
				c("OFF : MachineStatus", "OFF", r(2, 7, 2, 7)), //
				c("BROKEN : MachineStatus", "BROKEN", r(2, 7, 2, 7)), //
				c("in : MachineStatus", "in", r(2, 7, 2, 7)));
	}

	@Test
	public void propertyPartSwitchIsNoExisting() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#switch Machine.status}\r\n" + //
				"  {#is | }\r\n" + // <-- completion here
				"{/switch}";
		testCompletionFor(template, 18, //
				c("> : Operator", ">", r(2, 7, 2, 7)), //
				c("gt : Operator", "gt", r(2, 7, 2, 7)), //
				c(">= : Operator", ">=", r(2, 7, 2, 7)), //
				c("ge : Operator", "ge", r(2, 7, 2, 7)), //
				c("< : Operator", "<", r(2, 7, 2, 7)), //
				c("lt : Operator", "lt", r(2, 7, 2, 7)), //
				c("<= : Operator", "<=", r(2, 7, 2, 7)), //
				c("le : Operator", "le", r(2, 7, 2, 7)), //
				c("!= : Operator", "!=", r(2, 7, 2, 7)), //
				c("ne : Operator", "ne", r(2, 7, 2, 7)), //
				c("not : Operator", "not", r(2, 7, 2, 7)), //
				c("in : Operator", "in", r(2, 7, 2, 7)), //
				c("!in : Operator", "!in", r(2, 7, 2, 7)), //
				c("ni : Operator", "ni", r(2, 7, 2, 7)), //
				c("ON : MachineStatus", "ON", r(2, 7, 2, 7)), //
				c("OFF : MachineStatus", "OFF", r(2, 7, 2, 7)), //
				c("BROKEN : MachineStatus", "BROKEN", r(2, 7, 2, 7)), //
				c("in : MachineStatus", "in", r(2, 7, 2, 7)));
	}

	@Test
	public void propertyPartWhenAlreadyCompleted() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ON |}\r\n" + // <-- should be no completion here with no in keyword
				"{/when}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartWhenAlreadyCompletedNotOperator() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is not ON |}\r\n" + // <-- should be no completion here with not as keyword
				"{/when}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartWhenNewLine() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ON }\r\n" + //
				"  {#is | }\r\n" + // <-- completion here
				"{/when}";
		testCompletionFor(template, 17, //
				c("> : Operator", ">", r(3, 7, 3, 7)), //
				c("gt : Operator", "gt", r(3, 7, 3, 7)), //
				c(">= : Operator", ">=", r(3, 7, 3, 7)), //
				c("ge : Operator", "ge", r(3, 7, 3, 7)), //
				c("< : Operator", "<", r(3, 7, 3, 7)), //
				c("lt : Operator", "lt", r(3, 7, 3, 7)), //
				c("<= : Operator", "<=", r(3, 7, 3, 7)), //
				c("le : Operator", "le", r(3, 7, 3, 7)), //
				c("!= : Operator", "!=", r(3, 7, 3, 7)), //
				c("ne : Operator", "ne", r(3, 7, 3, 7)), //
				c("not : Operator", "not", r(3, 7, 3, 7)), //
				c("in : Operator", "in", r(3, 7, 3, 7)), //
				c("!in : Operator", "!in", r(3, 7, 3, 7)), //
				c("ni : Operator", "ni", r(3, 7, 3, 7)), //
				c("OFF : MachineStatus", "OFF", r(3, 7, 3, 7)), //
				c("BROKEN : MachineStatus", "BROKEN", r(3, 7, 3, 7)), //
				c("in : MachineStatus", "in", r(3, 7, 3, 7)));
	}

	@Test
	public void propertyPartWhenWithIn() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ON }\r\n" + //
				"  {#is in BROKEN |}\r\n" + // <-- completion here
				"{/when}";
		testCompletionFor(template, 1, //
				c("OFF : MachineStatus", "OFF", r(3, 17, 3, 17)));
	}

	@Test
	public void propertyPartWhenWithNotIn() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ni ON |}\r\n" + // <-- completion here
				"{/when}";
		testCompletionFor(template, 3, //
				c("OFF : MachineStatus", "OFF", r(2, 13, 2, 13)), //
				c("BROKEN : MachineStatus", "BROKEN", r(2, 13, 2, 13)), //
				c("in : MachineStatus", "in", r(2, 13, 2, 13)));
	}

	@Test
	public void propertyPartWhenWithNotInNewLine() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is ON }\r\n" + //
				"  {#is ni |}\r\n" + // <-- completion here
				"{/when}";
		testCompletionFor(template, 3, //
				c("OFF : MachineStatus", "OFF", r(3, 10, 3, 10)), //
				c("BROKEN : MachineStatus", "BROKEN", r(3, 10, 3, 10)), //
				c("in : MachineStatus", "in", r(3, 10, 3, 10)));
	}

	@Test
	public void propertyPartSwitchNoExisting() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#switch Machine.status}\r\n" + //
				"  {#case | }\r\n" + // <-- completion here
				"{/switch}";
		testCompletionFor(template, 18, //
				c("> : Operator", ">", r(2, 9, 2, 9)), //
				c("gt : Operator", "gt", r(2, 9, 2, 9)), //
				c(">= : Operator", ">=", r(2, 9, 2, 9)), //
				c("ge : Operator", "ge", r(2, 9, 2, 9)), //
				c("< : Operator", "<", r(2, 9, 2, 9)), //
				c("lt : Operator", "lt", r(2, 9, 2, 9)), //
				c("<= : Operator", "<=", r(2, 9, 2, 9)), //
				c("le : Operator", "le", r(2, 9, 2, 9)), //
				c("!= : Operator", "!=", r(2, 9, 2, 9)), //
				c("ne : Operator", "ne", r(2, 9, 2, 9)), //
				c("not : Operator", "not", r(2, 9, 2, 9)), //
				c("in : Operator", "in", r(2, 9, 2, 9)), //
				c("!in : Operator", "!in", r(2, 9, 2, 9)), //
				c("ni : Operator", "ni", r(2, 9, 2, 9)), //
				c("ON : MachineStatus", "ON", r(2, 9, 2, 9)), //
				c("OFF : MachineStatus", "OFF", r(2, 9, 2, 9)), //
				c("BROKEN : MachineStatus", "BROKEN", r(2, 9, 2, 9)), //
				c("in : MachineStatus", "in", r(2, 9, 2, 9)));
	}

	@Test
	public void propertyPartWhenCaseNoExisting() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#case | }\r\n" + // <-- completion here
				"{/when}";
		testCompletionFor(template, 18, //
				c("> : Operator", ">", r(2, 9, 2, 9)), //
				c("gt : Operator", "gt", r(2, 9, 2, 9)), //
				c(">= : Operator", ">=", r(2, 9, 2, 9)), //
				c("ge : Operator", "ge", r(2, 9, 2, 9)), //
				c("< : Operator", "<", r(2, 9, 2, 9)), //
				c("lt : Operator", "lt", r(2, 9, 2, 9)), //
				c("<= : Operator", "<=", r(2, 9, 2, 9)), //
				c("le : Operator", "le", r(2, 9, 2, 9)), //
				c("!= : Operator", "!=", r(2, 9, 2, 9)), //
				c("ne : Operator", "ne", r(2, 9, 2, 9)), //
				c("not : Operator", "not", r(2, 9, 2, 9)), //
				c("in : Operator", "in", r(2, 9, 2, 9)), //
				c("!in : Operator", "!in", r(2, 9, 2, 9)), //
				c("ni : Operator", "ni", r(2, 9, 2, 9)), //
				c("ON : MachineStatus", "ON", r(2, 9, 2, 9)), //
				c("OFF : MachineStatus", "OFF", r(2, 9, 2, 9)), //
				c("BROKEN : MachineStatus", "BROKEN", r(2, 9, 2, 9)), //
				c("in : MachineStatus", "in", r(2, 9, 2, 9)));
	}

	@Test
	public void propertyPartWhenCaseCompleteOperator() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#case | ON}\r\n" + // <-- completion here
				"{/when}";
		testCompletionFor(template, 14, //
				c("> : Operator", ">", r(2, 9, 2, 9)), //
				c("gt : Operator", "gt", r(2, 9, 2, 9)), //
				c(">= : Operator", ">=", r(2, 9, 2, 9)), //
				c("ge : Operator", "ge", r(2, 9, 2, 9)), //
				c("< : Operator", "<", r(2, 9, 2, 9)), //
				c("lt : Operator", "lt", r(2, 9, 2, 9)), //
				c("<= : Operator", "<=", r(2, 9, 2, 9)), //
				c("le : Operator", "le", r(2, 9, 2, 9)), //
				c("!= : Operator", "!=", r(2, 9, 2, 9)), //
				c("ne : Operator", "ne", r(2, 9, 2, 9)), //
				c("not : Operator", "not", r(2, 9, 2, 9)), //
				c("in : Operator", "in", r(2, 9, 2, 9)), //
				c("!in : Operator", "!in", r(2, 9, 2, 9)), //
				c("ni : Operator", "ni", r(2, 9, 2, 9)));
	}

	@Test
	public void propertyPartWhenCaseCompleteWithMultiOperator() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#case | ON OFF}\r\n" + // <-- completion here
				"{/when}";
		testCompletionFor(template, 3, //
				c("in : Operator", "in", r(2, 9, 2, 9)), //
				c("ni : Operator", "ni", r(2, 9, 2, 9)), //
				c("!in : Operator", "!in", r(2, 9, 2, 9)));
	}

	@Test
	public void propertyPartWhenCaseCompleteAlreadyDone() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#case | lt ON}\r\n" + //
				"{/when}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartWhenCaseCompleteAlreadyDoneMulti() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#case | in ON OFF}\r\n" + //
				"{/when}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartWhenEmptyWhen() throws Exception {
		String template = "{#when}\r\n" + //
				"  {#is | }\r\n" + //
				"{/when}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartWhenInvalidPart() throws Exception {
		String template = "{#when XXX}\r\n" + //
				"  {#is | }\r\n" + //
				"{/when}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartWhenProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#when item.name}\r\n" + //
				"  {#is | }\r\n" + //
				"{/when}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartWhenObject() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#when item}\r\n" + //
				"  {#is | }\r\n" + //
				"{/when}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartWhenNoSwitchWhenParentSection() throws Exception {
		String template = "{#is |}\r\n";
		testCompletionFor(template, 0);
	}
}