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

import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaCodeAction;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.ca;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.createCodeActionParams;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.d;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Test;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileJavaCodeActionParams;
import com.redhat.microprofile.commons.MicroProfileJavaDiagnosticsParams;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.internal.restclient.MicroProfileRestClientConstants;
import com.redhat.microprofile.jdt.internal.restclient.MicroProfileRestClientErrorCode;

/**
 * Java diagnostics for MicroProfile RestClient.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaDiagnosticsMicroProfileRestClientTest extends BasePropertiesManagerTest {

	@Test
	public void restClientAnnotationMissingForFields() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.rest_client_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams params = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/restclient/Fields.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		params.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d1 = d(12, 18, 26,
				"The corresponding `org.acme.restclient.MyService` interface does not have the @RegisterRestClient annotation. The field `service1` will not be injected as a CDI bean.",
				DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE, null);
		Diagnostic d2 = d(12, 28, 36,
				"The corresponding `org.acme.restclient.MyService` interface does not have the @RegisterRestClient annotation. The field `service2` will not be injected as a CDI bean.",
				DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE, null);
		Diagnostic d3 = d(15, 25, 52,
				"The Rest Client object should have the @RestClient annotation to be injected as a CDI bean.",
				DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE,
				MicroProfileRestClientErrorCode.RestClientAnnotationMissing);
		Diagnostic d4 = d(18, 25, 48,
				"The Rest Client object should have the @Inject annotation to be injected as a CDI bean.",
				DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE,
				MicroProfileRestClientErrorCode.InjectAnnotationMissing);
		Diagnostic d5 = d(20, 25, 61,
				"The Rest Client object should have the @Inject and @RestClient annotations to be injected as a CDI bean.",
				DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE,
				MicroProfileRestClientErrorCode.InjectAndRestClientAnnotationMissing);

		assertJavaDiagnostics(params, utils, //
				d1, //
				d2, //
				d3, //
				d4, //
				d5);

		String uri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d3);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Insert @RestClient", d3, //
						te(14, 1, 14, 1, "@RestClient\r\n\t")));

		codeActionParams = createCodeActionParams(uri, d4);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Insert @Inject", d4, //
						te(17, 1, 17, 1, "@Inject\r\n\t")));

		codeActionParams = createCodeActionParams(uri, d5);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Insert @Inject, @RestClient", d5, //
						te(20, 1, 20, 1, "@RestClient\r\n\t@Inject\r\n\t")));

	}

	@Test
	public void restClientAnnotationMissingForInterface() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.rest_client_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams params = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/restclient/MyService.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		params.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(2, 17, 26,
				"The interface `MyService` does not have the @RegisterRestClient annotation. The 1 fields references will not be injected as CDI beans.",
				DiagnosticSeverity.Warning, MicroProfileRestClientConstants.DIAGNOSTIC_SOURCE,
				MicroProfileRestClientErrorCode.RegisterRestClientAnnotationMissing);

		assertJavaDiagnostics(params, utils, //
				d);

		String uri = javaFile.getLocation().toFile().toURI().toString();
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Insert @RegisterRestClient", d, //
						te(0, 28, 2, 0,
								"\r\n\r\nimport org.eclipse.microprofile.rest.client.inject.RegisterRestClient;\r\n\r\n@RegisterRestClient\r\n")));
	}
}
