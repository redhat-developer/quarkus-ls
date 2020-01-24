/*******************************************************************************
0* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

import static com.redhat.microprofile.services.MicroProfileAssert.c;
import static com.redhat.microprofile.services.MicroProfileAssert.r;
import static com.redhat.microprofile.services.MicroProfileAssert.testCompletionFor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.commons.BadLocationException;

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
				c("quarkus.log.category.{*}.level", "quarkus.log.category.{*}.level=inherit", r(0, 0, 20)));
		testCompletionFor(value, true, c("quarkus.log.category.{*}.level",
				"quarkus.log.category.${1:key}.level=${2|DEBUG,ERROR,OFF,SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST,ALL|}",
				r(0, 0, 20)));
	}

	@Test
	public void completionOnEmptyLine() throws BadLocationException {
		String value = "quarkus.application.name= \r\n" + //
				"|\r\n" + //
				"quarkus.application.version= ";
		testCompletionFor(value, false,
				c("quarkus.log.category.{*}.level", "quarkus.log.category.{*}.level=inherit", r(1, 0, 0)));
		testCompletionFor(value, true, c("quarkus.log.category.{*}.level",
				"quarkus.log.category.${1:key}.level=${2|DEBUG,ERROR,OFF,SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST,ALL|}",
				r(1, 0, 0)));
	}

	@Test
	public void completionOnValueNoCompletionItems() throws BadLocationException {
		String value = "quarkus.application.name = | ";
		testCompletionFor(value, true, 0);
	}

	@Test
	public void completionOnValueOnAssign() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=| ";
		testCompletionFor(value, true, c("block", "block", r(0, 35, 36)), c("discard", "discard", r(0, 35, 36)));
	}

	@Test
	public void completionOnValueOnPropertyValue() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=BLO| ";
		testCompletionFor(value, true, c("block", "block", r(0, 35, 39)));
	}

	@Test
	public void completionOnValueBetweenPropertyValue() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=B|L";
		testCompletionFor(value, true, c("block", "block", r(0, 35, 37)));
	}

	@Test
	public void completionOnKeyWithEnums() throws BadLocationException {
		String value = "|";
		// OverflowAction enum type
		testCompletionFor(value, false,
				c("quarkus.log.console.async.overflow", "quarkus.log.console.async.overflow=block", r(0, 0, 0)));
		testCompletionFor(value, true, c("quarkus.log.console.async.overflow",
				"quarkus.log.console.async.overflow=${1|block,discard|}", r(0, 0, 0)));

		// Boolean type
		testCompletionFor(value, false,
				c("quarkus.datasource.enable-metrics", "quarkus.datasource.enable-metrics=false", r(0, 0, 0)));
		testCompletionFor(value, true, c("quarkus.datasource.enable-metrics",
				"quarkus.datasource.enable-metrics=${1|false,true|}", r(0, 0, 0)));
	}

	@Test
	public void completionOnValueWithEnumsKebabCase() throws BadLocationException {
		String value = "quarkus.datasource.transaction-isolation-level=|";
		testCompletionFor(value, true, //
				c("undefined", "undefined", r(0, 47, 47)), //
				c("none", "none", r(0, 47, 47)), //
				c("read-uncommitted", "read-uncommitted", r(0, 47, 47)), //
				c("read-committed", "read-committed", r(0, 47, 47)), //
				c("repeatable-read", "repeatable-read", r(0, 47, 47)), //
				c("serializable", "serializable", r(0, 47, 47)) //
		);
	}

	@Test
	public void completionOnProfile() throws BadLocationException {
		String value = "%|";
		testCompletionFor(value, true, 3,
				c("dev", "%dev", r(0, 0, 1),
						"dev" + System.lineSeparator() + System.lineSeparator()
								+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator()), //
				c("prod", "%prod", r(0, 0, 1), "prod" + System.lineSeparator() + System.lineSeparator()
						+ "The default profile when not running in development or test mode." + System.lineSeparator()), //
				c("test", "%test", r(0, 0, 1), "test" + System.lineSeparator() + System.lineSeparator()
						+ "Profile activated when running tests." + System.lineSeparator()));

		value = "%st|aging.";
		testCompletionFor(value, true, 4, c("staging", "%staging", r(0, 0, 9)), //
				c("dev", "%dev", r(0, 0, 9),
						"dev" + System.lineSeparator() + System.lineSeparator()
								+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator()), //
				c("prod", "%prod", r(0, 0, 9), "prod" + System.lineSeparator() + System.lineSeparator()
						+ "The default profile when not running in development or test mode." + System.lineSeparator()), //
				c("test", "%test", r(0, 0, 9), "test" + System.lineSeparator() + System.lineSeparator()
						+ "Profile activated when running tests." + System.lineSeparator()));

		value = "%staging|.";
		testCompletionFor(value, true, 4, c("staging", "%staging", r(0, 0, 9)), //
				c("dev", "%dev", r(0, 0, 9),
						"dev" + System.lineSeparator() + System.lineSeparator()
								+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator()), //
				c("prod", "%prod", r(0, 0, 9), "prod" + System.lineSeparator() + System.lineSeparator()
						+ "The default profile when not running in development or test mode." + System.lineSeparator()), //
				c("test", "%test", r(0, 0, 9), "test" + System.lineSeparator() + System.lineSeparator()
						+ "Profile activated when running tests." + System.lineSeparator()));
	}

	@Test
	public void completionAfterProfile() throws BadLocationException {
		String value = "%dev.|";
		testCompletionFor(value, false, c("quarkus.http.cors", "%dev.quarkus.http.cors=false", r(0, 0, 5)));
		testCompletionFor(value, true, c("quarkus.http.cors", "%dev.quarkus.http.cors=${1|false,true|}", r(0, 0, 5)));
	}

	@Test
	public void noCompletionForExistingProperties() throws BadLocationException {

		String value = "|";

		MicroProfileProjectInfo projectInfo = new MicroProfileProjectInfo();
		List<ItemMetadata> properties = new ArrayList<ItemMetadata>();
		ItemMetadata p1 = new ItemMetadata();
		p1.setName("quarkus.http.cors");
		properties.add(p1);
		ItemMetadata p2 = new ItemMetadata();
		p2.setName("quarkus.application.name");
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

		MicroProfileProjectInfo projectInfo = new MicroProfileProjectInfo();
		List<ItemMetadata> properties = new ArrayList<ItemMetadata>();
		ItemMetadata p1 = new ItemMetadata();
		p1.setName("quarkus.http.cors");
		properties.add(p1);
		ItemMetadata p2 = new ItemMetadata();
		p2.setName("quarkus.application.name");
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

	@Test
	public void completionOnValueForLevelBasedOnRule() throws BadLocationException {
		// quarkus.log.file.level has 'java.util.logging.Level' which has no
		// enumeration
		// to fix it, quarkus-values-rules.json defines the Level enumerations
		String value = "quarkus.log.file.level=| ";
		testCompletionFor(value, true, c("OFF", "OFF", r(0, 23, 24)), c("SEVERE", "SEVERE", r(0, 23, 24)));
	}

	@Test
	public void completionSpacingSurroundingEquals() throws BadLocationException {
		String value = "|";
		testCompletionFor(value, false, true, c("quarkus.http.cors", "quarkus.http.cors = false", r(0, 0, 0)));
		testCompletionFor(value, true, true,
				c("quarkus.http.cors", "quarkus.http.cors = ${1|false,true|}", r(0, 0, 0)));
	}

}
