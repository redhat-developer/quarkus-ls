/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.restclient;

import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Test;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileJavaDiagnosticsParams;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.internal.restclient.MicroProfileRestClientConstants;

/**
 * Java diagnostics for MicroProfile RestClient.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaDiagnosticsMicroProfileRestClientTest extends BasePropertiesManagerTest {

	@Test
	public void restClientDiagnostics() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.rest_client_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams params = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/restclient/Fields.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		params.setDocumentFormat(DocumentFormat.Markdown);

		assertJavaDiagnostics(params, utils, //
				d(12, 18, 26,
						"The corresponding `org.acme.restclient.MyService` interface does not have the @RegisterRestClient annotation. The field `service1` will not be injected as a CDI bean.",
						DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE, null), //
				d(12, 28, 36,
						"The corresponding `org.acme.restclient.MyService` interface does not have the @RegisterRestClient annotation. The field `service2` will not be injected as a CDI bean.",
						DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE, null), //
				d(15, 25, 52,
						"The Rest Client object should have the @RestClient annotation to be injected as a CDI bean.",
						DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE, null), //
				d(18, 25, 48, "The Rest Client object should have the @Inject annotation to be injected as a CDI bean.",
						DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE, null), //
				d(20, 25, 61,
						"The Rest Client object should have the @Inject and @RestClient annotations to be injected as a CDI bean.",
						DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE, null));

		params = new MicroProfileJavaDiagnosticsParams();
		javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/restclient/MyService.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		assertJavaDiagnostics(params, utils, //
				d(2, 17, 26,
						"This interface does not have the @RegisterRestClient annotation. Any references will not be injected as CDI beans.",
						DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE, null));

	}
}
