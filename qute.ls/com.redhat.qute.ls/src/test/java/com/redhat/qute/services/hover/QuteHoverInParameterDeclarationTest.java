package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

public class QuteHoverInParameterDeclarationTest {

	@Test
	public void hoverInUnkwownClass() throws Exception {
		String template = "{@org.acme.XX|XX item}";
		assertHover(template);
	}

	@Test
	public void hoverInAlias() throws Exception {
		String template = "{@org.acme.XXXX it|em}";
		assertHover(template);
	}

	@Test
	public void hoverInKwonwClass() throws Exception {
		String template = "{@org.acme.It|em item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(0, 2, 0, 15));

		template = "{@org.acme.Item| item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(0, 2, 0, 15));
	}

	@Test
	public void hoverInUnkwonwList() throws Exception {
		String template = "{@java.util.ListX|XX<org.acme.Item> item}";
		assertHover(template);
	}

	@Test
	public void hoverInKwonwList() throws Exception {
		String template = "{@java.util.L|ist<org.acme.Item> item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"java.util.List<E>" + //
				System.lineSeparator() + //
				"```", //
				r(0, 2, 0, 16));

		template = "{@java.util.List|<org.acme.Item> item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"java.util.List<E>" + //
				System.lineSeparator() + //
				"```", //
				r(0, 2, 0, 16));

		template = "{@|java.util.List<org.acme.Item> item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"java.util.List<E>" + //
				System.lineSeparator() + //
				"```", //
				r(0, 2, 0, 16));

		template = "{@java.util.List<org.acme.Item|> item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(0, 17, 0, 30));
	}

	@Test
	public void hoverInUnkwonwClassInsideList() throws Exception {
		String template = "{@java.util.List<org.acme.It|emXXX> item}";
		assertHover(template);
	}

	@Test
	public void hoverInKwonwClassInsideList() throws Exception {
		String template = "{@java.util.List<org.acme.It|em> item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(0, 17, 0, 30));

		template = "{@java.util.List<|org.acme.Item> item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(0, 17, 0, 30));

		template = "{@java.util.List<org.acme.Item|> item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(0, 17, 0, 30));
	}
}
