package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

public class QuteDiagnosticsInExpressionWithForSectionTest {

	@Test
	public void definedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in itemsXXX}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}";

		Diagnostic d = d(2, 14, 2, 22, QuteErrorCode.UndefinedVariable, //
				"`itemsXXX` cannot be resolved to a variable.", DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("itemsXXX", true));

		testDiagnosticsFor(template, d, //
				d(3, 2, 3, 6, QuteErrorCode.UnkwownType, //
						"`item` cannot be resolved to a type.", //
						DiagnosticSeverity.Error));
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.util.List itemsXXX}\r\n")));
	}

	@Test
	public void unkwownProperty() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.nameXXX}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template, //
				d(3, 7, 3, 14, QuteErrorCode.UnkwownProperty,
						"`nameXXX` cannot be resolved or is not a field of `org.acme.Item` Java type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void noIterable() throws Exception {
		String template = "{@org.acme.Item items}\r\n" + // <-- here items is not an iterable Class
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}}";
		testDiagnosticsFor(template, //
				d(2, 14, 2, 19, QuteErrorCode.NotInstanceOfIterable,
						"`org.acme.Item` is not an instance of `java.lang.Iterable`.", DiagnosticSeverity.Error),
				d(3, 2, 3, 6, QuteErrorCode.UnkwownType, "`org.acme.Item` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void iterableWith2Parts() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#for review in item.reviews}\r\n" + // <- here 2 part in expression
				"		{review.average}    \r\n" + //
				"	{/for}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void noIterableWith2Parts() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#for review in item.name}\r\n" + // <- here 2 part in expression
				"		{review.average}    \r\n" + //
				"	{/for}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template, //
				d(3, 22, 3, 26, QuteErrorCode.NotInstanceOfIterable,
						"`java.lang.String` is not an instance of `java.lang.Iterable`.", DiagnosticSeverity.Error),
				d(4, 3, 4, 9, QuteErrorCode.UnkwownType, "`java.lang.String` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#expression_resolution
	 * 
	 * @throws Exception
	 */
	@Test
	public void derived() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"<html>\r\n" + //
				"{item.name} \r\n" + //
				"<ul>\r\n" + //
				"{#for item in item.derivedItems} \r\n" + //
				"  <li>\r\n" + //
				"  {item.name} \r\n" + //
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + //
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testDiagnosticsFor(template);
	}
	
	/**
	 * @see https://quarkus.io/guides/qute-reference#expression_resolution
	 * 
	 * @throws Exception
	 */
	@Test
	public void derivedWithObjectArray() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"<html>\r\n" + //
				"{item.name} \r\n" + //
				"<ul>\r\n" + //
				"{#for item in item.derivedItemArray} \r\n" + // derivedItemArray is an org.acme.Item item[]
				"  <li>\r\n" + //
				"  {item.name} \r\n" + //
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + //
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testDiagnosticsFor(template);
	}
	
	
}
