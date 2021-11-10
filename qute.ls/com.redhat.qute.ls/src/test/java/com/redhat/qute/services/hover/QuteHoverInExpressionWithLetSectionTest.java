package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

public class QuteHoverInExpressionWithLetSectionTest {

	@Test
	public void referencedParameter() throws Exception {
		String template = "{#set name=item.name age=10 long=10L negDouble=-10D isActive=true simpleQuote='abcd' doubleQuote=\"efgh\"}\r\n"
				+ //
				"  {true}\r\n" + //
				"  {name}\r\n" + //
				"  {neg|Double}\r\n" + //
				"  {isActive}\r\n" + //
				"  {simpleQuote}\r\n" + //
				"  {doubleQuote}\r\n" + //
				"{/set}\r\n" + //
				"";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"java.lang.Double" + //
				System.lineSeparator() + //
				"```", //
				r(3, 3, 3, 12));
	}

}
