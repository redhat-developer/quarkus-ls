package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

public class QuteDefinitionInForSectionTest {

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{#for item in ite|ms}\r\n" + //
				"		{it.name}\r\n" + //
				"{/for item in}";
		testDefinitionFor(template);
	}

	@Test
	public void definedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in ite|ms}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 14, 1, 19), r(0, 32, 0, 37)));
	}

	@Test
	public void definitionInStartTagSection() throws Exception {
		String template = "{|#for item in items}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 1, 0, 5), r(2, 1, 2, 5)));
		
		template = "{#fo|r item in items}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 1, 0, 5), r(2, 1, 2, 5)));

		template = "{#for| item in items}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 1, 0, 5), r(2, 1, 2, 5)));
	}

	@Test
	public void definitionInEndTagSection() throws Exception {
		String template = "{#for item in items}\r\n" + //
				"		{item.name}\r\n" + //
				"{|/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 1, 2, 5), r(0, 1, 0, 5)));

		template = "{#for item in items}\r\n" + //
				"		{item.name}\r\n" + //
				"{/fo|r}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 1, 2, 5), r(0, 1, 0, 5)));
		
		template = "{#for item in items}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for|}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 1, 2, 5), r(0, 1, 0, 5)));
	}

	@Test
	public void definitionInObjectPart() throws Exception {
		String template = "{#for item in items}\r\n" + //
				"		{it|em.name}\r\n" + //
				"{/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 3, 1, 7), r(0, 6, 0, 10)));

		template = "{#for item in items}\r\n" + //
				"		{item|.name}\r\n" + //
				"{/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 3, 1, 7), r(0, 6, 0, 10)));
	}

	@Test
	public void metadata() throws Exception {
		String template = "{#for item in items}\r\n" + //
				"		{cou|nt}\r\n" + //
				"{/for}";
		testDefinitionFor(template);
	}
}
