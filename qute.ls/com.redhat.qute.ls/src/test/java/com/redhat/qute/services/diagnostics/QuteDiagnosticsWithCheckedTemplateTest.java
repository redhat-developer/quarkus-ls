package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion with @CkeckedTemplate.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsWithCheckedTemplateTest {

	@Test
	public void noCheckedTemplateMatching() throws Exception {
		String template = "Item: {items}";
		Diagnostic d = d(0, 7, 0, 12, QuteErrorCode.UndefinedObject, "`items` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("items", false));
		testDiagnosticsFor(template, d);
	}

	@Test
	public void checkedTemplateMatching() throws Exception {
		String template = "Item: {items}";
		testDiagnosticsFor(template, //
				"src/main/resources/templates/ItemResource/items.qute.html");
	}
}