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

import static com.redhat.quarkus.services.QuarkusAssert.assertHoverMarkdown;
import static com.redhat.quarkus.services.QuarkusAssert.assertHoverPlaintext;

import org.junit.Test;

import com.redhat.quarkus.ls.commons.BadLocationException;

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
		String value = "quarkus.applica|tion.name = \"name\"";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.lang.String`" + System.lineSeparator() + //
				" * Phase: `buildtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverPlaintext() throws BadLocationException {
		String value = "quarkus.applica|tion.name = \"name\"";
		String hoverLabel = "quarkus.application.name" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator() + //
				"Type: java.lang.String" + System.lineSeparator() + //
				"Phase: buildtime" + System.lineSeparator() + //
				"Extension: quarkus-core";
		assertHoverPlaintext(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverNoSpaces() throws BadLocationException {
		String value = "quarkus.applica|tion.name=\"name\"";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.lang.String`" + System.lineSeparator() + //
				" * Phase: `buildtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverOnEqualsSign() throws BadLocationException {
		String value = "quarkus.application.name |= \"name\"";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.lang.String`" + System.lineSeparator() + //
				" * Phase: `buildtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverOnEqualsSignNoSpaces() throws BadLocationException {
		String value = "quarkus.application.name|=\"name\"";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Type: `java.lang.String`" + System.lineSeparator() + //
				" * Phase: `buildtime`" + System.lineSeparator() + //
				" * Extension: `quarkus-core`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testDefaultProfileHover() throws BadLocationException {
		String value = "%d|ev.quarkus.log.syslog.async.overflow=DISCARD";
		String hoverLabelMarkdown = "**dev**\n\nProfile activated when in development mode (quarkus:dev).\n";
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);
	}

	@Test
	public void testDefaultProfileHoverSpacesInFront() throws BadLocationException {
		String value = "        %d|ev.quarkus.log.syslog.async.overflow=DISCARD";
		String hoverLabelMarkdown = "**dev**\n\nProfile activated when in development mode (quarkus:dev).\n";
		assertHoverMarkdown(value, hoverLabelMarkdown, 8);
	}

	@Test
	public void testOnlyDefaultProfile() throws BadLocationException {
		String value = "%de|v";
		String hoverLabelMarkdown = "**dev**\n\nProfile activated when in development mode (quarkus:dev).\n";
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);

		value = "|%prod";
		hoverLabelMarkdown = "**prod**\n\nThe default profile when not running in development or test mode.\n";
		assertHoverMarkdown(value, hoverLabelMarkdown, 0);

		value = "%test|";
		hoverLabelMarkdown = "**test**\n\nProfile activated when running tests.\n";
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
		String value = "%dev.quarkus.applica|tion.name = \"name\"";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator() + //
				"The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator() + //
				" * Profile: `dev`" + System.lineSeparator() + //
				" * Type: `java.lang.String`" + System.lineSeparator() + //
				" * Phase: `buildtime`" + System.lineSeparator() + //
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
}
