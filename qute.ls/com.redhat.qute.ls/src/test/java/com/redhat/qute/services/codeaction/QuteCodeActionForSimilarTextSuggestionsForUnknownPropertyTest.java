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

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.cad;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testCodeActionsWithConfigurationUpdateFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
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
		testCodeActionsWithConfigurationUpdateFor(template, d, //
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
						QuteQuickStartProject.PROJECT_URI)),
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
		testCodeActionsWithConfigurationUpdateFor(template, d, //
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
						"org.acme.Item", QuteQuickStartProject.PROJECT_URI)),
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
		testCodeActionsWithConfigurationUpdateFor(template, d, //
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
						"org.acme.Item", QuteQuickStartProject.PROJECT_URI)),
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
	public void similarWithJavaFieldWithClassWithCyclicInheritance() throws Exception {
		// Similar with Item.name

		String template = "{@org.acme.qute.cyclic.ClassA classA}\r\n" + //
				"{classA.nme}";

		Diagnostic d = d(1, 8, 1, 11, //
				QuteErrorCode.UnknownProperty, //
				"`nme` cannot be resolved or is not a field of `org.acme.qute.cyclic.ClassA` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("org.acme.qute.cyclic.ClassA"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 8, 1, 11, "name")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Field, "nme", "org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Getter, "nme", "org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "nme",
						"org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI, "org.acme.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "nme",
						"org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI, "org.acme.foo.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "nme",
						"org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI)));
		testCodeActionsWithConfigurationUpdateFor(template, d, //
				ca(d, te(1, 8, 1, 11, "name")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Field, "nme", "org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.Getter, "nme", "org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI)), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "nme",
						"org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI, "org.acme.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.AppendTemplateExtension, "nme",
						"org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI, "org.acme.foo.TemplateExtensions")), //
				cad(d, new GenerateMissingJavaMemberParams(MemberType.CreateTemplateExtension, "nme",
						"org.acme.qute.cyclic.ClassA",
						QuteQuickStartProject.PROJECT_URI)),
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

}
