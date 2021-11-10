package com.redhat.qute.services;

import static com.redhat.qute.QuteAssert.l;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testReferencesFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

public class QuteReferenceTest {

	@Test
	public void parameterDeclarationAlias() throws BadLocationException {
		String template = "{@org.acme.Item it|em}\r\n" + //
				"{item.name}}\r\n" + //
				"{item}";
		testReferencesFor(template, //
				l("test.qute", r(1, 1, 1, 5)), //
				l("test.qute", r(2, 1, 2, 5)));
	}
}
