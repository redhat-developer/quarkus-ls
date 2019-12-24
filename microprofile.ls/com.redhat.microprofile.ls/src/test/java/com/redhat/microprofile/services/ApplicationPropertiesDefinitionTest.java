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

import static com.redhat.microprofile.services.MicroProfileAssert.ll;
import static com.redhat.microprofile.services.MicroProfileAssert.r;
import static com.redhat.microprofile.services.MicroProfileAssert.testDefinitionFor;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.redhat.microprofile.ls.commons.BadLocationException;

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
		String value = "quarkus.datasource.driv|er";
		testDefinitionFor(value, ll(
				"jdt://contents/quarkus-agroal-0.21.1.jar/io.quarkus.agroal.runtime/DataSourceBuildTimeConfig.class?=all-quarkus-extensions/C:%5C/Users%5C/azerr%5C/.m2%5C/repository%5C/io%5C/quarkus%5C/quarkus-agroal%5C/0.21.1%5C/quarkus-agroal-0.21.1.jar%3Cio.quarkus.agroal.runtime(DataSourceBuildTimeConfig.class",
				r(0, 0, 25), r(14, 28, 34)));
	}

	@Test
	public void definitionOnEnumValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.log.syslog.async.overflow=BLO|CK";
		testDefinitionFor(value, ll(
				"jdt://contents/jboss-logmanager-embedded-1.0.4.jar/org.jboss.logmanager.handlers/AsyncHandler$OverflowAction.class?=abc/%5C/home%5C/dakwon%5C/.m2%5C/repository%5C/org%5C/jboss%5C/logmanager%5C/jboss-logmanager-embedded%5C/1.0.4%5C/jboss-logmanager-embedded-1.0.4.jar=/maven.pomderived=/true=/=/maven.pomderived=/true=/=/javadoc_location=/jar:file:%5C/home%5C/dakwon%5C/.m2%5C/repository%5C/org%5C/jboss%5C/logmanager%5C/jboss-logmanager-embedded%5C/1.0.4%5C/jboss-logmanager-embedded-1.0.4-javadoc.jar%5C!%5C/=/=/maven.groupId=/org.jboss.logmanager=/=/maven.artifactId=/jboss-logmanager-embedded=/=/maven.version=/1.0.4=/=/maven.scope=/compile=/%3Corg.jboss.logmanager.handlers(AsyncHandler$OverflowAction.class",
				r(0, 34, 39), r(222, 8, 13)));
	}

	@Test
	public void definitionOnOptionalEnumValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.transaction-isolation-level=no|ne";
		testDefinitionFor(value, ll(
				"jdt://contents/agroal-api-1.5.jar/io.agroal.api.configuration/AgroalConnectionFactoryConfiguration$TransactionIsolation.class?=all-quarkus-extensions/%5C/home%5C/dakwon%5C/.m2%5C/repository%5C/io%5C/agroal%5C/agroal-api%5C/1.5%5C/agroal-api-1.5.jar=/maven.pomderived=/true=/=/maven.pomderived=/true=/=/maven.groupId=/io.agroal=/=/maven.artifactId=/agroal-api=/=/maven.version=/1.5=/=/maven.scope=/compile=/%3Cio.agroal.api.configuration(AgroalConnectionFactoryConfiguration$TransactionIsolation.class",
				r(0, 47, 51), r(42, 19, 23)));
	}

	@Test
	public void definitionOnMappedPropertyOptionalEnumValue() throws BadLocationException, InterruptedException, ExecutionException {
		String value = "quarkus.datasource.key.transaction-isolation-level=no|ne";
		testDefinitionFor(value, ll(
				"jdt://contents/agroal-api-1.5.jar/io.agroal.api.configuration/AgroalConnectionFactoryConfiguration$TransactionIsolation.class?=all-quarkus-extensions/%5C/home%5C/dakwon%5C/.m2%5C/repository%5C/io%5C/agroal%5C/agroal-api%5C/1.5%5C/agroal-api-1.5.jar=/maven.pomderived=/true=/=/maven.pomderived=/true=/=/maven.groupId=/io.agroal=/=/maven.artifactId=/agroal-api=/=/maven.version=/1.5=/=/maven.scope=/compile=/%3Cio.agroal.api.configuration(AgroalConnectionFactoryConfiguration$TransactionIsolation.class",
				r(0, 51, 55), r(42, 19, 23)));
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
