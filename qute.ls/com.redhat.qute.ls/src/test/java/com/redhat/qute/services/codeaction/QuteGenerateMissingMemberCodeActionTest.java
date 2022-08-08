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
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionResolveSupportCapabilities;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.services.codeactions.CodeActionResolverKind;
import com.redhat.qute.services.codeactions.CodeActionUnresolvedData;
import com.redhat.qute.services.diagnostics.JavaBaseTypeOfPartData;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.settings.SharedSettings;

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
						"qute-quickstart", "org.acme.TemplateExtensions")), //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "asdf", "org.acme.Item",
						"qute-quickstart", "org.acme.foo.TemplateExtensions")), //
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
						"java.lang.String", "qute-quickstart", "org.acme.TemplateExtensions")), //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "asdf",
						"java.lang.String", "qute-quickstart", "org.acme.foo.TemplateExtensions")), //
				ca(d, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "asdf",
						"java.lang.String", "qute-quickstart")));
	}
	
	@Test
	public void userGeneratedClassResolve() throws Exception {
		String template = "{@org.acme.Item item}\n" //
				+ "{item.asdf}\n";
		
		CodeActionResolveSupportCapabilities resolveCapabilities = new CodeActionResolveSupportCapabilities();
		resolveCapabilities.setProperties(Arrays.asList("edit"));
		CodeActionCapabilities codeActionCapabilities = new CodeActionCapabilities();
		codeActionCapabilities.setDataSupport(true);
		codeActionCapabilities.setResolveSupport(resolveCapabilities);
		SharedSettings settings = new SharedSettings();
		settings.getCodeActionSettings().setCapabilities(codeActionCapabilities);

		Diagnostic d = d(1, 6, 1, 10, //
				QuteErrorCode.UnknownProperty, //
				"`asdf` cannot be resolved or is not a field of `org.acme.Item` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("org.acme.Item"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, settings, //
				ca(d, new CodeActionUnresolvedData("test.qute", CodeActionResolverKind.GenerateMissingMember, new GenerateMissingJavaMemberParams(MemberType.Field, "asdf", "org.acme.Item",
						"qute-quickstart"))), //
				ca(d, new CodeActionUnresolvedData("test.qute", CodeActionResolverKind.GenerateMissingMember, new GenerateMissingJavaMemberParams(MemberType.Getter, "asdf", "org.acme.Item",
						"qute-quickstart"))), //
				ca(d, new CodeActionUnresolvedData("test.qute", CodeActionResolverKind.GenerateMissingMember, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "asdf", "org.acme.Item",
						"qute-quickstart", "org.acme.TemplateExtensions"))), //
				ca(d, new CodeActionUnresolvedData("test.qute", CodeActionResolverKind.GenerateMissingMember, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "asdf", "org.acme.Item",
						"qute-quickstart", "org.acme.foo.TemplateExtensions"))), //
				ca(d, new CodeActionUnresolvedData("test.qute", CodeActionResolverKind.GenerateMissingMember, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "asdf", "org.acme.Item",
						"qute-quickstart"))));
	}

	private static CodeAction ca(Diagnostic d, Object data) {
		CodeAction codeAction = new CodeAction("");
		codeAction.setDiagnostics(Arrays.asList(d));
		codeAction.setData(data);
		return codeAction;
	}

}
