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
	public void testKeyHoverMarkdown() throws BadLocationException {
		String value = "quarkus.applica|tion.name = \"name\"";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator()
				+ "The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator()
				+ " * Type: `java.lang.String`" + System.lineSeparator() + " * Phase: `buildtime`" + System.lineSeparator() + " * Location: `quarkus-core-deployment-0.19.1.jar`" + System.lineSeparator() + " * Source: `io.quarkus.deployment.ApplicationConfig#name`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testKeyHoverPlaintext() throws BadLocationException {
		String value = "quarkus.applica|tion.name = \"name\"";
		String hoverLabel = "quarkus.application.name" + System.lineSeparator() + System.lineSeparator()
				+ "The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator()
				+ "Type: java.lang.String" + System.lineSeparator() + "Phase: buildtime" + System.lineSeparator() + "Location: quarkus-core-deployment-0.19.1.jar" + System.lineSeparator() + "Source: io.quarkus.deployment.ApplicationConfig#name";
		assertHoverPlaintext(value, hoverLabel, 0);
	};

	@Test
	public void testKeyHoverNoSpaces() throws BadLocationException {
		String value = "quarkus.applica|tion.name=\"name\"";
		String hoverLabel = "**quarkus.application.name**" + System.lineSeparator() + System.lineSeparator()
				+ "The name of the application.\n If not set, defaults to the name of the project."
				+ System.lineSeparator() + System.lineSeparator()
				+ " * Type: `java.lang.String`" + System.lineSeparator() + " * Phase: `buildtime`" + System.lineSeparator() + " * Location: `quarkus-core-deployment-0.19.1.jar`" + System.lineSeparator() + " * Source: `io.quarkus.deployment.ApplicationConfig#name`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testNoKeyHoverOnEqualsSign() throws BadLocationException {
		String value = "quarkus.application.name |= \"name\"";
		assertHoverMarkdown(value, null, null);
	};

	@Test
	public void testNoQuarkusKeyHoverOnEqualsSignNoSpaces() throws BadLocationException {
		String value = "quarkus.application.name|=\"name\"";
		assertHoverMarkdown(value, null, null);
	};

}
