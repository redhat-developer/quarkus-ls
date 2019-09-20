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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
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

		value = " quarkus.application.name =| ";
		testCompletionFor(value, true, 0);
	}

	@Test
	public void completionOnKeyMap() throws BadLocationException {
		String value = "quarkus.log.category|";
		testCompletionFor(value, false,
				c("quarkus.log.category.{*}.min-level", "quarkus.log.category.{*}.min-level=inherit", r(0, 0, 20)));
		testCompletionFor(value, true, c("quarkus.log.category.{*}.min-level",
				"quarkus.log.category.${1:key}.min-level=${0:inherit}", r(0, 0, 20)));
	}

	@Test
	public void completionOnEmptyLine() throws BadLocationException {
		String value = "quarkus.application.name= \r\n" + //
				"|\r\n" + //
				"quarkus.application.version= ";
		testCompletionFor(value, false,
				c("quarkus.log.category.{*}.min-level", "quarkus.log.category.{*}.min-level=inherit", r(1, 0, 0)));
		testCompletionFor(value, true, c("quarkus.log.category.{*}.min-level",
				"quarkus.log.category.${1:key}.min-level=${0:inherit}", r(1, 0, 0)));
	}

	@Test
	public void completionOnValueNoCompletionItems() throws BadLocationException {
		String value = "quarkus.application.name = | ";
		testCompletionFor(value, true, 0);
	}

	@Test
	public void completionOnValueOnAssign() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=| ";
		testCompletionFor(value, true, c("BLOCK", "BLOCK", r(0, 35, 36)), c("DISCARD", "DISCARD", r(0, 35, 36)));
	}

	@Test
	public void completionOnValueOnPropertyValue() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=BLO| ";
		testCompletionFor(value, true, c("BLOCK", "BLOCK", r(0, 35, 39)));
	}

	@Test
	public void completionOnValueBetweenPropertyValue() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=B|L";
		testCompletionFor(value, true, c("BLOCK", "BLOCK", r(0, 35, 37)));
	}

	@Test
	public void completionOnKeyWithEnums() throws BadLocationException {
		String value = "|";
		// OverflowAction enum type
		testCompletionFor(value, false,
				c("quarkus.log.console.async.overflow", "quarkus.log.console.async.overflow=block", r(0, 0, 0)));
		testCompletionFor(value, true, c("quarkus.log.console.async.overflow",
				"quarkus.log.console.async.overflow=${1|BLOCK,DISCARD|}", r(0, 0, 0)));

		// Boolean type
		testCompletionFor(value, false,
				c("quarkus.datasource.enable-metrics", "quarkus.datasource.enable-metrics=false", r(0, 0, 0)));
		testCompletionFor(value, true, c("quarkus.datasource.enable-metrics",
				"quarkus.datasource.enable-metrics=${1|false,true|}", r(0, 0, 0)));

	}

	@Test
	public void completionOnProfil() throws BadLocationException {
		String value = "%|";
		testCompletionFor(value, true, 3, c("dev", "%dev", r(0, 0, 1)), //
				c("prod", "%prod", r(0, 0, 1)), //
				c("test", "%test", r(0, 0, 1)));

		value = "%st|aging.";
		testCompletionFor(value, true, 4, c("staging", "%staging", r(0, 0, 9)), //
				c("dev", "%dev", r(0, 0, 9)), //
				c("prod", "%prod", r(0, 0, 9)), //
				c("test", "%test", r(0, 0, 9)));

		value = "%staging|.";
		testCompletionFor(value, true, 4, c("staging", "%staging", r(0, 0, 9)), //
				c("dev", "%dev", r(0, 0, 9)), //
				c("prod", "%prod", r(0, 0, 9)), //
				c("test", "%test", r(0, 0, 9)));
	}

	@Test
	public void completionAfterProfil() throws BadLocationException {
		String value = "%dev.|";
		testCompletionFor(value, false, c("quarkus.http.cors", "%dev.quarkus.http.cors=false", r(0, 0, 5)));
		testCompletionFor(value, true, c("quarkus.http.cors", "%dev.quarkus.http.cors=${1|false,true|}", r(0, 0, 5)));
	}

	@Test
	public void noCompletionForExistingProperties() throws BadLocationException {

		String value = "|";

		QuarkusProjectInfo projectInfo = new QuarkusProjectInfo();
		List<ExtendedConfigDescriptionBuildItem> properties = new ArrayList<ExtendedConfigDescriptionBuildItem>();
		ExtendedConfigDescriptionBuildItem p1 = new ExtendedConfigDescriptionBuildItem();
		p1.setPropertyName("quarkus.http.cors");
		properties.add(p1);
		ExtendedConfigDescriptionBuildItem p2 = new ExtendedConfigDescriptionBuildItem();
		p2.setPropertyName("quarkus.application.name");
		properties.add(p2);

		projectInfo.setProperties(properties);

		testCompletionFor(value, false, null, 2, projectInfo, c("quarkus.http.cors", "quarkus.http.cors=", r(0, 0, 0)),
				c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)));

		value = "quarkus.http.cors=false\r\n" + //
				"|";

		testCompletionFor(value, false, null, 1, projectInfo,
				c("quarkus.application.name", "quarkus.application.name=", r(1, 0, 0)));

	}

	@Test
	public void completionForExistingPropertiesDifferentProfile() throws BadLocationException {

		String value = "|";

		QuarkusProjectInfo projectInfo = new QuarkusProjectInfo();
		List<ExtendedConfigDescriptionBuildItem> properties = new ArrayList<ExtendedConfigDescriptionBuildItem>();
		ExtendedConfigDescriptionBuildItem p1 = new ExtendedConfigDescriptionBuildItem();
		p1.setPropertyName("quarkus.http.cors");
		properties.add(p1);
		ExtendedConfigDescriptionBuildItem p2 = new ExtendedConfigDescriptionBuildItem();
		p2.setPropertyName("quarkus.application.name");
		properties.add(p2);

		projectInfo.setProperties(properties);

		testCompletionFor(value, false, null, 2, projectInfo, c("quarkus.http.cors", "quarkus.http.cors=", r(0, 0, 0)),
				c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)));

		value = "quarkus.http.cors=false\r\n" + //
				"%dev.|";

		testCompletionFor(value, false, null, 2, projectInfo,
				c("quarkus.http.cors", "%dev.quarkus.http.cors=", r(1, 0, 5)),
				c("quarkus.application.name", "%dev.quarkus.application.name=", r(1, 0, 5)));

		value = "quarkus.http.cors=false\r\n" + //
				"%dev.quarkus.application.name\r\n" + //
				"%prod.|";

		testCompletionFor(value, false, null, 2, projectInfo,
				c("quarkus.http.cors", "%prod.quarkus.http.cors=", r(2, 0, 6)),
				c("quarkus.application.name", "%prod.quarkus.application.name=", r(2, 0, 6)));

	}
}
