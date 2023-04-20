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

public class QuteDiagnosticsInExpressionWithEachSectionTest {

	@Test
	public void definedObject() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.name}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedObject() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each itemsXXX}\r\n" + //
				"	{it.name}    \r\n" + //
				"{/each}";

		Diagnostic d = d(2, 7, 2, 15, QuteErrorCode.UndefinedObject, //
				"`itemsXXX` cannot be resolved to an object.", DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d, //
				d(3, 2, 3, 4, QuteErrorCode.UnknownType, //
						"`it` cannot be resolved to a type.", //
						DiagnosticSeverity.Error));
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.util.List itemsXXX}\r\n")), //
				ca(d, te(2, 15, 2, 15, "??")));
	}

	@Test
	public void unknownProperty() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.nameXXX}    \r\n" + //
				"{/each}";
		testDiagnosticsFor(template, //
				d(3, 5, 3, 12, QuteErrorCode.UnknownProperty,
						"`nameXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						new JavaBaseTypeOfPartData("org.acme.Item"), DiagnosticSeverity.Error));
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
