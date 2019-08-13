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
 * Test with completion in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesHoverTest {

	@Test
	public void testQuarkusKeyHoverMarkdown() throws BadLocationException {
		String value = "quarkus.applica|tion.name = \"name\"";
		String hoverLabel = "**quarkus.application.name**\n\nThe name of the application.\n If not set, defaults to the name of the project.\n\n * Type: `java.lang.String`\n * Phase: `buildtime`\n * Location: `quarkus-core-deployment-0.19.1.jar`\n * Source: `io.quarkus.deployment.ApplicationConfig#name`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverPlaintext() throws BadLocationException {
		String value = "quarkus.applica|tion.name = \"name\"";
		String hoverLabel = "quarkus.application.name\n\nThe name of the application.\n If not set, defaults to the name of the project.\n\nType: java.lang.String\nPhase: buildtime\nLocation: quarkus-core-deployment-0.19.1.jar\nSource: io.quarkus.deployment.ApplicationConfig#name";
		assertHoverPlaintext(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverNoSpaces() throws BadLocationException {
		String value = "quarkus.applica|tion.name=\"name\"";
		String hoverLabel = "**quarkus.application.name**\n\nThe name of the application.\n If not set, defaults to the name of the project.\n\n * Type: `java.lang.String`\n * Phase: `buildtime`\n * Location: `quarkus-core-deployment-0.19.1.jar`\n * Source: `io.quarkus.deployment.ApplicationConfig#name`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverOnEqualsSign() throws BadLocationException {
		String value = "quarkus.application.name |= \"name\"";
		String hoverLabel = "**quarkus.application.name**\n\nThe name of the application.\n If not set, defaults to the name of the project.\n\n * Type: `java.lang.String`\n * Phase: `buildtime`\n * Location: `quarkus-core-deployment-0.19.1.jar`\n * Source: `io.quarkus.deployment.ApplicationConfig#name`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

	@Test
	public void testQuarkusKeyHoverOnEqualsSignNoSpaces() throws BadLocationException {
		String value = "quarkus.application.name|=\"name\"";
		String hoverLabel = "**quarkus.application.name**\n\nThe name of the application.\n If not set, defaults to the name of the project.\n\n * Type: `java.lang.String`\n * Phase: `buildtime`\n * Location: `quarkus-core-deployment-0.19.1.jar`\n * Source: `io.quarkus.deployment.ApplicationConfig#name`";
		assertHoverMarkdown(value, hoverLabel, 0);
	};

}