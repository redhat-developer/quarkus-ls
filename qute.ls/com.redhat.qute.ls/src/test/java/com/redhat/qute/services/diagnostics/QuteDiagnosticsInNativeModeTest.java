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

import static com.redhat.qute.QuteAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.settings.QuteValidationSettings;

/**
 * Test with expressions with '@TemplateData'.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInNativeModeTest {

	@Test
	public void templateDataField() {
		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		// public class Item
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name}";
		testDiagnosticsFor(template, nativeImagesSettings, //
				d(1, 6, 1, 10, QuteErrorCode.PropertyNotSupportedInNativeMode,
						"Property `name` of `org.acme.Item` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		// @TemplateData
		// public class ItemWithTemplateData
		template = "{@org.acme.ItemWithTemplateData item}\r\n" + //
				"{item.name}";
		testDiagnosticsFor(template, nativeImagesSettings);
	}

	@Test
	public void templateDataFieldIgnoreSubClasses() {
		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		// @TemplateData(ignoreSubClasses = true)
		// public class ItemWithTemplateDataIgnoreSubClasses
		String template = "{@org.acme.ItemWithTemplateDataIgnoreSubClasses item}\r\n" + //
				"{item.price}\r\n" + // <-- no error because price comes from ItemWithTemplateDataIgnoreSubClasses
				"{item.base}"; // <-- error because base comes from super class BaseItem
		testDiagnosticsFor(template, nativeImagesSettings, //
				d(2, 6, 2, 10, QuteErrorCode.InheritedPropertyNotSupportedInNativeMode,
						"Inherited property `base` of `org.acme.ItemWithTemplateDataIgnoreSubClasses` Java type cannot be used in native image mode because Java type is annotated with `@TemplateData(ignoreSuperclasses = true)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void templateDataMethod() {
		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		// public class Item
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview2()}";
		testDiagnosticsFor(template, nativeImagesSettings, //
				d(1, 6, 1, 16, QuteErrorCode.MethodNotSupportedInNativeMode,
						"Method `getReview2` of `org.acme.Item` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		// @TemplateData
		// public class ItemWithTemplateData
		template = "{@org.acme.ItemWithTemplateData item}\r\n" + //
				"{item.getReview2()}";
		testDiagnosticsFor(template, nativeImagesSettings);
	}

	@Test
	public void templateDataMethodIgnoreSubClasses() {
		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		// @TemplateData(ignoreSubClasses = true)
		// public class ItemWithTemplateDataIgnoreSubClasses
		String template = "{@org.acme.ItemWithTemplateDataIgnoreSubClasses item}\r\n" + //
				"{item.getSubClasses()}\r\n" + // <-- no error because getSubClasses comes from
												// ItemWithTemplateDataIgnoreSubClasses
				"{item.getReviews()}"; // <-- error because getReviews comes from super class BaseItem
		testDiagnosticsFor(template, nativeImagesSettings, //
				d(2, 6, 2, 16, QuteErrorCode.InheritedMethodNotSupportedInNativeMode,
						"Inherited method `getReviews` of `org.acme.ItemWithTemplateDataIgnoreSubClasses` Java type cannot be used in native image mode because Java type is annotated with `@TemplateData(ignoreSuperclasses = true)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void registerForReflectionField() {
		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		// public class Item
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name}";
		testDiagnosticsFor(template, nativeImagesSettings, //
				d(1, 6, 1, 10, QuteErrorCode.PropertyNotSupportedInNativeMode,
						"Property `name` of `org.acme.Item` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		// @RegisterForReflection
		// public class ItemWithRegisterForReflection
		template = "{@org.acme.ItemWithRegisterForReflection item}\r\n" + //
				"{item.name}";
		testDiagnosticsFor(template, nativeImagesSettings);
	}

	@Test
	public void registerForReflectionNoFields() {
		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		// @RegisterForReflection(fields = false)
		// public class ItemWithRegisterForReflectionNoFields
		String template = "{@org.acme.ItemWithRegisterForReflectionNoFields item}\r\n" + //
				"{item.getReview2()}\r\n" + // <-- no error because it's a method
				"{item.base}"; // <-- error because fields are ignored
		testDiagnosticsFor(template, nativeImagesSettings, //
				d(2, 6, 2, 10, QuteErrorCode.ForbiddenByRegisterForReflectionFields,
						"The field `base` of `org.acme.ItemWithRegisterForReflectionNoFields` Java type cannot be used in native image mode because Java type is annotated with `@RegisterForReflection(fields = false)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void registerForReflectionNoMethods() {
		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		// @RegisterForReflection(methods = false)
		// public class ItemWithRegisterForReflectionNoMethods
		String template = "{@org.acme.ItemWithRegisterForReflectionNoMethods item}\r\n" + //
				"{item.getReview2()}\r\n" + // <-- error because methods are ignored
				"{item.base}"; // <-- no error because it's a field
		testDiagnosticsFor(template, nativeImagesSettings, //
				d(1, 6, 1, 16, QuteErrorCode.ForbiddenByRegisterForReflectionMethods,
						"The method `getReview2` of `org.acme.ItemWithRegisterForReflectionNoMethods` Java type cannot be used in native image mode because Java type is annotated with `@RegisterForReflection(methods = false)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void registerForReflectionMethod() {
		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		// public class Item
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview2()}";
		testDiagnosticsFor(template, nativeImagesSettings, //
				d(1, 6, 1, 16, QuteErrorCode.MethodNotSupportedInNativeMode,
						"Method `getReview2` of `org.acme.Item` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		// @RegisterForReflection
		// public class ItemWithRegisterForReflection
		template = "{@org.acme.ItemWithRegisterForReflection item}\r\n" + //
				"{item.getReview2()}";
		testDiagnosticsFor(template, nativeImagesSettings);
	}

	private static void testDiagnosticsFor(String value, QuteNativeSettings nativeImagesSettings,
			Diagnostic... expected) {
		QuteValidationSettings validationSettings = new QuteValidationSettings();
		QuteAssert.testDiagnosticsFor(value, "test.qute", null, QuteQuickStartProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, false, validationSettings, nativeImagesSettings, expected);
	}
}
