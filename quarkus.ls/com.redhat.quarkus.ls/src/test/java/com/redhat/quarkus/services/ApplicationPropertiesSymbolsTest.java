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

import static com.redhat.quarkus.services.QuarkusAssert.r;
import static com.redhat.quarkus.services.QuarkusAssert.s;
import static com.redhat.quarkus.services.QuarkusAssert.testSymbolInformationsFor;

import org.eclipse.lsp4j.SymbolKind;
import org.junit.Test;

import com.redhat.quarkus.ls.commons.BadLocationException;

/**
 * Test with symbols in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesSymbolsTest {

	@Test
	public void symbolsInformation() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.datasource.driver=org.postgresql.Driver\n" + //
				"quarkus.datasource.username=quarkus_test\n" + //
				"quarkus.datasource.password=quarkus_test\n" + //
				"\n" + //
				"       \n" + //
				"quarkus.datasource.max-size\n" + //
				"quarkus.datasource.min-size=\n" + //
				"\n" + //
				"";
		testSymbolInformationsFor(value, //
				s("quarkus.datasource.driver", SymbolKind.Property, "application.properties",
						r(1, 0, 47)), //
				s("quarkus.datasource.username", SymbolKind.Property, "application.properties",
						r(2, 0, 40)), //
				s("quarkus.datasource.password", SymbolKind.Property, "application.properties",
						r(3, 0, 40)), //
				s("quarkus.datasource.max-size", SymbolKind.Property, "application.properties", r(6, 0, 27)), //
				s("quarkus.datasource.min-size", SymbolKind.Property, "application.properties", r(7, 0, 28)) //
		);
	};
}
