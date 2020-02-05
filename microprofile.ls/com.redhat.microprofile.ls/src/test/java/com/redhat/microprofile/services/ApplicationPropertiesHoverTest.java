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

import static com.redhat.microprofile.services.MicroProfileAssert.assertHoverMarkdown;
import static com.redhat.microprofile.services.MicroProfileAssert.assertHoverPlaintext;

import org.junit.Test;

import com.redhat.microprofile.ls.commons.BadLocationException;

/**
 * Test with hover in 'application.properties' file.
 *
 */
public class ApplicationPropertiesHoverTest {

	@Test
	public void unkwownProperty() throws BadLocationException {
		String value = "unkwo|wn";
		String hoverLabel = null;
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverMarkdown() throws BadLocationException {
		String value = "quarkus.applica|tion.name = name";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all)."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.util.Optional<java.lang.String>`" + System.lineSeparator() + //
				" * Phase: `buildtime & runtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverPlaintext() throws BadLocationException {
		String value = "quarkus.applica|tion.name = name";
		String hoverLabel = "quarkus.application.name" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all)."
				+ System.lineSeparator() + System.lineSeparator() + //
				"Type: java.util.Optional<java.lang.String>" + System.lineSeparator() + //
				"Phase: buildtime & runtime" + System.lineSeparator() + //
				"Extension: quarkus-core";
		assertHoverPlaintext(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverNoSpaces() throws BadLocationException {
		String value = "quarkus.applica|tion.name=name";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all)."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.util.Optional<java.lang.String>`" + System.lineSeparator() + //
				" * Phase: `buildtime & runtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testNoQuarkusKeyHoverOnEqualsSign() throws BadLocationException {
		assertHoverMarkdown("quarkus.application.name |= name", null, 0);
		assertHoverMarkdown("quarkus.application.name|=name", null, 0);
		assertHoverMarkdown("quarkus.log.syslog.async.overflow|=DISCARD", null, 0);
	};

	@Test
	public void testNoQuarkusValueHoverOnEqualsSign() throws BadLocationException {
		assertHoverMarkdown("quarkus.log.syslog.async.overflow |= DISCARD", null, 0);
		assertHoverMarkdown("quarkus.log.syslog.async.overflow|=DISCARD", null, 0);
	};

	@Test
	public void testNoHoverOnEqualsWhenNoValue() throws BadLocationException {
		String value = "a=1\n" + //
				"b=|";
		assertHoverMarkdown(value, null, 0);
	};

	@Test
	public void testDefaultProfileHover() throws BadLocationException {
		String value = "%d|ev.quarkus.log.syslog.async.overflow=DISCARD";
		String hoverLabelMarkdown = "**dev**" + System.lineSeparator() + System.lineSeparator()
				+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);
	}

	@Test
	public void testDefaultProfileHoverSpacesInFront() throws BadLocationException {
		String value = "        %d|ev.quarkus.log.syslog.async.overflow=DISCARD";
		String hoverLabelMarkdown = "**dev**" + System.lineSeparator() + System.lineSeparator()
				+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 8);
	}

	@Test
	public void testOnlyDefaultProfile() throws BadLocationException {
		String value = "%de|v";
		String hoverLabelMarkdown = "**dev**" + System.lineSeparator() + System.lineSeparator()
				+ "Profile activated when in development mode (quarkus:dev)." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);

		value = "|%prod";
		hoverLabelMarkdown = "**prod**" + System.lineSeparator() + System.lineSeparator()
				+ "The default profile when not running in development or test mode." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);

		value = "%test|";
		hoverLabelMarkdown = "**test**" + System.lineSeparator() + System.lineSeparator()
				+ "Profile activated when running tests." + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);
	};

	@Test
	public void testOnlyNonDefaultProfile() throws BadLocationException {
		String value = "%hel|lo";
		String hoverLabel = null;
		assertHoverMarkdown(value, hoverLabel, 0);

		value = "%hello|";
		assertHoverMarkdown(value, hoverLabel, 0);

		value = "|%hello";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyWithProfileHoverMarkdown() throws BadLocationException {
		String value = "%dev.quarkus.applica|tion.name = name";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all)."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Profile: `dev`" + System.lineSeparator() + //
				" * Type: `java.util.Optional<java.lang.String>`" + System.lineSeparator() + //
				" * Phase: `buildtime & runtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyMap() throws BadLocationException {
		String value = "quar|kus.log.category.\"com.lordofthejars\".level=DEBUG";
		String hoverLabel = "**quarkus.log.category.\\{\\*\\}.level**" + System.lineSeparator() + System.lineSeparator()
				+ //
				"The log level level for this category" + System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.lang.String`" + System.lineSeparator() + //
				" * Default: `inherit`" + System.lineSeparator() + //
				" * Phase: `runtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void hoverWithEnums() throws BadLocationException {
		String value = "quarkus.log.console.async.overflow=BLO|CK";
		// OverflowAction enum type
		String hoverLabel = "**BLOCK**" + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 35);
	}

	@Test
	public void hoverOnValueForLevelBasedOnRule() throws BadLocationException {
		// quarkus.log.file.level has 'java.util.logging.Level' which has no
		// enumeration
		// to fix it, quarkus-values-rules.json defines the Level enumerations
		String value = "quarkus.log.file.level=OF|F ";
		String hoverLabel = "**OFF**" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"OFF is a special level that can be used to turn off logging.\nThis level is initialized to `Integer.MAX_VALUE`."
				+ //
				System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 23);
	}

	@Test
	public void hoverWithEnumsKebabCase() throws BadLocationException {
		String value = "quarkus.datasource.transaction-isolation-level = read-unc|ommitted";
		// io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation
		// enum type
		String hoverLabel = "**READ_UNCOMMITTED**" + System.lineSeparator();
		assertHoverMarkdown(value, hoverLabel, 49);
	}
}
