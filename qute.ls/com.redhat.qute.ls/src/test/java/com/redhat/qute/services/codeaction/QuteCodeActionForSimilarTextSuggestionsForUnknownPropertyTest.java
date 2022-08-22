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

import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.cad;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.services.diagnostics.JavaBaseTypeOfPartData;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test code action for similar text suggestions for
 * {@link QuteErrorCode#UnknownProperty}.
 *
 */
public class QuteCodeActionForSimilarTextSuggestionsForUnknownPropertyTest {

	@Test
	public void similarWithJavaField() throws Exception {
		// Similar with Item.name

		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nme}";

		Diagnostic d = d(1, 6, 1, 9, //
				QuteErrorCode.UnknownProperty, //
				"`nme` cannot be resolved or is not a field of `org.acme.Item` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("org.acme.Item"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 6, 1, 9, "name")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Field, "nme", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Getter, "nme", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "nme", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI, "org.acme.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "nme", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI, "org.acme.foo.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "nme", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI)));
	}

	@Test
	public void similarWithJavaMethod() throws Exception {
		// Similar with Item.isAvailable()

		String template = "{@org.acme.Item item}\r\n" + //
				"{item.avilable}";

		Diagnostic d = d(1, 6, 1, 14, //
				QuteErrorCode.UnknownProperty, //
				"`avilable` cannot be resolved or is not a field of `org.acme.Item` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("org.acme.Item"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 6, 1, 14, "available")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Field, "avilable", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Getter, "avilable", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "avilable",
						"org.acme.Item", QuteQuickStartProject.PROJECT_URI, "org.acme.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "avilable",
						"org.acme.Item", QuteQuickStartProject.PROJECT_URI, "org.acme.foo.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "avilable",
						"org.acme.Item", QuteQuickStartProject.PROJECT_URI)));
	}

	@Test
	public void similarWithResolver() throws Exception {
		// Similar with ItemResource.discountedPrice(item : org.acme.Item) :
		// java.math.BigDecimal

		String template = "{@org.acme.Item item}\r\n" + //
				"{item.discountedPrce}";

		Diagnostic d = d(1, 6, 1, 20, //
				QuteErrorCode.UnknownProperty, //
				"`discountedPrce` cannot be resolved or is not a field of `org.acme.Item` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("org.acme.Item"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 6, 1, 20, "discountedPrice")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Field, "discountedPrce", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Getter, "discountedPrce", "org.acme.Item",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "discountedPrce",
						"org.acme.Item", QuteQuickStartProject.PROJECT_URI, "org.acme.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "discountedPrce",
						"org.acme.Item", QuteQuickStartProject.PROJECT_URI, "org.acme.foo.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "discountedPrce",
						"org.acme.Item", QuteQuickStartProject.PROJECT_URI)));
	}

}
