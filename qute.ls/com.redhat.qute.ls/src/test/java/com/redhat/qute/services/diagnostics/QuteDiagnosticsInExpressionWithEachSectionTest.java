package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

public class QuteDiagnosticsInExpressionWithEachSectionTest {

	@Test
	public void definedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.name}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each itemsXXX}\r\n" + //
				"	{it.name}    \r\n" + //
				"{/each}";

		Diagnostic d = d(2, 7, 2, 15, QuteErrorCode.UndefinedObject, //
				"`itemsXXX` cannot be resolved to an object.", DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("itemsXXX", true));

		testDiagnosticsFor(template, d, //
				d(3, 2, 3, 4, QuteErrorCode.UnknownType, //
						"`it` cannot be resolved to a type.", //
						DiagnosticSeverity.Error));
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.util.List itemsXXX}\r\n")));
	}

	@Test
	public void unkwownProperty() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.nameXXX}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template, //
				d(3, 5, 3, 12, QuteErrorCode.UnknownProperty,
						"`nameXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void noIterable() throws Exception {
		String template = "{@org.acme.Item items}\r\n" + // <-- here items is not an iterable Class
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.name}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template, //
				d(2, 7, 2, 12, QuteErrorCode.IterationError,
						"Iteration error: {items} resolved to [org.acme.Item] which is not iterable.",
						DiagnosticSeverity.Error),
				d(3, 2, 3, 4, QuteErrorCode.UnknownType, "`org.acme.Item` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void iterableWith2Parts() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#each item.reviews}\r\n" + // <- here 2 part in expression
				"		{it.average}    \r\n" + //
				"	{/each}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void noIterableWith2Parts() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#each item.name}\r\n" + // <- here 2 part in expression
				"		{it.average}    \r\n" + //
				"	{/each}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template, //
				d(3, 13, 3, 17, QuteErrorCode.IterationError,
						"Iteration error: {item.name} resolved to [java.lang.String] which is not iterable.",
						DiagnosticSeverity.Error),
				d(4, 3, 4, 5, QuteErrorCode.UnknownType, "`java.lang.String` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void metadata() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it_count}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template);
	}
}
