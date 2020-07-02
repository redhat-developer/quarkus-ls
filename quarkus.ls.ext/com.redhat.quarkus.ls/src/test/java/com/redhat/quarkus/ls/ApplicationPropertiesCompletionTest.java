/*******************************************************************************
0* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.ls;

import static com.redhat.microprofile.services.MicroProfileAssert.assertCompletionWithProperties;
import static com.redhat.microprofile.services.MicroProfileAssert.c;
import static com.redhat.microprofile.services.MicroProfileAssert.r;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.Arrays;

import org.junit.Test;

import com.redhat.microprofile.ls.commons.BadLocationException;

/**
 * Test with completion in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesCompletionTest {

	@Test
	public void snippetCompletionDatasource() throws BadLocationException {
		String value = "qds|";
		assertCompletionWithProperties(value, null, singleton("quarkus.datasource.jdbc.url"),
				c("qds", "quarkus.datasource.db-kind=${1|mariadb,mysql,h2,postgresql,derby,mssql|}"
						+ System.lineSeparator() + "quarkus.datasource.username=${2:developer}" + System.lineSeparator()
						+ "quarkus.datasource.password=${3:developer}" + System.lineSeparator()
						+ "quarkus.datasource.jdbc.url=${4|jdbc:mariadb://localhost:3306/mydb,jdbc:mysql://localhost:3306/test,jdbc:h2:mem:mydb,jdbc:postgresql://localhost:5432/mydb,jdbc:derby://localhost:1527/mydb,jdbc:sqlserver://localhost:1433;databaseName=mydb|}"
						+ System.lineSeparator() + "quarkus.datasource.jdbc.min-size=${5:5}" + System.lineSeparator()
						+ "quarkus.datasource.jdbc.max-size=${6:15}", r(0, 0, 3)));
		assertCompletionWithProperties(value, 0, emptySet());
	}

	@Test
	public void snippetCompletionJaeger() throws BadLocationException {
		String value = "qj|";
		assertCompletionWithProperties(value, null, singleton("quarkus.jaeger.service-name"),
				c("qj", "quarkus.jaeger.service-name=${1:myservice}" + System.lineSeparator()
						+ "quarkus.jaeger.sampler-type=${2:const}" + System.lineSeparator()
						+ "quarkus.jaeger.sampler-param=${3:1}" + System.lineSeparator()
						+ "quarkus.jaeger.endpoint=${4:http://localhost:14268/api/traces}", r(0, 0, 2)));
		assertCompletionWithProperties(value, 0, emptySet());
	}

	@Test
	public void multipleContextProperties() {
		String value = "qdstest|";
		assertCompletionWithProperties(value, null,
				Arrays.asList("quarkus.datasource.db-kind", "quarkus.datasource.jdbc.url"),
				c("qdstest", "quarkus.datasource.db-kind=${1|mariadb,mysql,h2,postgresql,derby,mssql|}"
						+ System.lineSeparator() + "quarkus.datasource.username=${2:developer}" + System.lineSeparator()
						+ "quarkus.datasource.password=${3:developer}" + System.lineSeparator()
						+ "quarkus.datasource.jdbc.url=${4|jdbc:mariadb://localhost:3306/mydb,jdbc:mysql://localhost:3306/test,jdbc:h2:mem:mydb,jdbc:postgresql://localhost:5432/mydb,jdbc:derby://localhost:1527/mydb,jdbc:sqlserver://localhost:1433;databaseName=mydb|}"
						+ System.lineSeparator() + "quarkus.datasource.jdbc.min-size=${5:5}" + System.lineSeparator()
						+ "quarkus.datasource.jdbc.max-size=${6:15}", r(0, 0, 7)));
		assertCompletionWithProperties(value, 0, Arrays.asList("quarkus.datasource.db-kind"));
	}
}
