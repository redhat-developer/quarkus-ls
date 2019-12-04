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

import static com.redhat.microprofile.services.MicroProfileAssert.ds;
import static com.redhat.microprofile.services.MicroProfileAssert.r;
import static com.redhat.microprofile.services.MicroProfileAssert.s;
import static com.redhat.microprofile.services.MicroProfileAssert.testDocumentSymbolsFor;
import static com.redhat.microprofile.services.MicroProfileAssert.testSymbolInformationsFor;

import java.util.Arrays;

import org.eclipse.lsp4j.SymbolKind;
import org.junit.Test;

import com.redhat.microprofile.ls.commons.BadLocationException;

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
				s("quarkus.datasource.driver", SymbolKind.Property, "application.properties", r(1, 0, 47)), //
				s("quarkus.datasource.username", SymbolKind.Property, "application.properties", r(2, 0, 40)), //
				s("quarkus.datasource.password", SymbolKind.Property, "application.properties", r(3, 0, 40)), //
				s("quarkus.datasource.max-size", SymbolKind.Property, "application.properties", r(6, 0, 27)), //
				s("quarkus.datasource.min-size", SymbolKind.Property, "application.properties", r(7, 0, 28)) //
		);
	};

	@Test
	public void documentSymbols() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.datasource.driver=org.postgresql.Driver\n" + //
				"quarkus.hibernate-orm.database.generation=drop-and-create\n" + //
				"quarkus.hibernate-orm.log.sql=true\n" + //
				"\n" + //
				"       \n" + //
				"quarkus.datasource.max-size\n" + //
				"quarkus.datasource.min-size=\n" + //
				"%dev.quarkus.datasource.max-size=2\n" + //
				"%dev.quarkus.datasource.min-size=1\n" + //
				"\n" + //
				"";
		testDocumentSymbolsFor(value, //
				ds("quarkus", SymbolKind.Package, r(1, 0, 47), null, //
						Arrays.asList( //
								ds("datasource", SymbolKind.Package, r(1, 0, 47), null, //
										Arrays.asList( //
												ds("driver", SymbolKind.Property, r(1, 0, 47), "org.postgresql.Driver"), //
												ds("max-size", SymbolKind.Property, r(6, 0, 27), null), //
												ds("min-size", SymbolKind.Property, r(7, 0, 28), "") //
										)), //
								ds("hibernate-orm", SymbolKind.Package, r(2, 0, 57), null, //
										Arrays.asList( //
												ds("database", SymbolKind.Package, r(2, 0, 57), null, //
														Arrays.asList(ds("generation", SymbolKind.Property, r(2, 0, 57),
																"drop-and-create"))), //
												ds("log", SymbolKind.Package, r(3, 0, 34), null, //
														Arrays.asList( //
																ds("sql", SymbolKind.Property, r(3, 0, 34), "true"))) //
										)))), //
				ds("%dev", SymbolKind.Package, r(8, 0, 34), null, //
						Arrays.asList( //
								ds("quarkus", SymbolKind.Package, r(8, 0, 34), null, //
										Arrays.asList(ds("datasource", SymbolKind.Package, r(8, 0, 34), null, //
												Arrays.asList( //
														ds("max-size", SymbolKind.Property, r(8, 0, 34), "2"), //
														ds("min-size", SymbolKind.Property, r(9, 0, 34), "1") //
												)))))) //
		);
	};
}
