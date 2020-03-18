/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

import static com.redhat.microprofile.services.MicroProfileAssert.assertFormat;
import static com.redhat.microprofile.services.MicroProfileAssert.assertRangeFormat;

import org.junit.Test;

import com.redhat.microprofile.ls.commons.BadLocationException;

/**
 * Test formatting and range formatting for the 'application.properties' file.
 *
 */
public class ApplicationPropertiesFormatterTest {

	@Test
	public void testExtraSpacesAroundEquals() throws BadLocationException {
		String value = "quarkus.http.port       =         8000";
		String expected = "quarkus.http.port=8000";
		assertFormat(value, expected, false);
	};

	@Test
	public void testExtraSpacesAtEnds() throws BadLocationException {
		String value = "             quarkus.http.port=8000              ";
		String expected = "quarkus.http.port=8000";
		assertFormat(value, expected, false);
	};

	@Test
	public void testFormatWithProfile() throws BadLocationException {
		String value = "             %dev.quarkus.http.port       =      8000              ";
		String expected = "%dev.quarkus.http.port=8000";
		assertFormat(value, expected, false);
	};

	@Test
	public void testFormatMissingValue() throws BadLocationException {
		String value = "quarkus.http.port       =                  ";
		String expected = "quarkus.http.port=";
		assertFormat(value, expected, false);
	};

	@Test
	public void testFormatMissingKey() throws BadLocationException {
		String value = "      =   false  ";
		String expected = "=false";
		assertFormat(value, expected, false);
	};

	@Test
	public void testFormatMissingKeyValue() throws BadLocationException {
		String value = "      =     ";
		String expected = "=";
		assertFormat(value, expected, false);
	};

	@Test
	public void testFormatMissingEquals() throws BadLocationException {
		String value = "    quarkus.http.port    8000     ";
		String expected = "quarkus.http.port    8000";
		assertFormat(value, expected, false);
	};

	@Test
	public void testInsertSpacesAroundEquals() throws BadLocationException {
		String value = "quarkus.http.port=8000";
		String expected = "quarkus.http.port = 8000";
		assertFormat(value, expected, true);
	};

	@Test
	public void testRemoveSpacesAroundEquals() throws BadLocationException {
		String value = "quarkus.http.port = 8000";
		String expected = "quarkus.http.port=8000";
		assertFormat(value, expected, false);
	};

	@Test
	public void testRemoveExtraNewlines() throws BadLocationException {
		String value = "quarkus.http.port=8080" + "\n" + //
				"%dev.quarkus.log.syslog.async.overflow=DISCARD" + "\n" + //
				"\n" + //
				"\n" + //
				"\n" + //
				"quarkus.application.name=name" + "\n" + //
				"%prod.quarkus.application.version=1.0" + "\n" + //
				"\n" + //
				"quarkus.http.cors=false" + "\n" + //
				"\n";
		String expected = "quarkus.http.port=8080" + "\n" + //
				"%dev.quarkus.log.syslog.async.overflow=DISCARD" + "\n" + //
				"quarkus.application.name=name" + "\n" + //
				"%prod.quarkus.application.version=1.0" + "\n" + //
				"quarkus.http.cors=false";
		assertFormat(value, expected, false);
	};

	@Test
	public void testRangeFormattingPartialLines() throws BadLocationException {
		String value = "quarkus.http.port       =        8080" + "\n" + //
				"%dev.quarku|s.log.syslog.async.overflow   =  DISCARD   " + "\n" + // <-- should be formatted
				"\n" + //
				"\n" + //
				"quarkus.application.|name  =   " + "\n" + // <-- should be formatted
				"\n" + //
				"%prod.quarkus.application.version=1.0" + "\n" + //
				"=   false" + "\n" + //
				"\n" + "quarkus.application.version  1.0";

		String expected = "quarkus.http.port       =        8080" + "\n" + //
				"%dev.quarkus.log.syslog.async.overflow=DISCARD" + "\n" + // <-- formatted
				"quarkus.application.name=" + "\n" + // <-- formatted
				"\n" + //
				"%prod.quarkus.application.version=1.0" + "\n" + //
				"=   false" + "\n" + //
				"\n" + //
				"quarkus.application.version  1.0";
		assertRangeFormat(value, expected, false);
	};

	@Test
	public void testRangeFormattingFullLines() throws BadLocationException {
		String value = "|quarkus.http.port       =        8080" + "\n" + // <-- should be formatted
				"%dev.quarkus.log.syslog.async.overflow   =  DISCARD   " + "\n" + // <-- should be formatted
				"\n" + //
				"\n" + //
				"quarkus.application.name  =   |" + "\n" + // <-- should be formatted
				"\n" + //
				"%prod.quarkus.application.version=1.0" + "\n" + //
				"=   false" + "\n" + //
				"\n" + "quarkus.application.version  1.0";

		String expected = "quarkus.http.port=8080" + "\n" + // <-- formatted
				"%dev.quarkus.log.syslog.async.overflow=DISCARD" + "\n" + // <-- formatted
				"quarkus.application.name=" + "\n" + // <-- formatted
				"\n" + //
				"%prod.quarkus.application.version=1.0" + "\n" + //
				"=   false" + "\n" + //
				"\n" + //
				"quarkus.application.version  1.0";
		assertRangeFormat(value, expected, false);
	};

	@Test
	public void testCommentsPersist() throws BadLocationException {
		String value = "quarkus.http.port=8000" + "\n" + //
				"# this is a comment" + "\n" + //
				"quarkus.application.version=1.0" + "\n" + //
				"# this is a comment" + "\n" + //
				"# this is a comment";
		String expected = value;
		assertFormat(value, expected, false);
	};

	@Test
	public void testRangeCommentsPersist() throws BadLocationException {
		// Test with bug https://github.com/redhat-developer/vscode-quarkus/issues/220
		String value = "# a comment \n" + //
				"qua|rkus.applic|ation.version    =   1.0";
		String expected = "# a comment \n" + //
				"quarkus.application.version=1.0"; // <-- formatted
		assertRangeFormat(value, expected, false);
	};

	@Test
	public void testMultiLineKeyAndValue() throws BadLocationException {
		String value = "quarkus.\\\n" +
		"application.\\\n" +
		"name=my\\\n" +
		"application\\\n" +
		"name   ";

		String expected = "quarkus.\\\n" +
		"application.\\\n" +
		"name=my\\\n" +
		"application\\\n" +
		"name";
		assertFormat(value, expected, false);
	}
}