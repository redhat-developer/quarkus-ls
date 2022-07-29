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
package com.redhat.qute.services.codeaction;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import java.util.Arrays;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.services.diagnostics.JavaBaseTypeOfPartData;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

public class QuteGenerateMissingMemberCodeActionTest {

	@Test
	public void userGeneratedClass() throws Exception {
		String template = "{@org.acme.Item item}\n" //
				+ "{item.asdf}\n";

		Diagnostic d = d(1, 6, 1, 10, //
				QuteErrorCode.UnknownProperty, //
				"`asdf` cannot be resolved or is not a field of `org.acme.Item` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("org.acme.Item"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.Field, "asdf", "org.acme.Item",
						"qute-quickstart")), //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.Getter, "asdf", "org.acme.Item",
						"qute-quickstart")), //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "asdf", "org.acme.Item",
						"qute-quickstart")), //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "asdf", "org.acme.Item",
						"qute-quickstart")));
	}

	@Test
	public void builtInClass() throws Exception {
		String template = "{@java.lang.String item}\n" //
				+ "{item.asdf}\n";

		Diagnostic d = d(1, 6, 1, 10, //
				QuteErrorCode.UnknownProperty, //
				"`asdf` cannot be resolved or is not a field of `java.lang.String` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("java.lang.String"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "asdf",
						"java.lang.String", "qute-quickstart")), //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "asdf",
						"java.lang.String", "qute-quickstart")));
	}

	private static CodeAction ca(Diagnostic d, Object data) {
		CodeAction codeAction = new CodeAction("");
		codeAction.setDiagnostics(Arrays.asList(d));
		codeAction.setData(data);
		return codeAction;
	}

}
