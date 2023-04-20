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
 * Test with #with section
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithWithSectionTest {

	@Test
	public void undefinedObject() throws Exception {
		String template = "{#with item}\r\n" + //
				"{/with}";

		Diagnostic d = d(0, 7, 0, 11, QuteErrorCode.UndefinedObject, "`item` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String item}\r\n")), //
				ca(d, te(0, 11, 0, 11, "??")));
	}

	@Test
	public void noError() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#with item}\r\n" + //
				"  <h1>{name}</h1>  \r\n" + //
				"  <p>{price}</p> \r\n" + //
				"{/with}";
		testDiagnosticsFor(template);
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

		Diagnostic d = d(6, 5, 6, 12, QuteErrorCode.UndefinedObject, "`average` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String average}\r\n")), //
				ca(d, te(6, 12, 6, 12, "??")));

	}
}
