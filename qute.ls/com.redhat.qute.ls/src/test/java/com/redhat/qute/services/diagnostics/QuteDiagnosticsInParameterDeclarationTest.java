package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

public class QuteDiagnosticsInParameterDeclarationTest {

	@Test
	public void kwownJavaClass() throws Exception {
		String template = "{@org.acme.Item item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownJavaClass() throws Exception {
		String template = "{@org.acme.ItemXXX item}";
		testDiagnosticsFor(template, //
				d(0, 2, 0, 18, QuteErrorCode.UnkwownType, "`org.acme.ItemXXX` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}
}
