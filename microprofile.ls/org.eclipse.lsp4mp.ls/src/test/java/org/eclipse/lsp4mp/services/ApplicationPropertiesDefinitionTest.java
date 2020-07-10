/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services;

import static org.eclipse.lsp4mp.services.MicroProfileAssert.ll;
import static org.eclipse.lsp4mp.services.MicroProfileAssert.r;
import static org.eclipse.lsp4mp.services.MicroProfileAssert.testDefinitionFor;

import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.junit.Test;

/**
 * Test with definition in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesDefinitionTest {

	@Test
	public void definitionOnComments() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "#|";
		testDefinitionFor(value);

		value = " #|";
		testDefinitionFor(value);
	}

	@Test
	public void definitionOnKey() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.application.na|me";
		testDefinitionFor(value, ll(
				"jdt://contents/quarkus-core-1.3.2.Final.jar/io.quarkus.runtime/ApplicationConfig.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/quarkus%5C/quarkus-core%5C/1.3.2.Final%5C/quarkus-core-1.3.2.Final.jar%3Cio.quarkus.runtime(ApplicationConfig.class",
				r(0, 0, 24), r(16, 28, 32)));
	}

	@Test
	public void definitionOnEnumValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.log.syslog.async.overflow=BLO|CK";
		testDefinitionFor(value, ll(
				"jdt://contents/jboss-logmanager-embedded-1.0.4.jar/org.jboss.logmanager.handlers/AsyncHandler$OverflowAction.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/org%5C/jboss%5C/logmanager%5C/jboss-logmanager-embedded%5C/1.0.4%5C/jboss-logmanager-embedded-1.0.4.jar%3Corg.jboss.logmanager.handlers(AsyncHandler$OverflowAction.class",
				r(0, 34, 39), r(222, 8, 13)));
	}

	@Test
	public void definitionOnOptionalEnumValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.transaction-isolation-level=no|ne";
		testDefinitionFor(value, ll(
				"jdt://contents/agroal-api-1.7.jar/io.agroal.api.configuration/AgroalConnectionFactoryConfiguration$TransactionIsolation.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/agroal%5C/agroal-api%5C/1.7%5C/agroal-api-1.7.jar%3Cio.agroal.api.configuration(AgroalConnectionFactoryConfiguration$TransactionIsolation.class",
				r(0, 47, 51), r(87, 19, 23)));
	}

	@Test
	public void definitionOnOptionalEnumValueKebabCase()
			throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.transaction-isolation-level=read-uncom|mitted";
		testDefinitionFor(value, ll(
				"jdt://contents/agroal-api-1.7.jar/io.agroal.api.configuration/AgroalConnectionFactoryConfiguration$TransactionIsolation.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/agroal%5C/agroal-api%5C/1.7%5C/agroal-api-1.7.jar%3Cio.agroal.api.configuration(AgroalConnectionFactoryConfiguration$TransactionIsolation.class",
				r(0, 47, 63), r(87, 25, 41)));
	}

	@Test
	public void definitionOnMappedPropertyOptionalEnumValue()
			throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.key.transaction-isolation-level=no|ne";
		testDefinitionFor(value, ll(
				"jdt://contents/agroal-api-1.7.jar/io.agroal.api.configuration/AgroalConnectionFactoryConfiguration$TransactionIsolation.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/agroal%5C/agroal-api%5C/1.7%5C/agroal-api-1.7.jar%3Cio.agroal.api.configuration(AgroalConnectionFactoryConfiguration$TransactionIsolation.class",
				r(0, 51, 55), r(87, 19, 23)));
	}

	@Test
	public void noDefinitionOnKey() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.driv|erXXXX";
		testDefinitionFor(value);
	}

	@Test
	public void noDefinitionOnValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.driver=XXX|X";
		testDefinitionFor(value);
	}
}
