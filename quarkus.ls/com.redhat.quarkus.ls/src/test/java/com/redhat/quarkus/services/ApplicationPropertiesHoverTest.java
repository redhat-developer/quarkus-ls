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
				" * Location: `quarkus-core-deployment-0.19.1.jar`" + System.lineSeparator() + //
				" * Source: `io.quarkus.deployment.ApplicationConfig#name`";
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
				"Location: quarkus-core-deployment-0.19.1.jar" + System.lineSeparator() + //
				"Source: io.quarkus.deployment.ApplicationConfig#name";
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
				" * Location: `quarkus-core-deployment-0.19.1.jar`" + System.lineSeparator() + //
				" * Source: `io.quarkus.deployment.ApplicationConfig#name`";
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
				" * Location: `quarkus-core-deployment-0.19.1.jar`" + System.lineSeparator() + //
				" * Source: `io.quarkus.deployment.ApplicationConfig#name`";
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
				" * Location: `quarkus-core-deployment-0.19.1.jar`" + System.lineSeparator() + //
				" * Source: `io.quarkus.deployment.ApplicationConfig#name`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void onlyProfile() throws BadLocationException {
		String value = "%de|v";
		String hoverLabel = null;
		assertHoverMarkdown(value, hoverLabel, 0);

		value = "%de|v.";
		hoverLabel = null;
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
				" * Location: `quarkus-core-deployment-0.19.1.jar`" + System.lineSeparator() + //
				" * Source: `io.quarkus.deployment.ApplicationConfig#name`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};
}
