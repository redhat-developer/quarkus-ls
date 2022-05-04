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
		// public class Item
		String template = // "{@org.acme.Review review}\r\n" + //
				"{review.name}";
		testDiagnosticsFor(template, //
				d(0, 8, 0, 12, QuteErrorCode.PropertyNotSupportedInNativeMode,
						"Property `name` of `org.acme.Review` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		template = "{@org.acme.Review review}\r\n" + //
				"{review.name}";
		testDiagnosticsFor(template);

		// @TemplateData
		// public class ItemWithTemplateData
		template = // "{@org.acme.ItemWithTemplateData itemWithTemplateData}\r\n" + //
				"{itemWithTemplateData.name}";
		testDiagnosticsFor(template);

		template = // "{@org.acme.ItemWithTemplateData itemWithTemplateData}\r\n" + //
				"{itemWithTemplateData.price.divide(0)}";
		testDiagnosticsFor(template, //
				d(0, 28, 0, 34, QuteErrorCode.MethodNotSupportedInNativeMode,
						"Method `divide` of `java.math.BigInteger` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		template = // "{@org.acme.ItemWithTemplateData itemWithTemplateData}\r\n" + //
				"{itemWithTemplateData.name.isEmpty()}";
		testDiagnosticsFor(template, //
				d(0, 27, 0, 34, QuteErrorCode.MethodNotSupportedInNativeMode,
						"Method `isEmpty` of `java.lang.String` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void templateDataFieldWithTarget() {
		// @TemplateData
		// @TemplateData(target = String.class)
		// public class ItemWithTemplateDataWithTarget
		String template = "{@org.acme.ItemWithTemplateDataWithTarget itemWithTemplateDataWithTarget}\r\n" + //
				"{itemWithTemplateDataWithTarget.name}";
		testDiagnosticsFor(template);

		template = // "{@org.acme.ItemWithTemplateDataWithTarget
					// itemWithTemplateDataWithTarget}\r\n" + //
				"{itemWithTemplateDataWithTarget.name.isEmpty()}";
		testDiagnosticsFor(template);

		template = // "{@org.acme.ItemWithTemplateDataWithTarget
					// itemWithTemplateDataWithTarget}\r\n" + //
				"{itemWithTemplateDataWithTarget.price.divide(0)}";
		testDiagnosticsFor(template, //
				d(0, 38, 0, 44, QuteErrorCode.MethodNotSupportedInNativeMode,
						"Method `divide` of `java.math.BigInteger` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		// here java.lang.String#isEmpty is supported in native mode because
		// ItemWithTemplateDataWithTarget defines @TemplateData(target = String.class)
		template = // "{@org.acme.ItemWithTemplateDataWithTarget
					// itemWithTemplateDataWithTarget}\r\n" + //
				"{itemWithTemplateDataWithTarget.name.isEmpty()}";
		testDiagnosticsFor(template);
	}

	@Test
	public void templateDataFieldIgnoreSubClasses() {
		// @TemplateData(ignoreSubClasses = true)
		// public class ItemWithTemplateDataIgnoreSubClasses
		String template = // "{@org.acme.ItemWithTemplateDataIgnoreSubClasses
							// itemWithTemplateDataIgnoreSubClasses}\r\n" + //
				"{itemWithTemplateDataIgnoreSubClasses.price}\r\n" + // <-- no error because price comes from
																		// ItemWithTemplateDataIgnoreSubClasses
						"{itemWithTemplateDataIgnoreSubClasses.base}"; // <-- error because base comes from super class
																		// BaseItem
		testDiagnosticsFor(template, //
				d(1, 38, 1, 42, QuteErrorCode.InheritedPropertyNotSupportedInNativeMode,
						"Inherited property `base` of `org.acme.ItemWithTemplateDataIgnoreSubClasses` Java type cannot be used in native image mode because Java type is annotated with `@TemplateData(ignoreSuperclasses = true)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void templateDataMethod() {

		// public class Item
		String template = // "{@org.acme.Review review}\r\n" + //
				"{review.getReviews()}";
		testDiagnosticsFor(template, //
				d(0, 8, 0, 18, QuteErrorCode.MethodNotSupportedInNativeMode,
						"Method `getReviews` of `org.acme.Review` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		// @TemplateData
		// public class ItemWithTemplateData
		template = // "{@org.acme.ItemWithTemplateData itemWithTemplateData}\r\n" + //
				"iItemWithTemplateData.getReview2()}";
		testDiagnosticsFor(template);
	}

	@Test
	public void templateDataMethodIgnoreSubClasses() {
		// @TemplateData(ignoreSubClasses = true)
		// public class ItemWithTemplateDataIgnoreSubClasses
		String template = // "{@org.acme.ItemWithTemplateDataIgnoreSubClasses
							// itemWithTemplateDataIgnoreSubClasses}\r\n" + //
				"{itemWithTemplateDataIgnoreSubClasses.getSubClasses()}\r\n" + // <-- no error because getSubClasses
																				// comes from
				// ItemWithTemplateDataIgnoreSubClasses
						"{itemWithTemplateDataIgnoreSubClasses.getReviews()}"; // <-- error because getReviews comes
																				// from super
																				// class BaseItem
		testDiagnosticsFor(template, //
				d(1, 38, 1, 48, QuteErrorCode.InheritedMethodNotSupportedInNativeMode,
						"Inherited method `getReviews` of `org.acme.ItemWithTemplateDataIgnoreSubClasses` Java type cannot be used in native image mode because Java type is annotated with `@TemplateData(ignoreSuperclasses = true)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void registerForReflectionField() {
		// public class Item
		String template = // "{@org.acme.Review review}\r\n" + //
				"{review.name}";
		testDiagnosticsFor(template, //
				d(0, 8, 0, 12, QuteErrorCode.PropertyNotSupportedInNativeMode,
						"Property `name` of `org.acme.Review` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		// @RegisterForReflection
		// public class ItemWithRegisterForReflection
		template = // "{@org.acme.ItemWithRegisterForReflection itemWithRegisterForReflection}\r\n"
					// + //
				"{itemWithRegisterForReflection.name}";
		testDiagnosticsFor(template);
	}

	@Test
	public void registerForReflectionNoFields() {

		// @RegisterForReflection(fields = false)
		// public class ItemWithRegisterForReflectionNoFields
		String template = // "{@org.acme.ItemWithRegisterForReflectionNoFields
							// itemWithRegisterForReflectionNoFields}\r\n" + //
				"{itemWithRegisterForReflectionNoFields.getReview2()}\r\n" + // <-- no error because it's a method
						"{itemWithRegisterForReflectionNoFields.base}"; // <-- error because fields are ignored
		testDiagnosticsFor(template, //
				d(1, 39, 1, 43, QuteErrorCode.ForbiddenByRegisterForReflectionFields,
						"The field `base` of `org.acme.ItemWithRegisterForReflectionNoFields` Java type cannot be used in native image mode because Java type is annotated with `@RegisterForReflection(fields = false)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void registerForReflectionNoMethods() {

		// @RegisterForReflection(methods = false)
		// public class ItemWithRegisterForReflectionNoMethods
		String template = // "{@org.acme.ItemWithRegisterForReflectionNoMethods
							// itemWithRegisterForReflectionNoMethods}\r\n" + //
				"{itemWithRegisterForReflectionNoMethods.getReview2()}\r\n" + // <-- error because methods are ignored
						"{itemWithRegisterForReflectionNoMethods.base}"; // <-- no error because it's a field
		testDiagnosticsFor(template, //
				d(0, 40, 0, 50, QuteErrorCode.ForbiddenByRegisterForReflectionMethods,
						"The method `getReview2` of `org.acme.ItemWithRegisterForReflectionNoMethods` Java type cannot be used in native image mode because Java type is annotated with `@RegisterForReflection(methods = false)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void registerForReflectionMethod() {
		// public class Item
		String template = //"{@org.acme.Review review}\r\n" + //
				"{review.getReviews()}";
		testDiagnosticsFor(template, //
				d(0, 8, 0, 18, QuteErrorCode.MethodNotSupportedInNativeMode,
						"Method `getReviews` of `org.acme.Review` Java type cannot be used in native image mode.",
						DiagnosticSeverity.Error));

		// @RegisterForReflection
		// public class ItemWithRegisterForReflection
		template = // "{@org.acme.ItemWithRegisterForReflection itemWithRegisterForReflection}\r\n" + //
				"{itemWithRegisterForReflection.getReview2()}";
		testDiagnosticsFor(template);
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		String templateUri = QuteQuickStartProject.NATIVEITEMRESOURCE_ITEMS_TEMPLATE_URI;

		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		QuteValidationSettings validationSettings = new QuteValidationSettings();

		QuteAssert.testDiagnosticsFor(value, templateUri, null, QuteQuickStartProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, false, validationSettings, nativeImagesSettings, expected);
	}
}
