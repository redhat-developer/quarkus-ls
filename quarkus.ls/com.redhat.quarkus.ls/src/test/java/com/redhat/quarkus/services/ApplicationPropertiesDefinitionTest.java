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

import static com.redhat.quarkus.services.QuarkusAssert.ll;
import static com.redhat.quarkus.services.QuarkusAssert.r;
import static com.redhat.quarkus.services.QuarkusAssert.testDefinitionFor;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.redhat.quarkus.ls.commons.BadLocationException;

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
