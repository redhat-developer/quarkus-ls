package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

public class QuteDefinitionInEachSectionTest {

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{#each ite|ms}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template);
	}

	@Test
	public void definedVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#each ite|ms}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 7, 1, 12), r(0, 32, 0, 37)));
	}

	@Test
	public void definitionInStartTagSection() throws Exception {
		String template = "{#ea|ch items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 2, 0, 6), r(2, 2, 2, 6)));

		template = "{#each| items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 2, 0, 6), r(2, 2, 2, 6)));

	}

	@Test
	public void definitionInEndTagSection() throws Exception {
		String template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/ea|ch}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 2, 2, 6), r(0, 2, 0, 6)));

		template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each|}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 2, 2, 6), r(0, 2, 0, 6)));
	}

	@Test
	public void definitionInDefaultAlias() throws Exception {
		String template = "{#each items}\r\n" + //
				"		{i|t.name}\r\n" + //
				"{/ea|ch}";
		testDefinitionFor(template);
	}
}
