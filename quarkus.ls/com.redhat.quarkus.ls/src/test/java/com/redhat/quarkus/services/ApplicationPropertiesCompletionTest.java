/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import static com.redhat.quarkus.services.QuarkusAssert.c;
import static com.redhat.quarkus.services.QuarkusAssert.r;
import static com.redhat.quarkus.services.QuarkusAssert.testCompletionFor;

import org.junit.Test;

import com.redhat.quarkus.ls.commons.BadLocationException;

/**
 * Test with completion in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesCompletionTest {

	@Test
	public void completionOnComments() throws BadLocationException {
		String value = "#|";
		testCompletionFor(value, true, 0);

		value = " #|";
		testCompletionFor(value, true, 0);
	}

	@Test
	public void completionOnKey() throws BadLocationException {
		String value = "|";
		testCompletionFor(value, false, c("quarkus.http.cors", "quarkus.http.cors=false", r(0, 0, 0)));
		testCompletionFor(value, true, c("quarkus.http.cors", "quarkus.http.cors=${1|false,true|}", r(0, 0, 0)));

		value = " |";
		testCompletionFor(value, false, c("quarkus.http.cors", "quarkus.http.cors=false", r(0, 0, 1)));
		testCompletionFor(value, true, c("quarkus.http.cors", "quarkus.http.cors=${1|false,true|}", r(0, 0, 1)));

		value = " quarkus.http.co|rs = ";
		testCompletionFor(value, false, c("quarkus.http.cors", "quarkus.http.cors=false", r(0, 0, 21)));
		testCompletionFor(value, true, c("quarkus.http.cors", "quarkus.http.cors=${1|false,true|}", r(0, 0, 21)));

		value = " quarkus.http.cors =| ";
		testCompletionFor(value, true, 0);
	}

	@Test
	public void completionOnKeyMap() throws BadLocationException {
		String value = "quarkus.log.category|";
		testCompletionFor(value, false,
				c("quarkus.log.category.{*}.min-level", "quarkus.log.category.\"\".min-level=inherit", r(0, 0, 20)));
		testCompletionFor(value, true, c("quarkus.log.category.{*}.min-level",
				"quarkus.log.category.\"${1:key}\".min-level=${0:inherit}", r(0, 0, 20)));
	}

	@Test
	public void completionOnEmptyLine() throws BadLocationException {
		String value = "quarkus.application.name= \r\n" + //
				"|\r\n" + //
				"quarkus.application.version= ";
		testCompletionFor(value, false,
				c("quarkus.log.category.{*}.min-level", "quarkus.log.category.\"\".min-level=inherit", r(1, 0, 0)));
		testCompletionFor(value, true, c("quarkus.log.category.{*}.min-level",
				"quarkus.log.category.\"${1:key}\".min-level=${0:inherit}", r(1, 0, 0)));
	}

	@Test
	public void completionOnValue() throws BadLocationException {
		String value = "quarkus.http.cors = | ";
		testCompletionFor(value, true, 0);
	}

	@Test
	public void completionOnKeyWithEnums() throws BadLocationException {
		String value = "|";
		// OverflowAction enum type
		testCompletionFor(value, false,
				c("quarkus.log.console.async.overflow", "quarkus.log.console.async.overflow=BLOCK", r(0, 0, 0)));
		testCompletionFor(value, true, c("quarkus.log.console.async.overflow",
				"quarkus.log.console.async.overflow=${1|BLOCK,DISCARD|}", r(0, 0, 0)));

		// Boolean type
		testCompletionFor(value, false,
				c("quarkus.datasource.enable-metrics", "quarkus.datasource.enable-metrics=false", r(0, 0, 0)));
		testCompletionFor(value, true, c("quarkus.datasource.enable-metrics",
				"quarkus.datasource.enable-metrics=${1|false,true|}", r(0, 0, 0)));

	}

}
